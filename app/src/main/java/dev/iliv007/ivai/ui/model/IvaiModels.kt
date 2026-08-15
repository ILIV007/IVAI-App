package dev.iliv007.ivai.ui.model

enum class MessageSender {
    USER,
    ASSISTANT,
    SYSTEM
}

enum class MessageContentType {
    TEXT,
    CODE,
    MIXED_BIDI
}

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: String,
    val type: MessageContentType = MessageContentType.TEXT,
    val codeSnippet: String? = null,
    val modelBadge: String? = null,
    val latencyMs: Long? = null
)

data class ChatThread(
    val id: String,
    val title: String,
    val snippet: String,
    val timestamp: String,
    val modelOrCombo: String,
    val messages: List<ChatMessage>,
    val projectId: String? = null,
    val projectName: String? = null
)

data class BasicAgentProfile(
    val id: String,
    val name: String,
    val description: String,
    val role: String,
    val stepLimit: Int,
    val allowedTools: List<String>,
    val requiresWriteConfirmation: Boolean,
    val boundModelOrCombo: String
)

data class AgentRunStep(
    val stepNumber: Int,
    val actionName: String,
    val targetOrInput: String,
    val status: StepStatus,
    val durationMs: Long,
    val details: String? = null
)

enum class StepStatus {
    COMPLETED,
    IN_PROGRESS,
    PENDING,
    CONFIRMATION_REQUIRED
}

data class WorkspaceProject(
    val id: String,
    val name: String,
    val description: String,
    val fileCount: Int,
    val lastModified: String
)

data class RouterComboMember(
    val priority: Int,
    val provider: String,
    val modelId: String,
    val timeoutSec: Int,
    val maxRetries: Int
)

data class RouterCombo(
    val id: String,
    val name: String,
    val description: String,
    val members: List<RouterComboMember>,
    val fallbackStrategy: String
)

enum class UiPreviewState {
    NORMAL,
    LOADING,
    EMPTY,
    ERROR,
    OFFLINE
}

object MockDataRepository {

    val rtlCorpusMessages = listOf(
        ChatMessage(
            id = "rtl-1",
            sender = MessageSender.USER,
            text = "سلام، پروژه IVAI را با Gemini 3 بررسی کن.",
            timestamp = "10:14 AM"
        ),
        ChatMessage(
            id = "rtl-2",
            sender = MessageSender.ASSISTANT,
            text = "مرحبا، افتح الملف README.md ثم اكتب summary.",
            timestamp = "10:15 AM",
            modelBadge = "gemini-2.5-flash",
            latencyMs = 280
        ),
        ChatMessage(
            id = "rtl-3",
            sender = MessageSender.USER,
            text = "نسخه 2.1 در مسیر /docs/RTL_BIDI.md قرار دارد.",
            timestamp = "10:16 AM"
        ),
        ChatMessage(
            id = "rtl-4",
            sender = MessageSender.ASSISTANT,
            text = "قیمت ۱۲۳٬۴۵۶ تومان و latency برابر 250ms بود.",
            timestamp = "10:17 AM",
            modelBadge = "openrouter/auto",
            latencyMs = 250
        ),
        ChatMessage(
            id = "rtl-5",
            sender = MessageSender.USER,
            text = "مدل openai/gpt-4.1-mini خطای HTTP 429 داد.",
            timestamp = "10:18 AM"
        ),
        ChatMessage(
            id = "rtl-6",
            sender = MessageSender.ASSISTANT,
            text = "لینک https://example.com/a?x=1 را باز نکن.",
            timestamp = "10:19 AM",
            modelBadge = "custom-openai",
            latencyMs = 310
        ),
        ChatMessage(
            id = "rtl-7",
            sender = MessageSender.USER,
            text = "کد: val title = \"سلام IVAI\"",
            timestamp = "10:20 AM",
            type = MessageContentType.CODE,
            codeSnippet = "val title = \"سلام IVAI\"\nprintln(\"Active locale: LTR shell with BiDi prose\")"
        ),
        ChatMessage(
            id = "rtl-8",
            sender = MessageSender.ASSISTANT,
            text = "(نسخه Alpha) برای Android 10+ آماده می‌شود.",
            timestamp = "10:21 AM",
            modelBadge = "gemini-2.5-pro",
            latencyMs = 420
        )
    )

