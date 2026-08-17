package dev.iliv007.ivai

import android.content.Context
import android.content.pm.ApplicationInfo
import org.xmlpull.v1.XmlPullParser
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import dev.iliv007.ivai.ui.components.IvaiSidebarContent
import dev.iliv007.ivai.ui.model.ChatMessage
import dev.iliv007.ivai.ui.model.ChatThread
import dev.iliv007.ivai.ui.model.MessageSender
import dev.iliv007.ivai.ui.navigation.NavDestination
import dev.iliv007.ivai.ui.theme.IvaiTheme
import dev.iliv007.ivai.ui.viewmodel.WorkspaceUiState
import dev.iliv007.ivai.ui.viewmodel.WorkspaceViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("IVAI", appName)
    }

    @Test
    fun `backup and device transfer remain disabled for local data`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
        assertEquals(0, applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP)

        val parser = context.resources.getXml(R.xml.ivai_data_extraction_rules)
        val excludedLocations = linkedSetOf<String>()
        var section = ""
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "cloud-backup", "device-transfer" -> section = parser.name
                    "exclude" -> {
                        val domain = parser.getAttributeValue(null, "domain")
                        val path = parser.getAttributeValue(null, "path")
                        excludedLocations += "$section:$domain:$path"
                    }
                }
            }
            parser.next()
        }

        val domains = setOf(
            "root",
            "file",
            "database",
            "sharedpref",
            "external",
            "device_root",
            "device_file",
            "device_database",
            "device_sharedpref"
        )
        val expectedExclusions = setOf("cloud-backup", "device-transfer").flatMap { transferType ->
            domains.map { domain -> "$transferType:$domain:." }
        }.toSet()
        assertEquals(expectedExclusions, excludedLocations)
        assertTrue(excludedLocations.none { it.contains("null") })

        val legacyParser = context.resources.getXml(R.xml.ivai_full_backup_rules)
        val legacyExclusions = linkedSetOf<String>()
        while (legacyParser.eventType != XmlPullParser.END_DOCUMENT) {
            if (legacyParser.eventType == XmlPullParser.START_TAG && legacyParser.name == "exclude") {
                val domain = legacyParser.getAttributeValue(null, "domain")
                val path = legacyParser.getAttributeValue(null, "path")
                legacyExclusions += "$domain:$path"
            }
            legacyParser.next()
        }
        assertEquals(domains.map { domain -> "$domain:." }.toSet(), legacyExclusions)
        assertTrue(legacyExclusions.none { it.contains("null") })
    }

    @Test
    fun `adaptive launcher icons expose a dedicated monochrome vector`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val androidNamespace = "http://schemas.android.com/apk/res/android"
        val launcherResources = listOf(R.mipmap.ic_launcher, R.mipmap.ic_launcher_round)

        launcherResources.forEach { launcherResource ->
            val parser = context.resources.getXml(launcherResource)
            var monochromeDrawable = 0
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG && parser.name == "monochrome") {
                    monochromeDrawable = parser.getAttributeResourceValue(androidNamespace, "drawable", 0)
                }
                parser.next()
            }
            assertEquals(R.drawable.ivai_launcher_monochrome, monochromeDrawable)
        }

        val vectorParser = context.resources.getXml(R.drawable.ivai_launcher_monochrome)
        var vectorFound = false
        var pathCount = 0
        while (vectorParser.eventType != XmlPullParser.END_DOCUMENT) {
            if (vectorParser.eventType == XmlPullParser.START_TAG) {
                if (vectorParser.name == "vector") vectorFound = true
                if (vectorParser.name == "path") pathCount += 1
            }
            vectorParser.next()
        }
        assertTrue(vectorFound)
        assertTrue(pathCount > 0)
    }

    @Test
    fun `verify sidebar navigation items rendering and selection`() {
        var selectedDest = NavDestination.CHATS
        composeTestRule.setContent {
            IvaiTheme {
                IvaiSidebarContent(
                    currentDestination = selectedDest,
                    onDestinationSelected = { selectedDest = it }
                )
            }
        }

        composeTestRule.onNodeWithTag("sidebar_button_new_chat").assertExists()
        composeTestRule.onNodeWithTag("nav_item_agents").assertExists()
        composeTestRule.onNodeWithTag("nav_item_projects").assertExists()
        composeTestRule.onNodeWithTag("nav_item_router").assertExists()
        composeTestRule.onNodeWithTag("nav_item_settings").assertExists()

        composeTestRule.onNodeWithTag("nav_item_agents").performClick()
        assertEquals(NavDestination.AGENTS, selectedDest)

        composeTestRule.onNodeWithTag("nav_item_projects").performClick()
        assertEquals(NavDestination.PROJECTS, selectedDest)
    }

    @Test
    fun `verify navigation across all five destinations`() {
        composeTestRule.setContent {
            IvaiTheme {
                IvaiMainApp()
            }
        }

        // Pixel 8 uses compact navigation. Destination controls remain persistent in the bottom bar.
        composeTestRule.onNodeWithTag("input_message_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ivai_compact_navigation").assertIsDisplayed()

        // Navigate to Agents through the destination control.
        composeTestRule.onNodeWithTag("nav_item_agents").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("agent_notice_banner").assertExists()

        // Navigate to Workspace through the destination control.
        composeTestRule.onNodeWithTag("nav_item_projects").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("projects_screen").assertExists()

        // Navigate to Connections through the destination control.
        composeTestRule.onNodeWithTag("nav_item_router").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("connections_hub").assertExists()

        // Navigate to Settings through the destination control.
        composeTestRule.onNodeWithTag("nav_item_settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_connections_shortcut").assertExists()
        composeTestRule.onNodeWithTag("button_open_connections_from_settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("connections_hub").assertExists()

        // Return to Chat through the destination control.
        composeTestRule.onNodeWithTag("nav_item_chats").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("input_message_text").assertExists()
    }

    @Test
    fun `verify top bar state switcher interaction`() {
        composeTestRule.setContent {
            IvaiTheme {
                IvaiMainApp()
            }
        }

        // Click on state switcher button
        composeTestRule.onNodeWithTag("button_state_switcher").assertIsDisplayed()
        composeTestRule.onNodeWithTag("button_state_switcher").performClick()
    }

    @Test
    fun `verify canonical chat path exposes composer stream and combo controls`() {
        val thread = ChatThread(
            id = "chat-controls",
            title = "Local chat",
            snippet = "",
            timestamp = "Now",
            modelOrCombo = "No execution target selected",
            messages = listOf(ChatMessage("control-message", MessageSender.USER, "Local fixture", "Now"))
        )
        val viewModel = WorkspaceViewModel(
            initialState = WorkspaceUiState(threads = listOf(thread), selectedThreadId = thread.id)
        )
        composeTestRule.setContent {
            IvaiTheme {
                IvaiMainApp(workspaceViewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("input_message_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("button_send_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("button_select_combo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_messages_list").assertIsDisplayed()
    }

    @Test
    fun `canonical chat renders mixed bidi message with LTR token`() {
        val message = ChatMessage(
            id = "mixed-bidi",
            sender = MessageSender.USER,
            text = "سلام IVAI — inspect README.md before ادامهٔ کار",
            timestamp = "10:16 AM"
        )
        val thread = ChatThread(
            id = "rtl-thread",
            title = "RTL verification",
            snippet = message.text,
            timestamp = "Just now",
            modelOrCombo = "Gemini Flash Combo",
            messages = listOf(message)
        )
        val workspaceViewModel = WorkspaceViewModel(
            WorkspaceUiState(threads = listOf(thread), selectedThreadId = thread.id)
        )

        composeTestRule.setContent {
            IvaiTheme {
                IvaiMainApp(workspaceViewModel = workspaceViewModel)
            }
        }

        composeTestRule.onNodeWithTag("message_bubble_mixed-bidi").assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_message_text").assertIsDisplayed()
    }

    @Test
    fun `verify markdown parser extracts tables, code blocks, and formatted blocks`() {
        val sampleMarkdown = """
            ### Title
            This is **bold** and *italic* text with `inline code`.
            
            > A safety quote block
            
            | Model | Latency |
            | :--- | ---: |
            | Gemini | 250ms |
            | Claude | 450ms |
            
            ```kotlin
            fun test() = true
            ```
            
            - Item 1
            - Item 2
            
            1. First
            2. Second
        """.trimIndent()

        val blocks = dev.iliv007.ivai.ui.components.MarkdownParser.parse(sampleMarkdown)
        
        // Assert AST structure
        assert(blocks.any { it is dev.iliv007.ivai.ui.components.MarkdownBlock.Heading })
        assert(blocks.any { it is dev.iliv007.ivai.ui.components.MarkdownBlock.Paragraph })
        assert(blocks.any { it is dev.iliv007.ivai.ui.components.MarkdownBlock.Blockquote })
        assert(blocks.any { it is dev.iliv007.ivai.ui.components.MarkdownBlock.Table })
        assert(blocks.any { it is dev.iliv007.ivai.ui.components.MarkdownBlock.CodeBlock })
        assert(blocks.any { it is dev.iliv007.ivai.ui.components.MarkdownBlock.BulletList })
        assert(blocks.any { it is dev.iliv007.ivai.ui.components.MarkdownBlock.OrderedList })

        val tableBlock = blocks.first { it is dev.iliv007.ivai.ui.components.MarkdownBlock.Table } as dev.iliv007.ivai.ui.components.MarkdownBlock.Table
        assertEquals(listOf("Model", "Latency"), tableBlock.headers)
        assertEquals(2, tableBlock.rows.size)
        assertEquals("Gemini", tableBlock.rows[0][0])
        assertEquals("250ms", tableBlock.rows[0][1])
    }

    @Test
    fun `verify markdown content composable renders without crashing`() {
        val markdown = """
            ## Gemini Flash Summary
            - Step 1: **Analyze**
            - Step 2: *Optimize*
            
            | Feature | Status |
            | :---: | :---: |
            | Markdown | Enabled |
            | Tables | Supported |
        """.trimIndent()

        composeTestRule.setContent {
            IvaiTheme {
                dev.iliv007.ivai.ui.components.MarkdownContent(
                    content = markdown,
                    isUser = false
                )
            }
        }

        // Successfully rendered without crashing
    }
}

