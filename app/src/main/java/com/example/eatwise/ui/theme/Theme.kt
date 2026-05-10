package com.example.eatwise.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    primaryContainer = GreenSoft,
    secondary = OrangePrimary,
    secondaryContainer = OrangeSoft,
    background = SurfaceWarm,
    surface = Color.White,
    surfaceVariant = SurfaceMuted,
    outline = LineSoft,
    error = RedPrimary,
    errorContainer = RedSoft,
    onPrimary = Color.White,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(28.dp),
)

@Composable
fun EatWiseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
