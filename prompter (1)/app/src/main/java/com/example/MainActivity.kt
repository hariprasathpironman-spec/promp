package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.PromptRepository
import com.example.ui.PromptViewModel
import com.example.ui.PromptViewModelFactory
import com.example.ui.Screen
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Translate
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.KeepNotesScreen
import com.example.ui.screens.TranslateScreen
import com.example.ui.theme.GlassyBackground
import com.example.ui.theme.GlassyCard
import com.example.ui.theme.LiquidGlassProvider
import com.example.ui.theme.PremiumMotion
import com.example.ui.theme.liquidGlassSurface
import com.example.ui.theme.LocalLiquidGlassHazeState
import dev.chrisbanes.haze.hazeSource
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.liquidPressEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.draw.scale
import androidx.compose.animation.togetherWith

class MainActivity : ComponentActivity() {

    // Lazy initialization of database and repository for optimal startup time
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "prompter_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    private val repository by lazy {
        PromptRepository(database.promptDao())
    }

    private val viewModel: PromptViewModel by viewModels {
        PromptViewModelFactory(repository)
    }

    private fun enableHighRefreshRate() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val display = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    display
                } else {
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay
                }
                
                val modes = display?.supportedModes
                val bestMode = modes?.maxByOrNull { it.refreshRate }
                
                val layoutParams = window.attributes
                if (bestMode != null && bestMode.refreshRate >= 120f) {
                    layoutParams.preferredDisplayModeId = bestMode.modeId
                }
                layoutParams.preferredRefreshRate = 144f
                window.attributes = layoutParams
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableHighRefreshRate()
        enableEdgeToEdge()
        setContent {
            // Theme reactivity orchestration
            val systemDark = isSystemInDarkTheme()
            val isDark = if (viewModel.useSystemTheme) systemDark else viewModel.isDarkTheme
            var isSplashLoading by rememberSaveable { mutableStateOf(true) }

            // Dynamic launch sync
            LaunchedEffect(viewModel.useSystemTheme) {
                if (viewModel.useSystemTheme) {
                    viewModel.isDarkTheme = systemDark
                }
            }

            MyApplicationTheme(darkTheme = isDark, dynamicColor = false) {
                LiquidGlassProvider(graphicsQualityHigh = viewModel.graphicsQualityHigh) {
                Crossfade(
                    targetState = isSplashLoading,
                    animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
                    label = "splash_crossfade"
                ) { loading ->
                    if (loading) {
                        SplashLoadingScreen(
                            isDark = isDark,
                            onLoadingComplete = { isSplashLoading = false }
                        )
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            contentWindowInsets = WindowInsets.navigationBars
                        ) { innerPadding ->
                            // Full-screen liquid-glass gradient background
                            GlassyBackground(isDark = isDark, graphicsQualityHigh = viewModel.graphicsQualityHigh) {
                                
                                // Active Content Router Screen Switcher
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    AnimatedContent(
                                        targetState = viewModel.activeScreen,
                                        transitionSpec = {
                                            (fadeIn(PremiumMotion.enterTween()) +
                                                slideInHorizontally(
                                                    animationSpec = PremiumMotion.enterTween(),
                                                    initialOffsetX = { it / 5 }
                                                ) +
                                                scaleIn(
                                                    initialScale = 0.96f,
                                                    animationSpec = PremiumMotion.enterTween()
                                                ))
                                                .togetherWith(
                                                    fadeOut(PremiumMotion.exitTween()) +
                                                        slideOutHorizontally(
                                                            animationSpec = PremiumMotion.exitTween(),
                                                            targetOffsetX = { -it / 6 }
                                                        ) +
                                                        scaleOut(
                                                            targetScale = 0.98f,
                                                            animationSpec = PremiumMotion.exitTween()
                                                        )
                                                )
                                        },
                                        label = "premium_screen_transition"
                                    ) { screen ->
                                        when (screen) {
                                            Screen.Home -> HomeScreen(viewModel = viewModel, isDark = isDark)
                                            Screen.Keep -> KeepNotesScreen(viewModel = viewModel, isDark = isDark)
                                            Screen.Translate -> TranslateScreen(viewModel = viewModel, isDark = isDark)
                                            Screen.History -> HistoryScreen(viewModel = viewModel, isDark = isDark)
                                            Screen.Favorites -> FavoritesScreen(viewModel = viewModel, isDark = isDark)
                                            Screen.Settings -> SettingsScreen(viewModel = viewModel, isDark = isDark)
                                        }
                                    }
                                }

                                // Floating Custom Frosted glass success/alert notifications
                                PositionedInAppToast(
                                    message = viewModel.toastMessage,
                                    isDark = isDark,
                                    onDismiss = { viewModel.toastMessage = null }
                                )

                                // Floating Translucent Bottom Glass Navigation bar
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(horizontal = 24.dp, vertical = 20.dp)
                                ) {
                                    BottomGlassNavigation(
                                        activeScreen = viewModel.activeScreen,
                                        isDark = isDark,
                                        graphicsQualityHigh = viewModel.graphicsQualityHigh,
                                        onTabSelected = { 
                                            viewModel.isGenerating = false
                                            viewModel.activeScreen = it 
                                        }
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

// In-app Premium Glass Toast Notification Banner Component
@Composable
fun PositionedInAppToast(
    message: String?,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (message != null) {
            LaunchedEffect(message) {
                delay(2500)
                onDismiss()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                GlassyCard(
                    isDark = isDark,
                    cornerRadius = 14.dp,
                    glowingAccent = true,
                    blurIntensity = 20.dp,
                    accentColors = listOf(Color(0xFF10A37F), Color(0xFF00E5FF)),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .testTag("glass_toast_message")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFF10A37F),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = message,
                            color = if (isDark) Color.White else Color(0xFF1E1B4B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// Custom Glass Bottom Navigation — LiquidGlassSwiftUI GlassEffectContainer style
@Composable
fun BottomGlassNavigation(
    activeScreen: Screen,
    isDark: Boolean,
    graphicsQualityHigh: Boolean = true,
    onTabSelected: (Screen) -> Unit
) {
    val hazeState = LocalLiquidGlassHazeState.current
    val navShape = RoundedCornerShape(24.dp)
    val shadowColor = if (isDark) Color(0x3B000000) else Color(0x10334155)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .drawBehind {
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(0f, 6f),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )
            }
            .clip(navShape)
            .hazeSource(state = hazeState, zIndex = 3f, key = "bottom_nav")
            .liquidGlassSurface(
                isDark = isDark,
                shape = navShape,
                blurEnabled = graphicsQualityHigh,
                interactive = false,
                cornerRadius = 24.dp
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidth = maxWidth
        val tabWidth = totalWidth / 6

        val targetIndex = when (activeScreen) {
            Screen.Home -> 0f
            Screen.Keep -> 1f
            Screen.Translate -> 2f
            Screen.History -> 3f
            Screen.Favorites -> 4f
            Screen.Settings -> 5f
        }

        // Sliding spring physics tab indicator
        val animatedIndex by animateFloatAsState(
            targetValue = targetIndex,
            animationSpec = PremiumMotion.bouncySpring(damping = 0.72f),
            label = "tab_slide"
        )

        // Liquid glass sliding capsule — iOS 26 tab indicator
        val pillShape = RoundedCornerShape(16.dp)
        Box(
            modifier = Modifier
                .width(tabWidth)
                .fillMaxHeight(0.82f)
                .offset(x = tabWidth * animatedIndex)
                .padding(4.dp)
                .drawBehind {
                    if (graphicsQualityHigh) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = if (isDark) {
                                    listOf(Color(0x4D007AFF), Color.Transparent)
                                } else {
                                    listOf(Color(0x33007AFF), Color.Transparent)
                                },
                                radius = size.width * 0.95f
                            ),
                            center = Offset(size.width / 2f, size.height / 2f)
                        )
                    }
                }
                .clip(pillShape)
                .liquidGlassSurface(
                    isDark = isDark,
                    shape = pillShape,
                    blurEnabled = graphicsQualityHigh,
                    interactive = false,
                    cornerRadius = 16.dp,
                    glowingAccent = true,
                    accentColors = listOf(Color(0xFF007AFF), Color(0xFF5AC8FA))
                )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationTabItem(
                screen = Screen.Home,
                label = "Home",
                icon = Icons.Default.Home,
                active = activeScreen == Screen.Home,
                isDark = isDark,
                onClick = { onTabSelected(Screen.Home) }
            )

            NavigationTabItem(
                screen = Screen.Keep,
                label = "Keep",
                icon = Icons.Default.PushPin,
                active = activeScreen == Screen.Keep,
                isDark = isDark,
                onClick = { onTabSelected(Screen.Keep) }
            )

            NavigationTabItem(
                screen = Screen.Translate,
                label = "Translate",
                icon = Icons.Default.Translate,
                active = activeScreen == Screen.Translate,
                isDark = isDark,
                onClick = { onTabSelected(Screen.Translate) }
            )

            NavigationTabItem(
                screen = Screen.History,
                label = "History",
                icon = Icons.Default.History,
                active = activeScreen == Screen.History,
                isDark = isDark,
                onClick = { onTabSelected(Screen.History) }
            )

            NavigationTabItem(
                screen = Screen.Favorites,
                label = "Favorites",
                icon = Icons.Default.Star,
                active = activeScreen == Screen.Favorites,
                isDark = isDark,
                onClick = { onTabSelected(Screen.Favorites) }
            )

            NavigationTabItem(
                screen = Screen.Settings,
                label = "Settings",
                icon = Icons.Default.Settings,
                active = activeScreen == Screen.Settings,
                isDark = isDark,
                onClick = { onTabSelected(Screen.Settings) }
            )
        }
    }
}

// Bottom Bar element detail with micro liquid-press feed animations
@Composable
fun RowScope.NavigationTabItem(
    screen: Screen,
    label: String,
    icon: ImageVector,
    active: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val activeColor = Color(0xFF007AFF)
    val inactiveColor = if (isDark) Color(0x6DFFFFFF) else Color(0x668E8E93)
    
    val currentTabColor = if (active) activeColor else inactiveColor

    // Animated spring-scale for luxury look & feel
    val iconScale by animateFloatAsState(
        targetValue = if (active) PremiumMotion.tabActiveScale else 1.0f,
        animationSpec = PremiumMotion.bouncySpring(),
        label = "tab_icon_scale"
    )

    val animatedTabColor by animateColorAsState(
        targetValue = currentTabColor,
        animationSpec = PremiumMotion.microTween(),
        label = "tab_color_anim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .liquidPressEffect(intensity = 0.90f) { onClick() }
            .padding(vertical = 8.dp)
            .testTag("nav_tab_${screen.name.lowercase()}"),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = animatedTabColor,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                }
        )
        
        Spacer(modifier = Modifier.height(3.dp))
        
        Text(
            text = label,
            fontSize = 11.sp,
            color = animatedTabColor,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun SplashLoadingScreen(
    isDark: Boolean,
    onLoadingComplete: () -> Unit
) {
    var startPercentage by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = startPercentage,
        animationSpec = tween(2400, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
        label = "progress_anim"
    )

    var activeLoadingText by remember { mutableStateOf("Initializing Prompter Engine...") }

    LaunchedEffect(Unit) {
        startPercentage = 1f
        delay(750)
        activeLoadingText = "Calibrating context metrics..."
        delay(850)
        activeLoadingText = "Polishing visual alignments..."
        delay(600)
        onLoadingComplete()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_and_rotate")
    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "logo_rotation"
    )

    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.93f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )

    // Reuse the exact same GlassyBackground to ensure visual continuity!
    GlassyBackground(
        isDark = isDark,
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_loading_screen"),
        graphicsQualityHigh = false // Force low overhead on initial cold boot
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Glowing orbital icon cluster
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(logoScale),
                    contentAlignment = Alignment.Center
                ) {
                    // outer neon halo
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = if (isDark) {
                                            listOf(Color(0x3D007AFF), Color.Transparent)
                                        } else {
                                            listOf(Color(0x2B007AFF), Color.Transparent)
                                        }
                                    )
                                )
                            }
                    )

                    // outer rotating dashed ring to simulate processing
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .graphicsLayer {
                                rotationZ = rotateAngle
                            }
                            .border(
                                width = 1.5.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFF007AFF).copy(alpha = 0.1f),
                                        Color(0xFF5AC8FA).copy(alpha = 0.8f),
                                        Color(0xFF007AFF).copy(alpha = 0.8f),
                                        Color(0xFF5AC8FA).copy(alpha = 0.1f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    // Inner core glassy card hosting the prompter intelligence spark (magic icon)
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = if (isDark) {
                                        listOf(Color(0xFF0F1324), Color(0xFF080B14))
                                    } else {
                                        listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9))
                                    }
                                )
                            )
                            .border(
                                1.dp,
                                if (isDark) Color(0x33007AFF) else Color(0x335AC8FA),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Prompter Intel",
                            tint = if (isDark) Color(0xFF007AFF) else Color(0xFF5AC8FA),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Text Title with brand naming
                Text(
                    text = "PROMPTER",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = if (isDark) Color.White else Color(0xFF111827)
                )

                Text(
                    text = "Next-Gen Prompt Orchestrator",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (isDark) Color(0x8CFFFFFF) else Color(0xFF6B7280)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Premium linear progress bar setup
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isDark) Color(0x1AFFFFFF) else Color(0x14000000))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        if (isDark) Color(0xFF007AFF) else Color(0xFF5AC8FA),
                                        if (isDark) Color(0xFF5AC8FA) else Color(0xFF007AFF)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Active loading status hint with high-end crossfade animation
                AnimatedContent(
                    targetState = activeLoadingText,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "loading_text_transition"
                ) { targetText ->
                    Text(
                        text = targetText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0x7AFFFFFF) else Color(0xFF6B7280)
                    )
                }
            }

            // High-End Premium Creator Signature Bottom Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "DESIGNED & DEVELOPED BY",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = if (isDark) Color(0x4DFFFFFF) else Color(0x736B7894)
                )
                Text(
                    text = "HARIPRASATH",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = if (isDark) Color(0xFF007AFF) else Color(0xFF5AC8FA)
                )
            }
        }
    }
}
