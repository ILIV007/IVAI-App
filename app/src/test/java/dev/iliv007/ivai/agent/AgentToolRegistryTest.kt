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
    fun `calculator evaluates bounded precedence and returns value only as observation`() {
        val result = completed("2 + 3 * 4")

        assertEquals("Calculator completed.", result.safeSummary)
        assertEquals("14", result.observation)
    }

    @Test
    fun `calculator supports parentheses decimals and unary operators`() {
        assertEquals("-8", completed("(-2.5 + .5) * 4").observation)
        assertEquals("0.125", completed("1 / 8").observation)
        assertEquals("7", completed("--7").observation)
    }

    @Test
    fun `calculator remains read only and never requests approval`() {
        val result = registry.evaluate(AgentToolRequest.Calculate("6 / 2"))

        assertTrue(result is AgentToolResult.Completed)
        assertTrue(result !is AgentToolResult.RequiresApproval)
    }

    @Test
    fun `calculator rejects non arithmetic input`() {
        assertRejected("open external url")
        assertRejected("1e3")
        assertRejected("2(3)")
        assertRejected("1,5")
    }

    @Test
    fun `calculator rejects invalid syntax and division by zero without echoing input`() {
        assertRejected("", "Calculator expression is empty.")
        assertRejected("1 +", "Calculator expression is invalid.")
        assertRejected("(1 + 2", "Calculator expression is invalid.")
        assertRejected("1 / 0", "Calculator cannot divide by zero.")
    }

    @Test
    fun `calculator rejects bounded length nesting and token limits`() {
        assertRejected("1 ".repeat(129), "Calculator expression is too long.")

        val nested = "(".repeat(65) + "1" + ")".repeat(65)
        assertRejected(nested, "Calculator expression is too deeply nested.")

        val tooManyTokens = List(65) { "1" }.joinToString("+")
        assertRejected(tooManyTokens, "Calculator expression is too complex.")
    }

    private fun completed(expression: String): AgentToolResult.Completed {
        val result = registry.evaluate(AgentToolRequest.Calculate(expression))
        assertTrue("Expected completed calculator result, got $result", result is AgentToolResult.Completed)
        return result as AgentToolResult.Completed
    }

    private fun assertRejected(expression: String, expectedReason: String? = null) {
        val result = registry.evaluate(AgentToolRequest.Calculate(expression))
        assertTrue("Expected rejected calculator result, got $result", result is AgentToolResult.Rejected)
        if (expectedReason != null) {
            assertEquals(expectedReason, (result as AgentToolResult.Rejected).safeReason)
        }
    }
}
