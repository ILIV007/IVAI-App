package dev.iliv007.ivai

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import dev.iliv007.ivai.ui.components.IvaiSidebarContent
import dev.iliv007.ivai.ui.navigation.NavDestination
import dev.iliv007.ivai.ui.theme.IvaiTheme
import org.junit.Assert.assertEquals
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

        // Initially in Chats
        composeTestRule.onNodeWithTag("input_message_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("button_sidebar_toggle").assertIsDisplayed()

        // Open sidebar and navigate to Agents
        composeTestRule.onNodeWithTag("button_sidebar_toggle").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_item_agents").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("agent_notice_banner").assertExists()

        // Open sidebar and navigate to Projects
        composeTestRule.onNodeWithTag("button_sidebar_toggle").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_item_projects").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("projects_notice_banner").assertExists()

        // Open sidebar and navigate to Router
        composeTestRule.onNodeWithTag("button_sidebar_toggle").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_item_router").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("router_notice_banner").assertExists()

        // Open sidebar and navigate to Settings
        composeTestRule.onNodeWithTag("button_sidebar_toggle").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_item_settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_byok_card").assertExists()

        // Open sidebar and return to Chats
        composeTestRule.onNodeWithTag("button_sidebar_toggle").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("sidebar_button_new_chat").performClick()
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
    fun `verify MainChatScreen message list and prompt input field`() {
        composeTestRule.setContent {
            IvaiTheme {
                dev.iliv007.ivai.ui.screens.MainChatScreen()
            }
        }

        composeTestRule.onNodeWithTag("input_message_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("button_send_message").assertIsDisplayed()
        composeTestRule.onNodeWithTag("button_active_model").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_messages_list").assertIsDisplayed()
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

