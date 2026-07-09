package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PromptViewModel
import com.example.ui.theme.GlassyCard
import com.example.ui.theme.GlassyButton
import com.example.ui.theme.liquidPressEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateScreen(
    viewModel: PromptViewModel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Translate local states
    var translateInputText by remember { mutableStateOf("") }
    var translatedResultText by remember { mutableStateOf("") }
    var targetLanguageSelection by remember { mutableStateOf("Spanish 🇪🇸") }
    var isTranslateDropdownExpanded by remember { mutableStateOf(false) }

    val languages = listOf(
        "Spanish 🇪🇸", "French 🇫🇷", "German 🇩🇪", "Japanese 🇯🇵", "Chinese 🇨🇳", "Kotlin 🌐",
        "Hindi 🇮🇳", "Tamil 🇮🇳", "Telugu 🇮🇳", "Kannada 🇮🇳", "Malayalam 🇮🇳"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 75.dp) // Margin for bottom bar
    ) {
        // Translation Hub Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Google Translate Dock",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF1E1B4B)
                )
                Text(
                    text = "Translate prompts globally while keeping technical variables secure",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xA9FFFFFF) else Color(0xFF6B7280)
                )
            }
            com.example.ui.theme.ThemeToggleButton(viewModel = viewModel, isDark = isDark)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Secure Prompt Translation",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF1E1B4B),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "Ensure your placeholder schemas, parameters, code segments (<context></context>), and brackets remain intact.",
                fontSize = 11.sp,
                color = if (isDark) Color(0xA9FFFFFF) else Color(0xFF6B7280),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Input Source card
            GlassyCard(isDark = isDark, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "DETECTED SOURCE (ENGLISH)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0x9AFFFFFF) else Color(0xFF6B7280)
                        )
                        
                        // Utility buttons to reload home result
                        TextButton(onClick = {
                            viewModel.generatedPromptResult?.let {
                                translateInputText = it
                            } ?: run {
                                viewModel.showToast("No active generated prompt on Home!")
                            }
                        }) {
                            Text("LOAD LATEST GENERATED", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    TextField(
                        value = translateInputText,
                        onValueChange = { translateInputText = it },
                        placeholder = {
                            Text(
                                "Enter prompt variables, parameters, or general instructions here to initiate high-fidelity translation...",
                                fontSize = 13.sp,
                                color = if (isDark) Color(0x5EFFFFFF) else Color(0xFF94A3B8)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = if (isDark) Color.White else Color(0xFF1E1B4B),
                            unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E1B4B)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Target Language Selector Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "TARGET LANGUAGE:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0x9AFFFFFF) else Color(0xFF6B7280)
                )

                Box {
                    TextButton(onClick = { isTranslateDropdownExpanded = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(targetLanguageSelection, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }

                    var isMenuEntered by remember(isTranslateDropdownExpanded) { mutableStateOf(false) }
                    LaunchedEffect(isTranslateDropdownExpanded) {
                        if (isTranslateDropdownExpanded) {
                            isMenuEntered = true
                        }
                    }
                    val menuScale by animateFloatAsState(
                        targetValue = if (isMenuEntered) 1f else 0.88f,
                        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
                        label = "translate_menu_scale"
                    )
                    val menuAlpha by animateFloatAsState(
                        targetValue = if (isMenuEntered) 1f else 0f,
                        animationSpec = tween(durationMillis = 200),
                        label = "translate_menu_alpha"
                    )

                    DropdownMenu(
                        expanded = isTranslateDropdownExpanded,
                        onDismissRequest = { 
                            isMenuEntered = false
                            isTranslateDropdownExpanded = false 
                        },
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = menuScale
                                scaleY = menuScale
                                alpha = menuAlpha
                            }
                            .background(if (isDark) Color(0xFF1F1D2B) else Color.White)
                            .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x116B7894), RoundedCornerShape(12.dp))
                    ) {
                        languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang) },
                                onClick = {
                                    targetLanguageSelection = lang
                                    isTranslateDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Target Results card
            if (translatedResultText.isNotEmpty()) {
                GlassyCard(
                    isDark = isDark,
                    glowingAccent = true,
                    accentColors = listOf(Color(0xFF007AFF), Color(0xFF5AC8FA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "TRANSLATION: ${targetLanguageSelection.uppercase()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF007AFF)
                            )

                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(translatedResultText))
                                viewModel.showToast("Copied translated output!")
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.ContentCopy, null, tint = Color(0xFF007AFF), modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = translatedResultText,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (isDark) Color.White else Color(0xFF1E1B4B)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Button(
                            onClick = {
                                viewModel.saveToGoogleKeep(
                                    raw = "Translated [$targetLanguageSelection]",
                                    optimized = translatedResultText,
                                    model = "Google Keep Notebook",
                                    category = "Research",
                                    color = "#B2DFDB" // Pale Teal
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PushPin, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Translated to Keep", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Translate Action Button
            Button(
                onClick = {
                    if (translateInputText.isBlank()) {
                        viewModel.showToast("Please enter text of prompt to translate!")
                        return@Button
                    }
                    viewModel.translatePrompt(translateInputText, targetLanguageSelection) { translated ->
                        translatedResultText = translated
                    }
                },
                enabled = !viewModel.isTranslating && translateInputText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val buttonTextColor = Color.White
                if (viewModel.isTranslating) {
                    CircularProgressIndicator(color = buttonTextColor, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Engaging Google API servers...", color = buttonTextColor)
                } else {
                    Icon(Icons.Default.Translate, null, tint = buttonTextColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Translate Using Google Translate", color = buttonTextColor, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                        intent.component = android.content.ComponentName(
                            "com.google.android.apps.translate",
                            "com.google.android.apps.translate.TranslateActivity"
                        )
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://translate.google.com/"))
                            context.startActivity(intent)
                        } catch (e2: Exception) {
                            viewModel.showToast("Google Translate could not be opened.")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.OpenInBrowser, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open in Google Translate App", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
