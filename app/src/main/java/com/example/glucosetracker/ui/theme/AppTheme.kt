package com.example.glucosetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val Background = Color(0xFFF8FAFE)
    val Card = Color(0xFFFFFFFF)
    val PrimaryGreen = Color(0xFF63C66D)
    val BlueAccent = Color(0xFF2F80ED)
    val TextDark = Color(0xFF06133D)
    val TextSecondary = Color(0xFF8A91A8)
    val Danger = Color(0xFFE85252)
    val HighGlucose = Color(0xFFF5C451)
}

private val AppLightColorScheme = lightColorScheme(
    primary = AppColors.PrimaryGreen,
    secondary = AppColors.BlueAccent,
    background = AppColors.Background,
    surface = AppColors.Card,
    error = AppColors.Danger,
    onPrimary = AppColors.Card,
    onSecondary = AppColors.Card,
    onBackground = AppColors.TextDark,
    onSurface = AppColors.TextDark,
    onError = AppColors.Card
)

@Composable
fun GlucoseTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppLightColorScheme,
        content = content
    )
}