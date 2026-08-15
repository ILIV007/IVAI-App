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
        private val projectIdPattern = Regex("[a-zA-Z0-9][a-zA-Z0-9._-]{0,63}")

        fun appPrivate(context: Context): ProjectWorkspace =
            ProjectWorkspace(File(context.applicationContext.filesDir, "ivai/projects"))
    }
}

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
