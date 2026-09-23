package com.example.mobileuse.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileuse.engine.DeviceSimulator
import com.example.mobileuse.model.DeviceScreenState
import com.example.mobileuse.model.UIElement
import com.example.mobileuse.ui.theme.AccentCyan
import com.example.mobileuse.ui.theme.AccentRose
import com.example.mobileuse.ui.theme.PrimaryBlue

@Composable
fun DeviceScreenCanvas(
    screenState: DeviceScreenState,
    tapIndicator: DeviceSimulator.TapIndicator?,
    showVisionBoxes: Boolean,
    onElementTap: (UIElement) -> Unit = {},
    onCanvasTap: (Float, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    // Phone Bezel Frame
    Box(
        modifier = modifier
            .aspectRatio(9f / 18.5f)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF0F172A))
            .border(4.dp, Color(0xFF334155), RoundedCornerShape(32.dp))
            .padding(6.dp)
            .testTag("device_screen_canvas")
    ) {
        // Inner Screen Canvas
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFF1E293B))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val relX = offset.x / size.width
                        val relY = offset.y / size.height
                        onCanvasTap(relX, relY)
                    }
                }
        ) {
            val canvasWidth = maxWidth
            val canvasHeight = maxHeight

            // 1. Android Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(Color(0xFF0F172A).copy(alpha = 0.8f))
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = screenState.currentTime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                // Camera punch hole
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.height(10.dp)
                    ) {
                        Box(modifier = Modifier.size(2.dp, 4.dp).background(Color.White))
                        Box(modifier = Modifier.size(2.dp, 7.dp).background(Color.White))
                        Box(modifier = Modifier.size(2.dp, 10.dp).background(Color.White))
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${screenState.batteryPercent}%",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp, 8.dp)
                            .border(1.dp, Color.White, RoundedCornerShape(2.dp))
                            .padding(1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(screenState.batteryPercent / 100f)
                                .fillMaxSize()
                                .background(Color(0xFF10B981), RoundedCornerShape(1.dp))
                        )
                    }
                }
            }

            // 2. Render Screen UI Elements based on normalized coordinates
            screenState.elements.forEach { element ->
                val elemLeft = canvasWidth * element.bounds.left
                val elemTop = canvasHeight * element.bounds.top
                val elemWidth = canvasWidth * element.bounds.width
                val elemHeight = canvasHeight * element.bounds.height

                Box(
                    modifier = Modifier
                        .offset(x = elemLeft, y = elemTop)
                        .size(width = elemWidth, height = elemHeight)
                        .clickable { onElementTap(element) }
                ) {
                    RenderElementVisual(
                        element = element,
                        currentApp = screenState.currentApp
                    )

                    // Bounding Box Overlay & Index Tag
                    if (showVisionBoxes) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 1.dp,
                                    color = if (element.isClickable) AccentCyan else Color(0x6694A3B8),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        ) {
                            Badge(
                                containerColor = if (element.isClickable) AccentCyan else Color(0xFF475569),
                                contentColor = Color.Black,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .offset(x = 1.dp, y = 1.dp)
                            ) {
                                Text(
                                    text = "#${element.id}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 3. Animated Tap Indicator
            tapIndicator?.let { tap ->
                val animScale = remember(tap.timestamp) { Animatable(0.2f) }
                val animAlpha = remember(tap.timestamp) { Animatable(1f) }

                LaunchedEffect(tap.timestamp) {
                    animScale.snapTo(0.2f)
                    animAlpha.snapTo(1f)
                    animScale.animateTo(1.8f, tween(450))
                    animAlpha.animateTo(0f, tween(300))
                }

                val tapX = canvasWidth * tap.x - 20.dp
                val tapY = canvasHeight * tap.y - 20.dp

                Box(
                    modifier = Modifier
                        .offset(x = tapX, y = tapY)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentRose.copy(alpha = animAlpha.value * 0.4f))
                        .border(2.dp, AccentRose.copy(alpha = animAlpha.value), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun RenderElementVisual(element: UIElement, currentApp: String) {
    when (element.role) {
        "Button" -> {
            if (currentApp == "Launcher") {
                // App launcher icon style
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val (icon, bgColor) = when {
                        element.text.contains("Settings") -> Icons.Default.Settings to Color(0xFF64748B)
                        element.text.contains("Gmail") -> Icons.Default.Email to Color(0xFFEA4335)
                        element.text.contains("Messages") -> Icons.Default.Email to Color(0xFF2563EB)
                        element.text.contains("Store") -> Icons.Default.ShoppingCart to Color(0xFF10B981)
                        else -> Icons.Default.Settings to Color(0xFF8B5CF6)
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = element.text,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = element.text,
                        fontSize = 9.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                // Standard UI Button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF334155))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = element.text,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        "EditText" -> {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF334155))
                    .border(
                        1.dp,
                        if (element.isFocused) PrimaryBlue else Color(0xFF475569),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = element.text.ifEmpty { "Type here..." },
                    fontSize = 11.sp,
                    color = if (element.text.isEmpty()) Color(0xFF94A3B8) else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        "ListItem" -> {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = element.text,
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        "Switch" -> {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = element.text,
                    fontSize = 11.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Switch(
                    checked = element.isChecked,
                    onCheckedChange = null,
                    modifier = Modifier.size(24.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryBlue
                    )
                )
            }
        }
        "Card" -> {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF334155).copy(alpha = 0.7f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = element.text,
                        fontSize = 10.sp,
                        color = Color.White,
                        lineHeight = 13.sp
                    )
                }
            }
        }
        "FAB" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = element.text,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        else -> {
            // TextView / Generic text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = element.text,
                    fontSize = if (element.text.contains("Settings") || element.text.contains("Messages") || element.text.contains("85%")) 16.sp else 11.sp,
                    fontWeight = if (element.text.contains("Settings") || element.text.contains("85%")) FontWeight.Bold else FontWeight.Normal,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
