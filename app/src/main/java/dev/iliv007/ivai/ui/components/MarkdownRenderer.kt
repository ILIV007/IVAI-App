package dev.iliv007.ivai.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iliv007.ivai.ui.theme.rememberIvaiTerminalControlColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * AST Nodes for parsed Markdown.
 */
sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class BulletList(val items: List<String>) : MarkdownBlock
    data class OrderedList(val items: List<Pair<Int, String>>) : MarkdownBlock
    data class Blockquote(val text: String) : MarkdownBlock
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock
    data class Table(
        val headers: List<String>,
        val rows: List<List<String>>,
        val alignments: List<TextAlign>
    ) : MarkdownBlock
    data object Divider : MarkdownBlock
}

/**
 * Lightweight, robust parser that converts Markdown string into structured AST blocks.
 */
object MarkdownParser {

    fun parse(markdown: String): List<MarkdownBlock> {
        val blocks = mutableListOf<MarkdownBlock>()
        val lines = markdown.lines()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            // Skip empty lines
            if (trimmed.isEmpty()) {
                i++
                continue
            }

            // 1. Fenced Code Block (```lang ... ```)
            if (trimmed.startsWith("```")) {
                val language = trimmed.removePrefix("```").trim()
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                if (i < lines.size && lines[i].trim().startsWith("```")) {
                    i++ // skip closing fence
                }
                blocks.add(MarkdownBlock.CodeBlock(language, codeLines.joinToString("\n")))
                continue
            }

            // 2. Horizontal Divider (---, ***, ___)
            if (trimmed.matches(Regex("^(\\-{3,}|\\*{3,}|_{3,})$"))) {
                blocks.add(MarkdownBlock.Divider)
                i++
                continue
            }

            // 3. Headings (# H1, ## H2, ### H3, #### H4)
            if (trimmed.startsWith("#")) {
                val match = Regex("^(#{1,6})\\s+(.*)$").find(trimmed)
                if (match != null) {
                    val level = match.groupValues[1].length
                    val content = match.groupValues[2].trim()
                    blocks.add(MarkdownBlock.Heading(level, content))
                    i++
                    continue
                }
            }

            // 4. Markdown Table (| Col 1 | Col 2 |)
            if (trimmed.startsWith("|") && trimmed.endsWith("|") && i + 1 < lines.size) {
                val nextLine = lines[i + 1].trim()
                if (isTableSeparator(nextLine)) {
                    val headers = parseTableRow(trimmed)
                    val alignments = parseTableAlignments(nextLine)
                    i += 2 // skip header and separator

                    val rows = mutableListOf<List<String>>()
                    while (i < lines.size) {
                        val rowLine = lines[i].trim()
                        if (rowLine.startsWith("|") && rowLine.endsWith("|")) {
                            rows.add(parseTableRow(rowLine))
                            i++
                        } else {
                            break
                        }
                    }
                    blocks.add(MarkdownBlock.Table(headers, rows, alignments))
                    continue
                }
            }

            // 5. Blockquote (> quote)
            if (trimmed.startsWith(">")) {
                val quoteLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().startsWith(">")) {
                    quoteLines.add(lines[i].trim().removePrefix(">").trim())
                    i++
                }
                blocks.add(MarkdownBlock.Blockquote(quoteLines.joinToString("\n")))
                continue
            }

            // 6. Unordered List (- item, * item, + item)
            if (isBulletListItem(trimmed)) {
                val items = mutableListOf<String>()
                while (i < lines.size && isBulletListItem(lines[i].trim())) {
                    val itemText = lines[i].trim().replaceFirst(Regex("^[-*+]\\s+"), "")
                    items.add(itemText)
                    i++
                }
                blocks.add(MarkdownBlock.BulletList(items))
                continue
            }

            // 7. Ordered List (1. item, 2. item)
            if (isOrderedListItem(trimmed)) {
                val items = mutableListOf<Pair<Int, String>>()
                while (i < lines.size && isOrderedListItem(lines[i].trim())) {
                    val lineTrim = lines[i].trim()
                    val match = Regex("^(\\d+)\\.\\s+(.*)$").find(lineTrim)
                    if (match != null) {
                        val num = match.groupValues[1].toIntOrNull() ?: (items.size + 1)
                        val text = match.groupValues[2]
                        items.add(Pair(num, text))
                    }
                    i++
                }
                blocks.add(MarkdownBlock.OrderedList(items))
                continue
            }

