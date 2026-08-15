package dev.iliv007.ivai.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentToolRegistryTest {
    private val registry = AgentToolRegistry { 0L }

    @Test
    fun `project file write always requests approval with a bounded preview`() {
        val content = "a".repeat(5_000)
        val result = registry.evaluate(AgentToolRequest.WriteProjectFile("notes/summary.md", content))

        assertTrue(result is AgentToolResult.RequiresApproval)
        result as AgentToolResult.RequiresApproval
        assertEquals("notes/summary.md", result.targetPath)
        assertEquals(4_000, result.preview.length)
    }

    @Test
    fun `project file traversal is rejected before approval`() {
        val result = registry.evaluate(AgentToolRequest.WriteProjectFile("../outside.md", "unsafe"))

        assertTrue(result is AgentToolResult.Rejected)
    }

    @Test
    fun `calculator rejects non arithmetic input`() {
        val result = registry.evaluate(AgentToolRequest.Calculate("open external url"))

        assertTrue(result is AgentToolResult.Rejected)
    }
}
