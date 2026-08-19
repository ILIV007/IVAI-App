package dev.iliv007.ivai.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import dev.iliv007.ivai.provider.ProviderAccountAuthMode
import dev.iliv007.ivai.provider.ProviderCapability
import dev.iliv007.ivai.provider.ProviderEndpointTrustMode
import dev.iliv007.ivai.provider.ProviderKind
import dev.iliv007.ivai.ui.screens.RouterScreen
import dev.iliv007.ivai.ui.theme.IvaiTheme
import dev.iliv007.ivai.ui.viewmodel.ProviderAccountCard
import dev.iliv007.ivai.ui.viewmodel.ProviderConnectionCard
import dev.iliv007.ivai.ui.viewmodel.ProviderManagementState
import dev.iliv007.ivai.ui.viewmodel.ProviderModelCard
import dev.iliv007.ivai.ui.viewmodel.RouterCandidateSelection
import dev.iliv007.ivai.ui.viewmodel.RouterManagementState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ProviderComboSetupTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun empty_connections_hub_is_visible_and_recordable() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                RouterScreen(
                    state = RouterManagementState(),
                    providers = ProviderManagementState(),
                    onAddProvider = { _, _, _, _, _, _, _, _, _, _ -> },
                    onDeleteProvider = {},
                    onAddAccountToConnection = { _, _, _, _ -> },
                    onAddModelToConnection = { _, _, _ -> },
                    onSetProviderEnabled = { _, _ -> },
                    onDismissProviderError = {},
                    onCreateCombo = { _, _, _ -> },
                    onDismissError = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("connections_hub").assertIsDisplayed()
        composeTestRule.onNodeWithTag("connections_empty_state").assertIsDisplayed()
        composeTestRule.onNodeWithTag("connections_combo_empty_state").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase72_connections_empty_dark.png")
    }

    @Test
    fun connection_is_created_after_account_save_without_forcing_a_model() {
        var saveCalls = 0
        var savedKind: ProviderKind? = null
        var savedModel = ""
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                RouterScreen(
                    state = RouterManagementState(),
                    providers = ProviderManagementState(),
                    onAddProvider = { kind, _, _, _, model, _, _, _, _, _ ->
                        saveCalls += 1
                        savedKind = kind
                        savedModel = model
                    },
                    onDeleteProvider = {},
                    onAddAccountToConnection = { _, _, _, _ -> },
                    onAddModelToConnection = { _, _, _ -> },
                    onSetProviderEnabled = { _, _ -> },
                    onDismissProviderError = {},
                    onCreateCombo = { _, _, _ -> },
                    onDismissError = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("button_add_provider").performClick()
        composeTestRule.onNodeWithTag("provider_setup_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("button_provider_setup_next").performClick()
        composeTestRule.onNodeWithTag("provider_setup_validation_error").assertIsDisplayed()
        assertEquals(0, saveCalls)

        composeTestRule.onNodeWithTag("provider_preset_selector").performClick()
        composeTestRule.onNodeWithTag("provider_family_gemini").performClick()
        composeTestRule.onNodeWithTag("button_provider_setup_next").performClick()
        composeTestRule.onNodeWithTag("button_provider_setup_next").performClick()
        composeTestRule.onNodeWithTag("input_provider_account_name").performClick()
        composeTestRule.onNodeWithTag("input_provider_account_name").performTextInput("Personal")
        composeTestRule.onNodeWithTag("input_provider_api_key").performClick()
        composeTestRule.onNodeWithTag("input_provider_api_key").performTextInput("x")
        composeTestRule.onNodeWithText("Stored only after final save; never shown again.").assertIsDisplayed()
        assertEquals(0, saveCalls)
        composeTestRule.onNodeWithTag("button_provider_setup_final_save").performScrollTo().performClick()

        assertEquals(1, saveCalls)
        assertEquals(ProviderKind.GEMINI, savedKind)
        assertEquals("", savedModel)
    }

    @Test
    fun account_is_added_to_existing_connection_without_creating_another_provider() {
        var providerSaveCalls = 0
        var savedConnectionId = ""
        var savedAccountName = ""
        var savedAuthMode: ProviderAccountAuthMode? = null
        var savedRawSecret: String? = null
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                RouterScreen(
                    state = RouterManagementState(),
                    providers = providerStateWithConnectionWithoutModels(),
                    onAddProvider = { _, _, _, _, _, _, _, _, _, _ -> providerSaveCalls += 1 },
                    onDeleteProvider = {},
                    onAddAccountToConnection = { connectionId, accountName, authMode, rawSecret ->
                        savedConnectionId = connectionId
                        savedAccountName = accountName
                        savedAuthMode = authMode
                        savedRawSecret = rawSecret
                    },
                    onAddModelToConnection = { _, _, _ -> },
                    onSetProviderEnabled = { _, _ -> },
                    onDismissProviderError = {},
                    onCreateCombo = { _, _, _ -> },
                    onDismissError = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("button_add_account_connection-1").performClick()
        composeTestRule.onNodeWithTag("provider_account_setup_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("button_provider_account_save").performClick()
        composeTestRule.onNodeWithTag("provider_account_validation_error").assertExists()
        composeTestRule.onNodeWithTag("input_provider_additional_account_name").performTextInput("Work")
        composeTestRule.onNodeWithTag("input_provider_additional_account_api_key").performTextInput("x")
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/r4_add_account_dark.png")
        composeTestRule.onNodeWithTag("button_provider_account_save").performClick()

        assertEquals(0, providerSaveCalls)
        assertEquals("connection-1", savedConnectionId)
        assertEquals("Work", savedAccountName)
        assertEquals(ProviderAccountAuthMode.API_KEY, savedAuthMode)
        assertEquals("x", savedRawSecret)
    }

    @Test
    fun model_is_added_to_existing_connection_without_creating_another_provider() {
        var providerSaveCalls = 0
        var savedConnectionId = ""
        var savedModelId = ""
        var savedCapabilities = emptySet<ProviderCapability>()
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                RouterScreen(
                    state = RouterManagementState(),
                    providers = providerStateWithConnectionWithoutModels(),
                    onAddProvider = { _, _, _, _, _, _, _, _, _, _ -> providerSaveCalls += 1 },
                    onDeleteProvider = {},
                    onAddAccountToConnection = { _, _, _, _ -> },
                    onAddModelToConnection = { connectionId, modelId, capabilities ->
                        savedConnectionId = connectionId
                        savedModelId = modelId
                        savedCapabilities = capabilities
                    },
                    onSetProviderEnabled = { _, _ -> },
                    onDismissProviderError = {},
                    onCreateCombo = { _, _, _ -> },
                    onDismissError = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("button_add_model_connection-1").performClick()
        composeTestRule.onNodeWithTag("provider_model_setup_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("button_provider_model_save").performClick()
        composeTestRule.onNodeWithTag("provider_model_validation_error").assertExists()
        composeTestRule.onNodeWithTag("input_provider_model_id").performClick()
        composeTestRule.onNodeWithTag("input_provider_model_id").performTextInput("model-two")
        composeTestRule.onNodeWithTag("provider_model_capability_text").performClick()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/r4_add_model_dark.png")
        composeTestRule.onNodeWithTag("button_provider_model_save").performClick()

        assertEquals(0, providerSaveCalls)
        assertEquals("connection-1", savedConnectionId)
        assertEquals("model-two", savedModelId)
        assertEquals(setOf(ProviderCapability.TEXT), savedCapabilities)
    }

    @Test
    fun local_endpoint_requires_explicit_trust_confirmation() {
        composeTestRule.setContent {
            IvaiTheme {
                RouterScreen(
                    state = RouterManagementState(),
                    providers = ProviderManagementState(),
                    onAddProvider = { _, _, _, _, _, _, _, _, _, _ -> },
                    onDeleteProvider = {},
                    onAddAccountToConnection = { _, _, _, _ -> },
                    onAddModelToConnection = { _, _, _ -> },
                    onSetProviderEnabled = { _, _ -> },
                    onDismissProviderError = {},
                    onCreateCombo = { _, _, _ -> },
                    onDismissError = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("button_add_provider").performClick()
        composeTestRule.onNodeWithTag("provider_preset_selector").performClick()
        composeTestRule.onNodeWithTag("provider_family_local_loopback").performClick()
        composeTestRule.onNodeWithTag("button_provider_setup_next").performClick()
        composeTestRule.onNodeWithTag("provider_trust_local_loopback_https").assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_provider_display_name").performClick()
        composeTestRule.onNodeWithTag("input_provider_display_name").performTextInput("Local server")
        composeTestRule.onNodeWithTag("input_provider_endpoint").performClick()
        composeTestRule.onNodeWithTag("input_provider_endpoint").performTextInput("https://127.0.0.1")
        composeTestRule.onNodeWithTag("button_provider_setup_next").performClick()
        composeTestRule.onNodeWithTag("provider_setup_validation_error").assertExists()
        composeTestRule.onNodeWithTag("local_endpoint_trust_confirmation").performClick()
        composeTestRule.onNodeWithTag("button_provider_setup_next").performClick()
        composeTestRule.onNodeWithTag("provider_setup_step_3").assertIsDisplayed()
        composeTestRule.onNodeWithTag("local_no_auth_selector").assertIsDisplayed()
    }

    @Test
    fun private_lan_https_uses_its_own_trust_disclosure() {
        composeTestRule.setContent {
            IvaiTheme {
                RouterScreen(
                    state = RouterManagementState(),
                    providers = ProviderManagementState(),
                    onAddProvider = { _, _, _, _, _, _, _, _, _, _ -> },
                    onDeleteProvider = {},
                    onAddAccountToConnection = { _, _, _, _ -> },
                    onAddModelToConnection = { _, _, _ -> },
                    onSetProviderEnabled = { _, _ -> },
                    onDismissProviderError = {},
                    onCreateCombo = { _, _, _ -> },
                    onDismissError = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("button_add_provider").performClick()
        composeTestRule.onNodeWithTag("provider_preset_selector").performClick()
        composeTestRule.onNodeWithTag("provider_family_local_lan").performClick()
        composeTestRule.onNodeWithTag("button_provider_setup_next").performClick()
        composeTestRule.onNodeWithTag("provider_trust_local_lan_https").assertIsDisplayed()
        composeTestRule.onNodeWithText("Private-LAN HTTPS").assertIsDisplayed()
    }

    @Test
    fun combo_builder_preserves_user_reordered_candidate_sequence_until_final_save() {
        var savedCandidates = emptyList<RouterCandidateSelection>()
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                RouterScreen(
                    state = RouterManagementState(),
                    providers = providerStateWithTwoModels(),
                    onAddProvider = { _, _, _, _, _, _, _, _, _, _ -> },
                    onDeleteProvider = {},
                    onAddAccountToConnection = { _, _, _, _ -> },
                    onAddModelToConnection = { _, _, _ -> },
                    onSetProviderEnabled = { _, _ -> },
                    onDismissProviderError = {},
                    onCreateCombo = { _, _, candidates -> savedCandidates = candidates },
                    onDismissError = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("button_create_combo").performClick()
        composeTestRule.onNodeWithTag("combo_builder_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_combo_name").performTextInput("Research fallback")
        composeTestRule.onNodeWithTag("combo_candidate_connection-1_account-1_model-a").performClick()
        composeTestRule.onNodeWithTag("combo_candidate_connection-1_account-1_model-b").performClick()
        composeTestRule.onNodeWithTag("button_combo_move_up_connection-1_account-1_model-b").performClick()
        composeTestRule.onNodeWithContentDescription("Fallback position 1: Personal OpenRouter · Personal · Model B").assertExists()
        composeTestRule.onNodeWithTag("button_combo_builder_review").performClick()
        composeTestRule.onNodeWithTag("combo_builder_final_review").performScrollTo().assertIsDisplayed()
        assertEquals(emptyList<RouterCandidateSelection>(), savedCandidates)
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase72_combo_review_dark.png")
        composeTestRule.onNodeWithTag("button_combo_builder_final_save").performClick()

        assertEquals(listOf("model-b", "model-a"), savedCandidates.map { it.modelId })
    }

    private fun providerStateWithConnectionWithoutModels() = providerStateWithTwoModels().copy(
        connections = providerStateWithTwoModels().connections.map { it.copy(manualModels = emptyList()) }
    )

    private fun providerStateWithTwoModels() = ProviderManagementState(
        connections = listOf(
            ProviderConnectionCard(
                connectionId = "connection-1",
                kind = ProviderKind.OPENROUTER,
                displayName = "Personal OpenRouter",
                baseUrlLabel = null,
                endpointTrustMode = ProviderEndpointTrustMode.REMOTE_HTTPS,
                localTrustConfirmed = false,
                enabled = true,
                accounts = listOf(
                    ProviderAccountCard(
                        accountId = "account-1",
                        displayName = "Personal",
                        credentialReference = "opaque-test-reference",
                        authMode = ProviderAccountAuthMode.API_KEY,
                        enabled = true,
                        credentialStored = true
                    )
                ),
                manualModels = listOf(
                    ProviderModelCard(
                        registryModelId = "model-a",
                        modelId = "model-a",
                        displayName = "Model A",
                        capabilities = listOf("TEXT", "STREAMING"),
                        selectable = true
                    ),
                    ProviderModelCard(
                        registryModelId = "model-b",
                        modelId = "model-b",
                        displayName = "Model B",
                        capabilities = listOf("TEXT", "STREAMING"),
                        selectable = true
                    )
                )
            )
        )
    )
}
