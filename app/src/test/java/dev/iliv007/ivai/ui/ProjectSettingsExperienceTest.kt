package dev.iliv007.ivai.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import dev.iliv007.ivai.ui.model.UiPreviewState
import dev.iliv007.ivai.ui.model.WorkspaceProject
import dev.iliv007.ivai.ui.screens.ProjectsScreen
import dev.iliv007.ivai.ui.screens.SettingsScreen
import dev.iliv007.ivai.ui.theme.IvaiTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ProjectSettingsExperienceTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun project_hub_uses_explicit_project_context_before_starting_a_chat() {
        var selectedProjectId: String? = null
        var chatProjectId: String? = null
        composeTestRule.setContent {
            IvaiTheme {
                ProjectsScreen(
                    projects = sampleProjects,
                    onSelectProject = { selectedProjectId = it },
                    onStartProjectChat = { chatProjectId = it }
                )
            }
        }

        composeTestRule.onNodeWithTag("project_card_project-1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("project_start_chat_project-1").performClick()

        assertEquals("project-1", selectedProjectId)
        assertEquals("project-1", chatProjectId)
    }

    @Test
    fun project_hub_supports_selection_clear_and_explicit_destination_routes() {
        var selectedProjectId: String? = "project-1"
        var chatsOpened = false
        var agentsOpened = false
        composeTestRule.setContent {
            IvaiTheme {
                ProjectsScreen(
                    projects = sampleProjects,
                    selectedProjectId = "project-1",
                    onSelectProject = { selectedProjectId = it },
                    onOpenChats = { chatsOpened = true },
                    onOpenAgents = { agentsOpened = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("workspace_selected_project_detail").assertIsDisplayed()
        composeTestRule.onNodeWithTag("projects_clear_context").performClick()
        composeTestRule.onNodeWithTag("projects_open_chats").performClick()
        composeTestRule.onNodeWithTag("projects_open_agents").performClick()

        assertEquals(null, selectedProjectId)
        assertTrue(chatsOpened)
        assertTrue(agentsOpened)
    }

    @Test
    fun selected_project_detail_exposes_only_known_local_workspace_facts() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                ProjectsScreen(
                    projects = sampleProjects,
                    selectedProjectId = "project-1"
                )
            }
        }

        composeTestRule.onNodeWithTag("workspace_selected_project_detail").assertIsDisplayed()
        composeTestRule.onNodeWithTag("project_detail_name_project-1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("project_detail_description_project-1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("project_detail_file_count_project-1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("workspace_activity_routes").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/r5_workspace_detail_dark.png")
        composeTestRule.onNodeWithText("Continue with this project").assertIsDisplayed()
    }

    @Test
    fun project_hub_empty_state_routes_to_chat_without_claiming_project_activity() {
        var chatsOpened = false
        composeTestRule.setContent {
            IvaiTheme {
                ProjectsScreen(
                    projects = emptyList(),
                    onOpenChats = { chatsOpened = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("projects_empty_state").assertIsDisplayed()
        composeTestRule.onNodeWithText("No local projects yet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("projects_empty_open_chat").performClick()

        assertTrue(chatsOpened)
    }

    @Test
    fun project_hub_preview_states_are_specific_and_safe() {
        composeTestRule.setContent {
            IvaiTheme {
                ProjectsScreen(
                    projects = sampleProjects,
                    previewState = UiPreviewState.OFFLINE
                )
            }
        }

        composeTestRule.onNodeWithTag("projects_preview_state").assertIsDisplayed()
        composeTestRule.onNodeWithText("Provider connection unavailable").assertIsDisplayed()
        composeTestRule.onNodeWithText("This workspace does not make a network request. Local project summaries remain available.")
            .assertIsDisplayed()
    }

    @Test
    fun project_hub_representative_dark_states_are_recordable() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                ProjectsScreen(
                    projects = sampleProjects,
                    selectedProjectId = "project-1"
                )
            }
        }

        composeTestRule.onNodeWithTag("projects_screen").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase74_projects_ready_dark.png")
    }

    @Test
    fun project_hub_representative_light_state_is_recordable() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = false) {
                ProjectsScreen(
                    projects = sampleProjects,
                    selectedProjectId = "project-1"
                )
            }
        }

        composeTestRule.onNodeWithTag("projects_screen").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase74_projects_ready_light.png")
    }

    @Test
    fun project_hub_empty_dark_state_is_recordable() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                ProjectsScreen(
                    projects = emptyList(),
                    previewState = UiPreviewState.EMPTY
                )
            }
        }

        composeTestRule.onNodeWithTag("projects_empty_state").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase74_projects_empty_dark.png")
    }

    @Test
    fun settings_exposes_only_explicit_theme_connections_and_local_data_actions() {
        var themeToggled = false
        var connectionsOpened = false
        var localDataDeleteRequested = false
        composeTestRule.setContent {
            IvaiTheme {
                SettingsScreen(
                    isDarkTheme = false,
                    onToggleTheme = { themeToggled = true },
                    onOpenConnections = { connectionsOpened = true },
                    onDeleteAllLocalData = { localDataDeleteRequested = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Toggle dark mode").assertIsDisplayed()
        composeTestRule.onNodeWithTag("switch_theme_mode").performClick()
        composeTestRule.onNodeWithTag("button_open_connections_from_settings").performClick()
        composeTestRule.onNodeWithTag("button_delete_all_data").performScrollTo().performClick()

        assertTrue(themeToggled)
        assertTrue(connectionsOpened)
        assertTrue(localDataDeleteRequested)
    }

    @Test
    fun settings_dark_and_light_surface_copy_is_recordable() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                SettingsScreen(isDarkTheme = true)
            }
        }

        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your data stays under your control").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase74_settings_dark.png")
    }

    @Test
    fun settings_light_surface_copy_is_recordable() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = false) {
                SettingsScreen(isDarkTheme = false)
            }
        }

        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Light mode is on.").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase74_settings_light.png")
    }

    @Test
    fun project_hub_loading_and_error_copy_have_uniform_state_cards() {
        val previewState = mutableStateOf(UiPreviewState.LOADING)
        composeTestRule.setContent {
            IvaiTheme {
                ProjectsScreen(
                    projects = sampleProjects,
                    previewState = previewState.value
                )
            }
        }
        composeTestRule.onNodeWithText("Loading local workspace").assertIsDisplayed()

        previewState.value = UiPreviewState.ERROR
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("projects_preview_state").assertIsDisplayed()
        composeTestRule.onNodeWithText("Workspace summary unavailable").assertIsDisplayed()
    }

    private companion object {
        val sampleProjects = listOf(
            WorkspaceProject(
                id = "project-1",
                name = "Arabic research notes",
                description = "Notes and source files kept in this local project.",
                fileCount = 3,
                lastModified = "Just now"
            )
        )
    }
}
