package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileuse.engine.AgentStateGraph
import com.example.mobileuse.engine.DataRepository
import com.example.mobileuse.engine.DeviceSimulator
import com.example.mobileuse.engine.GeminiAgentClient
import com.example.mobileuse.ui.screens.DeviceInspectorScreen
import com.example.mobileuse.ui.screens.GraphArchitectureScreen
import com.example.mobileuse.ui.screens.HistoryScreen
import com.example.mobileuse.ui.screens.SettingsScreen
import com.example.mobileuse.ui.screens.TaskExecutionScreen
import com.example.mobileuse.ui.theme.AccentCyan
import com.example.mobileuse.ui.theme.MobileUseTheme
import com.example.mobileuse.ui.theme.PrimaryBlue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileUseTheme {
                MainAppContainer()
            }
        }
    }
}

enum class NavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    STUDIO("studio", "Agent Studio", Icons.Default.PlayArrow, "nav_studio"),
    INSPECTOR("inspector", "UI Perception", Icons.Default.Search, "nav_inspector"),
    ARCHITECTURE("architecture", "Architecture", Icons.Default.Share, "nav_architecture"),
    HISTORY("history", "History", Icons.Default.DateRange, "nav_history"),
    SETTINGS("settings", "Settings", Icons.Default.Settings, "nav_settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val dataRepository = remember { DataRepository(context) }
    val deviceSimulator = remember { DeviceSimulator() }
    val geminiClient = remember {
        GeminiAgentClient(
            apiKeyProvider = { dataRepository.getEffectiveApiKey() },
            modelProvider = { dataRepository.selectedModel.value }
        )
    }
    val agentGraph = remember {
        AgentStateGraph(
            deviceSimulator = deviceSimulator,
            geminiClient = geminiClient,
            scope = coroutineScope
        )
    }

    var currentDestination by remember { mutableStateOf(NavDestination.STUDIO) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mobile-Use",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    Text(
                        text = "v2.0 • Autonomous GUI Agent",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentCyan,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryBlue,
                tonalElevation = 6.dp
            ) {
                NavDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentDestination) {
                NavDestination.STUDIO -> TaskExecutionScreen(
                    agentGraph = agentGraph,
                    deviceSimulator = deviceSimulator,
                    dataRepository = dataRepository
                )
                NavDestination.INSPECTOR -> DeviceInspectorScreen(
                    deviceSimulator = deviceSimulator
                )
                NavDestination.ARCHITECTURE -> GraphArchitectureScreen()
                NavDestination.HISTORY -> HistoryScreen(
                    dataRepository = dataRepository
                )
                NavDestination.SETTINGS -> SettingsScreen(
                    dataRepository = dataRepository,
                    agentGraph = agentGraph
                )
            }
        }
    }
}
