package com.example.glucosetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppColors {
    val Background = Color(0xFFF8FAFE)
    val Card = Color(0xFFFFFFFF)
    val PrimaryGreen = Color(0xFF43C66F)
    val PrimaryGreenSoft = Color(0xFFEAF8EF)
    val BlueAccent = Color(0xFF2F80ED)
    val BlueAccentSoft = Color(0xFFEAF3FF)
    val TextDark = Color(0xFF06133D)
    val TextSecondary = Color(0xFF8A91A8)
    val TextMuted = Color(0xFFB6BCD0)
    val Danger = Color(0xFFE85252)
    val DangerSoft = Color(0xFFFFECEC)
    val HighGlucose = Color(0xFFF5C451)
    val HighGlucoseSoft = Color(0xFFFFF6D8)
    val Border = Color(0xFFE8EDF8)
    val Shadow = Color(0x1A1F3F77)
    val ChartGrid = Color(0xFFEFF3FB)
    val TargetRangeFill = Color(0x1F43C66F)
}

object AppDimens {
    val ScreenHorizontalPadding = 16.dp
    val CardRadius = 28.dp
    val SmallCardRadius = 24.dp
    val ChipRadius = 22.dp
    val SoftBorderWidth = 1.dp
    val CardElevation = 3.dp
    val BottomBarRadius = 28.dp
}

private val AppLightColorScheme = lightColorScheme(
    primary = AppColors.PrimaryGreen,
    secondary = AppColors.BlueAccent,
    background = AppColors.Background,
    surface = AppColors.Card,
    surfaceVariant = AppColors.Background,
    outline = AppColors.Border,
    error = AppColors.Danger,
    onPrimary = AppColors.Card,
    onSecondary = AppColors.Card,
    onBackground = AppColors.TextDark,
    onSurface = AppColors.TextDark,
    onSurfaceVariant = AppColors.TextSecondary,
    onError = AppColors.Card
)

private val AppShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(AppDimens.SmallCardRadius),
    large = androidx.compose.foundation.shape.RoundedCornerShape(AppDimens.CardRadius)
)

@Composable
fun GlucoseTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppLightColorScheme,
        typography = Typography(),
        shapes = AppShapes,
        content = content
    )
}