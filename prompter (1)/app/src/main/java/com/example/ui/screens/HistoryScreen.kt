package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PromptViewModel
import com.example.ui.theme.GlassyCard
import com.example.ui.theme.LiquidGlassIconButton
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: PromptViewModel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val history by viewModel.historyState.collectAsState()
    val view = LocalView.current
    
    // Dialog for clear all confirmation
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 75.dp) // Safety margin for floating bottom navigation
    ) {
        // Navigation / Action Header bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "History",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF1E1B4B)
                )
                Text(
                    text = "Manage your optimized creations",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xA9FFFFFF) else Color(0xFF6B7280)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                com.example.ui.theme.ThemeToggleButton(viewModel = viewModel, isDark = isDark)
                if (history.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    LiquidGlassIconButton(
                        onClick = { showClearConfirmDialog = true },
                        icon = Icons.Default.DeleteSweep,
                        contentDescription = "Clear all historical entries",
                        isDark = isDark,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.testTag("clear_history_all_button")
                    )
                }
            }
        }

        // Realtime Search Bar Search Query Input
        GlassyCard(
            isDark = isDark,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = if (isDark) Color(0x66FFFFFF) else Color(0x734B5563),
                    modifier = Modifier.size(20.dp)
                )
                
                TextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search categories, prompts, or models...",
                            color = if (isDark) Color(0x66FFFFFF) else Color(0x734B5563),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_history_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = if (isDark) Color.White else Color(0xFF1E1B4B),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E1B4B)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )

                if (viewModel.searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.searchQuery = "" },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear query text",
                            tint = if (isDark) Color(0x8DFFFFFF) else Color(0xFF4B5563)
                        )
                    }
                }
            }
        }

        // Listings display container
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HistoryEmptyState(
                    isDark = isDark,
                    searchQueryEmpty = viewModel.searchQuery.isEmpty(),
                    onActionClick = {
                        try {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                        } catch (e: Exception) {}
                        viewModel.activeScreen = com.example.ui.Screen.Home
                    }
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(
                    items = history,
                    key = { it.id }
                ) { prompt ->
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(prompt.id) {
                        isVisible = true
                    }
                    val itemScale by animateFloatAsState(if(isVisible) 1f else 0.8f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow))
                    val itemAlpha by animateFloatAsState(if(isVisible) 1f else 0f, tween(400))

                    val dateFormatted = remember(prompt.timestamp) {
                        val formatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                        formatter.format(Date(prompt.timestamp))
                    }

                    val catEmoji = when (prompt.category) {
                        "Coding" -> "💻"
                        "Writing" -> "📝"
                        "Education" -> "🎓"
                        "Marketing" -> "📈"
                        "Business" -> "👔"
                        "Productivity" -> "⏱️"
                        "Image Generation" -> "🎨"
                        "Research" -> "🧪"
                        "Creative" -> "🎭"
                        else -> "🔮"
                    }

                    GlassyCard(
                        isDark = isDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(itemScale)
                            .alpha(itemAlpha)
                            .clickable {
                                viewModel.loadSelectedPrompt(prompt)
                            }
                            .testTag("history_item_${prompt.id}")
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Top Metabar: Category, brand and time
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = catEmoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = prompt.category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF007AFF)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "•",
                                        color = if (isDark) Color(0x3BFFFFFF) else Color(0x3C6B7894)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = prompt.modelName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xBCFFFFFF) else Color(0xFF1E1B4B)
                                    )
                                }

                                Text(
                                    text = dateFormatted,
                                    fontSize = 10.sp,
                                    color = if (isDark) Color(0x4DFFFFFF) else Color(0xFF6B7280)
                                )
                            }

                            // Raw Idea text overview
                            Text(
                                text = prompt.rawIdea,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E1B4B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 10.dp)
                            )

                            // Optimized preview
                            Text(
                                text = prompt.optimizedPrompt,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = if (isDark) Color(0xCCFFFFFF) else Color(0xFF334155),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isDark) Color(0x0E000000) else Color(0x0C000000))
                                    .padding(6.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Controls Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tap to load & edit prompt",
                                    fontSize = 10.sp,
                                    color = if (isDark) Color(0x66FFFFFF) else Color(0x8C4B5563),
                                    fontWeight = FontWeight.SemiBold
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Toggle fav inline
                                    IconButton(
                                        onClick = { viewModel.toggleEntityFavorite(prompt) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("history_item_fav_toggle_${prompt.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (prompt.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "Favorite toggle",
                                            tint = if (prompt.isFavorite) Color(0xFF007AFF) else (if (isDark) Color(0x4DFFFFFF) else Color(0xFF4B5563)),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Delete inline
                                    IconButton(
                                        onClick = { viewModel.deletePrompt(prompt.id) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("history_item_delete_button_${prompt.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete prompt item",
                                            tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
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

    // Confirmation Alert dialogue box for clearing all records
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "Clear All History?",
                    color = if (isDark) Color.White else Color(0xFF1E1B4B),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This action will permanently delete all optimized prompts from your local storage history. Are you absolutely sure?",
                    color = if (isDark) Color(0xDDFFFFFF) else Color(0xFF4B5563)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearConfirmDialog = false
                    },
                    modifier = Modifier.testTag("confirm_clear_history_button")
                ) {
                    Text("Clear All", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = if (isDark) Color(0x8DFFFFFF) else Color(0xFF4B5563))
                }
            },
            containerColor = if (isDark) Color(0xFF0F0F10) else Color(0xFFFFFFFF),
            shape = RoundedCornerShape(16.dp)
        )
    }
}


@Composable
fun HistoryEmptyState(
    isDark: Boolean,
    searchQueryEmpty: Boolean,
    onActionClick: () -> Unit
) {
    val primaryAccent = Color(0xFF007AFF)
    val secondaryAccent = Color(0xFF5AC8FA)
    
    val infiniteTransition = rememberInfiniteTransition(label = "history_empty_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )
    
    val clockRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hands_spinning"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Beautiful floating hourglass/clock illustration
        Box(
            modifier = Modifier
                .size(160.dp)
                .graphicsLayer { translationY = floatOffset }
                .drawBehind {
                    // Soft bottom glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryAccent.copy(alpha = if (isDark) 0.18f else 0.08f), Color.Transparent),
                            radius = size.width / 1.5f
                        ),
                        center = Offset(size.width / 2f, size.height / 1.2f)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(110.dp)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.3f else 0.8f),
                                Color.White.copy(alpha = if (isDark) 0.05f else 0.2f)
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f
                
                // Draw liquid glass circular body
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.08f else 0.45f),
                            primaryAccent.copy(alpha = if (isDark) 0.03f else 0.10f)
                        )
                    )
                )
                
                // Draw clock indicators (ticks)
                for (angle in 0 until 360 step 30) {
                    val angleRad = Math.toRadians(angle.toDouble())
                    val startX = (center.x + (radius - 12f) * Math.cos(angleRad)).toFloat()
                    val startY = (center.y + (radius - 12f) * Math.sin(angleRad)).toFloat()
                    val endX = (center.x + (radius - 4f) * Math.cos(angleRad)).toFloat()
                    val endY = (center.y + (radius - 4f) * Math.sin(angleRad)).toFloat()
                    
                    drawLine(
                        color = (if (isDark) Color.White else Color(0xFF1E1B4B)).copy(alpha = if (angle % 90 == 0) 0.4f else 0.15f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (angle % 90 == 0) 2.5f else 1.5f
                    )
                }

                // Draw sand hourglass with custom Path
                val glassPath = Path().apply {
                    moveTo(center.x - radius * 0.4f, center.y - radius * 0.5f)
                    lineTo(center.x + radius * 0.4f, center.y - radius * 0.5f)
                    quadraticTo(center.x + radius * 0.05f, center.y, center.x + radius * 0.4f, center.y + radius * 0.5f)
                    lineTo(center.x - radius * 0.4f, center.y + radius * 0.5f)
                    quadraticTo(center.x - radius * 0.05f, center.y, center.x - radius * 0.4f, center.y - radius * 0.5f)
                    close()
                }
                
                drawPath(
                    path = glassPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryAccent.copy(alpha = 0.08f),
                            secondaryAccent.copy(alpha = 0.02f)
                        )
                    )
                )
                
                drawPath(
                    path = glassPath,
                    color = (if (isDark) Color.White else Color(0xFF1E1B4B)).copy(alpha = 0.08f),
                    style = Stroke(width = 1.5f)
                )

                // Hourglass sand slipping streams
                drawLine(
                    color = primaryAccent.copy(alpha = 0.4f),
                    start = Offset(center.x, center.y - radius * 0.18f),
                    end = Offset(center.x, center.y + radius * 0.35f),
                    strokeWidth = 2f
                )

                // Bottom sand pile
                drawArc(
                    color = secondaryAccent.copy(alpha = 0.25f),
                    startAngle = 30f,
                    sweepAngle = 120f,
                    useCenter = true,
                    topLeft = Offset(center.x - radius * 0.25f, center.y + radius * 0.25f),
                    size = Size(radius * 0.5f, radius * 0.25f)
                )

                // Top sand pile
                drawArc(
                    color = primaryAccent.copy(alpha = 0.20f),
                    startAngle = 210f,
                    sweepAngle = 120f,
                    useCenter = true,
                    topLeft = Offset(center.x - radius * 0.25f, center.y - radius * 0.45f),
                    size = Size(radius * 0.5f, radius * 0.2f)
                )

                // Dynamic spinning clock hands indicating time passing
                rotate(degrees = clockRotation, pivot = center) {
                    // Minutes hand
                    drawLine(
                        color = primaryAccent.copy(alpha = 0.8f),
                        start = center,
                        end = Offset(center.x, center.y - radius * 0.55f),
                        strokeWidth = 3f
                    )
                }
                
                rotate(degrees = clockRotation / 12f, pivot = center) {
                    // Hour hand
                    drawLine(
                        color = secondaryAccent.copy(alpha = 0.7f),
                        start = center,
                        end = Offset(center.x + radius * 0.38f, center.y),
                        strokeWidth = 4.5f
                    )
                }

                // Center hub pin
                drawCircle(
                    color = if (isDark) Color.White else Color(0xFF1E1B4B),
                    radius = 4.5f,
                    center = center
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (searchQueryEmpty) "No history items yet" else "No matching prompts found",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isDark) Color.White else Color(0xFF1E1B4B),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (searchQueryEmpty) {
                "Your optimized prompts will appear here as soon as you generate them."
            } else {
                "No outputs fit your current query criteria. Try altering your filter keywords."
            },
            fontSize = 13.sp,
            color = if (isDark) Color(0xA1FFFFFF) else Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (searchQueryEmpty) {
            Spacer(modifier = Modifier.height(26.dp))
            
            // Primary Glass CTA Button
            com.example.ui.theme.GlassyButton(
                onClick = onActionClick,
                isDark = isDark,
                colors = listOf(primaryAccent, secondaryAccent),
                modifier = Modifier
                    .widthIn(min = 200.dp)
                    .testTag("empty_history_cta_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate First Prompt",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
