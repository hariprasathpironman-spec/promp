package com.example.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.compositionLocalOf

/** iOS 26–inspired motion tokens — crisp, fluid, premium. */
object PremiumMotion {
    val easeOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val easeInOutSoft = CubicBezierEasing(0.45f, 0f, 0.15f, 1f)
    val easeSpringSnap = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

    fun <T> gentleSpring(
        damping: Float = 0.72f,
        stiffness: Float = Spring.StiffnessMediumLow
    ): SpringSpec<T> = spring(dampingRatio = damping, stiffness = stiffness)

    fun <T> bouncySpring(
        damping: Float = 0.58f,
        stiffness: Float = Spring.StiffnessMedium
    ): SpringSpec<T> = spring(dampingRatio = damping, stiffness = stiffness)

    fun <T> snappySpring(
        damping: Float = 0.82f,
        stiffness: Float = Spring.StiffnessMediumHigh
    ): SpringSpec<T> = spring(dampingRatio = damping, stiffness = stiffness)

    fun <T> enterTween(duration: Int = 420): TweenSpec<T> =
        tween(durationMillis = duration, easing = easeOutExpo)

    fun <T> exitTween(duration: Int = 280): TweenSpec<T> =
        tween(durationMillis = duration, easing = easeInOutSoft)

    fun <T> microTween(duration: Int = 220): TweenSpec<T> =
        tween(durationMillis = duration, easing = easeOutExpo)

    const val pressScale = 0.94f
    const val hoverScale = 1.04f
    const val tabActiveScale = 1.24f
    const val chipSelectedScale = 1.06f
    const val listItemStaggerMs = 55
}

val LocalPremiumGraphics = compositionLocalOf { true }
