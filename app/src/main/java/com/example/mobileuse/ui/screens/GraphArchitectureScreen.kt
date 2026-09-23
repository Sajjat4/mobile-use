package com.example.mobileuse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileuse.model.AgentType
import com.example.mobileuse.ui.theme.AccentCyan
import com.example.mobileuse.ui.theme.AccentEmerald
import com.example.mobileuse.ui.theme.CortexColor
import com.example.mobileuse.ui.theme.ExecutorColor
import com.example.mobileuse.ui.theme.OrchestratorColor
import com.example.mobileuse.ui.theme.PlannerColor
import com.example.mobileuse.ui.theme.PrimaryBlue

@Composable
fun GraphArchitectureScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
            .testTag("graph_architecture_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Banner: Benchmark & Research Achievement
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentEmerald.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AccentEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AndroidWorld 100% Benchmark",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "First autonomous agent to complete 100% accuracy",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "\"Do Multi-Agents Dream of Electric Screens? Achieving Perfect Accuracy on AndroidWorld Through Task Decomposition\" (Favreau et al., 2026, arXiv:2602.07787).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Multi-Agent Architecture Explanation
        item {
            Text(
                text = "LangGraph Agent Nodes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }

        items(architectureNodes) { node ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(node.color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = node.icon,
                                contentDescription = node.title,
                                tint = node.color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = node.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = node.color
                            )
                            Text(
                                text = node.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = node.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF090D16))
                            .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = node.codeSnippet,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Tools Registry
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Mobile Device Tools Registry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AccentCyan
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    toolsRegistry.forEachIndexed { index, tool ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AccentCyan)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tool.name,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = tool.params,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (index < toolsRegistry.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

private data class ArchNode(
    val type: AgentType,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val description: String,
    val codeSnippet: String
)

private data class ToolInfo(val name: String, val params: String)

private val architectureNodes = listOf(
    ArchNode(
        type = AgentType.PLANNER,
        title = "Planner Agent",
        subtitle = "Task Decomposition Core",
        icon = Icons.Default.List,
        color = PlannerColor,
        description = "Analyzes high-level natural language instructions and partitions them into a sequence of tractable, verifiable subgoals.",
        codeSnippet = "class PlannerNode(ctx):\n  async def __call__(state: State) -> dict:\n    plan = await llm.decompose_goal(state.goal)\n    return {\"subgoal_plan\": plan}"
    ),
    ArchNode(
        type = AgentType.ORCHESTRATOR,
        title = "Orchestrator Agent",
        subtitle = "Execution Coordinator",
        icon = Icons.Default.PlayArrow,
        color = OrchestratorColor,
        description = "Tracks active subgoal progression, determines whether to proceed to the next milestone, and triggers dynamic replanning upon execution blockers.",
        codeSnippet = "class OrchestratorNode(ctx):\n  def evaluate(state):\n    active_subgoal = get_current_subgoal(state.subgoal_plan)\n    return {\"active_subgoal_id\": active_subgoal.id}"
    ),
    ArchNode(
        type = AgentType.CONTEXTOR,
        title = "Contextor Agent",
        subtitle = "Device Perception & Accessibility Hierarchy",
        icon = Icons.Default.Search,
        color = AccentCyan,
        description = "Inspects the device screen state via accessibility trees and visual layout coordinates, labeling every clickable element with numbered tags [#1, #2, ...].",
        codeSnippet = "class ContextorNode(ctx):\n  def capture(state):\n    tree = accessibility.dump_ui_hierarchy()\n    return {\"screen_elements\": tree.elements}"
    ),
    ArchNode(
        type = AgentType.CORTEX,
        title = "Cortex Agent",
        subtitle = "Cognitive Decision & Tool Selection",
        icon = Icons.Default.Info,
        color = CortexColor,
        description = "The cognitive brain that reasons over the current screen elements and subgoal, emitting structured tool calls or extracting requested data.",
        codeSnippet = "class CortexNode(ctx):\n  def reason(state):\n    decision = llm.decide(elements=state.elements, goal=state.subgoal)\n    return {\"tool_calls\": [decision.tool_call]}"
    ),
    ArchNode(
        type = AgentType.EXECUTOR,
        title = "Executor Node",
        subtitle = "Tool Dispatcher",
        icon = Icons.Default.Send,
        color = ExecutorColor,
        description = "Dispatches low-level actions to the device controller via ADB / UIAutomator / native accessibility services.",
        codeSnippet = "class ExecutorNode(ctx):\n  def execute(tool_call):\n    return controller.dispatch(tool_call.name, tool_call.args)"
    ),
    ArchNode(
        type = AgentType.SUMMARIZER,
        title = "Summarizer Agent",
        subtitle = "Observation & Scratchpad Memory",
        icon = Icons.Default.Edit,
        color = AccentEmerald,
        description = "Monitors tool output, evaluates whether the active subgoal succeeded, updates scratchpad memory, and prepares state for convergence check.",
        codeSnippet = "class SummarizerNode(ctx):\n  def summarize(state):\n    return {\"memory_buffer\": state.history, \"subgoal_completed\": True}"
    ),
    ArchNode(
        type = AgentType.CONVERGENCE,
        title = "Convergence Gate",
        subtitle = "Multi-Agent Routing Gate",
        icon = Icons.Default.CheckCircle,
        color = Color(0xFF14B8A6),
        description = "Evaluates termination criteria: routes back to Contextor for next action, Planner for replanning, or END if goal is accomplished.",
        codeSnippet = "def convergence_gate(state):\n  if one_is_failure: return 'replan'\n  if all_completed: return 'end'\n  return 'continue'"
    )
)

private val toolsRegistry = listOf(
    ToolInfo("tap(x, y)", "coords: float [0..1]"),
    ToolInfo("tap_element(element_id)", "id: int"),
    ToolInfo("swipe(direction, distance)", "dir: up|down|left|right"),
    ToolInfo("focus_and_input_text(text)", "text: str, element_id: int"),
    ToolInfo("focus_and_clear_text()", "element_id: int"),
    ToolInfo("erase_one_char()", "element_id: int"),
    ToolInfo("press_key(key)", "key: back|home|enter|recents"),
    ToolInfo("launch_app(app_name)", "app: str, package: str"),
    ToolInfo("stop_app(package_name)", "package: str"),
    ToolInfo("open_link(url)", "url: str"),
    ToolInfo("wait_for_delay(seconds)", "seconds: float")
)