            // 8. Paragraph (Regular text block)
            val paragraphLines = mutableListOf<String>()
            while (i < lines.size) {
                val curLine = lines[i].trim()
                if (curLine.isEmpty() ||
                    curLine.startsWith("```") ||
                    curLine.startsWith("#") ||
                    curLine.startsWith(">") ||
                    isBulletListItem(curLine) ||
                    isOrderedListItem(curLine) ||
                    (curLine.startsWith("|") && curLine.endsWith("|")) ||
                    curLine.matches(Regex("^(\\-{3,}|\\*{3,}|_{3,})$"))
                ) {
                    break
                }
                paragraphLines.add(lines[i])
                i++
            }
            if (paragraphLines.isNotEmpty()) {
                blocks.add(MarkdownBlock.Paragraph(paragraphLines.joinToString("\n")))
            }
        }

        return blocks
    }

    private fun isBulletListItem(line: String): Boolean =
        line.matches(Regex("^[-*+]\\s+.*$"))

    private fun isOrderedListItem(line: String): Boolean =
        line.matches(Regex("^\\d+\\.\\s+.*$"))

    private fun isTableSeparator(line: String): Boolean {
        if (!line.startsWith("|") || !line.endsWith("|")) return false
        val cells = line.split("|").filter { it.isNotBlank() }
        return cells.isNotEmpty() && cells.all { cell ->
            cell.trim().matches(Regex("^:?-+:?$"))
        }
    }

    private fun parseTableRow(line: String): List<String> {
        val parts = line.split("|")
        return if (parts.size >= 2) {
            parts.subList(1, parts.size - 1).map { it.trim() }
        } else {
            emptyList()
        }
    }

    private fun parseTableAlignments(line: String): List<TextAlign> {
        val parts = line.split("|").filter { it.isNotBlank() }
        return parts.map { part ->
            val cell = part.trim()
            when {
                cell.startsWith(":") && cell.endsWith(":") -> TextAlign.Center
                cell.endsWith(":") -> TextAlign.End
                else -> TextAlign.Start
            }
        }
    }
}

/**
 * Builds an AnnotatedString parsing inline markdown:
 * - Bold: `**bold**` or `__bold__`
 * - Italic: `*italic*` or `_italic_`
 * - Bold & Italic: `***text***`
 * - Inline code: `` `code` ``
 * - Strikethrough: `~~strike~~`
 * - Links: `[text](url)`
 */
@Composable
fun rememberMarkdownAnnotatedString(
    text: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    codeBgColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    codeTextColor: Color = MaterialTheme.colorScheme.primary
): AnnotatedString {
    return remember(text, textColor, primaryColor, codeBgColor, codeTextColor) {
        buildMarkdownAnnotatedString(text, textColor, primaryColor, codeBgColor, codeTextColor)
    }
}

fun buildMarkdownAnnotatedString(
    text: String,
    textColor: Color,
    primaryColor: Color,
    codeBgColor: Color,
    codeTextColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        // Regex pattern to match inline markdown elements:
        // Group 1: BoldItalic (*** or ___)
        // Group 2: Bold (** or __)
        // Group 3: Italic (* or _)
        // Group 4: Inline code (`code`)
        // Group 5: Strikethrough (~~strike~~)
        // Group 6: Link [title](url)
        val inlineRegex = Regex(
            "(\\*\\*\\*(.*?)\\*\\*\\*|___(.*?)___)|" +
                    "(\\*\\*(.*?)\\*\\*|__(.*?)__)|" +
                    "(\\*(.*?)\\*|_(.*?)_)|" +
                    "(`([^`]+)`)|" +
                    "(~~(.*?)~~)|" +
                    "(\\[(.*?)\\]\\((.*?)\\))"
        )

        var lastIndex = 0
        for (match in inlineRegex.findAll(text)) {
            val start = match.range.first
            val end = match.range.last + 1

            // Append preceding raw text
            if (start > lastIndex) {
                append(text.substring(lastIndex, start))
            }

            val matchedStr = match.value
            when {
                // 1. Bold Italic ***text*** or ___text___
                matchedStr.startsWith("***") && matchedStr.endsWith("***") -> {
                    val inner = matchedStr.removeSurrounding("***")
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, color = textColor))
                    append(inner)
                    pop()
                }
                matchedStr.startsWith("___") && matchedStr.endsWith("___") -> {
                    val inner = matchedStr.removeSurrounding("___")
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, color = textColor))
                    append(inner)
                    pop()
                }
                // 2. Bold **text** or __text__
                matchedStr.startsWith("**") && matchedStr.endsWith("**") -> {
                    val inner = matchedStr.removeSurrounding("**")
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor))
                    append(inner)
                    pop()
                }
                matchedStr.startsWith("__") && matchedStr.endsWith("__") -> {
                    val inner = matchedStr.removeSurrounding("__")
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor))
                    append(inner)
                    pop()
                }
                // 3. Inline code `code`
                matchedStr.startsWith("`") && matchedStr.endsWith("`") -> {
                    val inner = matchedStr.removeSurrounding("`")
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.5.sp,
                            background = codeBgColor,
                            color = codeTextColor
                        )
                    )
                    append(" $inner ")
                    pop()
                }
                // 4. Strikethrough ~~text~~
                matchedStr.startsWith("~~") && matchedStr.endsWith("~~") -> {
                    val inner = matchedStr.removeSurrounding("~~")
                    pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = textColor.copy(alpha = 0.7f)))
                    append(inner)
                    pop()
                }
                // 5. Link [text](url)
                matchedStr.startsWith("[") && matchedStr.contains("](") && matchedStr.endsWith(")") -> {
                    val title = matchedStr.substringAfter("[").substringBefore("](")
                    pushStyle(
                        SpanStyle(
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                    append(title)
                    pop()
                }
                // 6. Italic *text* or _text_
                matchedStr.startsWith("*") && matchedStr.endsWith("*") -> {
                    val inner = matchedStr.removeSurrounding("*")
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = textColor))
                    append(inner)
                    pop()
                }
                matchedStr.startsWith("_") && matchedStr.endsWith("_") -> {
                    val inner = matchedStr.removeSurrounding("_")
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = textColor))
                    append(inner)
                    pop()
                }
                else -> {
                    append(matchedStr)
                }
            }

            lastIndex = end
        }

        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

