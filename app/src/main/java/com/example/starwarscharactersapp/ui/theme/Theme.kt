package com.example.starwarscharactersapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.starwarscharactersapp.data.local.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = StarWarsYellow,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color.Black,
    surface = PurpleGrey60,
    onPrimary = StarWarsBlack,
    onSecondary = Color.White,
    onTertiary = PurpleGrey80,
    onBackground = StarWarsYellow,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = OffWhiteBackground,
    surface = Purple80,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = PurpleGrey80,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun StarWarsCharactersAppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    isSplashVisible: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            // Splash always renders on a black background regardless of theme,
            // so it needs light status/nav bar icons even in Light mode.
            // This must be recomputed here whenever isSplashVisible flips
            val useLightIcons = isSplashVisible || darkTheme
            insetsController.isAppearanceLightStatusBars = !useLightIcons
            insetsController.isAppearanceLightNavigationBars = !useLightIcons
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}