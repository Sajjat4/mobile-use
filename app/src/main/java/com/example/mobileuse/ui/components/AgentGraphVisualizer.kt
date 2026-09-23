package com.example.mobileuse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileuse.model.AgentType
import com.example.mobileuse.ui.theme.ContextorColor
import com.example.mobileuse.ui.theme.CortexColor
import com.example.mobileuse.ui.theme.ExecutorColor
import com.example.mobileuse.ui.theme.OrchestratorColor
import com.example.mobileuse.ui.theme.PlannerColor
import com.example.mobileuse.ui.theme.SummarizerColor

@Composable
fun AgentGraphVisualizer(
    activeAgent: AgentType?,
    onAgentClick: (AgentType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val agentNodes = listOf(
        AgentNodeInfo(AgentType.PLANNER, "Planner", Icons.Default.List, PlannerColor),
        AgentNodeInfo(AgentType.ORCHESTRATOR, "Orchestrator", Icons.Default.PlayArrow, OrchestratorColor),
        AgentNodeInfo(AgentType.CONTEXTOR, "Contextor", Icons.Default.Search, ContextorColor),
        AgentNodeInfo(AgentType.CORTEX, "Cortex", Icons.Default.Star, CortexColor),
        AgentNodeInfo(AgentType.EXECUTOR, "Executor", Icons.Default.Send, ExecutorColor),
        AgentNodeInfo(AgentType.SUMMARIZER, "Summarizer", Icons.Default.Edit, SummarizerColor),
        AgentNodeInfo(AgentType.CONVERGENCE, "Gate", Icons.Default.CheckCircle, Color(0xFF14B8A6))
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .testTag("agent_graph_visualizer")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LangGraph State Architecture",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (activeAgent != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CortexColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Active: ${activeAgent.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CortexColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            agentNodes.forEachIndexed { index, node ->
                val isActive = activeAgent == node.type

                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isActive) 1.08f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                val borderColor by animateColorAsState(
                    targetValue = if (isActive) node.color else Color.Transparent,
                    label = "border"
                )

                Box(
                    modifier = Modifier
                        .scale(if (isActive) pulseScale else 1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isActive) node.color.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        )
                        .border(
                            width = if (isActive) 2.dp else 1.dp,
                            color = if (isActive) borderColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onAgentClick(node.type) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(node.color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = node.icon,
                                contentDescription = node.name,
                                tint = node.color,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = node.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) node.color else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (index < agentNodes.size - 1) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "next",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

private data class AgentNodeInfo(
    val type: AgentType,
    val name: String,
    val icon: ImageVector,
    val color: Color
)
