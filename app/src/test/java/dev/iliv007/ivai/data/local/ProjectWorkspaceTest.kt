package dev.iliv007.ivai.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ProjectWorkspaceTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `bounded read canonicalizes path and never returns more than requested preview`() {
        val workspace = workspace()
        workspace.writeText("project-a", "docs/readme.md", "x".repeat(40))

        val read = workspace.readTextBounded("project-a", "docs/readme.md", maxChars = 12)
        val traversal = runCatching {
            workspace.readTextBounded("project-a", "../outside.md")
        }.exceptionOrNull()

        assertEquals("docs/readme.md", read.relativePath)
        assertEquals(12, read.content.length)
        assertEquals(40, read.totalCharCount)
        assertTrue(read.isTruncated)
        assertTrue(traversal is IllegalArgumentException)
    }

    @Test
    fun `bounded read rejects oversized files before exposing their content`() {
        val workspace = workspace()
        val large = "a".repeat(ProjectWorkspace.MAX_AGENT_READ_BYTES + 1)
        workspace.writeText("project-a", "large.txt", large)

        val failure = runCatching {
            workspace.readTextBounded("project-a", "large.txt")
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
        assertTrue(failure?.message?.contains("exceeds the Agent read limit") == true)
    }

    @Test
    fun `bounded list and search expose only project-relative metadata and honor caps`() {
        val workspace = workspace()
        workspace.writeText("project-a", "docs/readme.md", "The local Alpha uses bounded tools.")
        workspace.writeText("project-a", "notes/plan.md", "Bounded workspace search remains local.")
        workspace.writeText("project-a", "notes/other.md", "No match here.")

        val listing = workspace.listFilesBounded("project-a", maxEntries = 2)
        val search = workspace.searchTextBounded("project-a", "bounded", maxResults = 1)

        assertEquals(2, listing.files.size)
        assertTrue(listing.isTruncated)
        assertTrue(listing.files.all { !it.relativePath.startsWith('/') && !it.relativePath.contains("..") })
        assertEquals(1, search.hits.size)
        assertTrue(search.isTruncated)
        assertTrue(search.hits.single().relativePath in setOf("docs/readme.md", "notes/plan.md"))
        assertFalse(search.hits.single().preview.contains(temporaryFolder.root.canonicalPath))
    }

    private fun workspace(): ProjectWorkspace =
        ProjectWorkspace(File(temporaryFolder.root, "workspace"))
}
