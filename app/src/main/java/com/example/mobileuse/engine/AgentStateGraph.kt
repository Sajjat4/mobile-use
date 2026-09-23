package com.example.mobileuse.engine

import com.example.mobileuse.model.AgentStepLog
import com.example.mobileuse.model.AgentType
import com.example.mobileuse.model.CortexDecision
import com.example.mobileuse.model.SubgoalItem
import com.example.mobileuse.model.SubgoalStatus
import com.example.mobileuse.model.ToolCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AgentExecutionState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val activeAgent: AgentType? = null,
    val goal: String = "",
    val outputDescription: String? = null,
    val subgoals: List<SubgoalItem> = emptyList(),
    val currentSubgoalId: String? = null,
    val stepCount: Int = 0,
    val maxSteps: Int = 20,
    val currentThought: String = "",
    val lastToolCall: ToolCall? = null,
    val stepLogs: List<AgentStepLog> = emptyList(),
    val structuredOutput: String? = null,
    val statusMessage: String = "Ready",
    val stepDelayMs: Long = 1000L,
    val isFinished: Boolean = false,
    val executionTimeMs: Long = 0L
)

class AgentStateGraph(
    private val deviceSimulator: DeviceSimulator,
    private val geminiClient: GeminiAgentClient,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(AgentExecutionState())
    val state: StateFlow<AgentExecutionState> = _state.asStateFlow()

    private var executionJob: Job? = null
    private var startTimeMs: Long = 0L

    fun setStepDelay(delayMs: Long) {
        _state.update { it.copy(stepDelayMs = delayMs) }
    }

    fun startTask(goal: String, outputDescription: String?) {
        stopTask()
        startTimeMs = System.currentTimeMillis()
        _state.value = AgentExecutionState(
            isRunning = true,
            goal = goal,
            outputDescription = outputDescription,
            statusMessage = "Starting multi-agent workflow..."
        )

        executionJob = scope.launch {
            runGraphLoop()
        }
    }

    fun pauseTask() {
        _state.update { it.copy(isPaused = true, statusMessage = "Execution paused") }
    }

    fun resumeTask() {
        if (_state.value.isPaused) {
            _state.update { it.copy(isPaused = false, statusMessage = "Resuming execution...") }
        }
    }

    fun stopTask() {
        executionJob?.cancel()
        executionJob = null
        _state.update {
            it.copy(
                isRunning = false,
                isPaused = false,
                activeAgent = null,
                statusMessage = if (it.isFinished) "Completed" else "Task cancelled"
            )
        }
    }

    private suspend fun runGraphLoop() {
        val goal = _state.value.goal
        val outputDesc = _state.value.outputDescription

        // 1. Planner Node: Break goal into subgoals
        transitionTo(AgentType.PLANNER, "Decomposing natural language task into structured plan...")
        delay(_state.value.stepDelayMs)
        val initialPlan = geminiClient.generateSubgoalPlan(goal, outputDesc)
        _state.update {
            it.copy(
                subgoals = initialPlan,
                currentSubgoalId = initialPlan.firstOrNull()?.id,
                stepLogs = it.stepLogs + AgentStepLog(
                    stepNumber = it.stepCount + 1,
                    agent = AgentType.PLANNER,
                    title = "Created Subgoal Plan",
                    detail = "Decomposed task into ${initialPlan.size} sequential milestones"
                )
            )
        }

        // Main agent cycle
        var stepCounter = 0
        var goalCompleted = false

        while (_state.value.isRunning && stepCounter < _state.value.maxSteps && !goalCompleted) {
            // Check pause
            while (_state.value.isPaused) {
                delay(200)
            }

            stepCounter++
            _state.update { it.copy(stepCount = stepCounter) }

            // 2. Orchestrator Node: Evaluate current subgoal and plan state
            transitionTo(AgentType.ORCHESTRATOR, "Evaluating active subgoal progression...")
            delay(_state.value.stepDelayMs / 2)
            val currentPlan = _state.value.subgoals
            val activeSubgoal = currentPlan.find { it.id == _state.value.currentSubgoalId }
                ?: currentPlan.find { it.status == SubgoalStatus.PENDING || it.status == SubgoalStatus.IN_PROGRESS }

            if (activeSubgoal == null) {
                // All subgoals handled
                goalCompleted = true
                break
            }

            // Mark active subgoal as IN_PROGRESS
            _state.update { s ->
                s.copy(
                    currentSubgoalId = activeSubgoal.id,
                    subgoals = s.subgoals.map {
                        if (it.id == activeSubgoal.id) it.copy(status = SubgoalStatus.IN_PROGRESS) else it
                    }
                )
            }

            // 3. Contextor Node: Capture mobile perception & UI hierarchy
            transitionTo(AgentType.CONTEXTOR, "Parsing screen hierarchy & accessibility tree...")
            delay(_state.value.stepDelayMs / 2)
            val screenState = deviceSimulator.screenState.value
            _state.update {
                it.copy(
                    stepLogs = it.stepLogs + AgentStepLog(
                        stepNumber = stepCounter,
                        agent = AgentType.CONTEXTOR,
                        title = "Captured Screen Context",
                        detail = "App: ${screenState.currentApp} | Identified ${screenState.elements.size} interactive elements"
                    )
                )
            }

            // 4. Cortex Node: High-level cognitive decision
            transitionTo(AgentType.CORTEX, "Analyzing screen elements and determining next action...")
            delay(_state.value.stepDelayMs)
            val decision: CortexDecision = geminiClient.generateCortexDecision(
                goal = goal,
                outputDescription = outputDesc,
                currentSubgoal = activeSubgoal,
                screenState = screenState,
                stepIndex = stepCounter
            )

            _state.update {
                it.copy(
                    currentThought = decision.thought,
                    lastToolCall = decision.toolCall,
                    structuredOutput = decision.extractedData ?: it.structuredOutput,
                    stepLogs = it.stepLogs + AgentStepLog(
                        stepNumber = stepCounter,
                        agent = AgentType.CORTEX,
                        title = "Cortex Decision",
                        detail = decision.thought,
                        toolCall = decision.toolCall
                    )
                )
            }

            // 5. Executor Node: Run tool call on device
            transitionTo(AgentType.EXECUTOR, "Executing action: ${decision.toolCall.displayDescription}...")
            delay(_state.value.stepDelayMs / 2)
            executeToolCall(decision.toolCall)

            _state.update {
                it.copy(
                    stepLogs = it.stepLogs + AgentStepLog(
                        stepNumber = stepCounter,
                        agent = AgentType.EXECUTOR,
                        title = "Action Dispatched",
                        detail = "Executed ${decision.toolCall.toolName} (${decision.toolCall.displayDescription})"
                    )
                )
            }

            // 6. Summarizer Node: Record step and update scratchpad
            transitionTo(AgentType.SUMMARIZER, "Summarizing outcome and updating execution buffer...")
            delay(_state.value.stepDelayMs / 2)

            // Update subgoal completion if signaled
            if (decision.subgoalComplete) {
                _state.update { s ->
                    val updatedSubgoals = s.subgoals.map {
                        if (it.id == activeSubgoal.id) it.copy(status = SubgoalStatus.COMPLETED) else it
                    }
                    val nextPending = updatedSubgoals.find { it.status == SubgoalStatus.PENDING }
                    s.copy(
                        subgoals = updatedSubgoals,
                        currentSubgoalId = nextPending?.id
                    )
                }
            }

            // 7. Convergence Gate: Check if entire goal is achieved
            transitionTo(AgentType.CONVERGENCE, "Verifying task completion criteria...")
            delay(_state.value.stepDelayMs / 3)

            if (decision.goalAchieved || _state.value.subgoals.all { it.status == SubgoalStatus.COMPLETED }) {
                goalCompleted = true
            }
        }

        // Completion
        val finalTime = System.currentTimeMillis() - startTimeMs
        _state.update {
            it.copy(
                isRunning = false,
                isFinished = true,
                activeAgent = null,
                executionTimeMs = finalTime,
                statusMessage = if (goalCompleted) "Task successfully accomplished!" else "Execution limit reached",
                subgoals = it.subgoals.map { sg ->
                    if (goalCompleted && sg.status != SubgoalStatus.COMPLETED) sg.copy(status = SubgoalStatus.COMPLETED) else sg
                }
            )
        }
    }

    private fun executeToolCall(toolCall: ToolCall) {
        when (toolCall.toolName.lowercase()) {
            "tap" -> {
                val elementId = toolCall.targetElementId
                if (elementId != null) {
                    deviceSimulator.tapElement(elementId)
                } else {
                    val x = toolCall.arguments["x"]?.toFloatOrNull() ?: 0.5f
                    val y = toolCall.arguments["y"]?.toFloatOrNull() ?: 0.5f
                    deviceSimulator.tap(x, y)
                }
            }
            "focus_and_input_text" -> {
                val text = toolCall.arguments["text"] ?: ""
                deviceSimulator.inputText(text, toolCall.targetElementId)
            }
            "focus_and_clear_text" -> {
                deviceSimulator.clearText(toolCall.targetElementId)
            }
            "press_key" -> {
                when (toolCall.arguments["key"]?.lowercase()) {
                    "back" -> deviceSimulator.pressBack()
                    "home" -> deviceSimulator.pressHome()
                    else -> {}
                }
            }
            "launch_app" -> {
                val appName = toolCall.arguments["app_name"] ?: "Settings"
                deviceSimulator.launchApp(appName)
            }
            "swipe" -> {
                val direction = toolCall.arguments["direction"] ?: "up"
                deviceSimulator.swipe(direction)
            }
            "wait_for_delay" -> {
                // Handled implicitly by graph timing
            }
        }
    }

    private fun transitionTo(agent: AgentType, status: String) {
        _state.update {
            it.copy(
                activeAgent = agent,
                statusMessage = status
            )
        }
    }
}
