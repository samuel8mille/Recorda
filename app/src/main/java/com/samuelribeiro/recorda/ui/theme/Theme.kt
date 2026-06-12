package com.samuelribeiro.recorda.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VioletBright,
    onPrimary = OnVioletDark,
    primaryContainer = VioletContainerDark,
    onPrimaryContainer = VioletContainerLight,
    secondary = CyanBright,
    onSecondary = OnCyanDark,
    secondaryContainer = CyanContainerDark,
    onSecondaryContainer = CyanContainerLight,
    tertiary = AmberBright,
    onTertiary = OnAmberDark,
    tertiaryContainer = AmberContainerDark,
    onTertiaryContainer = AmberContainerLight,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = BackgroundDark,
    onSurface = OnBackgroundDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricViolet,
    onPrimary = Color.White,
    primaryContainer = VioletContainerLight,
    onPrimaryContainer = OnVioletContainerLight,
    secondary = DeepCyan,
    onSecondary = Color.White,
    secondaryContainer = CyanContainerLight,
    onSecondaryContainer = OnCyanContainerLight,
    tertiary = VibrantAmber,
    onTertiary = OnAmber,
    tertiaryContainer = AmberContainerLight,
    onTertiaryContainer = OnAmberContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = BackgroundLight,
    onSurface = OnBackgroundLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
)

/**
 * Recorda's Material 3 theme with the "Foco Elétrico" brand palette.
 *
 * @param darkTheme Whether to use the dark color scheme.
 * @param dynamicColor When `true`, Android 12+ wallpaper-based colors override the brand
 * palette. Disabled by default so the vibrant brand identity applies on every device.
 * @param content The app UI.
 */
@Composable
fun RecordaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
