package com.example.ui.theme

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

private val DarkColorScheme =
    darkColorScheme(
        primary = GeoDarkPrimary,
        onPrimary = GeoDarkOnPrimary,
        primaryContainer = GeoDarkPrimaryContainer,
        onPrimaryContainer = GeoDarkOnPrimaryContainer,
        background = Color(0xFF1B1B1F),
        surface = Color(0xFF1B1B1F),
        onBackground = Color(0xFFE3E2E6),
        onSurface = Color(0xFFE3E2E6)
    )

private val LightColorScheme =
    lightColorScheme(
        primary = GeoPrimary,
        onPrimary = GeoOnPrimary,
        primaryContainer = GeoPrimaryContainer,
        onPrimaryContainer = GeoOnPrimaryContainer,
        secondary = GeoSecondary,
        secondaryContainer = GeoSecondaryContainer,
        onSecondaryContainer = GeoOnSecondaryContainer,
        tertiary = GeoTertiary,
        tertiaryContainer = GeoTertiaryContainer,
        onTertiaryContainer = GeoOnTertiaryContainer,
        background = GeoBackground,
        onBackground = GeoOnBackground,
        surface = GeoSurface,
        onSurface = GeoOnSurface,
        surfaceVariant = GeoSurfaceVariant,
        onSurfaceVariant = GeoOnSurfaceVariant,
        outline = GeoOutline
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
