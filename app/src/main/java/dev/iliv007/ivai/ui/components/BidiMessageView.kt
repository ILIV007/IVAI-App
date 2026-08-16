package dev.iliv007.ivai.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iliv007.ivai.ui.model.ChatMessage
import dev.iliv007.ivai.ui.model.MessageSender
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * BidiMessageBubble renders distinct user and AI chat bubbles with BiDi support,
 * custom headers, badges, terminal code blocks, and action toolbars.
 */
@Composable
fun BidiMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
    onCopyMessage: ((ChatMessage) -> Unit)? = null,
    onDeleteMessage: ((ChatMessage) -> Unit)? = null
) {
    if (message.sender == MessageSender.USER) {
        UserMessageBubble(
            message = message,
            modifier = modifier,
            onCopyMessage = onCopyMessage,
            onDeleteMessage = onDeleteMessage
        )
    } else {
        AiMessageBubble(
            message = message,
            modifier = modifier,
            onCopyMessage = onCopyMessage,
            onDeleteMessage = onDeleteMessage
        )
    }
}

/**
 * Distinct User Chat Bubble:
 * - Asymmetrical right-leaning chat bubble with rounded corners
 * - Vibrant gradient accent background with high contrast
 * - Timestamp and delivery checkmark
 */
@Composable
fun UserMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
    onCopyMessage: ((ChatMessage) -> Unit)? = null,
    onDeleteMessage: ((ChatMessage) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    val userBubbleShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 4.dp,
        bottomStart = 18.dp,
        bottomEnd = 18.dp
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.widthIn(max = 330.dp)
        ) {
            // User Chat Bubble Container with vibrant distinct gradient
            Box(
                modifier = Modifier
                    .clip(userBubbleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF059669), // Emerald
                                Color(0xFF0284C7)  // Deep Sky Cyan
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Color(0xFF38F9B6).copy(alpha = 0.4f),
                        userBubbleShape
                    )
                    .testTag("message_bubble_${message.id}")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Message Body with Markdown formatting and BiDi handling
                    SelectionContainer {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("message_text_${message.id}")
                        ) {
                            MarkdownContent(
                                content = message.text,
                                isUser = true
                            )
                        }
                    }

                    // Optional Code Snippet inside User message
                    if (message.codeSnippet != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TerminalCodeBlock(
                            code = message.codeSnippet,
                            messageId = message.id
                        )
                    }

                    // User Bubble Footer (Timestamp, Actions)
                    Spacer(modifier = Modifier.height(4.dp))
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Action buttons
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("user_msg", message.text)
                                        clipboard.setPrimaryClip(clip)
                                        onCopyMessage?.invoke(message)
                                        isCopied = true
                                        scope.launch {
                                            delay(2000)
                                            isCopied = false
                                        }
                                    },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("button_copy_message_${message.id}")
                                ) {
                                    if (isCopied) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Copied",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy message",
                                            tint = Color.White.copy(alpha = 0.75f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }

                                if (onDeleteMessage != null) {
                                    IconButton(
                                        onClick = { onDeleteMessage.invoke(message) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("button_delete_message_${message.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete message",
                                            tint = Color.White.copy(alpha = 0.75f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }

                                AnimatedVisibility(
                                    visible = isCopied,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    Text(
                                        text = "Copied!",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }
                            }

                            // Timestamp and Delivery Checkmark
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = message.timestamp,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        textDirection = TextDirection.Ltr
                                    ),
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Delivered",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Distinct AI / Assistant Chat Bubble:
 * - Asymmetrical left-leaning chat bubble
 * - AI Assistant Avatar with glowing cyan ring & badge
 * - Header with AI model badge and latency pulse chip
 * - Terminal code block styling with macOS traffic light window dots
 * - Comprehensive action footer
 */
@Composable
fun AiMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
    onCopyMessage: ((ChatMessage) -> Unit)? = null,
    onDeleteMessage: ((ChatMessage) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    val aiBubbleShape = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 18.dp,
        bottomStart = 18.dp,
        bottomEnd = 18.dp
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            // AI Chat Bubble Container
            Surface(
                shape = aiBubbleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .clip(aiBubbleShape)
                    .testTag("message_bubble_${message.id}")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    // AI Header: Name, Model badge, Latency chip
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "IVAI",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                message.modelBadge?.let { badge ->
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .border(
                                                0.5.dp,
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.secondary)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = badge,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.5.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                message.latencyMs?.let { latency ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${latency}ms",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Corner 'Copy to Clipboard' Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isCopied) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                        )
                                        .border(
                                            0.5.dp,
                                            if (isCopied) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val fullText = buildString {
                                                append(message.text)
                                                if (message.codeSnippet != null) {
                                                    append("\n\n")
                                                    append(message.codeSnippet)
                                                }
                                            }
                                            val clip = ClipData.newPlainText("ai_response", fullText)
                                            clipboard.setPrimaryClip(clip)
                                            onCopyMessage?.invoke(message)
                                            isCopied = true
                                            scope.launch {
                                                delay(2000)
                                                isCopied = false
                                            }
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("copy_to_clipboard_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                            contentDescription = "Copy to Clipboard",
                                            tint = if (isCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = if (isCopied) "Copied!" else "Copy",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = if (isCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                    // Message Body with rich Markdown formatting (tables, code blocks, headers, lists) and BiDi handling
                    SelectionContainer {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("message_text_${message.id}")
                        ) {
                            MarkdownContent(
                                content = message.text,
                                isUser = false
                            )
                        }
                    }

                    // Terminal Code Block
                    if (message.codeSnippet != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TerminalCodeBlock(
                            code = message.codeSnippet,
                            messageId = message.id
                        )
                    }

                    // AI Bubble Footer (Action toolbar & Timestamp)
                    Spacer(modifier = Modifier.height(8.dp))
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Action toolbar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val fullText = buildString {
                                            append(message.text)
                                            if (message.codeSnippet != null) {
                                                append("\n\n")
                                                append(message.codeSnippet)
                                            }
                                        }
                                        val clip = ClipData.newPlainText("ai_response", fullText)
                                        clipboard.setPrimaryClip(clip)
                                        onCopyMessage?.invoke(message)
                                        isCopied = true
                                        scope.launch {
                                            delay(2000)
                                            isCopied = false
                                        }
                                    },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("button_copy_message_${message.id}")
                                ) {
                                    if (isCopied) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Copied",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy message",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }

                                if (onDeleteMessage != null) {
                                    IconButton(
                                        onClick = { onDeleteMessage.invoke(message) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("button_delete_message_${message.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete message",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }

                                AnimatedVisibility(
                                    visible = isCopied,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    Text(
                                        text = "Copied!",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Response timestamp
                            Text(
                                text = message.timestamp,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    textDirection = TextDirection.Ltr
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Terminal Code Block with macOS window controls & LTR locking
 */
@Composable
fun TerminalCodeBlock(
    code: String,
    messageId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCodeCopied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Column {
                // Window top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5F56))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFBD2E))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF27C93F))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "KOTLIN / LTR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("code_snippet", code)
                            clipboard.setPrimaryClip(clip)
                            isCodeCopied = true
                            scope.launch {
                                delay(2000)
                                isCodeCopied = false
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("button_copy_code_$messageId")
                    ) {
                        Icon(
                            imageVector = if (isCodeCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = if (isCodeCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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


