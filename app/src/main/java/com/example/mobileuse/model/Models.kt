package com.example.mobileuse.model

import kotlinx.serialization.Serializable

enum class AgentType(val displayName: String, val roleDescription: String) {
    PLANNER("Planner", "Decomposes the high-level natural language goal into sequential subgoals"),
    ORCHESTRATOR("Orchestrator", "Coordinates active subgoal progression, verifies completion, and initiates replanning"),
    CONTEXTOR("Contextor", "Perceives mobile screen state, parses UI hierarchy and accessibility nodes"),
    CORTEX("Cortex", "Cognitive reasoning core: analyzes screen elements and decides next optimal action"),
    EXECUTOR("Executor", "Dispatches low-level touch, swipe, keyboard, and application control actions"),
    SUMMARIZER("Summarizer", "Evaluates action outcomes, maintains memory buffer and scratchpad"),
    CONVERGENCE("Convergence Gate", "Verifies subgoal criteria and routes to next step, replan, or completion")
}

enum class SubgoalStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

@Serializable
data class SubgoalItem(
    val id: String,
    val title: String,
    val description: String,
    val status: SubgoalStatus = SubgoalStatus.PENDING,
    val actionCount: Int = 0
)

@Serializable
data class RectBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

@Serializable
data class UIElement(
    val id: Int,
    val role: String,
    val text: String = "",
    val contentDescription: String = "",
    val resourceId: String = "",
    val bounds: RectBounds,
    val isClickable: Boolean = true,
    val isEditable: Boolean = false,
    val isScrollable: Boolean = false,
    val isChecked: Boolean = false,
    val isFocused: Boolean = false
)

@Serializable
data class ToolCall(
    val toolName: String,
    val arguments: Map<String, String> = emptyMap(),
    val targetElementId: Int? = null,
    val displayDescription: String = ""
)

@Serializable
data class CortexDecision(
    val thought: String,
    val toolCall: ToolCall,
    val subgoalComplete: Boolean = false,
    val goalAchieved: Boolean = false,
    val extractedData: String? = null
)

@Serializable
data class AgentStepLog(
    val stepNumber: Int,
    val agent: AgentType,
    val title: String,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolCall: ToolCall? = null
)

@Serializable
data class DeviceScreenState(
    val currentApp: String,
    val currentActivity: String,
    val elements: List<UIElement>,
    val batteryPercent: Int = 85,
    val isWifiOn: Boolean = true,
    val currentTime: String = "10:42 AM",
    val focusedElementId: Int? = null,
    val notificationCount: Int = 3
)

enum class LLMProvider(val id: String, val displayName: String, val defaultModel: String) {
    GEMINI("gemini", "Google Gemini", "gemini-3.5-flash"),
    OPENAI("openai", "OpenAI", "gpt-4o"),
    ANTHROPIC("anthropic", "Anthropic Claude", "claude-3-7-sonnet"),
    MINIMAX("minimax", "MiniMax", "MiniMax-M2.7"),
    LOCAL("local", "Local / OpenAI Compatible", "meta-llama-3")
}

@Serializable
data class TaskPreset(
    val id: String,
    val category: String,
    val title: String,
    val prompt: String,
    val outputDescription: String? = null,
    val targetApp: String,
    val estimatedSteps: Int = 4
)

@Serializable
data class ExecutionRecord(
    val id: String,
    val prompt: String,
    val outputDescription: String? = null,
    val status: String, // SUCCESS, FAILED, CANCELLED
    val durationMs: Long,
    val stepCount: Int,
    val timestamp: Long,
    val structuredOutput: String? = null,
    val subgoals: List<SubgoalItem> = emptyList(),
    val actionSummary: List<String> = emptyList()
)
