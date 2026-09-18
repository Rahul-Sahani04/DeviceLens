package com.devicelens.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

private val DeviceLensScheme = darkColorScheme(
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceRaised,
    primary = PrimaryGreen,
    secondary = AccentBlue,
    tertiary = Warning,
    error = Danger,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = Divider
)

@Composable
fun DeviceLensTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DeviceLensScheme,
        typography = MaterialTheme.typography.copy(
            // Numeric metrics should feel mono/instrument-like
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace)
        ),
        content = content
    )
}