/**
 * Main Markdown Composable rendering rich formatted text, headers, lists, quotes,
 * code blocks, and markdown tables.
 */
@Composable
fun MarkdownContent(
    content: String,
    modifier: Modifier = Modifier,
    isUser: Boolean = false,
    onCopyCode: ((String) -> Unit)? = null
) {
    val blocks = remember(content) { MarkdownParser.parse(content) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    MarkdownHeading(block = block, isUser = isUser)
                }
                is MarkdownBlock.Paragraph -> {
                    MarkdownParagraph(text = block.text, isUser = isUser)
                }
                is MarkdownBlock.BulletList -> {
                    MarkdownBulletList(items = block.items, isUser = isUser)
                }
                is MarkdownBlock.OrderedList -> {
                    MarkdownOrderedList(items = block.items, isUser = isUser)
                }
                is MarkdownBlock.Blockquote -> {
                    MarkdownBlockquote(text = block.text, isUser = isUser)
                }
                is MarkdownBlock.CodeBlock -> {
                    MarkdownCodeBlockView(
                        language = block.language,
                        code = block.code,
                        onCopyCode = onCopyCode
                    )
                }
                is MarkdownBlock.Table -> {
                    MarkdownTableView(table = block)
                }
                is MarkdownBlock.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 1.dp,
                        color = if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MarkdownHeading(
    block: MarkdownBlock.Heading,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    val textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
    val annotatedString = rememberMarkdownAnnotatedString(
        text = block.text,
        textColor = textColor,
        primaryColor = if (isUser) Color.White else MaterialTheme.colorScheme.primary,
        codeBgColor = if (isUser) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        codeTextColor = if (isUser) Color.White else MaterialTheme.colorScheme.primary
    )

    val typography = when (block.level) {
        1 -> MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
        2 -> MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold)
        3 -> MaterialTheme.typography.titleSmall.copy(fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
        else -> MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
    }

    Text(
        text = annotatedString,
        style = typography.copy(
            color = textColor,
            textDirection = TextDirection.ContentOrLtr
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
fun MarkdownParagraph(
    text: String,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    val textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
    val annotatedString = rememberMarkdownAnnotatedString(
        text = text,
        textColor = textColor,
        primaryColor = if (isUser) Color.White else MaterialTheme.colorScheme.primary,
        codeBgColor = if (isUser) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        codeTextColor = if (isUser) Color.White else MaterialTheme.colorScheme.primary
    )

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = textColor,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            textDirection = TextDirection.ContentOrLtr
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun MarkdownBulletList(
    items: List<String>,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    val textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
    val bulletColor = if (isUser) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.secondary

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { item ->
            val annotated = rememberMarkdownAnnotatedString(
                text = item,
                textColor = textColor,
                primaryColor = if (isUser) Color.White else MaterialTheme.colorScheme.primary,
                codeBgColor = if (isUser) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                codeTextColor = if (isUser) Color.White else MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 7.dp, end = 8.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(bulletColor)
                )
                Text(
                    text = annotated,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textColor,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        textDirection = TextDirection.ContentOrLtr
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MarkdownOrderedList(
    items: List<Pair<Int, String>>,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    val textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
    val numberColor = if (isUser) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.secondary

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { (index, item) ->
            val annotated = rememberMarkdownAnnotatedString(
                text = item,
                textColor = textColor,
                primaryColor = if (isUser) Color.White else MaterialTheme.colorScheme.primary,
                codeBgColor = if (isUser) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                codeTextColor = if (isUser) Color.White else MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "$index.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = numberColor,
                    modifier = Modifier
                        .widthIn(min = 20.dp)
                        .padding(top = 2.dp, end = 6.dp)
                )
                Text(
                    text = annotated,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textColor,
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        textDirection = TextDirection.ContentOrLtr
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MarkdownBlockquote(
    text: String,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    val barColor = if (isUser) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.secondary
    val bgColor = if (isUser) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    val textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface

    val annotated = rememberMarkdownAnnotatedString(
        text = text,
        textColor = textColor,
        primaryColor = if (isUser) Color.White else MaterialTheme.colorScheme.primary,
        codeBgColor = if (isUser) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        codeTextColor = if (isUser) Color.White else MaterialTheme.colorScheme.primary
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .height(IntrinsicSize.Min)
                .background(barColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = annotated,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = FontStyle.Italic,
                color = textColor.copy(alpha = 0.9f),
                fontSize = 13.5.sp,
                lineHeight = 20.sp,
                textDirection = TextDirection.ContentOrLtr
            ),
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp, horizontal = 4.dp)
        )
    }
}

/**
 * Terminal Code Block with macOS window controls, language label, and copy button.
 */
@Composable
fun MarkdownCodeBlockView(
    language: String,
    code: String,
    modifier: Modifier = Modifier,
    onCopyCode: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var isCodeCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val terminalControls = rememberIvaiTerminalControlColors()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            ),
            shadowElevation = 1.dp,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Header window bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(terminalControls.close))
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(terminalControls.minimize))
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(terminalControls.maximize))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language.isNotBlank()) language.uppercase() else "CODE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedVisibility(
                            visible = isCodeCopied,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Text(
                                text = "Copied!",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("code", code)
                                clipboard.setPrimaryClip(clip)
                                onCopyCode?.invoke(code)
                                isCodeCopied = true
                                scope.launch {
                                    delay(2000)
                                    isCodeCopied = false
                                }
                            },
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("copy_to_clipboard_button")
                                .testTag("copy_code_button")
                        ) {
                            Icon(
                                imageVector = if (isCodeCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy to Clipboard",
                                tint = if (isCodeCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable Monospace Code
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(8.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    SelectionContainer {
                        Text(
                            text = code,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.5.sp,
                                lineHeight = 17.sp,
                                textDirection = TextDirection.Ltr
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Markdown Table View with scrollable layout, alternating row striping, and column alignment.
 */
@Composable
fun MarkdownTableView(
    table: MarkdownBlock.Table,
    modifier: Modifier = Modifier
) {
    if (table.headers.isEmpty() && table.rows.isEmpty()) return

    val context = LocalContext.current
    var isTableCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val horizontalScrollState = rememberScrollState()

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            // Optional mini top-bar for table copy action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TABLE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedVisibility(
                        visible = isTableCopied,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = "Copied!",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val tableMarkdown = buildString {
                                append("| ")
                                append(table.headers.joinToString(" | "))
                                append(" |\n| ")
                                append(table.headers.map { "---" }.joinToString(" | "))
                                append(" |\n")
                                table.rows.forEach { row ->
                                    append("| ")
                                    append(table.headers.indices.map { row.getOrElse(it) { "" } }.joinToString(" | "))
                                    append(" |\n")
                                }
                            }
                            val clip = ClipData.newPlainText("table", tableMarkdown)
                            clipboard.setPrimaryClip(clip)
                            isTableCopied = true
                            scope.launch {
                                delay(2000)
                                isTableCopied = false
                            }
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("copy_to_clipboard_button")
                            .testTag("copy_table_button")
                    ) {
                        Icon(
                            imageVector = if (isTableCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy to Clipboard",
                            tint = if (isTableCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState)
                    .padding(1.dp)
            ) {
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    table.headers.forEachIndexed { index, header ->
                        val align = table.alignments.getOrElse(index) { TextAlign.Start }
                        val annotatedHeader = rememberMarkdownAnnotatedString(
                            text = header,
                            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            primaryColor = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = annotatedHeader,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = align
                            ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .widthIn(min = 85.dp)
                                .padding(horizontal = 8.dp)
                        )
                    }
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
                )

                // Body Rows
                table.rows.forEachIndexed { rowIndex, row ->
                    val isEven = rowIndex % 2 == 0
                    val rowBg = if (isEven) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .padding(vertical = 7.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        table.headers.indices.forEach { colIndex ->
                            val cellText = row.getOrElse(colIndex) { "" }
                            val align = table.alignments.getOrElse(colIndex) { TextAlign.Start }
                            val annotatedCell = rememberMarkdownAnnotatedString(
                                text = cellText,
                                textColor = MaterialTheme.colorScheme.onSurface,
                                primaryColor = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = annotatedCell,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    textAlign = align
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .widthIn(min = 85.dp)
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }

                    if (rowIndex < table.rows.size - 1) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
}
