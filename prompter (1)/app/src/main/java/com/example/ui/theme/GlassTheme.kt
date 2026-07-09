package com.example.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.testTag
import com.example.ui.PromptViewModel
import dev.chrisbanes.haze.hazeSource

// Custom liquid-glass visual styles reflecting Apple's Liquid Glass Design
object GlassTheme {
    val DarkBackgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF000000), Color(0xFF080808), Color(0xFF000000))
    )

    val LightBackgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFF6F7FB), Color(0xFFEBEFF5), Color(0xFFF6F7FB))
    )

    val GlassBorderDark = Color(0x23007AFF)   // Translucent Primary Accent
    val GlassBorderLight = Color(0x2B5AC8FA)  // Translucent Secondary Accent

    val SparkleColor1 = Color(0xFF007AFF) // Primary Accent (Apple Blue)
    val SparkleColor2 = Color(0xFF5AC8FA) // Secondary Accent (Apple Cyan)
    val SparkleColor3 = Color(0xFF30D158) // Success Color (Apple Green)

    val SparkleColor1Light = Color(0xFF007AFF) // Accent Primary (Light Mode)
    val SparkleColor2Light = Color(0xFF5AC8FA) // Accent Secondary (Light Mode)
}

// Custom modifier for simulating organic fluid scale deformation upon push interactions
fun Modifier.liquidPressEffect(
    intensity: Float = 0.95f,
    onClick: (() -> Unit)? = null
): Modifier = this.composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetScale = when {
        isPressed -> intensity
        isHovered -> PremiumMotion.hoverScale
        else -> 1f
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = PremiumMotion.bouncySpring(damping = 0.70f),
        label = "premium_interactive_scale"
    )

    val view = androidx.compose.ui.platform.LocalView.current

    this
        .hoverable(interactionSource)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = onClick != null,
            onClick = {
                try {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                } catch (e: Exception) {
                    // Fail gracefully on systems without hardware haptics
                }
                onClick?.invoke()
            }
        )
        .scale(animatedScale)
}

// Shimmer effect modifier for generating premium gloss waves on call-to-actions
fun Modifier.shimmer(): Modifier = this.composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )

    this.drawBehind {
        val width = size.width
        val height = size.height
        if (width > 0 && height > 0) {
            val shimmerBrush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.0f),
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.28f),
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.0f)
                ),
                start = Offset(translateAnim, 0f),
                end = Offset(translateAnim + 420f, height)
            )
            drawRect(brush = shimmerBrush)
        }
    }
}

