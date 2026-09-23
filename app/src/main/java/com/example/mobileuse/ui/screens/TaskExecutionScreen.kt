package com.example.mobileuse.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileuse.engine.AgentStateGraph
import com.example.mobileuse.engine.DataRepository
import com.example.mobileuse.engine.DeviceSimulator
import com.example.mobileuse.model.ExecutionRecord
import com.example.mobileuse.model.SubgoalStatus
import com.example.mobileuse.ui.components.AgentGraphVisualizer
import com.example.mobileuse.ui.components.DeviceScreenCanvas
import com.example.mobileuse.ui.theme.AccentCyan
import com.example.mobileuse.ui.theme.AccentEmerald
import com.example.mobileuse.ui.theme.AccentRose
import com.example.mobileuse.ui.theme.CortexColor
import com.example.mobileuse.ui.theme.PrimaryBlue

@Composable
fun TaskExecutionScreen(
    agentGraph: AgentStateGraph,
    deviceSimulator: DeviceSimulator,
    dataRepository: DataRepository,
    modifier: Modifier = Modifier
) {
    val executionState by agentGraph.state.collectAsState()
    val screenState by deviceSimulator.screenState.collectAsState()
    val tapIndicator by deviceSimulator.tapIndicator.collectAsState()
    val presets by dataRepository.presets.collectAsState()

    var goalText by remember { mutableStateOf("Go to settings and tell me my current battery level") }
    var outputDescText by remember { mutableStateOf("A JSON summary of battery percentage and remaining time") }
    var showOutputDesc by remember { mutableStateOf(true) }
    var showVisionBoxes by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
            .testTag("task_execution_screen")
    ) {
        // 1. Natural Language Task Input & Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Natural Language Command",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { showVisionBoxes = !showVisionBoxes },
                            modifier = Modifier.size(32.dp).testTag("toggle_vision_tags")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Toggle Vision Tags",
                                tint = if (showVisionBoxes) AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                deviceSimulator.resetToHome()
                                Toast.makeText(context, "Device reset to Home", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp).testTag("reset_device_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Device",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it },
                    placeholder = { Text("What should mobile-use accomplish on the device?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_prompt_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    maxLines = 3
                )

                AnimatedVisibility(visible = showOutputDesc) {
                    Column {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = outputDescText,
                            onValueChange = { outputDescText = it },
                            placeholder = { Text("Optional structured output description (e.g. JSON schema)") },
                            label = { Text("Structured Output Schema (--output-description)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("task_output_desc_input"),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Preset Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        FilterChip(
                            selected = goalText == preset.prompt,
                            onClick = {
                                goalText = preset.prompt
                                outputDescText = preset.outputDescription ?: ""
                                deviceSimulator.resetToHome()
                            },
                            label = { Text(preset.title, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue.copy(alpha = 0.2f),
                                selectedLabelColor = PrimaryBlue
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!executionState.isRunning) {
                        Button(
                            onClick = {
                                if (goalText.isNotBlank()) {
                                    agentGraph.startTask(goalText, outputDescText.ifBlank { null })
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("run_task_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run Autonomous Agent", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (executionState.isPaused) agentGraph.resumeTask() else agentGraph.pauseTask()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pause_resume_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (executionState.isPaused) AccentEmerald else Color(0xFFD97706)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                if (executionState.isPaused) Icons.Default.PlayArrow else Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (executionState.isPaused) "Resume" else "Pause", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                agentGraph.stopTask()
                                if (executionState.structuredOutput != null) {
                                    dataRepository.addExecutionRecord(
                                        ExecutionRecord(
                                            id = "exec_${System.currentTimeMillis()}",
                                            prompt = executionState.goal,
                                            outputDescription = executionState.outputDescription,
                                            status = "STOPPED",
                                            durationMs = executionState.executionTimeMs,
                                            stepCount = executionState.stepCount,
                                            timestamp = System.currentTimeMillis(),
                                            structuredOutput = executionState.structuredOutput,
                                            subgoals = executionState.subgoals
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.testTag("stop_task_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRose),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Multi-Agent Graph Visualizer
        AgentGraphVisualizer(
            activeAgent = executionState.activeAgent,
            onAgentClick = { /* Can show agent details */ }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Execution Status Bar
        if (executionState.isRunning || executionState.isFinished) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (executionState.isRunning) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = executionState.statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (executionState.isFinished) AccentEmerald else MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Step ${executionState.stepCount} / ${executionState.maxSteps}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 4. Main Body: Split or Tabs between Virtual Phone & Agent Intel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left: Device Canvas
            Box(
                modifier = Modifier
                    .weight(0.48f)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                DeviceScreenCanvas(
                    screenState = screenState,
                    tapIndicator = tapIndicator,
                    showVisionBoxes = showVisionBoxes,
                    onElementTap = { element ->
                        deviceSimulator.tapElement(element.id)
                    },
                    onCanvasTap = { x, y ->
                        deviceSimulator.tap(x, y)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Right: Multi-Agent Workspace (Subgoals, Thoughts, JSON Output, Action Trace)
            Card(
                modifier = Modifier
                    .weight(0.52f)
                    .fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        edgePadding = 8.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = PrimaryBlue
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Subgoals", fontSize = 12.sp) },
                            icon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Output JSON", fontSize = 12.sp) },
                            icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Trace Logs", fontSize = 12.sp) },
                            icon = { Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        when (selectedTab) {
                            0 -> SubgoalsAndThoughtsTab(executionState)
                            1 -> StructuredOutputTab(executionState.structuredOutput) {
                                clipboardManager.setText(AnnotatedString(executionState.structuredOutput ?: ""))
                                Toast.makeText(context, "Copied JSON to clipboard", Toast.LENGTH_SHORT).show()
                            }
                            2 -> TraceLogsTab(executionState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubgoalsAndThoughtsTab(state: com.example.mobileuse.engine.AgentExecutionState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Current Cortex Reasoning Thought
        if (state.currentThought.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CortexColor.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = CortexColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cortex Reasoning",
                                style = MaterialTheme.typography.labelSmall,
                                color = CortexColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.currentThought,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Subgoal List
        item {
            Text(
                text = "Planner Milestones (${state.subgoals.count { it.status == SubgoalStatus.COMPLETED }}/${state.subgoals.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(state.subgoals) { subgoal ->
            val isCurrent = subgoal.id == state.currentSubgoalId
            val isCompleted = subgoal.status == SubgoalStatus.COMPLETED

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            isCurrent -> PrimaryBlue.copy(alpha = 0.12f)
                            isCompleted -> AccentEmerald.copy(alpha = 0.08f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isCurrent -> PrimaryBlue
                            isCompleted -> AccentEmerald.copy(alpha = 0.5f)
                            else -> Color.Transparent
                        },
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> AccentEmerald
                                isCurrent -> PrimaryBlue
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    } else if (isCurrent) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Running",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subgoal.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (subgoal.description.isNotEmpty()) {
                        Text(
                            text = subgoal.description,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StructuredOutputTab(jsonText: String?, onCopy: () -> Unit) {
    if (jsonText.isNullOrEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No structured output extracted yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Run a task with --output-description to extract data",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Extracted JSON Schema",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentCyan,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp).testTag("copy_json_output")) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Copy JSON",
                        tint = AccentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF090D16))
                    .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = jsonText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF38BDF8),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun TraceLogsTab(state: com.example.mobileuse.engine.AgentExecutionState) {
    if (state.stepLogs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Trace logs will appear when agent starts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(state.stepLogs.reversed()) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "[${log.agent.displayName}]",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (log.agent) {
                            com.example.mobileuse.model.AgentType.PLANNER -> com.example.mobileuse.ui.theme.PlannerColor
                            com.example.mobileuse.model.AgentType.ORCHESTRATOR -> com.example.mobileuse.ui.theme.OrchestratorColor
                            com.example.mobileuse.model.AgentType.CONTEXTOR -> com.example.mobileuse.ui.theme.ContextorColor
                            com.example.mobileuse.model.AgentType.CORTEX -> CortexColor
                            com.example.mobileuse.model.AgentType.EXECUTOR -> com.example.mobileuse.ui.theme.ExecutorColor
                            com.example.mobileuse.model.AgentType.SUMMARIZER -> AccentEmerald
                            com.example.mobileuse.model.AgentType.CONVERGENCE -> Color(0xFF14B8A6)
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = log.title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = log.detail,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
