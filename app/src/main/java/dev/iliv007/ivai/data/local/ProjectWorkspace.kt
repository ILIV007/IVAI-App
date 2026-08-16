package dev.iliv007.ivai.data.local

import android.content.Context
import java.io.File
import java.nio.file.Files

/**
 * App-private project-file boundary. All project files live below [rootDirectory] and every
 * caller-supplied relative path is canonicalized before access.
 */
class ProjectWorkspace(
    rootDirectory: File
) {
    private val rootDirectory: File = rootDirectory.canonicalFile

    init {
        requireDirectory(this.rootDirectory)
    }

    fun writeText(projectId: String, relativePath: String, content: String) {
        val target = resolveProjectFile(projectId, relativePath)
        target.parentFile?.let(::requireDirectory)
        target.writeText(content, Charsets.UTF_8)
    }

    fun readText(projectId: String, relativePath: String): String =
        resolveProjectFile(projectId, relativePath).readText(Charsets.UTF_8)

    /**
     * Reads an app-private project file through the Agent-safe boundary. The complete content is
     * never persisted by this method; callers receive at most [maxChars] in memory and should only
     * persist metadata in run traces.
     */
    fun readTextBounded(
        projectId: String,
        relativePath: String,
        maxChars: Int = DEFAULT_MAX_READ_CHARS
    ): BoundedProjectFileRead {
        require(maxChars in 1..DEFAULT_MAX_READ_CHARS) { "Read preview limit is invalid" }
        val target = resolveProjectFile(projectId, relativePath)
        require(target.isFile) { "Project file was not found" }
        val byteCount = target.length()
        require(byteCount <= MAX_AGENT_READ_BYTES) { "Project file exceeds the Agent read limit" }
        val content = target.readText(Charsets.UTF_8)
        return BoundedProjectFileRead(
            relativePath = validateRelativePath(relativePath),
            content = content.take(maxChars),
            byteCount = byteCount,
            totalCharCount = content.length,
            isTruncated = content.length > maxChars
        )
    }

    /** Lists a bounded, app-private project directory without exposing canonical device paths. */
    fun listFilesBounded(
        projectId: String,
        maxEntries: Int = DEFAULT_MAX_LIST_ENTRIES
    ): ProjectWorkspaceFileListing {
        require(maxEntries in 1..DEFAULT_MAX_LIST_ENTRIES) { "Workspace list limit is invalid" }
        val projectDirectory = projectDirectory(projectId)
        if (!projectDirectory.exists()) return ProjectWorkspaceFileListing(emptyList(), false)

        val entries = mutableListOf<ProjectWorkspaceFile>()
        var isTruncated = false
        for (candidate in projectDirectory.walkTopDown()) {
            if (!candidate.isFile) continue
            val canonicalFile = candidate.canonicalFile
            requireInside(projectDirectory, canonicalFile)
            if (entries.size >= maxEntries) {
                isTruncated = true
                break
            }
            entries += ProjectWorkspaceFile(
                relativePath = validateRelativePath(canonicalFile.relativeTo(projectDirectory).invariantSeparatorsPath),
                byteCount = canonicalFile.length()
            )
        }
        return ProjectWorkspaceFileListing(entries.sortedBy { it.relativePath }, isTruncated)
    }

    /**
     * Performs literal, case-insensitive search only inside bounded app-private text files. File
     * previews are returned in memory for the caller; trace callers must retain summaries only.
     */
    fun searchTextBounded(
        projectId: String,
        query: String,
        maxResults: Int = DEFAULT_MAX_SEARCH_RESULTS
    ): ProjectWorkspaceSearchResult {
        val normalizedQuery = query.trim()
        require(normalizedQuery.length in 1..MAX_SEARCH_QUERY_CHARS) { "Search query is invalid" }
        require(maxResults in 1..DEFAULT_MAX_SEARCH_RESULTS) { "Search result limit is invalid" }

        val listing = listFilesBounded(projectId, DEFAULT_MAX_LIST_ENTRIES)
        val hits = mutableListOf<ProjectWorkspaceSearchHit>()
        var scannedFileCount = 0
        var skippedLargeFileCount = 0
        var isTruncated = listing.isTruncated
        for (entry in listing.files) {
            if (entry.byteCount > MAX_AGENT_READ_BYTES) {
                skippedLargeFileCount += 1
                continue
            }
            val read = runCatching { readTextBounded(projectId, entry.relativePath) }.getOrNull() ?: continue
            scannedFileCount += 1
            val matchIndex = read.content.indexOf(normalizedQuery, ignoreCase = true)
            if (matchIndex < 0) continue
            if (hits.size >= maxResults) {
                isTruncated = true
                break
            }
            val previewStart = (matchIndex - SEARCH_PREVIEW_CONTEXT_CHARS).coerceAtLeast(0)
            val previewEnd = (matchIndex + normalizedQuery.length + SEARCH_PREVIEW_CONTEXT_CHARS)
                .coerceAtMost(read.content.length)
            hits += ProjectWorkspaceSearchHit(
                relativePath = entry.relativePath,
                preview = read.content.substring(previewStart, previewEnd)
                    .replace(Regex("\\s+"), " ")
                    .take(MAX_SEARCH_PREVIEW_CHARS)
            )
        }
        return ProjectWorkspaceSearchResult(hits, scannedFileCount, skippedLargeFileCount, isTruncated)
    }

    fun importFile(projectId: String, relativePath: String, source: File) {
        require(source.isFile) { "Import source must be a regular file" }
        val canonicalSource = source.canonicalFile
        val target = resolveProjectFile(projectId, relativePath)
        target.parentFile?.let(::requireDirectory)
        Files.copy(canonicalSource.toPath(), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING)
    }

    fun snapshotFiles(projectIds: Set<String>): List<WorkspaceArchiveFile> =
        projectIds.sorted().flatMap { projectId ->
            val projectDirectory = projectDirectory(projectId)
            if (!projectDirectory.exists()) {
                emptyList()
            } else {
                projectDirectory.walkTopDown()
                    .filter { it.isFile }
                    .map { file ->
                        val canonicalFile = file.canonicalFile
                        requireInside(projectDirectory, canonicalFile)
                        val relativePath = canonicalFile.relativeTo(projectDirectory).invariantSeparatorsPath
                        WorkspaceArchiveFile(
                            projectId = projectId,
                            relativePath = validateRelativePath(relativePath),
                            bytes = canonicalFile.readBytes()
                        )
                    }
                    .toList()
            }
        }

    fun validateProjectIds(projectIds: Set<String>) {
        projectIds.forEach(::projectDirectory)
    }

    fun validateArchiveFiles(files: List<WorkspaceArchiveFile>, projectIds: Set<String>) {
        validateProjectIds(projectIds)
        val seenPaths = mutableSetOf<Pair<String, String>>()
        files.forEach { archiveFile ->
            require(archiveFile.projectId in projectIds) { "Archive file references an unknown project" }
            projectDirectory(archiveFile.projectId)
            val normalizedPath = validateRelativePath(archiveFile.relativePath)
            require(seenPaths.add(archiveFile.projectId to normalizedPath)) {
                "Archive contains duplicate project file paths"
            }
        }
    }

    /**
     * Replaces the on-device workspace only after the archive service has completely decoded and
     * validated its input. A staging directory avoids accepting a partially written import.
     */
    fun replaceFromArchive(files: List<WorkspaceArchiveFile>) {
        val staging = File(rootDirectory.parentFile, "${rootDirectory.name}.staging-${System.nanoTime()}")
        val backup = File(rootDirectory.parentFile, "${rootDirectory.name}.backup-${System.nanoTime()}")
        require(!staging.exists() && !backup.exists()) { "Workspace staging collision" }

        try {
            requireDirectory(staging)
            files.forEach { archiveFile ->
                val projectDirectory = projectDirectoryIn(staging, archiveFile.projectId)
                val target = resolveFileWithin(projectDirectory, archiveFile.relativePath)
                target.parentFile?.let(::requireDirectory)
                target.writeBytes(archiveFile.bytes)
            }

            val originalExists = rootDirectory.exists()
            if (originalExists && !rootDirectory.renameTo(backup)) {
                error("Unable to stage the current workspace for replacement")
            }
            if (!staging.renameTo(rootDirectory)) {
                if (originalExists) {
                    backup.renameTo(rootDirectory)
                }
                error("Unable to commit imported workspace")
            }
            if (backup.exists() && !backup.deleteRecursively()) {
                error("Unable to remove previous workspace after import")
            }
        } finally {
            if (staging.exists()) {
                staging.deleteRecursively()
            }
            if (backup.exists() && !rootDirectory.exists()) {
                backup.renameTo(rootDirectory)
            }
        }
    }

    fun deleteAllProjectFiles() {
        rootDirectory.listFiles()?.forEach { child ->
            check(child.deleteRecursively()) { "Unable to delete workspace data" }
        }
    }

    private fun projectDirectory(projectId: String): File =
        projectDirectoryIn(rootDirectory, projectId)

    private fun projectDirectoryIn(parent: File, projectId: String): File {
        require(projectIdPattern.matches(projectId)) { "Invalid project identifier" }
        return File(parent, projectId).canonicalFile.also { requireInside(parent.canonicalFile, it) }
    }

    private fun resolveProjectFile(projectId: String, relativePath: String): File =
        resolveFileWithin(projectDirectory(projectId), relativePath)

    private fun resolveFileWithin(projectDirectory: File, relativePath: String): File {
        val normalizedPath = validateRelativePath(relativePath)
        return File(projectDirectory, normalizedPath).canonicalFile.also {
            requireInside(projectDirectory, it)
        }
    }

    private fun validateRelativePath(relativePath: String): String {
        require(relativePath.isNotBlank()) { "Project file path must not be blank" }
        require(!relativePath.contains('\\')) { "Project file path must use forward slashes" }
        require(!File(relativePath).isAbsolute) { "Project file path must be relative" }
        val segments = relativePath.split('/')
        require(segments.none { it.isBlank() || it == "." || it == ".." }) {
            "Project file path escapes the workspace"
        }
        return segments.joinToString("/")
    }

    private fun requireDirectory(directory: File) {
        if (!directory.exists()) {
            check(directory.mkdirs()) { "Unable to create app-private workspace" }
        }
        check(directory.isDirectory) { "Workspace path is not a directory" }
    }

    private fun requireInside(parent: File, child: File) {
        val parentPath = parent.canonicalPath
        val childPath = child.canonicalPath
        require(childPath == parentPath || childPath.startsWith("$parentPath${File.separator}")) {
            "Path escapes the app-private workspace"
        }
    }

    companion object {
        const val MAX_AGENT_READ_BYTES = 64 * 1024
        const val DEFAULT_MAX_READ_CHARS = 8 * 1024
        const val DEFAULT_MAX_LIST_ENTRIES = 100
        const val DEFAULT_MAX_SEARCH_RESULTS = 20
        private const val MAX_SEARCH_QUERY_CHARS = 128
        private const val SEARCH_PREVIEW_CONTEXT_CHARS = 96
        private const val MAX_SEARCH_PREVIEW_CHARS = 240
        private val projectIdPattern = Regex("[a-zA-Z0-9][a-zA-Z0-9._-]{0,63}")

        fun appPrivate(context: Context): ProjectWorkspace =
            ProjectWorkspace(File(context.applicationContext.filesDir, "ivai/projects"))
    }
}

data class BoundedProjectFileRead(
    val relativePath: String,
    val content: String,
    val byteCount: Long,
    val totalCharCount: Int,
    val isTruncated: Boolean
)

data class ProjectWorkspaceFile(val relativePath: String, val byteCount: Long)

data class ProjectWorkspaceFileListing(val files: List<ProjectWorkspaceFile>, val isTruncated: Boolean)

data class ProjectWorkspaceSearchHit(val relativePath: String, val preview: String)

data class ProjectWorkspaceSearchResult(
    val hits: List<ProjectWorkspaceSearchHit>,
    val scannedFileCount: Int,
    val skippedLargeFileCount: Int,
    val isTruncated: Boolean
)

data class WorkspaceArchiveFile(
    val projectId: String,
    val relativePath: String,
    val bytes: ByteArray
) {
    init {
        require(bytes.size <= MAX_FILE_BYTES) { "Project file is too large for a local archive" }
    }

    companion object {
        const val MAX_FILE_BYTES = 4 * 1024 * 1024
    }
}