// Authentic Crystallic Clear Liquid Glass Card Container
@Composable
fun GlassyCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    blurIntensity: Dp = 12.dp,
    cornerRadius: Dp = 24.dp,
    glowingAccent: Boolean = false,
    graphicsQualityHigh: Boolean = LocalPremiumGraphics.current,
    accentColors: List<Color> = listOf(Color(0xFF007AFF), Color(0xFF5AC8FA)),
    content: @Composable ColumnScope.() -> Unit
) {
    // Dynamic glare reflections that shift naturally (Physically accurate dynamic lighting)
    val infiniteTransition = if (graphicsQualityHigh) rememberInfiniteTransition(label = "glass_highlights") else null
    val highlightOffsetState = if (graphicsQualityHigh && infiniteTransition != null) {
        infiniteTransition.animateFloat(
            initialValue = -200f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "glass_glare"
        )
    } else {
        null
    }

    // Layer 4: Edge specular reflection lines mimicking physical glass refraction
    val borderBrush = if (isDark) {
        if (glowingAccent) {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.38f),
                    accentColors.first().copy(alpha = 0.70f),
                    accentColors.last().copy(alpha = 0.30f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.28f),
                    Color.White.copy(alpha = 0.05f),
                    Color.White.copy(alpha = 0.15f),
                    Color.White.copy(alpha = 0.03f)
                )
            )
        }
    } else {
        if (glowingAccent) {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),
                    accentColors.first().copy(alpha = 0.50f),
                    accentColors.last().copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.40f)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.98f),
                    Color(0xFFE2E8F0).copy(alpha = 0.55f),
                    Color.White.copy(alpha = 0.75f),
                    Color(0xFFCBD5E1).copy(alpha = 0.35f)
                )
            )
        }
    }

    // Layer 2: Semi-transparent material tint (backdrop blur handled by liquid glass)
    val shape = RoundedCornerShape(cornerRadius)
    val shadowColor1 = if (isDark) Color(0x4D000000) else Color(0x0C334155)
    val shadowColor2 = if (isDark) Color(0x24000000) else Color(0x04334155)

    Box(
        modifier = modifier
            .drawBehind {
                // Layer 5: Ambient Shadow Rendering (Physically elevated offsets)
                drawRoundRect(
                    color = shadowColor1,
                    topLeft = Offset(0f, if (graphicsQualityHigh) 14f else 8f),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                )
                if (graphicsQualityHigh) {
                    drawRoundRect(
                        color = shadowColor2,
                        topLeft = Offset(0f, 6f),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                    )
                }

                // Layer 6: Dynamic Lighting Aura (Bleeding back glow)
                if (glowingAccent && graphicsQualityHigh) {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(accentColors.first().copy(alpha = 0.18f), Color.Transparent),
                            radius = size.width * 0.85f,
                            center = Offset(size.width / 2f, size.height)
                        ),
                        topLeft = Offset(-12f, -12f),
                        size = Size(size.width + 24f, size.height + 24f),
                        cornerRadius = CornerRadius(cornerRadius.toPx() + 12f, cornerRadius.toPx() + 12f)
                    )
                }

                // Layer 3: Internal Highlight Gradient (Continuous orbital glare shifting)
                if (graphicsQualityHigh && highlightOffsetState != null) {
                    val highlightOffset = highlightOffsetState.value
                    val dynamicGlareBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = if (isDark) 0.06f else 0.14f),
                            Color.White.copy(alpha = if (isDark) 0.12f else 0.28f),
                            Color.White.copy(alpha = if (isDark) 0.06f else 0.14f),
                            Color.Transparent
                        ),
                        start = Offset(highlightOffset, 0f),
                        end = Offset(highlightOffset + 240f, size.height)
                    )
                    drawRoundRect(
                        brush = dynamicGlareBrush,
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                    )
                }

                // Layer 4: Edge overlay (Specular halo top outline)
                val topSheenBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.22f else 0.40f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height * 0.15f
                )
                drawRoundRect(
                    brush = topSheenBrush,
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                )
            }
            .clip(shape)
            .liquidGlassSurface(
                isDark = isDark,
                shape = shape,
                variant = if (glowingAccent) LiquidGlassVariant.Regular else LiquidGlassVariant.Clear,
                blurEnabled = graphicsQualityHigh,
                interactive = false,
                cornerRadius = cornerRadius,
                withBorder = false,
                glowingAccent = glowingAccent,
                accentColors = accentColors
            )
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = shape
            )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            content = content
        )
    }
}

