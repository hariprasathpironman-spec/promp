package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState

/** iOS 26 liquid glass material variants. */
enum class LiquidGlassVariant {
    /** `.glassEffect(.clear)` — ultra-transparent, max blur */
    Clear,
    /** `.glassEffect(.regular)` — balanced depth */
    Regular
}

val LocalLiquidGlassHazeState = compositionLocalOf<HazeState> {
    error("LiquidGlassHazeState not provided. Wrap content in LiquidGlassProvider.")
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun LiquidGlassProvider(
    graphicsQualityHigh: Boolean = true,
    content: @Composable () -> Unit
) {
    val hazeState = rememberHazeState(blurEnabled = graphicsQualityHigh)
    CompositionLocalProvider(
        LocalLiquidGlassHazeState provides hazeState,
        LocalPremiumGraphics provides graphicsQualityHigh
    ) {
        content()
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun LiquidGlassContainer(
    modifier: Modifier = Modifier,
    spacing: Dp = 16.dp,
    isDark: Boolean = true,
    cornerRadius: Dp = 24.dp,
    variant: LiquidGlassVariant = LiquidGlassVariant.Clear,
    content: @Composable ColumnScope.() -> Unit
) {
    val hazeState = LocalLiquidGlassHazeState.current
    val blurEnabled = LocalPremiumGraphics.current
    val shape = RoundedCornerShape(cornerRadius)

    Column(
        modifier = modifier
            .clip(shape)
            .hazeSource(state = hazeState, zIndex = 2f, key = "liquid_glass_container")
            .liquidGlassSurface(
                isDark = isDark,
                shape = shape,
                variant = variant,
                blurEnabled = blurEnabled,
                interactive = true,
                cornerRadius = cornerRadius
            )
            .padding(spacing),
        content = content
    )
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun Modifier.liquidGlassSurface(
    isDark: Boolean,
    shape: Shape = RoundedCornerShape(24.dp),
    variant: LiquidGlassVariant = LiquidGlassVariant.Clear,
    blurEnabled: Boolean = LocalPremiumGraphics.current,
    interactive: Boolean = true,
    cornerRadius: Dp = 24.dp,
    withBorder: Boolean = true,
    glowingAccent: Boolean = false,
    accentColors: List<Color> = listOf(Color(0xFF007AFF), Color(0xFF5AC8FA))
): Modifier {
    val hazeState = LocalLiquidGlassHazeState.current

    val containerColor = when {
        isDark && variant == LiquidGlassVariant.Clear -> Color(0xFFFFFFFF).copy(alpha = 0.06f)
        isDark -> Color(0xFFFFFFFF).copy(alpha = 0.10f)
        variant == LiquidGlassVariant.Clear -> Color(0xFFFFFFFF).copy(alpha = 0.65f)
        else -> Color(0xFFFFFFFF).copy(alpha = 0.78f)
    }

    val materialStyle = when (variant) {
        LiquidGlassVariant.Clear -> CupertinoMaterials.ultraThin(containerColor = containerColor.value)
        LiquidGlassVariant.Regular -> CupertinoMaterials.thin(containerColor = containerColor.value)
    }

    val borderBrush = when {
        glowingAccent -> Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.40f else 0.90f),
                accentColors.first().copy(alpha = if (isDark) 0.65f else 0.45f),
                accentColors.last().copy(alpha = if (isDark) 0.28f else 0.30f),
                Color.White.copy(alpha = if (isDark) 0.10f else 0.35f)
            )
        )
        isDark -> Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.38f),
                Color.White.copy(alpha = 0.05f),
                Color(0xFF5AC8FA).copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.16f)
            )
        )
        else -> Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.98f),
                Color(0xFFE2E8F0).copy(alpha = 0.50f),
                Color(0xFF007AFF).copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.72f)
            )
        )
    }

    var result = this
        .hazeEffect(state = hazeState, style = materialStyle) {
            this.blurEnabled = blurEnabled
            blurRadius = if (variant == LiquidGlassVariant.Clear) 32.dp else 24.dp
            noiseFactor = if (blurEnabled) 0.10f else 0f
            alpha = if (blurEnabled) 1f else 0.94f
        }
        .drawBehind {
            val radius = cornerRadius.toPx()

            if (glowingAccent) {
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColors.first().copy(alpha = if (isDark) 0.20f else 0.10f),
                            Color.Transparent
                        ),
                        radius = size.width * 0.9f,
                        center = Offset(size.width / 2f, size.height)
                    ),
                    topLeft = Offset(-8f, -8f),
                    size = androidx.compose.ui.geometry.Size(size.width + 16f, size.height + 16f),
                    cornerRadius = CornerRadius(radius + 8f, radius + 8f)
                )
            }

            // Specular top sheen — liquid glass hallmark
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.26f else 0.45f),
                        Color.White.copy(alpha = if (isDark) 0.06f else 0.12f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height * 0.35f
                ),
                size = size,
                cornerRadius = CornerRadius(radius, radius)
            )

            // Edge refraction rim
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF5AC8FA).copy(alpha = if (isDark) 0.06f else 0.04f),
                        Color.Transparent
                    ),
                    center = Offset(size.width, 0f),
                    radius = size.width * 0.55f
                ),
                size = size,
                cornerRadius = CornerRadius(radius, radius)
            )
        }

    if (withBorder) {
        result = result.border(width = 1.dp, brush = borderBrush, shape = shape)
    }

    if (interactive) {
        result = result.liquidPressEffect(intensity = PremiumMotion.pressScale)
    }

    return result
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun LiquidGlassCircleButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    diameter: Dp = 48.dp,
    tint: Color = Color.White,
    isDark: Boolean = true,
    variant: LiquidGlassVariant = LiquidGlassVariant.Clear
) {
    val hazeState = LocalLiquidGlassHazeState.current
    val blurEnabled = LocalPremiumGraphics.current

    Box(
        modifier = modifier
            .size(diameter)
            .clip(CircleShape)
            .hazeSource(state = hazeState, zIndex = 3f)
            .liquidGlassSurface(
                isDark = isDark,
                shape = CircleShape,
                variant = variant,
                blurEnabled = blurEnabled,
                interactive = false,
                cornerRadius = diameter / 2
            )
            .liquidPressEffect(intensity = 0.90f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(diameter * 0.44f)
        )
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun LiquidGlassIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    tint: Color = if (isDark) Color.White else Color(0xFF1E1B4B),
    cornerRadius: Dp = 14.dp
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .liquidGlassSurface(
                isDark = isDark,
                shape = shape,
                interactive = false,
                cornerRadius = cornerRadius
            )
            .liquidPressEffect(intensity = 0.90f, onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun LiquidGlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    emoji: String? = null,
    selectedGradient: List<Color> = listOf(Color(0xFF007AFF), Color(0xFF5AC8FA))
) {
    val shape = RoundedCornerShape(22.dp)
    val scale by animateFloatAsState(
        targetValue = if (selected) PremiumMotion.chipSelectedScale else 1f,
        animationSpec = PremiumMotion.bouncySpring(),
        label = "chip_scale"
    )
    val textColor by animateColorAsState(
        targetValue = when {
            selected -> Color.White
            isDark -> Color(0xD9FFFFFF)
            else -> Color(0xFF4B5563)
        },
        animationSpec = PremiumMotion.microTween(),
        label = "chip_text"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .then(
                if (selected) {
                    Modifier
                        .background(Brush.linearGradient(selectedGradient))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), shape)
                } else {
                    Modifier.liquidGlassSurface(
                        isDark = isDark,
                        shape = shape,
                        interactive = false,
                        cornerRadius = 22.dp
                    )
                }
            )
            .liquidPressEffect(intensity = 0.93f, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        if (emoji != null) {
            Text(text = emoji, fontSize = 14.sp, modifier = Modifier.padding(end = 6.dp))
        }
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

/** Dimmed liquid-glass scrim for sheets and overlays. */
@Composable
fun LiquidGlassScrim(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    onDismiss: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .background(
                if (isDark) Color.Black.copy(alpha = 0.55f) else Color(0xFF0F172A).copy(alpha = 0.40f)
            )
            .liquidPressEffect(intensity = 1f, onClick = onDismiss)
    )
}

@Composable
fun Modifier.liquidGlassSource(
    zIndex: Float = 1f,
    key: String? = null
): Modifier {
    val hazeState = LocalLiquidGlassHazeState.current
    return if (key != null) {
        hazeSource(state = hazeState, zIndex = zIndex, key = key)
    } else {
        hazeSource(state = hazeState, zIndex = zIndex)
    }
}

@Composable
fun Modifier.premiumListEnter(
    visible: Boolean,
    index: Int = 0
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.88f,
        animationSpec = PremiumMotion.bouncySpring(
            damping = 0.65f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "list_enter_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 380 + index * PremiumMotion.listItemStaggerMs,
            easing = PremiumMotion.easeOutExpo
        ),
        label = "list_enter_alpha"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 24f,
        animationSpec = PremiumMotion.enterTween(400 + index * PremiumMotion.listItemStaggerMs),
        label = "list_enter_y"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
        translationY = offsetY
    }
}

@Composable
fun LiquidGlassLayer(
    modifier: Modifier = Modifier,
    zIndex: Float = 0f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.liquidGlassSource(zIndex = zIndex),
        content = content
    )
}
