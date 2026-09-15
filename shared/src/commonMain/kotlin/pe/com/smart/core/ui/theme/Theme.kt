package pe.com.smart.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(

    // ========================================================
    // PRIMARY
    // ========================================================

    primary = MedicalBlue,
    onPrimary = CardBackground,

    primaryContainer = MedicalBlueContainer,
    onPrimaryContainer = MedicalBlueDark,

    // ========================================================
    // SECONDARY
    // ========================================================

    secondary = TextSecondary,
    onSecondary = CardBackground,

    // ========================================================
    // BACKGROUND
    // ========================================================

    background = AppBackground,
    onBackground = TextPrimary,

    // ========================================================
    // SURFACES
    // ========================================================

    surface = CardBackground,
    onSurface = TextPrimary,

    surfaceVariant = InputBackground,
    onSurfaceVariant = TextSecondary,

    // ========================================================
    // BORDERS
    // ========================================================

    outline = BorderColor,
    outlineVariant = DividerColor,

    // ========================================================
    // ERROR
    // ========================================================

    error = ErrorRed,
    onError = CardBackground
)

private val DarkColorScheme = darkColorScheme(

    // ========================================================
    // PRIMARY
    // ========================================================

    primary = DarkPrimary,
    onPrimary = DarkBackground,

    primaryContainer = DarkSurface,
    onPrimaryContainer = DarkTextPrimary,

    // ========================================================
    // SECONDARY
    // ========================================================

    secondary = DarkTextSecondary,
    onSecondary = DarkBackground,

    // ========================================================
    // BACKGROUND
    // ========================================================

    background = DarkBackground,
    onBackground = DarkTextPrimary,

    // ========================================================
    // SURFACES
    // ========================================================

    surface = DarkSurface,
    onSurface = DarkTextPrimary,

    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextSecondary,

    // ========================================================
    // BORDERS
    // ========================================================

    outline = DarkBorder,
    outlineVariant = DarkBorder,

    // ========================================================
    // ERROR
    // ========================================================

    error = ErrorRed,
    onError = CardBackground
)

@Composable
fun SmartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}