    val defaultChatThreads = listOf(
        ChatThread(
            id = "chat-1",
            title = "BiDi & RTL Validation Corpus",
            snippet = "(نسخه Alpha) برای Android 10+ آماده می‌شود.",
            timestamp = "Just now",
            modelOrCombo = "Gemini Flash Combo",
            messages = rtlCorpusMessages,
            projectId = "proj-2",
            projectName = "BiDi Corpus & Tests"
        ),
        ChatThread(
            id = "chat-2",
            title = "Local Architecture Review",
            snippet = "IVAI stores chats, settings and run traces strictly on-device.",
            timestamp = "Yesterday",
            modelOrCombo = "OpenRouter Auto",
            messages = listOf(
                ChatMessage(
                    id = "msg-2-1",
                    sender = MessageSender.USER,
                    text = "Explain the security boundaries of IVAI Alpha.",
                    timestamp = "09:00 AM"
                ),
                ChatMessage(
                    id = "msg-2-2",
                    sender = MessageSender.ASSISTANT,
                    text = """
### IVAI Architecture & Security Boundaries

IVAI is a **zero-cloud-server Android workspace** running fully on-device with direct **BYOK (Bring-Your-Own-Key)** access.

> **Key Guarantee**: Local keys and chat transcripts are stored strictly in private sandbox storage and never shared across telemetry.

#### 1. Supported AI Providers & Routing Table

| Provider | Default Model | Latency | Security Level |
| :--- | :--- | :---: | ---: |
| Google Gemini | `gemini-2.5-flash` | 240ms | Keystore Hardware |
| OpenRouter Auto | `auto/routed` | 380ms | Private App Sandbox |
| Custom Endpoint | `gpt-4.1-mini` | 310ms | AES-256 Vault |

#### 2. Key Capabilities
- **BiDi Engine**: Full RTL (Persian/Arabic) & LTR script isolation.
- **Local Database**: Room persistence with automatic schema migrations.
- **Safety Limits**: Max 10 execution steps with manual write confirmations.

```kotlin
fun initializeIvaiWorkspace(context: Context) {
    val config = IvaiConfig(
        storage = StorageMode.ON_DEVICE_ONLY,
        bidiSupport = true,
        markdownRendering = true
    )
    println("IVAI Workspace Ready: ${'$'}config")
}
```
                    """.trimIndent(),
                    timestamp = "09:01 AM",
                    modelBadge = "gemini-2.5-pro",
                    latencyMs = 350
                )
            ),
            projectId = "proj-1",
            projectName = "IVAI Native Android"
        ),
        ChatThread(
            id = "chat-3",
            title = "Bounded Agent Step Limits",
            snippet = "Basic bounded agent enforces max 10 steps and confirmation on file write.",
            timestamp = "Aug 9",
            modelOrCombo = "Deterministic Trio Combo",
            messages = listOf(
                ChatMessage(
                    id = "msg-3-1",
                    sender = MessageSender.USER,
                    text = "What is the policy for agent write operations?",
                    timestamp = "02:30 PM"
                ),
                ChatMessage(
                    id = "msg-3-2",
                    sender = MessageSender.ASSISTANT,
                    text = "Any file or workspace write requires explicit user preview and confirmation before execution. Step budgets prevent infinite loops.",
                    timestamp = "02:31 PM",
                    modelBadge = "openrouter/claude",
                    latencyMs = 410
                )
            ),
            projectId = "proj-1",
            projectName = "IVAI Native Android"
        ),
        ChatThread(
            id = "chat-4",
            title = "Latency & Fallback Failover Policy",
            snippet = "Router switches provider after 2 failed retries or 429 status code.",
            timestamp = "Aug 8",
            modelOrCombo = "Gemini Flash Combo",
            messages = listOf(
                ChatMessage(
                    id = "msg-4-1",
                    sender = MessageSender.USER,
                    text = "چگونه استراتژی Failover در روتر کار می‌کند؟",
                    timestamp = "11:20 AM"
                ),
                ChatMessage(
                    id = "msg-4-2",
                    sender = MessageSender.ASSISTANT,
                    text = "روتر به صورت خودکار در صورت مواجهه با خطای Rate-limit (429) یا تایم‌اوت، درخواست را به عضو بعدی در کامبو (Fallback) منتقل می‌نماید.",
                    timestamp = "11:21 AM",
                    modelBadge = "gemini-2.5-flash",
                    latencyMs = 210
                )
            ),
            projectId = "proj-3",
            projectName = "Router Rules"
        )
    )

