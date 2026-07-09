package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PromptViewModel
import com.example.ui.theme.GlassTheme
import com.example.ui.theme.GlassyButton
import com.example.ui.theme.GlassyCard
import com.example.ui.theme.LiquidGlassChip
import com.example.ui.theme.LiquidGlassIconButton
import com.example.ui.theme.LiquidGlassScrim
import com.example.ui.theme.LiquidGlassVariant
import com.example.ui.theme.PremiumMotion
import com.example.ui.theme.liquidGlassSurface
import com.example.ui.theme.liquidPressEffect
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun DualOrbStudioSpinner(
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dual_spinner")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_factor"
    )

    Box(
        modifier = modifier
            .size(90.dp)
            .scale(scaleFactor),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing primary-color ring
        Box(
            modifier = Modifier
                .size(76.dp)
                .graphicsLayer { rotationZ = angle }
                .border(
                    width = 3.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF007AFF),
                            Color(0xFF5AC8FA).copy(alpha = 0.3f),
                            Color.Transparent,
                            Color(0xFF007AFF)
                        )
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
        // Inner reversed secondary-color ring
        Box(
            modifier = Modifier
                .size(50.dp)
                .graphicsLayer { rotationZ = -angle * 1.5f }
                .border(
                    width = 2.5.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF30D158),
                            Color(0xFF5AC8FA),
                            Color.Transparent,
                            Color(0xFF30D158)
                        )
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
        
        // Center spark point
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFF007AFF).copy(alpha = 0.6f))
                    )
                )
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PromptViewModel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val primaryAccent = Color(0xFF007AFF)
    val secondaryAccent = Color(0xFF5AC8FA)

    // Internal edit state for the generated prompt overlay
    var isEditingGeneratedPrompt by remember { mutableStateOf(false) }
    var editedPromptText by remember { mutableStateOf("") }

    // Focus state for the input container illumination
    var isInputFocused by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "breathing_input_transitions")

    // Breathing factor for ambient glow
    val breathFactor by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "input_breath"
    )

    // Voice button pulse halo circle scale and alpha
    val voicePulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "voice_scale"
    )
    val voicePulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "voice_alpha"
    )

    val view = LocalView.current

    Box(modifier = modifier.fillMaxSize()) {
        // Main Input form flow
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 90.dp) // Leave safety padding for the bottom bar
        ) {
            // Elegant Tagline Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val greeting = remember {
                    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                    when (hour) {
                        in 0..11 -> "Good Morning"
                        in 12..16 -> "Good Afternoon"
                        else -> "Good Evening"
                    }
                }
                Column {
                    Text(
                        text = greeting,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF8E8E93) else Color(0xFF6B7280),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "What would you like to create today?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White else Color(0xFF111827),
                        style = MaterialTheme.typography.titleLarge,
                        letterSpacing = (-0.5).sp
                    )
                }
                com.example.ui.theme.ThemeToggleButton(viewModel = viewModel, isDark = isDark)
            }

            // Step 1 Section: Raw Idea Input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "1. Express your raw idea",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color(0xDCFFFFFF) else Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "*",
                        color = primaryAccent,
                        fontWeight = FontWeight.Bold
                    )
                }

                GlassyCard(
                    isDark = isDark,
                    glowingAccent = isInputFocused,
                    cornerRadius = 24.dp, // large premium rounded corners
                    accentColors = listOf(
                        primaryAccent,
                        secondaryAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // Subtle breathing ambient glow inside the floating panel
                            val baseGlowAlpha = if (isInputFocused) 0.18f else 0.08f
                            val activeAlpha = baseGlowAlpha * breathFactor
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryAccent.copy(alpha = activeAlpha),
                                        Color.Transparent
                                    ),
                                    radius = size.width * 0.8f
                                ),
                                center = Offset(size.width / 2f, size.height / 2f)
                            )
                        }
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            value = viewModel.rawIdeaInput,
                            onValueChange = { viewModel.rawIdeaInput = it },
                            placeholder = {
                                Text(
                                    text = "Describe what you want the AI to do... e.g. 'Build a beautiful, interactive clock widget using jetpack compose.'",
                                    color = if (isDark) Color(0x66FFFFFF) else Color(0x734B5563),
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 110.dp)
                                .onFocusChanged { isInputFocused = it.isFocused }
                                .testTag("raw_idea_input"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = if (isDark) Color.White else Color(0xFF111827),
                                unfocusedTextColor = if (isDark) Color.White else Color(0xFF111827)
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )

                        if (viewModel.rawIdeaInput.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.rawIdeaInput = "" },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .testTag("clear_idea_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear raw input",
                                    tint = if (isDark) Color(0x8DFFFFFF) else Color(0xFF6B7280)
                                )
                            }
                        }

                        // Google Assistant Mic Speech-to-Text Button
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Pulse circle animation halo
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .scale(voicePulseScale)
                                    .drawBehind {
                                        drawCircle(
                                            color = primaryAccent.copy(alpha = voicePulseAlpha),
                                            radius = size.width / 2f
                                        )
                                    }
                            )

                            val speechLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                                contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
                            ) { result ->
                                if (result.resultCode == android.app.Activity.RESULT_OK) {
                                    val spokenText = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                                    if (!spokenText.isNullOrBlank()) {
                                        viewModel.rawIdeaInput = spokenText
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .liquidPressEffect(intensity = 0.88f) {
                                        try {
                                            view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                                            val intent = Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                putExtra(
                                                    android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                    android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                                )
                                                putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak your prompt idea...")
                                            }
                                            speechLauncher.launch(intent)
                                        } catch (e: Exception) {
                                            viewModel.showToast("Voice typing not supported on this device")
                                        }
                                    }
                                    .clip(CircleShape)
                                    .liquidGlassSurface(
                                        isDark = isDark,
                                        shape = CircleShape,
                                        interactive = false,
                                        cornerRadius = 19.dp,
                                        glowingAccent = true,
                                        accentColors = listOf(primaryAccent, secondaryAccent)
                                    )
                                    .testTag("voice_mic_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Speak idea using Google Voice typing",
                                    tint = primaryAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Dynamic prompt limit calculations for specific models
                val characterCount = viewModel.rawIdeaInput.length
                val selectedModelId = viewModel.selectedModelId
                
                val idealMin = 100
                val idealMax = when (selectedModelId) {
                    "gemini" -> 8000
                    "chatgpt" -> 6000
                    "claude" -> 8000
                    "grok" -> 5000
                    "deepseek" -> 6000
                    else -> 6000
                }
                
                val maxContextLabel = when (selectedModelId) {
                    "gemini" -> "2M tokens (~8M chars)"
                    "chatgpt" -> "128K tokens (~512K chars)"
                    "claude" -> "200K tokens (~800K chars)"
                    "grok" -> "131K tokens (~524K chars)"
                    "deepseek" -> "64K tokens (~256K chars)"
                    else -> "128K tokens"
                }

                val currentModelName = viewModel.aiModels.find { it.id == selectedModelId }?.name ?: "Selected AI"
                val progressPercent = if (idealMax > 0) {
                    (characterCount.toFloat() / idealMax.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        characterCount == 0 -> if (isDark) Color(0x33FFFFFF) else Color(0x33000000)
                                        characterCount < idealMin -> primaryAccent
                                        characterCount <= idealMax -> if (isDark) Color(0xFF10B981) else Color(0xFF059669)
                                        else -> if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Draft: $characterCount characters",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                characterCount == 0 -> if (isDark) Color(0x7AFFFFFF) else Color(0xFF6B7280)
                                characterCount < idealMin -> primaryAccent
                                characterCount <= idealMax -> if (isDark) Color(0xFF10B981) else Color(0xFF059669)
                                else -> if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)
                            }
                        )
                    }

                    Text(
                        text = "$currentModelName Max Context: $maxContextLabel",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0x8DFFFFFF) else Color(0xFF4B5563)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isDark) Color(0x19FFFFFF) else Color(0x0F000000))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressPercent)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = when {
                                        characterCount < idealMin -> listOf(primaryAccent, secondaryAccent)
                                        characterCount <= idealMax -> listOf(Color(0xFF10B981), Color(0xFF34D399))
                                        else -> listOf(Color(0xFFF59E0B), Color(0xFFF97316))
                                    }
                                )
                            )
                    )
                }

                // Friendly prompt length coach/guide
                val helperTip = when {
                    characterCount == 0 -> "Describe your raw idea with specific constraints and clear objectives for the best optimization."
                    characterCount < idealMin -> "Coaching: Add more technical context, targets, or instructions for optimal reasoning ($characterCount/$idealMin recommended chars)."
                    characterCount <= idealMax -> "Excellent complexity! Your raw instruction fits perfectly inside $currentModelName's optimal context."
                    else -> "Highly complex idea. High prompt length works great, but keeping it focused avoids unnecessary model distractions."
                }

                Text(
                    text = helperTip,
                    fontSize = 11.sp,
                    color = if (isDark) Color(0x8CFFFFFF) else Color(0xFF4B5563),
                    modifier = Modifier
                        .padding(top = 6.dp, start = 4.dp, end = 4.dp)
                        .alpha(0.85f)
                )
            }

            // Step 2 Section: Prompt Categories
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = "2. Select target category",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xDCFFFFFF) else Color(0xFF1E1B4B),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(viewModel.categories) { category ->
                        val isSelected = viewModel.selectedCategory == category.id
                        LiquidGlassChip(
                            label = category.name,
                            emoji = category.displayIcon,
                            selected = isSelected,
                            onClick = { viewModel.selectedCategory = category.id },
                            isDark = isDark,
                            selectedGradient = if (isDark) {
                                listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
                            } else {
                                listOf(Color(0xFF4F46E5), Color(0xFF4338CA))
                            },
                            modifier = Modifier.testTag("category_chip_${category.id}")
                        )
                    }
                }
            }

            // Step 3 Section: AI Target Model
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "3. Select target AI model",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xDCFFFFFF) else Color(0xFF1E1B4B),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Space-saving horizontal sliding track for selecting AI models
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    items(viewModel.aiModels) { model ->
                        val isSelected = viewModel.selectedModelId == model.id
                        
                        // Spring dynamic scale factor
                        val animatedScale by animateFloatAsState(
                            targetValue = if (isSelected) PremiumMotion.chipSelectedScale else 0.96f,
                            animationSpec = PremiumMotion.bouncySpring(),
                            label = "model_card_scale"
                        )

                        val animatedTranslationY by animateFloatAsState(
                            targetValue = if (isSelected) -6f else 0f,
                            animationSpec = PremiumMotion.bouncySpring(damping = 0.68f),
                            label = "model_card_translation"
                        )

                        val cardShape = RoundedCornerShape(18.dp)
                        val shadowColor = if (isDark) {
                            if (isSelected) primaryAccent.copy(alpha = 0.28f) else Color(0x14000000)
                        } else {
                            if (isSelected) primaryAccent.copy(alpha = 0.12f) else Color(0x06334155)
                        }

                        Box(
                            modifier = Modifier
                                .width(104.dp)
                                .height(112.dp)
                                .graphicsLayer {
                                    scaleX = animatedScale
                                    scaleY = animatedScale
                                    translationY = animatedTranslationY.dp.toPx()
                                }
                                .drawBehind {
                                    drawRoundRect(
                                        color = shadowColor,
                                        topLeft = Offset(0f, if (isSelected) 10f else 4f),
                                        size = Size(size.width, size.height),
                                        cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
                                    )
                                }
                                .clip(cardShape)
                                .liquidGlassSurface(
                                    isDark = isDark,
                                    shape = cardShape,
                                    variant = if (isSelected) LiquidGlassVariant.Regular else LiquidGlassVariant.Clear,
                                    interactive = false,
                                    cornerRadius = 18.dp,
                                    withBorder = true,
                                    glowingAccent = isSelected,
                                    accentColors = model.brandColors
                                )
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            width = 1.5.dp,
                                            brush = Brush.sweepGradient(model.brandColors),
                                            shape = cardShape
                                        )
                                    } else Modifier
                                )
                                .clickable {
                                    try {
                                        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                    } catch (e: Exception) {}
                                    viewModel.selectModel(model.id)
                                }
                                .padding(8.dp)
                                .testTag("model_selector_${model.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            val badgeText = when (model.id) {
                                "gemini" -> "SMART"
                                "chatgpt" -> "LOGIC"
                                "claude" -> "PROS"
                                "grok" -> "WITTY"
                                "deepseek" -> "REASON"
                                "perplexity" -> "SEARCH"
                                "llama" -> "FAST"
                                "copilot" -> "OFFICE"
                                "mistral" -> "COMPACT"
                                else -> "ROUTER"
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Dynamic capability badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) {
                                                Brush.linearGradient(model.brandColors)
                                            } else {
                                                Brush.linearGradient(listOf(Color(0x11FFFFFF), Color(0x09FFFFFF)))
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = badgeText,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Color.White else (if (isDark) Color(0x99FFFFFF) else Color(0xFF6B7280)),
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                // Interactive Round Logo Container with brand gradients
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) {
                                                Brush.linearGradient(listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.22f)))
                                            } else {
                                                Brush.linearGradient(model.brandColors)
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = if (isDark) 0.18f else 0.40f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = model.displayIcon, fontSize = 18.sp)
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                // Typographic model label
                                Text(
                                    text = model.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) {
                                        if (isDark) Color.White else Color(0xFF111827)
                                    } else {
                                        if (isDark) Color(0xCEFFFFFF) else Color(0xFF4B5563)
                                    },
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Dynamic tuning console for selected AI Engine
                val selectedModel = viewModel.aiModels.find { it.id == viewModel.selectedModelId }
                if (selectedModel != null) {
                    GlassyCard(
                        isDark = isDark,
                        glowingAccent = true,
                        accentColors = selectedModel.brandColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Model Profile Header
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Brush.linearGradient(selectedModel.brandColors)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(selectedModel.displayIcon, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${selectedModel.name} Tuning Console",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White else Color(0xFF1E1B4B)
                                )
                            }
                            
                            Text(
                                text = selectedModel.strength,
                                fontSize = 11.sp,
                                color = if (isDark) Color(0xCCFFFFFF) else Color(0xFF4B5563),
                                modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                            )
                            
                            HorizontalDivider(
                                color = if (isDark) Color(0x1BFFFFFF) else Color(0x206B7894),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            
                            // TONE STYLE SELECTOR (Professional, Creative, Concise, Technical, etc.)
                            Text(
                                text = "Optimization Tone / Style Style",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xBBFFFFFF) else Color(0xFF1E1B4B),
                                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                            )
                            
                            var isToneDropdownExpanded by remember { mutableStateOf(false) }
                            val toneOptions = listOf(
                                "Professional 💼",
                                "Creative 🎨",
                                "Concise ⚡",
                                "Technical 🛠️",
                                "Balanced ⚖️",
                                "Precise 🎯"
                            )
                            
                            val currentDisplayTone = toneOptions.find { it.startsWith(viewModel.selectedPromptTone) } ?: "${viewModel.selectedPromptTone} ✨"

                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isDark) Color(0x11FFFFFF) else Color(0x0A000000))
                                        .border(
                                            1.dp,
                                            if (isDark) Color(0x1AFFFFFF) else Color(0x1A6B7894),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { isToneDropdownExpanded = true }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Selected Tone:",
                                            fontSize = 11.sp,
                                            color = if (isDark) Color(0x88FFFFFF) else Color(0xFF6B7280),
                                            modifier = Modifier.padding(end = 6.dp)
                                        )
                                        Text(
                                            text = currentDisplayTone,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.White else Color(0xFF1E1B4B)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Style",
                                        tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF1E1B4B).copy(alpha = 0.7f)
                                    )
                                }

                                var isMenuEntered by remember(isToneDropdownExpanded) { mutableStateOf(false) }
                                LaunchedEffect(isToneDropdownExpanded) {
                                    if (isToneDropdownExpanded) {
                                        isMenuEntered = true
                                    }
                                }
                                val menuScale by animateFloatAsState(
                                    targetValue = if (isMenuEntered) 1f else 0.88f,
                                    animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
                                    label = "tone_menu_scale"
                                )
                                val menuAlpha by animateFloatAsState(
                                    targetValue = if (isMenuEntered) 1f else 0f,
                                    animationSpec = tween(durationMillis = 200),
                                    label = "tone_menu_alpha"
                                )

                                DropdownMenu(
                                    expanded = isToneDropdownExpanded,
                                    onDismissRequest = { 
                                        isMenuEntered = false
                                        isToneDropdownExpanded = false 
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .graphicsLayer {
                                            scaleX = menuScale
                                            scaleY = menuScale
                                            alpha = menuAlpha
                                        }
                                        .background(if (isDark) Color(0xFF1F1D2B) else Color.White)
                                        .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x116B7894), RoundedCornerShape(12.dp))
                                ) {
                                    toneOptions.forEach { option ->
                                        val toneKey = option.split(" ").first()
                                        val isSelected = viewModel.selectedPromptTone == toneKey
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = option,
                                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                        color = if (isSelected) {
                                                            primaryAccent
                                                        } else {
                                                            if (isDark) Color.White else Color(0xFF1E1B4B)
                                                        },
                                                        fontSize = 13.sp
                                                    )
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Selected",
                                                            tint = primaryAccent,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                viewModel.selectedPromptTone = toneKey
                                                isToneDropdownExpanded = false
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Generate Button Action
            HeroGenerateButton(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.optimizeRawIdea()
                },
                isDark = isDark,
                enabled = viewModel.rawIdeaInput.isNotBlank() && !viewModel.isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .testTag("generate_prompt_button")
            )
        }

        // Active Prompt Generating Overlay (Skeleton Screen!)
        AnimatedVisibility(
            visible = viewModel.isGenerating,
            modifier = Modifier.fillMaxSize(),
            enter = slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = PremiumMotion.bouncySpring(damping = 0.68f)
            ) + fadeIn(PremiumMotion.enterTween()),
            exit = slideOutVertically(
                targetOffsetY = { it / 2 },
                animationSpec = PremiumMotion.snappySpring()
            ) + fadeOut(PremiumMotion.exitTween())
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                LiquidGlassScrim(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { viewModel.isGenerating = false },
                    isDark = isDark
                )
                GlassyCard(
                    isDark = isDark,
                    cornerRadius = 24.dp,
                    glowingAccent = true,
                    accentColors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.6f), Color(0xFFEC4899).copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.88f)
                        .clickable(enabled = false) {} // Prevent click-throughs
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Title / Header controls representation
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = Color(0xFF8B5CF6),
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Optimizer is Crafting Prompt...",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White else Color(0xFF1E1B4B)
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.isGenerating = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Generation",
                                    tint = if (isDark) Color(0x66FFFFFF) else Color(0xFF4B5563)
                                )
                            }
                        }

                        // Code Container Skeleton Body
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF070B19) else Color(0xFFF1F5F9))
                                .border(
                                    width = 1.dp,
                                    color = if (isDark) Color(0x1BFFFFFF) else Color(0x40B5C0D0),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Status cycle row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    val statusMessages = listOf(
                                        "Analyzing raw user instructions...",
                                        "Drafting high-context prompt framework...",
                                        "Sourcing structural system keys for ${viewModel.aiModels.find { it.id == viewModel.selectedModelId }?.name ?: "Target Model"}...",
                                        "Structuring model variables and delimiters...",
                                        "Injecting chosen perspective (${viewModel.selectedPromptTone} style)...",
                                        "Aligning output constraints and format borders...",
                                        "Compiling final high-fidelity prompt template..."
                                    )
                                    val progressStep = remember { mutableStateOf(0) }
                                    LaunchedEffect(Unit) {
                                        while(true) {
                                            delay(1400)
                                            progressStep.value = (progressStep.value + 1) % statusMessages.size
                                        }
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF00D26A))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Status: ${statusMessages[progressStep.value]}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                         color = primaryAccent
                                    )
                                }

                                // Shimmering wireframe rows
                                val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 0.15f,
                                    targetValue = 0.45f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(850, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "shimmer_alpha"
                                )

                                val shimmerColor = if (isDark) {
                                    Color.White.copy(alpha = 0.06f * alpha)
                                } else {
                                    Color.Black.copy(alpha = 0.04f * alpha)
                                }

                                // Mimicking structured system constraints & paragraphs of markdown template
                                val lineWidths = listOf(
                                    0.9f, 0.75f, 0.85f, 0.45f,
                                    0.88f, 0.7f, 0.95f, 0.6f,
                                    0.35f, 0.85f, 0.5f
                                )
                                lineWidths.forEach { widthFraction ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(widthFraction)
                                            .height(13.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(shimmerColor)
                                    )
                                }
                            }

                            // Centered Dual Glowing Star Spinner
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(0.85f),
                                contentAlignment = Alignment.Center
                            ) {
                                FloatingParticles(isDark = isDark)
                                DualOrbStudioSpinner(isDark = isDark)
                            }
                        }

                        // Bottom actions controls skeleton row representation
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Represent inline actions like Favorite/Copy
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                repeat(3) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isDark) Color(0x11FFFFFF) else Color(0x11000000))
                                            .alpha(0.6f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f))
                                        )
                                    }
                                }
                            }

                            // Represent main copy/send button
                            Box(
                                modifier = Modifier
                                    .width(135.dp)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color(0x15FFFFFF) else Color(0x11000000))
                                    .alpha(0.6f)
                            )
                        }
                    }
                }
            }
        }

        // Expanded Bottom Glass Sheet overlay containing the Generated Optimized Prompt
        AnimatedVisibility(
            visible = viewModel.generatedPromptResult != null,
            modifier = Modifier.fillMaxSize(),
            enter = slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = PremiumMotion.bouncySpring(damping = 0.68f)
            ) + fadeIn(PremiumMotion.enterTween()),
            exit = slideOutVertically(
                targetOffsetY = { it / 2 },
                animationSpec = PremiumMotion.snappySpring()
            ) + fadeOut(PremiumMotion.exitTween())
        ) {
            val promptText = viewModel.generatedPromptResult ?: ""
            
            // Sync internal editor string when the overlay displays & launch premium fade-in animation
            val outputDisplayAlpha = remember { Animatable(0f) }
            val outputDisplayTranslationY = remember { Animatable(30f) }

            LaunchedEffect(viewModel.generatedPromptResult) {
                if (promptText.isNotEmpty()) {
                    editedPromptText = promptText
                    outputDisplayAlpha.snapTo(0f)
                    outputDisplayTranslationY.snapTo(30f)
                    launch {
                        outputDisplayAlpha.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = 850,
                                easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f) // Custom premium ease-out bezier curve
                            )
                        )
                    }
                    launch {
                        outputDisplayTranslationY.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(
                                durationMillis = 850,
                                easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f) // Custom premium ease-out bezier curve
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                LiquidGlassScrim(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            isEditingGeneratedPrompt = false
                            focusManager.clearFocus()
                        },
                    isDark = isDark
                )
                GlassyCard(
                    isDark = isDark,
                    cornerRadius = 24.dp,
                    glowingAccent = true,
                    accentColors = listOf(primaryAccent, secondaryAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.88f)
                        .clickable(enabled = false) {} // Prevent click-throughs to dismiss
                        .testTag("prompt_output_container")
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Title / Header controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF8B5CF6),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ready Optimized Prompt",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White else Color(0xFF1E1B4B)
                                )
                            }

                            IconButton(
                                onClick = {
                                    isEditingGeneratedPrompt = false
                                    viewModel.closeGeneratedPromptDetail()
                                },
                                modifier = Modifier.testTag("close_output_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close overlay",
                                    tint = if (isDark) Color(0x66FFFFFF) else Color(0xFF4B5563)
                                )
                            }
                        }

                        // Code Container Body
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = outputDisplayAlpha.value
                                    translationY = outputDisplayTranslationY.value
                                }
                                .clip(RoundedCornerShape(14.dp))
                                .liquidGlassSurface(
                                    isDark = isDark,
                                    shape = RoundedCornerShape(14.dp),
                                    variant = LiquidGlassVariant.Clear,
                                    interactive = false,
                                    cornerRadius = 14.dp
                                )
                                .padding(12.dp)
                        ) {
                            if (isEditingGeneratedPrompt) {
                                TextField(
                                    value = editedPromptText,
                                    onValueChange = { editedPromptText = it },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("edit_output_textfield"),
                                    textStyle = LocalTextStyle.current.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        color = if (isDark) Color.White else Color(0xFF1E1B4B)
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    )
                                )
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = promptText,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = if (isDark) Color(0xEEFFFFFF) else Color(0xFF334155),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Controls Bar: Edit save vs regular controls
                        if (isEditingGeneratedPrompt) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GlassyButton(
                                    onClick = { isEditingGeneratedPrompt = false },
                                    isDark = isDark,
                                    colors = listOf(Color(0xFF6B7280), Color(0xFF4B5563)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Discard", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                GlassyButton(
                                    onClick = {
                                        if (editedPromptText.isNotEmpty()) {
                                            viewModel.updateLoadedPrompt(editedPromptText)
                                        }
                                        isEditingGeneratedPrompt = false
                                    },
                                    isDark = isDark,
                                    colors = listOf(Color(0xFF10A37F), Color(0xFF0F7F60)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, "Save icon", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Primary quick access control panel
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Inline actions: Edit & Favorite
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    LiquidGlassIconButton(
                                        onClick = { isEditingGeneratedPrompt = true },
                                        icon = Icons.Default.Edit,
                                        contentDescription = "Edit manual prompt text",
                                        isDark = isDark,
                                        modifier = Modifier.testTag("inline_edit_button")
                                    )

                                    LiquidGlassIconButton(
                                        onClick = { viewModel.toggleCurrentPromptFavorite() },
                                        icon = if (viewModel.isCurrentPromptFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Save to favorites",
                                        isDark = isDark,
                                        tint = if (viewModel.isCurrentPromptFavorite) primaryAccent else (if (isDark) Color.White else Color(0xFF1E1B4B)),
                                        modifier = Modifier.testTag("inline_favorite_button")
                                    )

                                    LiquidGlassIconButton(
                                        onClick = {
                                            viewModel.saveToGoogleKeep(
                                                raw = viewModel.rawIdeaInput,
                                                optimized = promptText,
                                                model = viewModel.aiModels.find { it.id == viewModel.selectedModelId }?.name ?: "Gemini",
                                                category = viewModel.selectedCategory,
                                                color = "#FFF9C4"
                                            )
                                        },
                                        icon = Icons.Default.PushPin,
                                        contentDescription = "Save to Google Keep Notebook",
                                        isDark = isDark,
                                        tint = if (isDark) Color(0xFFFFF9C4) else Color(0xFFF1C40F),
                                        modifier = Modifier.testTag("inline_keep_button")
                                    )

                                    LiquidGlassIconButton(
                                        onClick = {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, promptText)
                                                type = "text/plain"
                                            }
                                            val shareIntent = Intent.createChooser(sendIntent, "Share Prompt")
                                            context.startActivity(shareIntent)
                                        },
                                        icon = Icons.Default.Share,
                                        contentDescription = "Share Prompt text",
                                        isDark = isDark,
                                        modifier = Modifier.testTag("inline_share_button")
                                    )
                                }

                                // Large glowing Copy button
                                val scope = rememberCoroutineScope()
                                var isCopyPulsing by remember { mutableStateOf(false) }
                                var isCopiedSuccessfully by remember { mutableStateOf(false) }
                                val copyButtonScale by animateFloatAsState(
                                    targetValue = if (isCopyPulsing) 0.9f else 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )

                                GlassyButton(
                                    onClick = {
                                        scope.launch {
                                            view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                                            isCopyPulsing = true
                                            isCopiedSuccessfully = true
                                            delay(150)
                                            isCopyPulsing = false
                                            delay(1850)
                                            isCopiedSuccessfully = false
                                        }
                                        clipboardManager.setText(AnnotatedString(promptText))
                                        viewModel.showToast("Prompt copied successfully.")
                                    },
                                    isDark = isDark,
                                    colors = if (isCopiedSuccessfully) {
                                        listOf(Color(0xFF00D26A), Color(0xFF00A250)) // Success material colors
                                    } else {
                                        if (isDark) listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)) else listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp)
                                        .scale(copyButtonScale)
                                        .testTag("copy_prompt_button")
                                ) {
                                    androidx.compose.animation.AnimatedContent(
                                        targetState = isCopiedSuccessfully,
                                        transitionSpec = {
                                            (androidx.compose.animation.scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.58f)) + androidx.compose.animation.fadeIn(animationSpec = tween(220)))
                                                .togetherWith(androidx.compose.animation.scaleOut(animationSpec = tween(180)) + androidx.compose.animation.fadeOut(animationSpec = tween(180)))
                                        },
                                        label = "copy_icon_anim",
                                        modifier = Modifier.fillMaxWidth()
                                    ) { successfullyCopied ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = if (successfullyCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (successfullyCopied) "Copied!" else "Copy Prompt",
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun HeroGenerateButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val view = androidx.compose.ui.platform.LocalView.current
    val infiniteTransition = rememberInfiniteTransition(label = "hero_glow")
    
    // Continuous timed gradient shift value
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_shift"
    )

    // Modulated breath glow value
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_breath"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Real touch spring physics
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) PremiumMotion.pressScale else 1.0f,
        animationSpec = PremiumMotion.bouncySpring(damping = 0.62f),
        label = "compression"
    )

    val primaryAccent = Color(0xFF007AFF)
    val secondaryAccent = Color(0xFF5AC8FA)

    val shadowColor = if (isDark) primaryAccent.copy(alpha = 0.40f * glowIntensity) else primaryAccent.copy(alpha = 0.20f * glowIntensity)

    Box(
        modifier = modifier
            .scale(buttonScale)
            .alpha(if (enabled) 1.0f else 0.55f)
            .drawBehind {
                // Outer glowing blur radial-shape behind capsule (shadow/glow layer)
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(-4f, 4f),
                    size = Size(size.width + 8f, size.height + 8f),
                    cornerRadius = CornerRadius(36.dp.toPx(), 36.dp.toPx())
                )
                // Specular light edge highlight reflection layer (crystal border accent)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.38f), Color.Transparent),
                        startY = 0f,
                        endY = size.height * 0.25f
                    ),
                    cornerRadius = CornerRadius(36.dp.toPx(), 36.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(36.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(bounded = true, color = Color.White),
                enabled = enabled,
                onClick = {
                    try {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                    } catch (e: Exception) {}
                    onClick()
                }
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(primaryAccent, secondaryAccent, primaryAccent),
                    start = Offset(gradientShift, 0f),
                    end = Offset(gradientShift + 600f, 400f)
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.65f), Color.White.copy(alpha = 0.15f))
                ),
                shape = RoundedCornerShape(36.dp)
            )
            .padding(vertical = 16.dp, horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Generate Optimized Prompt",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun FloatingParticles(
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "particles")
    val primaryAccent = Color(0xFF007AFF)
    
    // Simulate 12 small floating glass bubbles/sparks drifting upwards
    Box(modifier = modifier.fillMaxSize()) {
        repeat(12) { index ->
            val initialX = remember { (100..900).random().toFloat() }
            val speedY = remember { (12000..18000).random() }
            val delayMs = remember { (0..3000).random() }
            val size = remember { (4..8).random().dp }
            
            val yOffset by transition.animateFloat(
                initialValue = 1000f,
                targetValue = -100f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = speedY, delayMillis = delayMs, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "particle_y_$index"
            )
            
            val wiggleX by transition.animateFloat(
                initialValue = -30f,
                targetValue = 30f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 3000 + (index * 200), easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "particle_x_$index"
            )

            Box(
                modifier = Modifier
                    .offset(x = wiggleX.dp + initialX.dp / 5, y = yOffset.dp / 8)
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                primaryAccent.copy(alpha = 0.65f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}
