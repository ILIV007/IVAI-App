package dev.iliv007.ivai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import dev.iliv007.ivai.ui.model.ChatThread
import dev.iliv007.ivai.ui.model.WorkspaceProject
import dev.iliv007.ivai.ui.theme.IvaiIconSizeTokens
import dev.iliv007.ivai.ui.theme.IvaiLayoutTokens
import dev.iliv007.ivai.ui.theme.IvaiShapeTokens
import dev.iliv007.ivai.ui.theme.IvaiSpacing
import dev.iliv007.ivai.ui.theme.IvaiStrokeTokens

/**
 * Chat-only history drawer. It intentionally owns thread search and project filtering so global
 * navigation remains limited to the five destination-level product areas.
 */
@Composable
fun ChatSessionDrawerContent(
    threads: List<ChatThread>,
    selectedThreadId: String,
    projects: List<WorkspaceProject>,
    selectedProjectId: String?,
    onSelectThread: (String) -> Unit,
    onSelectProject: (String?) -> Unit,
    onNewChat: () -> Unit,
    onDeleteThread: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredThreads = remember(threads, searchQuery, selectedProjectId) {
        threads.filter { thread ->
            val matchesSearch = searchQuery.isBlank() ||
                thread.title.contains(searchQuery, ignoreCase = true) ||
                thread.snippet.contains(searchQuery, ignoreCase = true) ||
                (thread.projectName?.contains(searchQuery, ignoreCase = true) == true)
            val matchesProject = selectedProjectId == null || thread.projectId == selectedProjectId
            matchesSearch && matchesProject
        }
    }

    ModalDrawerSheet(
        modifier = modifier
            .width(IvaiLayoutTokens.ChatDrawerWidth)
            .fillMaxHeight()
            .testTag("chat_session_drawer"),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = RoundedCornerShape(topEnd = IvaiShapeTokens.Sheet, bottomEnd = IvaiShapeTokens.Sheet)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(IvaiSpacing.XSmall),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            IvaiPageHeader(
                title = "Chats",
                subtitle = "Local conversation history",
                testTag = "chat_session_drawer_header"
            )
            Surface(
                onClick = onNewChat,
                shape = RoundedCornerShape(IvaiShapeTokens.Control),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IvaiLayoutTokens.MinimumTouchTarget)
                    .testTag("chat_session_drawer_new_chat")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(IvaiIconSizeTokens.Inline))
                    Spacer(Modifier.width(IvaiSpacing.XxSmall))
                    Text("New chat", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(IvaiShapeTokens.Control))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(IvaiStrokeTokens.Default, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(IvaiShapeTokens.Control))
                    .padding(horizontal = IvaiSpacing.XSmall, vertical = IvaiSpacing.XxSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(IvaiIconSizeTokens.Inline)
                )
                Spacer(Modifier.width(IvaiSpacing.XxSmall))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_session_search"),
                    decorationBox = { input ->
                        if (searchQuery.isBlank()) {
                            Text("Search chats", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        input()
                    }
                )
                if (searchQuery.isNotBlank()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.testTag("chat_session_clear_search")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear chat search")
                    }
                }
            }
            Text(
                text = "PROJECT FILTER",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = IvaiSpacing.XSmall)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XxSmall)
            ) {
                ChatProjectFilterChip(
                    label = "All",
                    selected = selectedProjectId == null,
                    onClick = { onSelectProject(null) },
                    testTag = "chat_session_filter_all"
                )
                projects.forEach { project ->
                    ChatProjectFilterChip(
                        label = project.name,
                        selected = project.id == selectedProjectId,
                        onClick = { onSelectProject(project.id) },
                        testTag = "chat_session_filter_${project.id}"
                    )
                }
            }
            Text(
                text = if (searchQuery.isBlank()) "${filteredThreads.size} conversations" else "Search results",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = IvaiSpacing.XSmall)
            )
            if (filteredThreads.isEmpty()) {
                ChatSessionDrawerEmptyState(hasSearch = searchQuery.isNotBlank())
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxxSmall)
                ) {
                    items(filteredThreads, key = { it.id }) { thread ->
                        ChatSessionDrawerItem(
                            thread = thread,
                            selected = thread.id == selectedThreadId,
                            onClick = { onSelectThread(thread.id) },
                            onDelete = { onDeleteThread(thread.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatProjectFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(IvaiShapeTokens.Control),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            IvaiStrokeTokens.Default,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = IvaiSpacing.XSmall, vertical = IvaiSpacing.XxSmall)
        )
    }
}

@Composable
private fun ChatSessionDrawerEmptyState(hasSearch: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(IvaiSpacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
    ) {
        Icon(
            Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(IvaiIconSizeTokens.Feature)
        )
        Text(
            text = if (hasSearch) "No chats match this search" else "No chats in this view",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChatSessionDrawerItem(
    thread: ChatThread,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(IvaiShapeTokens.Control),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        border = if (selected) androidx.compose.foundation.BorderStroke(IvaiStrokeTokens.Default, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Chat: ${thread.title}" }
            .testTag("chat_session_item_${thread.id}")
    ) {
        Row(
            modifier = Modifier.padding(IvaiSpacing.XSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XxSmall)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxxSmall)) {
                Text(
                    text = thread.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = thread.projectName ?: "No project",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.testTag("chat_session_delete_${thread.id}")) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete chat")
            }
        }
    }
}
