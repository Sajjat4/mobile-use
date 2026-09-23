package com.example.mobileuse.engine

import com.example.mobileuse.model.DeviceScreenState
import com.example.mobileuse.model.RectBounds
import com.example.mobileuse.model.UIElement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceSimulator {

    data class TapIndicator(val x: Float, val y: Float, val timestamp: Long)

    private val _tapIndicator = MutableStateFlow<TapIndicator?>(null)
    val tapIndicator: StateFlow<TapIndicator?> = _tapIndicator.asStateFlow()

    private val _screenState = MutableStateFlow(createHomeScreen())
    val screenState: StateFlow<DeviceScreenState> = _screenState.asStateFlow()

    private var navigationBackStack: MutableList<DeviceScreenState> = mutableListOf()

    fun resetToHome() {
        navigationBackStack.clear()
        _screenState.value = createHomeScreen()
    }

    fun launchApp(appName: String) {
        val current = _screenState.value
        navigationBackStack.add(current)
        val normalized = appName.lowercase().trim()
        val newState = when {
            normalized.contains("setting") -> createSettingsHomeScreen()
            normalized.contains("gmail") || normalized.contains("mail") || normalized.contains("email") -> createGmailHomeScreen()
            normalized.contains("message") || normalized.contains("chat") || normalized.contains("whatsapp") -> createMessagesHomeScreen()
            normalized.contains("shop") || normalized.contains("store") || normalized.contains("cart") -> createShoppingHomeScreen()
            normalized.contains("clock") || normalized.contains("alarm") -> createClockHomeScreen()
            else -> createSettingsHomeScreen()
        }
        _screenState.value = newState
    }

    fun tap(x: Float, y: Float): UIElement? {
        _tapIndicator.value = TapIndicator(x, y, System.currentTimeMillis())
        val elements = _screenState.value.elements
        // Find element that contains (x, y)
        val target = elements.find { el ->
            x >= el.bounds.left && x <= el.bounds.right &&
                    y >= el.bounds.top && y <= el.bounds.bottom
        }
        if (target != null) {
            handleElementInteraction(target)
        }
        return target
    }

    fun tapElement(elementId: Int): UIElement? {
        val element = _screenState.value.elements.find { it.id == elementId }
        if (element != null) {
            _tapIndicator.value = TapIndicator(element.bounds.centerX, element.bounds.centerY, System.currentTimeMillis())
            handleElementInteraction(element)
        }
        return element
    }

    fun inputText(text: String, elementId: Int? = null) {
        val current = _screenState.value
        val targetId = elementId ?: current.focusedElementId
        if (targetId != null) {
            val updatedElements = current.elements.map { el ->
                if (el.id == targetId) {
                    el.copy(text = (el.text + text).takeLast(60), isFocused = true)
                } else el
            }
            _screenState.value = current.copy(elements = updatedElements)
        }
    }

    fun clearText(elementId: Int? = null) {
        val current = _screenState.value
        val targetId = elementId ?: current.focusedElementId
        if (targetId != null) {
            val updatedElements = current.elements.map { el ->
                if (el.id == targetId) {
                    el.copy(text = "", isFocused = true)
                } else el
            }
            _screenState.value = current.copy(elements = updatedElements)
        }
    }

    fun pressBack() {
        if (navigationBackStack.isNotEmpty()) {
            _screenState.value = navigationBackStack.removeAt(navigationBackStack.lastIndex)
        } else {
            resetToHome()
        }
    }

    fun pressHome() {
        resetToHome()
    }

    fun swipe(direction: String) {
        // Scroll list items simulated
        val current = _screenState.value
        if (direction.lowercase() == "up" || direction.lowercase() == "down") {
            // Re-order or shift items slightly
            _tapIndicator.value = TapIndicator(0.5f, 0.5f, System.currentTimeMillis())
        }
    }

    private fun handleElementInteraction(element: UIElement) {
        val current = _screenState.value
        navigationBackStack.add(current)

        when {
            // Home screen app launch
            current.currentApp == "Launcher" -> {
                when {
                    element.text.contains("Settings") -> _screenState.value = createSettingsHomeScreen()
                    element.text.contains("Gmail") -> _screenState.value = createGmailHomeScreen()
                    element.text.contains("Messages") -> _screenState.value = createMessagesHomeScreen()
                    element.text.contains("Store") || element.text.contains("Shopping") -> _screenState.value = createShoppingHomeScreen()
                    element.text.contains("Clock") -> _screenState.value = createClockHomeScreen()
                }
            }
            // Settings navigation
            current.currentApp == "Settings" -> {
                when {
                    element.text.contains("Battery") -> _screenState.value = createBatteryScreen()
                    element.text.contains("Wi-Fi") || element.text.contains("Network") -> _screenState.value = createWifiScreen()
                    element.text.contains("Apps") -> _screenState.value = createAppsListScreen()
                    element.role == "Switch" -> {
                        val toggled = current.elements.map {
                            if (it.id == element.id) it.copy(isChecked = !it.isChecked) else it
                        }
                        _screenState.value = current.copy(elements = toggled)
                    }
                }
            }
            // Gmail navigation
            current.currentApp == "Gmail" -> {
                when {
                    element.role == "EditText" -> {
                        _screenState.value = current.copy(focusedElementId = element.id)
                    }
                    element.text.contains("Security Alert") -> _screenState.value = createEmailDetailScreen(
                        sender = "GitHub Security",
                        subject = "Security alert: token generated",
                        body = "A new personal access token was created from an unknown IP address. Please review."
                    )
                    element.text.contains("Stripe") -> _screenState.value = createEmailDetailScreen(
                        sender = "Stripe Billing",
                        subject = "Your invoice #1029 is ready",
                        body = "Your subscription invoice for October has been generated. Total: $49.00 USD."
                    )
                    element.text.contains("Alice Smith") -> _screenState.value = createEmailDetailScreen(
                        sender = "Alice Smith",
                        subject = "Q4 Product Roadmap Update",
                        body = "Attached is the finalized timeline for the mobile release sprint."
                    )
                }
            }
            // Messages navigation
            current.currentApp == "Messages" -> {
                when {
                    element.role == "EditText" -> {
                        _screenState.value = current.copy(focusedElementId = element.id)
                    }
                    element.text.contains("Send") -> {
                        // send message and update conversation
                        val inputElem = current.elements.find { it.role == "EditText" }
                        val sentText = inputElem?.text?.ifEmpty { "Running late 5 mins" } ?: "Running late 5 mins"
                        _screenState.value = createChatDetailScreen(activeContact = "Team Standup", lastSent = sentText)
                    }
                    element.text.contains("Team") -> _screenState.value = createChatDetailScreen("Team Standup")
                    element.text.contains("Alex") -> _screenState.value = createChatDetailScreen("Alex Miller")
                }
            }
            // Shopping navigation
            current.currentApp == "Shopping" -> {
                when {
                    element.role == "EditText" -> {
                        _screenState.value = current.copy(focusedElementId = element.id)
                    }
                    element.text.contains("Search") || element.role == "Button" && element.text.contains("Go") -> {
                        _screenState.value = createShoppingSearchResultsScreen()
                    }
                }
            }
        }
    }

    // Screen builders with realistic Android UI elements and bounding coordinates
    private fun createHomeScreen(): DeviceScreenState {
        var idCounter = 1
        val elements = listOf(
            UIElement(idCounter++, "TextView", text = "10:42 AM", bounds = RectBounds(0.1f, 0.08f, 0.9f, 0.16f), isClickable = false),
            UIElement(idCounter++, "TextView", text = "Wednesday, Sep 23", bounds = RectBounds(0.1f, 0.16f, 0.9f, 0.20f), isClickable = false),
            UIElement(idCounter++, "EditText", text = "Search apps or web...", bounds = RectBounds(0.08f, 0.22f, 0.92f, 0.28f), isEditable = true),
            UIElement(idCounter++, "Button", text = "Settings", contentDescription = "System Settings App", resourceId = "com.android.settings", bounds = RectBounds(0.10f, 0.35f, 0.30f, 0.46f)),
            UIElement(idCounter++, "Button", text = "Gmail", contentDescription = "Email Client", resourceId = "com.google.android.gm", bounds = RectBounds(0.40f, 0.35f, 0.60f, 0.46f)),
            UIElement(idCounter++, "Button", text = "Messages", contentDescription = "SMS and Chat", resourceId = "com.google.android.apps.messaging", bounds = RectBounds(0.70f, 0.35f, 0.90f, 0.46f)),
            UIElement(idCounter++, "Button", text = "Store", contentDescription = "Shopping Mall App", resourceId = "com.shopping.mall", bounds = RectBounds(0.10f, 0.50f, 0.30f, 0.61f)),
            UIElement(idCounter++, "Button", text = "Clock", contentDescription = "Alarm and Timers", resourceId = "com.google.android.deskclock", bounds = RectBounds(0.40f, 0.50f, 0.60f, 0.61f)),
            UIElement(idCounter++, "Button", text = "Photos", contentDescription = "Gallery & Photos", resourceId = "com.google.android.apps.photos", bounds = RectBounds(0.70f, 0.50f, 0.90f, 0.61f)),
            UIElement(idCounter++, "Button", text = "Phone", contentDescription = "Dialer App", resourceId = "com.android.dialer", bounds = RectBounds(0.12f, 0.88f, 0.32f, 0.96f)),
            UIElement(idCounter++, "Button", text = "Browser", contentDescription = "Chrome Browser", resourceId = "com.android.chrome", bounds = RectBounds(0.68f, 0.88f, 0.88f, 0.96f))
        )
        return DeviceScreenState(
            currentApp = "Launcher",
            currentActivity = "com.android.launcher3.Launcher",
            elements = elements
        )
    }

    private fun createSettingsHomeScreen(): DeviceScreenState {
        var id = 1
        val elements = listOf(
            UIElement(id++, "TextView", text = "Settings", bounds = RectBounds(0.08f, 0.04f, 0.92f, 0.09f), isClickable = false),
            UIElement(id++, "EditText", text = "Search settings...", bounds = RectBounds(0.08f, 0.10f, 0.92f, 0.16f), isEditable = true),
            UIElement(id++, "ListItem", text = "Network & internet (Wi-Fi, Mobile, Hotspot)", bounds = RectBounds(0.05f, 0.18f, 0.95f, 0.26f)),
            UIElement(id++, "ListItem", text = "Connected devices (Bluetooth, Pair new)", bounds = RectBounds(0.05f, 0.27f, 0.95f, 0.35f)),
            UIElement(id++, "ListItem", text = "Apps (Recent apps, Default apps, Permissions)", bounds = RectBounds(0.05f, 0.36f, 0.95f, 0.44f)),
            UIElement(id++, "ListItem", text = "Battery - 85% (Should last until 11:30 PM)", bounds = RectBounds(0.05f, 0.45f, 0.95f, 0.53f)),
            UIElement(id++, "ListItem", text = "Display (Dark theme, Brightness, Font size)", bounds = RectBounds(0.05f, 0.54f, 0.95f, 0.62f)),
            UIElement(id++, "ListItem", text = "Sound & vibration (Volume, Do Not Disturb)", bounds = RectBounds(0.05f, 0.63f, 0.95f, 0.71f)),
            UIElement(id++, "ListItem", text = "Security & privacy (Screen lock, App security)", bounds = RectBounds(0.05f, 0.72f, 0.95f, 0.80f)),
            UIElement(id++, "ListItem", text = "About phone (Android 15, Model Pixel 9)", bounds = RectBounds(0.05f, 0.81f, 0.95f, 0.89f))
        )
        return DeviceScreenState(
            currentApp = "Settings",
            currentActivity = "com.android.settings.SettingsActivity",
            elements = elements
        )
    }

    private fun createBatteryScreen(): DeviceScreenState {
        var id = 1
        val elements = listOf(
            UIElement(id++, "Button", text = "← Back", bounds = RectBounds(0.04f, 0.04f, 0.20f, 0.09f)),
            UIElement(id++, "TextView", text = "Battery", bounds = RectBounds(0.22f, 0.04f, 0.80f, 0.09f), isClickable = false),
            UIElement(id++, "TextView", text = "85%", bounds = RectBounds(0.10f, 0.12f, 0.90f, 0.24f), isClickable = false),
            UIElement(id++, "TextView", text = "Should last until about 11:30 PM", bounds = RectBounds(0.10f, 0.25f, 0.90f, 0.29f), isClickable = false),
            UIElement(id++, "Switch", text = "Battery Saver", bounds = RectBounds(0.08f, 0.32f, 0.92f, 0.40f), isChecked = false),
            UIElement(id++, "Switch", text = "Adaptive Battery", bounds = RectBounds(0.08f, 0.42f, 0.92f, 0.50f), isChecked = true),
            UIElement(id++, "ListItem", text = "Battery usage since last charge (Screen: 1h 45m)", bounds = RectBounds(0.08f, 0.52f, 0.92f, 0.60f)),
            UIElement(id++, "ListItem", text = "Battery Health: 98% Normal", bounds = RectBounds(0.08f, 0.62f, 0.92f, 0.70f))
        )
        return DeviceScreenState(
            currentApp = "Settings",
            currentActivity = "com.android.settings.fuelgauge.PowerUsageSummary",
            elements = elements,
            batteryPercent = 85
        )
    }

    private fun createWifiScreen(): DeviceScreenState {
        var id = 1
        val elements = listOf(
            UIElement(id++, "Button", text = "← Back", bounds = RectBounds(0.04f, 0.04f, 0.20f, 0.09f)),
            UIElement(id++, "TextView", text = "Internet", bounds = RectBounds(0.22f, 0.04f, 0.80f, 0.09f), isClickable = false),
            UIElement(id++, "Switch", text = "Wi-Fi", bounds = RectBounds(0.08f, 0.12f, 0.92f, 0.18f), isChecked = true),
            UIElement(id++, "ListItem", text = "Home_Fiber_5G (Connected, Excellent signal)", bounds = RectBounds(0.08f, 0.22f, 0.92f, 0.30f)),
            UIElement(id++, "ListItem", text = "Office_Guest (Saved)", bounds = RectBounds(0.08f, 0.32f, 0.92f, 0.40f)),
            UIElement(id++, "ListItem", text = "CoffeeShop_Free (Open)", bounds = RectBounds(0.08f, 0.42f, 0.92f, 0.50f)),
            UIElement(id++, "Button", text = "+ Add network", bounds = RectBounds(0.08f, 0.54f, 0.50f, 0.62f))
        )
        return DeviceScreenState(
            currentApp = "Settings",
            currentActivity = "com.android.settings.wifi.WifiSettings",
            elements = elements
        )
    }

    private fun createAppsListScreen(): DeviceScreenState {
        var id = 1
        val elements = listOf(
            UIElement(id++, "Button", text = "← Back", bounds = RectBounds(0.04f, 0.04f, 0.20f, 0.09f)),
            UIElement(id++, "TextView", text = "Apps", bounds = RectBounds(0.22f, 0.04f, 0.80f, 0.09f), isClickable = false),
            UIElement(id++, "ListItem", text = "Gmail (Used 12 min ago - 145 MB)", bounds = RectBounds(0.08f, 0.14f, 0.92f, 0.22f)),
            UIElement(id++, "ListItem", text = "Messages (Used 45 min ago - 68 MB)", bounds = RectBounds(0.08f, 0.24f, 0.92f, 0.32f)),
            UIElement(id++, "ListItem", text = "Chrome (Used 1 hr ago - 320 MB)", bounds = RectBounds(0.08f, 0.34f, 0.92f, 0.42f)),
            UIElement(id++, "ListItem", text = "Photos (Used 3 hr ago - 210 MB)", bounds = RectBounds(0.08f, 0.44f, 0.92f, 0.52f))
        )
        return DeviceScreenState(
            currentApp = "Settings",
            currentActivity = "com.android.settings.applications.ManageApplications",
            elements = elements
        )
    }

    private fun createGmailHomeScreen(): DeviceScreenState {
        var id = 1
        val elements = listOf(
            UIElement(id++, "EditText", text = "Search in mail", bounds = RectBounds(0.06f, 0.04f, 0.82f, 0.10f), isEditable = true),
            UIElement(id++, "ImageView", text = "Profile Avatar (user@example.com)", bounds = RectBounds(0.85f, 0.04f, 0.95f, 0.10f)),
            UIElement(id++, "TextView", text = "Primary Inbox (3 unread)", bounds = RectBounds(0.06f, 0.12f, 0.94f, 0.16f), isClickable = false),
            UIElement(
                id = id++,
                role = "ListItem",
                text = "GitHub Security • Security alert: token generated\nA new personal access token was created from an unknown IP address...",
                contentDescription = "Email from GitHub Security",
                bounds = RectBounds(0.05f, 0.18f, 0.95f, 0.30f)
            ),
            UIElement(
                id = id++,
                role = "ListItem",
                text = "Stripe Billing • Your invoice #1029 is ready\nYour subscription invoice for October has been generated. Total: $49.00 USD...",
                contentDescription = "Email from Stripe Billing",
                bounds = RectBounds(0.05f, 0.32f, 0.95f, 0.44f)
            ),
            UIElement(
                id = id++,
                role = "ListItem",
                text = "Alice Smith • Q4 Product Roadmap Update\nAttached is the finalized timeline for the mobile release sprint...",
                contentDescription = "Email from Alice Smith",
                bounds = RectBounds(0.05f, 0.46f, 0.95f, 0.58f)
            ),
            UIElement(
                id = id++,
                role = "ListItem",
                text = "Coursera • Weekly Learning Digest\nPick up where you left off in Kotlin Android Architecture...",
                contentDescription = "Email from Coursera",
                bounds = RectBounds(0.05f, 0.60f, 0.95f, 0.72f)
            ),
            UIElement(
                id = id++,
                role = "FAB",
                text = "Compose",
                contentDescription = "Compose new email",
                bounds = RectBounds(0.65f, 0.85f, 0.92f, 0.94f)
            )
        )
        return DeviceScreenState(
            currentApp = "Gmail",
            currentActivity = "com.google.android.gm.ConversationListActivity",
            elements = elements
        )
    }

    private fun createEmailDetailScreen(sender: String, subject: String, body: String): DeviceScreenState {
        var id = 1
        val elements = listOf(
            UIElement(id++, "Button", text = "← Back to Inbox", bounds = RectBounds(0.04f, 0.04f, 0.35f, 0.09f)),
            UIElement(id++, "TextView", text = subject, bounds = RectBounds(0.06f, 0.11f, 0.94f, 0.18f), isClickable = false),
            UIElement(id++, "TextView", text = "From: $sender", bounds = RectBounds(0.06f, 0.19f, 0.94f, 0.24f), isClickable = false),
            UIElement(id++, "TextView", text = "To: me <user@example.com>", bounds = RectBounds(0.06f, 0.25f, 0.94f, 0.28f), isClickable = false),
            UIElement(id++, "TextView", text = body, bounds = RectBounds(0.06f, 0.32f, 0.94f, 0.58f), isClickable = false),
            UIElement(id++, "Button", text = "Reply", bounds = RectBounds(0.08f, 0.62f, 0.35f, 0.69f)),
            UIElement(id++, "Button", text = "Forward", bounds = RectBounds(0.40f, 0.62f, 0.67f, 0.69f))
        )
        return DeviceScreenState(
            currentApp = "Gmail",
            currentActivity = "com.google.android.gm.ComposeActivityGmail",
            elements = elements
        )
    }

    private fun createMessagesHomeScreen(): DeviceScreenState {
        var id = 1
        val elements = listOf(
            UIElement(id++, "TextView", text = "Messages", bounds = RectBounds(0.06f, 0.04f, 0.70f, 0.10f), isClickable = false),
            UIElement(id++, "EditText", text = "Search conversations", bounds = RectBounds(0.06f, 0.12f, 0.94f, 0.18f), isEditable = true),
            UIElement(
                id = id++,
                role = "ListItem",
                text = "Team Standup • Dave: Are we meeting at 11 AM?",
                contentDescription = "Chat with Team Standup",
                bounds = RectBounds(0.05f, 0.20f, 0.95f, 0.30f)
            ),
            UIElement(
                id = id++,
                role = "ListItem",
                text = "Alex Miller • See you at lunch tomorrow!",
                contentDescription = "Chat with Alex Miller",
                bounds = RectBounds(0.05f, 0.32f, 0.95f, 0.42f)
            ),
            UIElement(
                id = id++,
                role = "ListItem",
                text = "Bank Alert • Security code: 829471. Valid for 5 min.",
                contentDescription = "Verification Code",
                bounds = RectBounds(0.05f, 0.44f, 0.95f, 0.54f)
            ),
            UIElement(
                id = id++,
                role = "FAB",
                text = "Start chat",
                bounds = RectBounds(0.62f, 0.85f, 0.92f, 0.94f)
            )
        )
        return DeviceScreenState(
            currentApp = "Messages",
            currentActivity = "com.google.android.apps.messaging.ui.ConversationListActivity",
            elements = elements
        )
    }

    private fun createChatDetailScreen(activeContact: String, lastSent: String? = null): DeviceScreenState {
        var id = 1
        val elements = mutableListOf(
            UIElement(id++, "Button", text = "← Back", bounds = RectBounds(0.04f, 0.04f, 0.20f, 0.09f)),
            UIElement(id++, "TextView", text = activeContact, bounds = RectBounds(0.22f, 0.04f, 0.85f, 0.09f), isClickable = false),
            UIElement(id++, "TextView", text = "Dave: Let us finalize the mobile automated tests.", bounds = RectBounds(0.06f, 0.14f, 0.80f, 0.22f), isClickable = false),
            UIElement(id++, "TextView", text = "Sarah: All PRs are merged to main.", bounds = RectBounds(0.06f, 0.24f, 0.80f, 0.32f), isClickable = false)
        )
        if (lastSent != null) {
            elements.add(
                UIElement(id++, "TextView", text = "Me: $lastSent", bounds = RectBounds(0.30f, 0.36f, 0.94f, 0.44f), isClickable = false)
            )
        }
        elements.add(
            UIElement(id++, "EditText", text = "", bounds = RectBounds(0.06f, 0.88f, 0.78f, 0.96f), isEditable = true)
        )
        elements.add(
            UIElement(id++, "Button", text = "Send", bounds = RectBounds(0.80f, 0.88f, 0.96f, 0.96f))
        )
        return DeviceScreenState(
            currentApp = "Messages",
            currentActivity = "com.google.android.apps.messaging.ui.conversation.ConversationActivity",
            elements = elements
        )
    }

    private fun createShoppingHomeScreen(): DeviceScreenState {
        var id = 1
        val elements = listOf(
            UIElement(id++, "TextView", text = "ShopMart", bounds = RectBounds(0.06f, 0.04f, 0.50f, 0.09f), isClickable = false),
            UIElement(id++, "Button", text = "Cart (0)", bounds = RectBounds(0.70f, 0.04f, 0.94f, 0.09f)),
            UIElement(id++, "EditText", text = "wireless earbuds", bounds = RectBounds(0.06f, 0.11f, 0.75f, 0.18f), isEditable = true),
            UIElement(id++, "Button", text = "Go", bounds = RectBounds(0.78f, 0.11f, 0.94f, 0.18f)),
            UIElement(id++, "TextView", text = "Top Deals of the Day", bounds = RectBounds(0.06f, 0.21f, 0.94f, 0.26f), isClickable = false),
            UIElement(id++, "Card", text = "Wireless Earbuds Pro • Active Noise Cancelling • $79.99 (40% OFF)", bounds = RectBounds(0.06f, 0.28f, 0.94f, 0.44f)),
            UIElement(id++, "Card", text = "Ultra Smartwatch Series 9 • Heart Rate & GPS • $199.00", bounds = RectBounds(0.06f, 0.46f, 0.94f, 0.62f)),
            UIElement(id++, "Card", text = "Fast Charger 65W GaN • Dual USB-C • $29.99", bounds = RectBounds(0.06f, 0.64f, 0.94f, 0.80f))
        )
        return DeviceScreenState(
            currentApp = "Shopping",
            currentActivity = "com.shopping.mall.MainActivity",
            elements = elements
        )
    }

    private fun createShoppingSearchResultsScreen(): DeviceScreenState {
        var id = 1
        val elements = listOf(
            UIElement(id++, "Button", text = "← Back", bounds = RectBounds(0.04f, 0.04f, 0.20f, 0.09f)),
            UIElement(id++, "EditText", text = "wireless earbuds", bounds = RectBounds(0.22f, 0.04f, 0.94f, 0.09f), isEditable = true),
            UIElement(id++, "TextView", text = "Results for 'wireless earbuds' (3 found)", bounds = RectBounds(0.06f, 0.11f, 0.94f, 0.15f), isClickable = false),
            UIElement(
                id = id++,
                role = "Card",
                text = "Sony WH-1000XM5 Wireless Headphones\nIndustry Leading Noise Cancellation • $299.99\nRating: 4.8 ★ (12k reviews)",
                bounds = RectBounds(0.05f, 0.17f, 0.95f, 0.35f)
            ),
            UIElement(
                id = id++,
                role = "Card",
                text = "Apple AirPods Pro (2nd Gen) USB-C\nSpatial Audio with Dynamic Head Tracking • $249.00\nRating: 4.9 ★ (28k reviews)",
                bounds = RectBounds(0.05f, 0.38f, 0.95f, 0.56f)
            ),
            UIElement(
                id = id++,
                role = "Card",
                text = "Bose QuietComfort Ultra Earbuds\nCustomTune Audio Technology • $299.00\nRating: 4.7 ★ (8k reviews)",
                bounds = RectBounds(0.05f, 0.59f, 0.95f, 0.77f)
            )
        )
        return DeviceScreenState(
            currentApp = "Shopping",
            currentActivity = "com.shopping.mall.SearchResultsActivity",
            elements = elements
        )
    }

    private fun createClockHomeScreen(): DeviceScreenState {
        var id = 1
        val elements = listOf(
            UIElement(id++, "TextView", text = "Clock", bounds = RectBounds(0.06f, 0.04f, 0.94f, 0.10f), isClickable = false),
            UIElement(id++, "TextView", text = "Alarm", bounds = RectBounds(0.06f, 0.12f, 0.30f, 0.17f), isClickable = true),
            UIElement(id++, "TextView", text = "Timer", bounds = RectBounds(0.35f, 0.12f, 0.60f, 0.17f), isClickable = true),
            UIElement(id++, "TextView", text = "Stopwatch", bounds = RectBounds(0.65f, 0.12f, 0.94f, 0.17f), isClickable = true),
            UIElement(id++, "Switch", text = "7:00 AM • Weekdays (Work)", bounds = RectBounds(0.06f, 0.22f, 0.94f, 0.34f), isChecked = true),
            UIElement(id++, "Switch", text = "8:30 AM • Weekends (Rest)", bounds = RectBounds(0.06f, 0.36f, 0.94f, 0.48f), isChecked = false),
            UIElement(id++, "FAB", text = "+", contentDescription = "Add new alarm", bounds = RectBounds(0.40f, 0.85f, 0.60f, 0.94f))
        )
        return DeviceScreenState(
            currentApp = "Clock",
            currentActivity = "com.google.android.deskclock.DeskClock",
            elements = elements
        )
    }
}
