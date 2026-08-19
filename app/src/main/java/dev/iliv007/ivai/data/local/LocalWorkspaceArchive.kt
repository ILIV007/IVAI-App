package dev.iliv007.ivai.data.local

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import dev.iliv007.ivai.ui.model.MessageContentType
import dev.iliv007.ivai.ui.model.MessageSender
import java.security.MessageDigest

/**
 * Versioned local `.ivai` archive for Room workspace records and app-private project files.
 * The archive deliberately has no dependency on DataStore, Android Keystore, or secret vaults.
 */
class LocalWorkspaceArchive(
    private val repository: LocalWorkspaceRepository,
    private val workspace: ProjectWorkspace,
    private val nowEpochMs: () -> Long = System::currentTimeMillis
) {
    suspend fun exportTo(destination: File) {
        val snapshot = repository.snapshotForArchive()
        val files = workspace.snapshotFiles(snapshot.projects.mapTo(mutableSetOf()) { it.id })
        val archive = WorkspaceArchivePayload(
            createdAtEpochMs = nowEpochMs(),
            snapshot = snapshot,
            files = files
        )
        val temporary = File(destination.parentFile ?: error("Archive destination must have a parent"),
            "${destination.name}.part-${System.nanoTime()}"
        )
        try {
            writeArchive(temporary, archive)
            replaceFile(temporary, destination)
        } finally {
            if (temporary.exists()) {
                temporary.delete()
            }
        }
    }

    suspend fun importFrom(source: File) {
        require(source.isFile) { "Archive source must be a regular file" }
        val archive = readArchive(source)
        validateArchive(archive)

        val previousSnapshot = repository.snapshotForArchive()
        val previousFiles = workspace.snapshotFiles(previousSnapshot.projects.mapTo(mutableSetOf()) { it.id })
        try {
            workspace.replaceFromArchive(archive.files)
            repository.replaceFromArchive(archive.snapshot)
        } catch (failure: Throwable) {
            runCatching {
                workspace.replaceFromArchive(previousFiles)
                repository.replaceFromArchive(previousSnapshot)
            }.onFailure { failure.addSuppressed(it) }
            throw failure
        }
    }

    private fun validateArchive(archive: WorkspaceArchivePayload) {
        require(archive.createdAtEpochMs >= 0) { "Archive timestamp is invalid" }
        val projects = archive.snapshot.projects
        val threads = archive.snapshot.threads
        val messages = archive.snapshot.messages
        require(projects.map { it.id }.distinct().size == projects.size) { "Archive contains duplicate projects" }
        require(threads.map { it.id }.distinct().size == threads.size) { "Archive contains duplicate threads" }
        require(messages.map { it.id }.distinct().size == messages.size) { "Archive contains duplicate messages" }

        val projectIds = projects.mapTo(mutableSetOf()) { it.id }
        val threadIds = threads.mapTo(mutableSetOf()) { it.id }
        require(projects.all {
            it.id.isNotBlank() && it.name.isNotBlank() && it.fileCount >= 0 && it.updatedAtEpochMs >= 0
        }) { "Archive contains an invalid project" }
        require(threads.all {
            it.id.isNotBlank() && it.title.isNotBlank() && it.modelOrCombo.isNotBlank() &&
                it.updatedAtEpochMs >= 0 && (it.projectId == null || it.projectId in projectIds)
        }) { "Archive thread references an unknown project" }
        require(messages.all {
            it.id.isNotBlank() && it.threadId in threadIds && it.createdAtEpochMs >= 0 &&
                runCatching { MessageSender.valueOf(it.sender) }.isSuccess &&
                runCatching { MessageContentType.valueOf(it.contentType) }.isSuccess
        }) { "Archive contains an invalid message" }
        workspace.validateProjectIds(projectIds)
        workspace.validateArchiveFiles(archive.files, projectIds)
    }

    private fun writeArchive(destination: File, archive: WorkspaceArchivePayload) {
        val payload = encodePayload(archive)
        require(payload.size <= MAX_ARCHIVE_BYTES) { "Local archive exceeds the size limit" }
        val checksum = sha256(payload)
        DataOutputStream(BufferedOutputStream(FileOutputStream(destination))).use { output ->
            output.writeInt(MAGIC)
            output.writeInt(CURRENT_FORMAT_VERSION)
            output.writeInt(payload.size)
            output.write(checksum)
            output.write(payload)
        }
    }

    private fun readArchive(source: File): WorkspaceArchivePayload =
        DataInputStream(BufferedInputStream(FileInputStream(source))).use { input ->
            require(input.readInt() == MAGIC) { "Unsupported local archive" }
            val formatVersion = input.readInt()
            require(formatVersion in SUPPORTED_FORMAT_VERSIONS) { "Unsupported local archive version" }
            val payloadSize = input.readInt()
            require(payloadSize in 1..MAX_ARCHIVE_BYTES) { "Archive payload size is invalid" }
            val expectedChecksum = ByteArray(SHA256_BYTES)
            input.readFully(expectedChecksum)
            val payload = ByteArray(payloadSize)
            input.readFully(payload)
            require(input.read() == -1) { "Archive contains trailing data" }
            require(MessageDigest.isEqual(expectedChecksum, sha256(payload))) { "Archive checksum mismatch" }
            decodePayload(payload, formatVersion)
        }

    private fun encodePayload(archive: WorkspaceArchivePayload): ByteArray {
        val buffer = java.io.ByteArrayOutputStream()
        DataOutputStream(buffer).use { output ->
            output.writeLong(archive.createdAtEpochMs)
            writeCollection(output, archive.snapshot.projects) { project ->
                output.writeString(project.id)
                output.writeString(project.name)
                output.writeString(project.description)
                output.writeInt(project.fileCount)
                output.writeLong(project.updatedAtEpochMs)
            }
            writeCollection(output, archive.snapshot.threads) { thread ->
                output.writeString(thread.id)
                output.writeString(thread.title)
                output.writeString(thread.snippet)
                output.writeLong(thread.updatedAtEpochMs)
                output.writeString(thread.modelOrCombo)
                output.writeNullableString(thread.projectId)
            }
            writeCollection(output, archive.snapshot.messages) { message ->
                output.writeString(message.id)
                output.writeString(message.threadId)
                output.writeString(message.sender)
                output.writeString(message.text)
                output.writeLong(message.createdAtEpochMs)
                output.writeString(message.contentType)
                output.writeNullableString(message.codeSnippet)
                output.writeNullableString(message.modelBadge)
                output.writeNullableLong(message.latencyMs)
                output.writeBoolean(message.isIncomplete)
            }
            writeCollection(output, archive.files) { file ->
                output.writeString(file.projectId)
                output.writeString(file.relativePath)
                output.writeInt(file.bytes.size)
                output.write(file.bytes)
            }
        }
        return buffer.toByteArray()
    }

    private fun decodePayload(payload: ByteArray, formatVersion: Int): WorkspaceArchivePayload =
        DataInputStream(payload.inputStream()).use { input ->
            val createdAtEpochMs = input.readLong()
            val projects = readCollection(input) {
                WorkspaceProjectEntity(
                    id = input.readString(),
                    name = input.readString(),
                    description = input.readString(),
                    fileCount = input.readInt(),
                    updatedAtEpochMs = input.readLong()
                )
            }
            val threads = readCollection(input) {
                ChatThreadEntity(
                    id = input.readString(),
                    title = input.readString(),
                    snippet = input.readString(),
                    updatedAtEpochMs = input.readLong(),
                    modelOrCombo = input.readString(),
                    projectId = input.readNullableString()
                )
            }
            val messages = readCollection(input) {
                ChatMessageEntity(
                    id = input.readString(),
                    threadId = input.readString(),
                    sender = input.readString(),
                    text = input.readString(),
                    createdAtEpochMs = input.readLong(),
                    contentType = input.readString(),
                    codeSnippet = input.readNullableString(),
                    modelBadge = input.readNullableString(),
                    latencyMs = input.readNullableLong(),
                    isIncomplete = if (formatVersion >= CURRENT_FORMAT_VERSION) input.readBoolean() else false
                )
            }
            val files = readCollection(input) {
                val projectId = input.readString()
                val relativePath = input.readString()
                val byteCount = input.readInt()
                require(byteCount in 0..WorkspaceArchiveFile.MAX_FILE_BYTES) { "Archive file payload is invalid" }
                WorkspaceArchiveFile(projectId, relativePath, ByteArray(byteCount).also(input::readFully))
            }
            require(input.available() == 0) { "Archive payload has trailing data" }
            WorkspaceArchivePayload(
                createdAtEpochMs = createdAtEpochMs,
                snapshot = LocalWorkspaceArchiveSnapshot(projects, threads, messages),
                files = files
            )
        }

    private fun <T> writeCollection(output: DataOutputStream, values: List<T>, write: (T) -> Unit) {
        require(values.size <= MAX_COLLECTION_SIZE) { "Local archive collection is too large" }
        output.writeInt(values.size)
        values.forEach(write)
    }

    private fun <T> readCollection(input: DataInputStream, read: () -> T): List<T> {
        val count = input.readInt()
        require(count in 0..MAX_COLLECTION_SIZE) { "Archive collection size is invalid" }
        return List(count) { read() }
    }

    private fun DataOutputStream.writeString(value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        require(bytes.size <= MAX_STRING_BYTES) { "Archive string is too large" }
        writeInt(bytes.size)
        write(bytes)
    }

    private fun DataInputStream.readString(): String {
        val size = readInt()
        require(size in 0..MAX_STRING_BYTES) { "Archive string size is invalid" }
        return ByteArray(size).also(::readFully).toString(Charsets.UTF_8)
    }

    private fun DataOutputStream.writeNullableString(value: String?) {
        writeBoolean(value != null)
        if (value != null) writeString(value)
    }

    private fun DataInputStream.readNullableString(): String? =
        if (readBoolean()) readString() else null

    private fun DataOutputStream.writeNullableLong(value: Long?) {
        writeBoolean(value != null)
        if (value != null) writeLong(value)
    }

    private fun DataInputStream.readNullableLong(): Long? =
        if (readBoolean()) readLong() else null

    private fun replaceFile(from: File, destination: File) {
        destination.parentFile?.mkdirs()
        require(!destination.exists() || destination.delete()) { "Unable to replace local archive" }
        require(from.renameTo(destination)) { "Unable to finalize local archive" }
    }

    private fun sha256(payload: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(payload)

    private data class WorkspaceArchivePayload(
        val createdAtEpochMs: Long,
        val snapshot: LocalWorkspaceArchiveSnapshot,
        val files: List<WorkspaceArchiveFile>
    )

    private companion object {
        const val MAGIC = 0x49564149 // IVAI
        const val LEGACY_FORMAT_VERSION = 1
        const val CURRENT_FORMAT_VERSION = 2
        val SUPPORTED_FORMAT_VERSIONS = setOf(LEGACY_FORMAT_VERSION, CURRENT_FORMAT_VERSION)
        const val SHA256_BYTES = 32
        const val MAX_ARCHIVE_BYTES = 16 * 1024 * 1024
        const val MAX_COLLECTION_SIZE = 10_000
        const val MAX_STRING_BYTES = 1 * 1024 * 1024
    }
}
