package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF007AFF),      // Apple Primary Accent #007AFF
    secondary = Color(0xFF5AC8FA),    // Apple Secondary Accent #5AC8FA
    tertiary = Color(0xFF30D158),     // Success Color #30D158
    background = Color(0xFF000000),   // Dark background #000000
    surface = Color(0x14FFFFFF),      // Glass Surface rgba(255,255,255,0.08)
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFFFFFFFF), // Text Primary
    onSurface = Color(0xFF8E8E93)     // Text Secondary #8E8E93
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007AFF),      // Apple Primary Accent #007AFF
    secondary = Color(0xFF5AC8FA),    // Apple Secondary Accent #5AC8FA
    tertiary = Color(0xFF30D158),     // Success Color #30D158
    background = Color(0xFFF6F7FB),   // Premium light background #F6F7FB
    surface = Color(0xB3FFFFFF),      // Glass Surface rgba(255,255,255,0.70)
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF111827), // Text Primary Light
    onSurface = Color(0xFF6B7280)     // Text Secondary Light
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to show our custom luxury colors
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
