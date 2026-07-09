package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

// Secure downloadable font provider configuration
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Global premium font instances
private val Poppins = GoogleFont("Poppins")
private val Montserrat = GoogleFont("Montserrat")

val HeadingFamily = FontFamily(
    Font(googleFont = Poppins, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = Poppins, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = Poppins, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = Poppins, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = Poppins, fontProvider = provider, weight = FontWeight.ExtraBold),
    Font(googleFont = Poppins, fontProvider = provider, weight = FontWeight.Black)
)

val BodyFamily = FontFamily(
    Font(googleFont = Montserrat, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = Montserrat, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = Montserrat, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = Montserrat, fontProvider = provider, weight = FontWeight.Bold)
)

// Precision-styled modern typography pairing
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = HeadingFamily,
        fontWeight = FontWeight.Black,
        fontSize = 42.sp,
        lineHeight = 48.sp,
        letterSpacing = (-1.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = HeadingFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-1).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = HeadingFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = HeadingFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.25).sp
    ),
    titleMedium = TextStyle(
        fontFamily = HeadingFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.25.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp
    )
)

