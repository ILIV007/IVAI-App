package dev.iliv007.ivai.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import dev.iliv007.ivai.agent.AgentRunStatus
import dev.iliv007.ivai.agent.AgentToolKind
import dev.iliv007.ivai.agent.ApprovalStatus
import dev.iliv007.ivai.ui.screens.AgentsScreen
import dev.iliv007.ivai.ui.theme.IvaiTheme
import dev.iliv007.ivai.ui.viewmodel.AgentApprovalCard
import dev.iliv007.ivai.ui.viewmodel.AgentManagementState
import dev.iliv007.ivai.ui.viewmodel.AgentProfileCard
import dev.iliv007.ivai.ui.viewmodel.AgentRunCard
import dev.iliv007.ivai.ui.viewmodel.AgentRunTraceStepCard
import dev.iliv007.ivai.ui.viewmodel.AgentTargetOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
class AgentWorkspaceExperienceTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun no_target_state_routes_to_connections_only_after_explicit_action() {
        var openConnectionsCount = 0
        render(state = AgentManagementState(), onOpenConnections = { openConnectionsCount += 1 })

        composeTestRule.onNodeWithTag("agent_profiles_no_target").assertIsDisplayed()
        assertEquals(0, openConnectionsCount)
        composeTestRule.onNodeWithTag("button_agent_open_connections").performClick()
        assertEquals(1, openConnectionsCount)
    }

    @Test
    fun profile_creation_waits_for_explicit_final_review() {
        var createCount = 0
        var createdTarget: String? = null
        render(
            state = AgentManagementState(availableTargets = listOf(target())),
            darkTheme = true,
            onCreate = { _, _, targetKind, targetId, _, _, _, _, _, _ ->
                createCount += 1
                createdTarget = "$targetKind:$targetId"
            }
        )

        composeTestRule.onNodeWithTag("agent_profiles_empty").assertIsDisplayed()
        composeTestRule.onNodeWithTag("button_agent_create_first_profile").performClick()
        composeTestRule.onNodeWithTag("input_agent_profile_name").performTextInput("Research helper")
        composeTestRule.onNodeWithTag("input_agent_profile_instructions").performTextInput("Keep summaries local")
        composeTestRule.onNodeWithTag("button_agent_editor_continue").performClick()
        composeTestRule.onNodeWithTag("agent_target_COMBO_research-combo").performClick()
        composeTestRule.onNodeWithTag("button_agent_editor_continue").performClick()
        composeTestRule.onNodeWithTag("button_agent_editor_continue").performClick()

        composeTestRule.onNodeWithText("Final review").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase73_agent_profile_review_dark.png")
        assertEquals(0, createCount)
        composeTestRule.onNodeWithTag("button_agent_create_final").performClick()
        assertEquals(1, createCount)
        assertEquals("COMBO:research-combo", createdTarget)
    }

    @Test
    fun running_workspace_exposes_trace_and_cancel_and_is_recordable() {
        var cancelledRunId: String? = null
        val run = run(status = AgentRunStatus.RUNNING)
        render(
            state = AgentManagementState(
                availableTargets = listOf(target()),
                profiles = listOf(profile()),
                activeRuns = listOf(run),
                selectedRunId = run.runId,
                selectedRunTrace = listOf(traceStep())
            ),
            onCancel = { cancelledRunId = it },
            darkTheme = true
        )

        composeTestRule.onNodeWithTag("agent_run_workspace_${run.runId}").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_run_status_${run.runId}").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trace_step_step-1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("button_workspace_cancel_${run.runId}").performClick()
        assertEquals(run.runId, cancelledRunId)
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase73_agent_running_dark.png")
    }

    @Test
    fun terminal_workspace_hides_cancel_and_shows_completed_state() {
        val run = run(status = AgentRunStatus.COMPLETED)
        render(
            state = AgentManagementState(
                availableTargets = listOf(target()),
                profiles = listOf(profile()),
                activeRuns = listOf(run),
                selectedRunId = run.runId
            )
        )

        composeTestRule.onNodeWithTag("agent_run_workspace_${run.runId}").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_run_status_${run.runId}").assertIsDisplayed()
        assertTrue(composeTestRule.onAllNodesWithTag("button_workspace_cancel_${run.runId}").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun awaiting_approval_workspace_announces_one_time_review_and_is_recordable() {
        val run = run(status = AgentRunStatus.AWAITING_APPROVAL)
        render(
            state = AgentManagementState(
                availableTargets = listOf(target()),
                profiles = listOf(profile()),
                activeRuns = listOf(run),
                selectedRunId = run.runId,
                pendingApprovals = listOf(approval())
            ),
            darkTheme = true
        )

        composeTestRule.onNodeWithTag("agent_run_workspace_${run.runId}").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Awaiting one-time approval").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase73_agent_awaiting_approval_dark.png")
    }

    @Test
    fun cancelled_workspace_hides_cancel_action() {
        val run = run(status = AgentRunStatus.CANCELLED)
        render(
            state = AgentManagementState(
                availableTargets = listOf(target()),
                profiles = listOf(profile()),
                activeRuns = listOf(run),
                selectedRunId = run.runId
            )
        )

        composeTestRule.onNodeWithTag("agent_run_workspace_${run.runId}").performScrollTo().assertIsDisplayed()
        assertTrue(composeTestRule.onAllNodesWithTag("button_workspace_cancel_${run.runId}").fetchSemanticsNodes().isEmpty())
        composeTestRule.onNodeWithText("Stopped").assertIsDisplayed()
    }

    @Test
    fun failed_run_recovery_explains_no_automatic_target_or_permission_change() {
        val run = run(status = AgentRunStatus.FAILED)
        render(
            state = AgentManagementState(
                availableTargets = listOf(target()),
                profiles = listOf(profile()),
                activeRuns = listOf(run),
                selectedRunId = run.runId,
                operationError = "The selected local target is unavailable."
            )
        )

        composeTestRule.onNodeWithText("Check the selected target").assertIsDisplayed()
        composeTestRule.onNodeWithText("IVAI did not change your target, permissions or local files automatically.").assertIsDisplayed()
    }

    @Test
    fun pending_write_approval_is_prioritized_above_profile_library_without_auto_resolution() {
        var resolved: Boolean? = null
        val pendingApproval = approval()
        render(
            state = AgentManagementState(
                availableTargets = listOf(target()),
                profiles = listOf(profile()),
                pendingApprovals = listOf(pendingApproval)
            ),
            onResolve = { _, allowOnce -> resolved = allowOnce }
        )

        composeTestRule.onNodeWithTag("agent_pending_approvals_priority").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_profile_library").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/r5_agent_pending_approval_dark.png")
        assertEquals(null, resolved)
        composeTestRule.onNodeWithTag("button_review_approval_${pendingApproval.approvalId}").performClick()
        assertEquals(null, resolved)
    }

    @Test
    fun approval_sheet_requires_explicit_allow_once_or_deny() {
        var resolved: Boolean? = null
        val approval = approval()
        render(
            state = AgentManagementState(
                availableTargets = listOf(target()),
                pendingApprovals = listOf(approval)
            ),
            onResolve = { _, allowOnce -> resolved = allowOnce }
        )

        composeTestRule.onNodeWithTag("button_review_approval_${approval.approvalId}").performClick()
        composeTestRule.onNodeWithTag("agent_write_approval_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_approval_path_${approval.approvalId}").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_approval_preview_${approval.approvalId}").assertIsDisplayed()
        assertEquals(null, resolved)
        composeTestRule.onNodeWithTag("deny_${approval.approvalId}").performClick()
        assertFalse(resolved ?: true)
        composeTestRule.onNodeWithTag("agent_approval_resolved").assertIsDisplayed()
        composeTestRule.onNodeWithText("Write denied").assertIsDisplayed()
    }

    private fun render(
        state: AgentManagementState,
        darkTheme: Boolean = false,
        onOpenConnections: () -> Unit = {},
        onCreate: (
            String,
            String,
            String,
            String,
            String?,
            String?,
            Set<AgentToolKind>,
            Int,
            Int,
            Long
        ) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
        onCancel: (String) -> Unit = {},
        onResolve: (String, Boolean) -> Unit = { _, _ -> }
    ) {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = darkTheme) {
                AgentsScreen(
                    state = state,
                    onCreateAgent = onCreate,
                    onStartRun = { _, _ -> },
                    onSelectRun = { },
                    onCancelRun = onCancel,
                    onResolveApproval = onResolve,
                    onDismissError = { },
                    onOpenConnections = onOpenConnections
                )
            }
        }
    }

    private fun target() = AgentTargetOption(
        targetKind = "COMBO",
        targetId = "research-combo",
        accountId = null,
        label = "Research Combo"
    )

    private fun profile() = AgentProfileCard(
        profileId = "agent-1",
        name = "Research helper",
        instructions = "Keep all work local.",
        targetLabel = "Research Combo",
        projectId = "project-1",
        enabledTools = listOf(AgentToolKind.CALCULATE, AgentToolKind.READ_PROJECT_FILE),
        maxSteps = 8,
        maxToolCalls = 6,
        maxRuntimeMs = 60_000L,
        enabled = true
    )

    private fun run(status: AgentRunStatus) = AgentRunCard(
        runId = "run-1",
        agentId = "agent-1",
        agentName = "Research helper",
        goal = "Summarize the local project notes",
        status = status,
        startedAtEpochMs = 1_700_000_000_000L,
        safeErrorMessage = if (status == AgentRunStatus.FAILED) "The selected local target is unavailable." else null
    )

    private fun traceStep() = AgentRunTraceStepCard(
        stepId = "step-1",
        runId = "run-1",
        position = 1,
        stepKind = "PLAN",
        status = "COMPLETED",
        safeSummary = "Prepared a bounded local plan.",
        createdAtEpochMs = 1_700_000_000_100L
    )

    private fun approval() = AgentApprovalCard(
        approvalId = "approval-1",
        runId = "run-1",
        toolKind = AgentToolKind.WRITE_PROJECT_FILE,
        targetPath = "notes/summary.md",
        preview = "# Local summary\nOnly this bounded preview can be written.",
        status = ApprovalStatus.PENDING,
        createdAtEpochMs = 1_700_000_000_200L
    )
}
