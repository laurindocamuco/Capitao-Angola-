package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = HeroRed,
    onPrimary = TextPrimary,
    primaryContainer = HeroRedDark,
    onPrimaryContainer = HeroYellowLight,
    secondary = HeroYellow,
    onSecondary = HeroBlack,
    secondaryContainer = HeroYellowDark,
    onSecondaryContainer = TextPrimary,
    tertiary = HeroGoldAura,
    background = HeroBlack,
    surface = HeroSurfaceDark,
    surfaceVariant = HeroCardDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = HeroBorderDark
)

private val LightColorScheme = DarkColorScheme // Modern dark hero aesthetic default

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = HeroBlack.toArgb()
                it.navigationBarColor = HeroBlack.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
