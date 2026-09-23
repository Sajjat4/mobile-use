package com.example.mobileuse.engine

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.mobileuse.model.ExecutionRecord
import com.example.mobileuse.model.LLMProvider
import com.example.mobileuse.model.TaskPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DataRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("mobile_use_prefs", Context.MODE_PRIVATE)

    private val _presets = MutableStateFlow(getInitialPresets())
    val presets: StateFlow<List<TaskPreset>> = _presets.asStateFlow()

    private val _history = MutableStateFlow<List<ExecutionRecord>>(emptyList())
    val history: StateFlow<List<ExecutionRecord>> = _history.asStateFlow()

    private val _selectedProvider = MutableStateFlow(LLMProvider.GEMINI)
    val selectedProvider: StateFlow<LLMProvider> = _selectedProvider.asStateFlow()

    private val _selectedModel = MutableStateFlow(
        prefs.getString("model_name", "gemini-3.5-flash") ?: "gemini-3.5-flash"
    )
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _apiKey = MutableStateFlow(
        prefs.getString("custom_api_key", "") ?: ""
    )
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    init {
        // Pre-populate sample historical runs for rich inspection
        _history.value = listOf(
            ExecutionRecord(
                id = "exec_101",
                prompt = "Go to settings and tell me my current battery level",
                outputDescription = "A JSON summary of battery percentage and remaining time",
                status = "SUCCESS",
                durationMs = 4320L,
                stepCount = 4,
                timestamp = System.currentTimeMillis() - 3600000L,
                structuredOutput = "{\n  \"device\": \"Pixel 9\",\n  \"battery_percentage\": \"85%\",\n  \"estimated_runtime\": \"Should last until 11:30 PM\",\n  \"status\": \"Discharging\"\n}",
                actionSummary = listOf(
                    "Launched Settings application",
                    "Navigated to Battery category",
                    "Captured battery gauge 85%",
                    "Extracted runtime diagnostics"
                )
            ),
            ExecutionRecord(
                id = "exec_102",
                prompt = "Open Gmail, find first 3 unread emails, and list their sender and subject line",
                outputDescription = "A JSON list of objects, each with 'sender' and 'subject' keys",
                status = "SUCCESS",
                durationMs = 5890L,
                stepCount = 5,
                timestamp = System.currentTimeMillis() - 7200000L,
                structuredOutput = "[\n  {\n    \"sender\": \"GitHub Security\",\n    \"subject\": \"Security alert: token generated\"\n  },\n  {\n    \"sender\": \"Stripe Billing\",\n    \"subject\": \"Your invoice #1029 is ready\"\n  },\n  {\n    \"sender\": \"Alice Smith\",\n    \"subject\": \"Q4 Product Roadmap Update\"\n  }\n]",
                actionSummary = listOf(
                    "Launched Gmail client",
                    "Scanned primary inbox feed",
                    "Identified 3 unread message nodes",
                    "Formatted structured JSON result"
                )
            )
        )
    }

    fun getEffectiveApiKey(): String {
        val customKey = _apiKey.value.trim()
        if (customKey.isNotEmpty()) return customKey

        // Fall back to BuildConfig.GOOGLE_API_KEY from .env
        return try {
            val buildConfigKey = BuildConfig.GOOGLE_API_KEY as? String ?: ""
            if (buildConfigKey.isNotBlank() && buildConfigKey != "YOUR_GEMINI_API_KEY" && buildConfigKey != "DEFAULT_KEY") {
                buildConfigKey
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun updateApiKey(newKey: String) {
        _apiKey.value = newKey
        prefs.edit().putString("custom_api_key", newKey).apply()
    }

    fun updateModel(model: String) {
        _selectedModel.value = model
        prefs.edit().putString("model_name", model).apply()
    }

    fun updateProvider(provider: LLMProvider) {
        _selectedProvider.value = provider
        _selectedModel.value = provider.defaultModel
    }

    fun addExecutionRecord(record: ExecutionRecord) {
        _history.update { listOf(record) + it }
    }

    fun clearHistory() {
        _history.value = emptyList()
    }

    private fun getInitialPresets(): List<TaskPreset> {
        return listOf(
            TaskPreset(
                id = "preset_battery",
                category = "AndroidWorld Benchmark",
                title = "Check Battery & Power Stats",
                prompt = "Go to settings and tell me my current battery level",
                outputDescription = "JSON object with battery_percentage and estimated_runtime",
                targetApp = "Settings",
                estimatedSteps = 4
            ),
            TaskPreset(
                id = "preset_email_scrape",
                category = "Data Scraping",
                title = "Extract Unread Gmail Messages",
                prompt = "Open Gmail, find first 3 unread emails, and list their sender and subject line",
                outputDescription = "A JSON list of objects, each with 'sender' and 'subject' keys",
                targetApp = "Gmail",
                estimatedSteps = 4
            ),
            TaskPreset(
                id = "preset_messaging",
                category = "App Lock & Messaging",
                title = "Send Team Standup Update",
                prompt = "Open Messages, find Team Standup chat, and send 'Running late 5 mins'",
                outputDescription = "JSON confirmation with status and timestamp",
                targetApp = "Messages",
                estimatedSteps = 4
            ),
            TaskPreset(
                id = "preset_shopping",
                category = "E-Commerce",
                title = "Compare Wireless Earbuds Prices",
                prompt = "Open Store, search 'wireless earbuds', and extract top 3 items with prices and ratings",
                outputDescription = "JSON array with item name, price, and rating",
                targetApp = "Shopping",
                estimatedSteps = 4
            ),
            TaskPreset(
                id = "preset_wifi",
                category = "System Controls",
                title = "Inspect Wi-Fi Network",
                prompt = "Go to Settings, open Network & internet, and verify connected Wi-Fi SSID",
                outputDescription = "JSON object with connected SSID and signal status",
                targetApp = "Settings",
                estimatedSteps = 3
            )
        )
    }
}
