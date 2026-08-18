package dev.iliv007.ivai.agent

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneOffset

sealed interface AgentToolRequest {
    val kind: AgentToolKind

    data class Calculate(val expression: String) : AgentToolRequest {
        override val kind = AgentToolKind.CALCULATE
    }

    data object CurrentTime : AgentToolRequest {
        override val kind = AgentToolKind.CURRENT_TIME
    }

    data class ReadProjectFile(val relativePath: String) : AgentToolRequest {
        override val kind = AgentToolKind.READ_PROJECT_FILE
    }

    data object ListWorkspace : AgentToolRequest {
        override val kind = AgentToolKind.LIST_WORKSPACE
    }

    data class SearchProjectFiles(val query: String) : AgentToolRequest {
        override val kind = AgentToolKind.SEARCH_PROJECT_FILES
    }

    data class WriteProjectFile(val relativePath: String, val content: String) : AgentToolRequest {
        override val kind = AgentToolKind.WRITE_PROJECT_FILE
    }
}

sealed interface AgentToolResult {
    /** [observation] is returned only in-memory to the runtime caller and is never a trace payload. */
    data class Completed(val safeSummary: String, val observation: String? = null) : AgentToolResult
    data class RequiresApproval(val targetPath: String, val preview: String) : AgentToolResult
    data class Rejected(val safeReason: String) : AgentToolResult
}

/** Safe subset only. Destructive writes are represented as approval requests and never executed here. */
class AgentToolRegistry(
    private val nowEpochMs: () -> Long = System::currentTimeMillis
) {
    fun evaluate(request: AgentToolRequest): AgentToolResult = when (request) {
        is AgentToolRequest.Calculate -> calculate(request.expression)
        AgentToolRequest.CurrentTime -> AgentToolResult.Completed(
            "UTC time: ${Instant.ofEpochMilli(nowEpochMs()).atOffset(ZoneOffset.UTC)}"
        )
        is AgentToolRequest.ReadProjectFile,
        AgentToolRequest.ListWorkspace,
        is AgentToolRequest.SearchProjectFiles -> AgentToolResult.Rejected(
            "Workspace tools must be evaluated by the bounded local runtime."
        )
        is AgentToolRequest.WriteProjectFile -> {
            val path = request.relativePath.trim()
            if (!isSafeRelativePath(path)) AgentToolResult.Rejected("Project file path is invalid.")
            else AgentToolResult.RequiresApproval(
                targetPath = path,
                preview = request.content.take(MAX_PREVIEW_CHARS)
            )
        }
    }

    private fun calculate(expression: String): AgentToolResult {
        val normalized = expression.trim()
        if (normalized.isEmpty()) return AgentToolResult.Rejected("Calculator expression is empty.")
        if (normalized.length > MAX_CALCULATOR_CHARS) {
            return AgentToolResult.Rejected("Calculator expression is too long.")
        }

        return try {
            val value = BoundedCalculatorParser(normalized).parse()
            AgentToolResult.Completed(
                safeSummary = "Calculator completed.",
                observation = value.stripTrailingZeros().toPlainString()
            )
        } catch (error: CalculatorInputException) {
            AgentToolResult.Rejected(error.safeReason)
        }
    }

    private fun isSafeRelativePath(path: String): Boolean =
        path.isNotBlank() && !path.startsWith('/') && !path.contains('\\') &&
            path.split('/').none { it.isBlank() || it == "." || it == ".." }

    private class BoundedCalculatorParser(private val input: String) {
        private var cursor = 0
        private var tokenCount = 0
        private var nestingDepth = 0

        fun parse(): BigDecimal {
            val value = parseExpression()
            skipWhitespace()
            if (!atEnd()) throw CalculatorInputException.invalid()
            return value
        }

        private fun parseExpression(): BigDecimal {
            var value = parseTerm()
            while (true) {
                when {
                    consume('+') -> value = value.add(parseTerm(), CALCULATOR_MATH_CONTEXT)
                    consume('-') -> value = value.subtract(parseTerm(), CALCULATOR_MATH_CONTEXT)
                    else -> return value
                }
            }
        }

        private fun parseTerm(): BigDecimal {
            var value = parseUnary()
            while (true) {
                when {
                    consume('*') -> value = value.multiply(parseUnary(), CALCULATOR_MATH_CONTEXT)
                    consume('/') -> {
                        val divisor = parseUnary()
                        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                            throw CalculatorInputException.divideByZero()
                        }
                        value = value.divide(divisor, CALCULATOR_MATH_CONTEXT)
                    }
                    else -> return value
                }
            }
        }

        private fun parseUnary(): BigDecimal = when {
            consume('+') -> parseUnary()
            consume('-') -> parseUnary().negate(CALCULATOR_MATH_CONTEXT)
            else -> parsePrimary()
        }

        private fun parsePrimary(): BigDecimal {
            if (consume('(')) {
                nestingDepth += 1
                if (nestingDepth > MAX_CALCULATOR_NESTING) {
                    throw CalculatorInputException.tooDeep()
                }
                try {
                    val value = parseExpression()
                    if (!consume(')')) throw CalculatorInputException.invalid()
                    return value
                } finally {
                    nestingDepth -= 1
                }
            }
            return parseNumber()
        }

        private fun parseNumber(): BigDecimal {
            skipWhitespace()
            val start = cursor
            var digitsBeforeDecimal = 0
            while (peek()?.isDigit() == true) {
                cursor += 1
                digitsBeforeDecimal += 1
            }

            var digitsAfterDecimal = 0
            var hasDecimal = false
            if (peek() == '.') {
                hasDecimal = true
                cursor += 1
                while (peek()?.isDigit() == true) {
                    cursor += 1
                    digitsAfterDecimal += 1
                }
            }

            if (digitsBeforeDecimal == 0 && digitsAfterDecimal == 0) {
                throw CalculatorInputException.invalid()
            }
            if (hasDecimal && digitsAfterDecimal == 0) {
                throw CalculatorInputException.invalid()
            }

            recordToken()
            return try {
                BigDecimal(input.substring(start, cursor), CALCULATOR_MATH_CONTEXT)
            } catch (_: NumberFormatException) {
                throw CalculatorInputException.invalid()
            }
        }

        private fun consume(expected: Char): Boolean {
            skipWhitespace()
            if (peek() != expected) return false
            cursor += 1
            recordToken()
            return true
        }

        private fun recordToken() {
            tokenCount += 1
            if (tokenCount > MAX_CALCULATOR_TOKENS) {
                throw CalculatorInputException.tooComplex()
            }
        }

        private fun skipWhitespace() {
            while (peek()?.isWhitespace() == true) cursor += 1
        }

        private fun peek(): Char? = input.getOrNull(cursor)

        private fun atEnd(): Boolean = cursor >= input.length
    }

    private class CalculatorInputException private constructor(val safeReason: String) : RuntimeException() {
        companion object {
            fun invalid() = CalculatorInputException("Calculator expression is invalid.")
            fun divideByZero() = CalculatorInputException("Calculator cannot divide by zero.")
            fun tooDeep() = CalculatorInputException("Calculator expression is too deeply nested.")
            fun tooComplex() = CalculatorInputException("Calculator expression is too complex.")
        }
    }

    private companion object {
        const val MAX_PREVIEW_CHARS = 4_000
        const val MAX_CALCULATOR_CHARS = 256
        const val MAX_CALCULATOR_NESTING = 64
        const val MAX_CALCULATOR_TOKENS = 128
        val CALCULATOR_MATH_CONTEXT = MathContext(34, RoundingMode.HALF_EVEN)
    }
}