// Responsive, Liquid Floating Glass Button
@Composable
fun GlassyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    enabled: Boolean = true,
    colors: List<Color> = if (isDark) {
        listOf(Color(0xFF007AFF), Color(0xFF5AC8FA)) // Premium Dark Accents
    } else {
        listOf(Color(0xFF007AFF), Color(0xFF5AC8FA)) // Premium Light Accents
    },
    content: @Composable RowScope.() -> Unit
) {
    val alphaAnim by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.5f,
        animationSpec = PremiumMotion.microTween(300),
        label = "button_alpha"
    )

    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .alpha(alphaAnim)
            .liquidPressEffect(intensity = PremiumMotion.pressScale) {
                if (enabled) onClick()
            }
            .clip(shape)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(colors.first().copy(alpha = 0.35f), Color.Transparent),
                        radius = size.width * 0.9f,
                        center = Offset(size.width / 2f, size.height)
                    ),
                    size = size,
                    cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
                )
            }
            .background(
                Brush.linearGradient(
                    colors = colors,
                    start = Offset(0f, 0f),
                    end = Offset(400f, 400f)
                )
            )
            .shimmer()
            .border(1.dp, Color.White.copy(alpha = 0.42f), shape)
            .padding(vertical = 14.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

// Background Visualizer with liquid glowing orbs for extra depth
@Composable
fun GlassyBackground(
    isDark: Boolean,
    modifier: Modifier = Modifier,
    graphicsQualityHigh: Boolean = true, // Performance controls
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = if (graphicsQualityHigh) rememberInfiniteTransition(label = "glowing_orbs") else null
    
    val orbOffset1 = if (graphicsQualityHigh && infiniteTransition != null) {
        infiniteTransition.animateValue(
            initialValue = Offset(80f, 120f),
            targetValue = Offset(880f, 750f),
            typeConverter = Offset.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(15000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orb_1"
        )
    } else {
        remember { mutableStateOf(Offset(280f, 350f)) }
    }

    val orbOffset2 = if (graphicsQualityHigh && infiniteTransition != null) {
        infiniteTransition.animateValue(
            initialValue = Offset(800f, 400f),
            targetValue = Offset(60f, 950f),
            typeConverter = Offset.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(19000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orb_2"
        )
    } else {
        remember { mutableStateOf(Offset(750f, 850f)) }
    }

    val orbOffset3 = if (graphicsQualityHigh && infiniteTransition != null) {
        infiniteTransition.animateValue(
            initialValue = Offset(180f, 950f),
            targetValue = Offset(900f, 150f),
            typeConverter = Offset.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(23000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orb_3"
        )
    } else {
        remember { mutableStateOf(Offset(120f, 900f)) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .hazeSource(
                state = LocalLiquidGlassHazeState.current,
                zIndex = 0f,
                key = "glass_background"
            )
            .drawBehind {
                // Background layer
                if (isDark) {
                    drawRect(brush = GlassTheme.DarkBackgroundGradient)
                    
                    // Liquid glowing orbs (Primary Accent & Secondary Accent) - only animate/glow brightly on high end
                    val alphaScale = if (graphicsQualityHigh) 1.0f else 0.45f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GlassTheme.SparkleColor1.copy(alpha = 0.22f * alphaScale), Color.Transparent),
                            radius = if (graphicsQualityHigh) 750f else 420f
                        ),
                        center = orbOffset1.value,
                        radius = if (graphicsQualityHigh) 700f else 400f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GlassTheme.SparkleColor2.copy(alpha = 0.18f * alphaScale), Color.Transparent),
                            radius = if (graphicsQualityHigh) 650f else 380f
                        ),
                        center = orbOffset2.value,
                        radius = if (graphicsQualityHigh) 600f else 350f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GlassTheme.SparkleColor3.copy(alpha = 0.15f * alphaScale), Color.Transparent),
                            radius = if (graphicsQualityHigh) 850f else 480f
                        ),
                        center = orbOffset3.value,
                        radius = if (graphicsQualityHigh) 800f else 450f
                    )
                } else {
                    drawRect(brush = GlassTheme.LightBackgroundGradient)
                    
                    val alphaScale = if (graphicsQualityHigh) 1.0f else 0.45f
                    // Liquid glowing orbs (Light Palette Accents)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GlassTheme.SparkleColor1Light.copy(alpha = 0.08f * alphaScale), Color.Transparent),
                            radius = if (graphicsQualityHigh) 700f else 400f
                        ),
                        center = orbOffset1.value,
                        radius = if (graphicsQualityHigh) 700f else 400f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GlassTheme.SparkleColor2Light.copy(alpha = 0.06f * alphaScale), Color.Transparent),
                            radius = if (graphicsQualityHigh) 600f else 350f
                        ),
                        center = orbOffset2.value,
                        radius = if (graphicsQualityHigh) 600f else 350f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF00D26A).copy(alpha = 0.05f * alphaScale), Color.Transparent),
                            radius = if (graphicsQualityHigh) 800f else 450f
                        ),
                        center = orbOffset3.value,
                        radius = if (graphicsQualityHigh) 800f else 450f
                    )
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(
                    state = LocalLiquidGlassHazeState.current,
                    zIndex = 1f,
                    key = "glass_content"
                )
        ) {
            content()
        }
    }
}

@Composable
fun ThemeToggleButton(
    viewModel: PromptViewModel,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    LiquidGlassCircleButton(
        onClick = {
            viewModel.useSystemTheme = false
            viewModel.isDarkTheme = !viewModel.isDarkTheme
            viewModel.showToast(if (viewModel.isDarkTheme) "Dark mode enabled 🌙" else "Light mode enabled ☀️")
        },
        icon = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
        contentDescription = "Toggle Theme",
        modifier = modifier.testTag("quick_theme_toggle_button"),
        diameter = 42.dp,
        tint = if (isDark) Color(0xFF007AFF) else Color(0xFF5AC8FA),
        isDark = isDark
    )
}
