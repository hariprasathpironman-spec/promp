package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PromptViewModel
import com.example.ui.theme.GlassyButton
import com.example.ui.theme.GlassyCard
import com.example.ui.theme.liquidPressEffect

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: PromptViewModel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val accentColor = Color(0xFF007AFF)

    // Feedback States
    var feedbackText by remember { mutableStateOf("") }
    var showFeedbackDialog by remember { mutableStateOf(false) }

    // Dialog state controllers
    var deleteHistoryConfirm by remember { mutableStateOf(false) }
    var deleteFavsConfirm by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp) // Safety margin for floating bottom navigation
    ) {
        // Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Settings",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF1E1B4B)
                )
                Text(
                    text = "Personalize app attributes and local indexes",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xA9FFFFFF) else Color(0xFF6B7280)
                )
            }
            com.example.ui.theme.ThemeToggleButton(viewModel = viewModel, isDark = isDark)
        }

        // Section 1: Display & Theme Configuration
        Text(
            text = "THEME & STYLING",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        GlassyCard(
            isDark = isDark,
            graphicsQualityHigh = viewModel.graphicsQualityHigh,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // System Adaptation Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SettingsSuggest,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Match System Theme",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E1B4B)
                            )
                            Text(
                                text = "Adapt interface mode automatically",
                                fontSize = 11.sp,
                                color = if (isDark) Color(0x73FFFFFF) else Color(0xFF6B7280)
                            )
                        }
                    }

                    Switch(
                        checked = viewModel.useSystemTheme,
                        onCheckedChange = { viewModel.useSystemTheme = it },
                        modifier = Modifier.testTag("match_system_theme_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF5AC8FA),
                            checkedTrackColor = accentColor.copy(alpha = 0.4f),
                            uncheckedThumbColor = if (isDark) Color(0x66FFFFFF) else Color(0x66000000),
                            uncheckedTrackColor = if (isDark) Color(0x22FFFFFF) else Color(0x11000000)
                        )
                    )
                }

                if (!viewModel.useSystemTheme) {
                    Divider(color = if (isDark) Color(0x11FFFFFF) else Color(0x1B000000))

                    // Dark Mode Manuel Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (viewModel.isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Force Dark Mode",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF1E1B4B)
                                )
                                Text(
                                    text = "Deep black premium visual styles",
                                    fontSize = 11.sp,
                                    color = if (isDark) Color(0x73FFFFFF) else Color(0xFF6B7280)
                                )
                            }
                        }

                        Switch(
                            checked = viewModel.isDarkTheme,
                            onCheckedChange = { viewModel.isDarkTheme = it },
                            modifier = Modifier.testTag("force_dark_theme_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF5AC8FA),
                                checkedTrackColor = accentColor.copy(alpha = 0.4f),
                                uncheckedThumbColor = if (isDark) Color(0x66FFFFFF) else Color(0x66000000),
                                uncheckedTrackColor = if (isDark) Color(0x22FFFFFF) else Color(0x11000000)
                            )
                        )
                    }
                }

                Divider(color = if (isDark) Color(0x11FFFFFF) else Color(0x1B000000))

                // High-End Glass Effects Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "High-End Glass Effects",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E1B4B)
                            )
                            Text(
                                text = if (viewModel.graphicsQualityHigh) "Multi-layer rendering & moving light orbs" else "Simplified rendering for fast performance",
                                fontSize = 11.sp,
                                color = if (isDark) Color(0x73FFFFFF) else Color(0xFF6B7280)
                            )
                        }
                    }

                    Switch(
                        checked = viewModel.graphicsQualityHigh,
                        onCheckedChange = { viewModel.graphicsQualityHigh = it },
                        modifier = Modifier.testTag("graphics_quality_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF5AC8FA),
                            checkedTrackColor = accentColor.copy(alpha = 0.4f),
                            uncheckedThumbColor = if (isDark) Color(0x66FFFFFF) else Color(0x66000000),
                            uncheckedTrackColor = if (isDark) Color(0x22FFFFFF) else Color(0x11000000)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 2: Storage Actions
        Text(
            text = "STORAGE & DATA MAINTENANCE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        GlassyCard(
            isDark = isDark,
            graphicsQualityHigh = viewModel.graphicsQualityHigh,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Clear History action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidPressEffect(intensity = 0.98f) { deleteHistoryConfirm = true }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Clear Generation History",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E1B4B)
                            )
                            Text(
                                text = "Erase all optimized runs from storage",
                                fontSize = 11.sp,
                                color = if (isDark) Color(0x73FFFFFF) else Color(0xFF6B7280)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (isDark) Color(0x3BFFFFFF) else Color(0x4D6B7894)
                    )
                }

                Divider(color = if (isDark) Color(0x11FFFFFF) else Color(0x1B000000))

                // Clear Favorites action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidPressEffect(intensity = 0.98f) { deleteFavsConfirm = true }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.StarOutline,
                            contentDescription = null,
                            tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Clear Saved Favorites",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E1B4B)
                            )
                            Text(
                                text = "Unstar and delete all marked templates",
                                fontSize = 11.sp,
                                color = if (isDark) Color(0x73FFFFFF) else Color(0xFF6B7280)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (isDark) Color(0x3BFFFFFF) else Color(0x4D6B7894)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 3: Feedback Form
        Text(
            text = "APP FEEDBACK",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        GlassyCard(
            isDark = isDark,
            graphicsQualityHigh = viewModel.graphicsQualityHigh,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Provide Feedback",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF1E1B4B)
                )
                Text(
                    text = "Help us craft Prompter into the finest tool for prompt engineering.",
                    fontSize = 11.sp,
                    color = if (isDark) Color(0x73FFFFFF) else Color(0xFF6B7280),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                TextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    placeholder = {
                        Text(
                            text = "Write your suggestions or report issues here...",
                            color = if (isDark) Color(0x4DFFFFFF) else Color(0x734B5563),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) Color(0x0E000000) else Color(0x0C000000))
                        .testTag("setting_feedback_textfield"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = if (isDark) Color.White else Color(0xFF1E1B4B),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E1B4B)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                GlassyButton(
                    onClick = {
                        if (feedbackText.isNotBlank()) {
                            showFeedbackDialog = true
                            feedbackText = ""
                        }
                    },
                    isDark = isDark,
                    enabled = feedbackText.isNotBlank(),
                    modifier = Modifier
                        .align(Alignment.End)
                        .testTag("send_feedback_btn")
                ) {
                    Text("Submit Feedback", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 4: Application details
        Text(
            text = "APPLICATION INFORMATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        GlassyCard(
            isDark = isDark,
            graphicsQualityHigh = viewModel.graphicsQualityHigh,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // About app
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidPressEffect(intensity = 0.98f) { showAboutDialog = true }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "About Prompter",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1E1B4B)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (isDark) Color(0x3BFFFFFF) else Color(0x4D6B7894)
                    )
                }

                Divider(color = if (isDark) Color(0x11FFFFFF) else Color(0x1B000000))

                // Privacy statement
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidPressEffect(intensity = 0.98f) { showPrivacyDialog = true }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Privacy Policy",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1E1B4B)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (isDark) Color(0x3BFFFFFF) else Color(0x4D6B7894)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gold Class Luxurious Credit Card / Author Signature
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isDark) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E1B4B).copy(alpha = 0.5f),
                                accentColor.copy(alpha = 0.08f),
                                Color(0xFF1E1B4B).copy(alpha = 0.5f)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF).copy(alpha = 0.8f),
                                Color(0xFF52C8FF).copy(alpha = 0.05f),
                                Color(0xFFFFFFFF).copy(alpha = 0.8f)
                            )
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = if (isDark) {
                            listOf(
                                Color(0x15FFFFFF),
                                Color(0x66007AFF),
                                Color(0x15FFFFFF)
                            )
                        } else {
                            listOf(
                                Color(0x10000000),
                                Color(0x5552C8FF),
                                Color(0x10000000)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Signature",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "DESIGNED & DEVELOPED BY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = if (isDark) Color(0x8CFFFFFF) else Color(0xFF6B7280)
                )
                
                Text(
                    text = "HARIPRASATH",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    color = accentColor
                )
                
                Text(
                    text = "Prompter Engine v1.0.0 • Premium Edition",
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = if (isDark) Color(0x4DFFFFFF) else Color(0x736B7894)
                )
            }
        }
    }

    // Modal Confirmation Dialogs
    if (deleteHistoryConfirm) {
        AlertDialog(
            onDismissRequest = { deleteHistoryConfirm = false },
            title = { Text("Clear All History?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all stored items. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllHistory()
                    deleteHistoryConfirm = false
                }) {
                    Text("Clear All", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteHistoryConfirm = false }) {
                    Text("Cancel", color = if (isDark) Color(0x8DFFFFFF) else Color(0xFF4B5563))
                }
            }
        )
    }

    if (deleteFavsConfirm) {
        AlertDialog(
            onDismissRequest = { deleteFavsConfirm = false },
            title = { Text("Clear Saved Favorites?", fontWeight = FontWeight.Bold) },
            text = { Text("This will unstar and remove all marked template entries from your Favorites tab.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllFavorites()
                    deleteFavsConfirm = false
                }) {
                    Text("Remove All", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteFavsConfirm = false }) {
                    Text("Cancel", color = if (isDark) Color(0x8DFFFFFF) else Color(0xFF4B5563))
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Prompter", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Prompter is a premium companion utility designed to bridge the semantic gap between simple human ideas and highly detailed artificial reasoning.")
                    Text("By integrating Gemini LLM instructions, we analyze your input target parameters and auto-assemble optimized guidelines, variable blocks, and clear XML schema configurations specifically requested to optimize LLM performance.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Excellent", color = accentColor, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Your privacy is extremely critical to us. Prompter behaves entirely offline by default.")
                    Text("All historical prompt generation outputs and items marked as Favorites are stored locally in secure private SQLite indexes on your physical device.")
                    Text("When optimizing, requests are dispatched directly via encrypted conduits to Google Gemini API servers. No identifying data, prompts, or inputs are routed or synced to any third-party clouds or analytics systems.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("I Understand", color = accentColor, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Feedback Submitted Alert
    if (showFeedbackDialog) {
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("Thank You!", fontWeight = FontWeight.Bold) },
            text = { Text("Your feedback has been successfully submitted. We appreciate your suggestions as we continue to shape Prompter.") },
            confirmButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("Done", color = accentColor, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
