package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PromptViewModel
import com.example.ui.theme.GlassyCard
import com.example.ui.theme.premiumListEnter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FavoritesScreen(
    viewModel: PromptViewModel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favoritesState.collectAsState()
    val view = LocalView.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 75.dp) // Safety margin for bottom glass navigation bar
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
                    text = "Favorites",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF1E1B4B)
                )
                Text(
                    text = "Access your marked templates with one-tap",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xA9FFFFFF) else Color(0xFF6B7280)
                )
            }
            com.example.ui.theme.ThemeToggleButton(viewModel = viewModel, isDark = isDark)
        }

        // Favorites core results list
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                FavoritesEmptyState(
                    isDark = isDark,
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
                    items = favorites,
                    key = { it.id }
                ) { prompt ->
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(prompt.id) {
                        isVisible = true
                    }

                    val dateFormatted = remember(prompt.timestamp) {
                        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
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
                            .premiumListEnter(visible = isVisible, index = favorites.indexOf(prompt))
                            .clickable {
                                viewModel.loadSelectedPrompt(prompt)
                            }
                            .testTag("favorites_item_${prompt.id}")
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
                                maxLines = 3,
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

                                IconButton(
                                    onClick = { viewModel.toggleEntityFavorite(prompt) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("favorite_star_remove_btn_${prompt.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Unfavorite",
                                        tint = Color(0xFF007AFF),
                                        modifier = Modifier.size(20.dp)
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


@Composable
fun FavoritesEmptyState(
    isDark: Boolean,
    onActionClick: () -> Unit
) {
    val primaryAccent = Color(0xFF007AFF)
    val secondaryAccent = Color(0xFF5AC8FA)
    
    val infiniteTransition = rememberInfiniteTransition(label = "fav_empty_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )
    
    val starGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Beautiful floating glassy star illustration
        Box(
            modifier = Modifier
                .size(160.dp)
                .graphicsLayer { translationY = floatOffset }
                .drawBehind {
                    // Soft background glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryAccent.copy(alpha = if (isDark) 0.16f else 0.08f), Color.Transparent),
                            radius = size.width / 1.4f
                        ),
                        center = Offset(size.width / 2f, size.height / 1.2f)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(90.dp)
                    .scale(starGlowScale)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f
                
                // Construct a 5-pointed star path
                val starPath = Path().apply {
                    val doublePi = 2 * Math.PI
                    val points = 5
                    var first = true
                    for (i in 0 until 2 * points) {
                        val r = if (i % 2 == 0) radius else radius * 0.42f
                        val angle = i * doublePi / (2 * points) - Math.PI / 2
                        val x = (center.x + r * Math.cos(angle)).toFloat()
                        val y = (center.y + r * Math.sin(angle)).toFloat()
                        if (first) {
                            moveTo(x, y)
                            first = false
                        } else {
                            lineTo(x, y)
                        }
                    }
                    close()
                }

                // Draw premium 3D overlapping light gradients
                drawPath(
                    path = starPath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = if (isDark) 0.70f else 0.85f), // Gold core
                            primaryAccent.copy(alpha = if (isDark) 0.25f else 0.40f)   // Premium transition
                        ),
                        center = center,
                        radius = radius
                    )
                )

                // 3D glass highlight overlay
                val highlightPath = Path().apply {
                    moveTo(center.x, 0f)
                    lineTo(center.x + radius * 0.26f, center.y - radius * 0.36f)
                    lineTo(center.x + radius * 0.95f, center.y - radius * 0.31f)
                    lineTo(center.x + radius * 0.42f, center.y + radius * 0.13f)
                    lineTo(center.x + radius * 0.59f, center.y + radius * 0.81f)
                    lineTo(center.x, center.y + radius * 0.42f)
                    close()
                }

                drawPath(
                    path = highlightPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.28f else 0.65f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )

                // Precise crystal outline
                drawPath(
                    path = starPath,
                    color = Color.White.copy(alpha = if (isDark) 0.45f else 0.85f),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Little glittering spark pin in the center
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = 4.dp.toPx(),
                    center = center
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "No favorites yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isDark) Color.White else Color(0xFF1E1B4B),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Mark prompt outputs with a star during generation to collect them here for quick template-loading.",
            fontSize = 13.sp,
            color = if (isDark) Color(0xA1FFFFFF) else Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(26.dp))
        
        // Primary Glass CTA Button
        com.example.ui.theme.GlassyButton(
            onClick = onActionClick,
            isDark = isDark,
            colors = listOf(primaryAccent, secondaryAccent),
            modifier = Modifier
                .widthIn(min = 200.dp)
                .testTag("empty_favorites_cta_button")
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Browse Templates",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