    val mockAgents = listOf(
        BasicAgentProfile(
            id = "agent-1",
            name = "Code & Doc Auditor",
            description = "Reads files, checks syntax, and inspects local schemas safely.",
            role = "Code Reviewer",
            stepLimit = 10,
            allowedTools = listOf("read_file", "search_workspace", "calculate"),
            requiresWriteConfirmation = true,
            boundModelOrCombo = "Gemini Flash Combo"
        ),
        BasicAgentProfile(
            id = "agent-2",
            name = "Local Research Assistant",
            description = "Analyzes local notes and extracts structured summaries.",
            role = "Synthesizer",
            stepLimit = 6,
            allowedTools = listOf("read_file", "calculate"),
            requiresWriteConfirmation = true,
            boundModelOrCombo = "Deterministic Trio Combo"
        )
    )

    val mockAgentTraceSteps = listOf(
        AgentRunStep(
            stepNumber = 1,
            actionName = "read_file",
            targetOrInput = "/workspace/README.md",
            status = StepStatus.COMPLETED,
            durationMs = 45,
            details = "Read 1,420 bytes successfully."
        ),
        AgentRunStep(
            stepNumber = 2,
            actionName = "calculate",
            targetOrInput = "tokens(1420 bytes) ~ 355",
            status = StepStatus.COMPLETED,
            durationMs = 12,
            details = "Estimated token budget: 355 tokens."
        ),
        AgentRunStep(
            stepNumber = 3,
            actionName = "propose_write_file",
            targetOrInput = "/workspace/summary.md",
            status = StepStatus.CONFIRMATION_REQUIRED,
            durationMs = 0,
            details = "Target: /workspace/summary.md (+4 lines diff)"
        )
    )

    val mockProjects = listOf(
        WorkspaceProject(
            id = "proj-1",
            name = "IVAI Native Android",
            description = "Core Kotlin + Compose local BYOK application workspace",
            fileCount = 14,
            lastModified = "Aug 10, 2026"
        ),
        WorkspaceProject(
            id = "proj-2",
            name = "BiDi Corpus & Tests",
            description = "Persian, Arabic, and mixed BiDi test cases for Android",
            fileCount = 6,
            lastModified = "Aug 09, 2026"
        ),
        WorkspaceProject(
            id = "proj-3",
            name = "Router Rules",
            description = "Fallback configurations and combo latency parameters",
            fileCount = 3,
            lastModified = "Aug 08, 2026"
        )
    )

    val mockCombos = listOf(
        RouterCombo(
            id = "combo-1",
            name = "Gemini Flash Combo",
            description = "Primary low-latency route with automatic secondary fallback",
            fallbackStrategy = "Sequential on timeout or 429/5xx error",
            members = listOf(
                RouterComboMember(1, "Google Gemini", "gemini-2.5-flash", 8, 2),
                RouterComboMember(2, "OpenRouter", "google/gemini-2.5-flash", 12, 1),
                RouterComboMember(3, "Custom OpenAI", "gpt-4.1-mini", 15, 1)
            )
        ),
        RouterCombo(
            id = "combo-2",
            name = "Deterministic Trio Combo",
            description = "High precision combo for code analysis and agent tasks",
            fallbackStrategy = "Sequential failover with attempt trace logging",
            members = listOf(
                RouterComboMember(1, "Google Gemini", "gemini-2.5-pro", 20, 1),
                RouterComboMember(2, "OpenRouter", "anthropic/claude-3.5-sonnet", 25, 1)
            )
        )
    )
}
