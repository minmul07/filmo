package com.filmo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FilmoPrimary50,
    onPrimary = FilmoPrimary900,
    primaryContainer = FilmoPrimary800,
    onPrimaryContainer = FilmoPrimary50,
    secondary = FilmoSecondary200,
    onSecondary = FilmoSecondary900,
    secondaryContainer = FilmoSecondary800,
    onSecondaryContainer = FilmoSecondary50,
    tertiary = FilmoPointOrange,
    onTertiary = FilmoPrimary900,
    background = FilmoPrimary900,
    onBackground = FilmoPrimary50,
    surface = FilmoPrimary800,
    onSurface = FilmoPrimary50,
    surfaceVariant = FilmoPrimary700,
    onSurfaceVariant = FilmoPrimary200,
    outline = FilmoPrimary500,
    outlineVariant = FilmoPrimary600,
    error = FilmoPointRed,
    onError = FilmoWhite
)

private val LightColorScheme = lightColorScheme(
    primary = FilmoPrimary900,
    onPrimary = FilmoWhite,
    primaryContainer = FilmoPrimary50,
    onPrimaryContainer = FilmoPrimary900,
    secondary = FilmoSecondary500,
    onSecondary = FilmoWhite,
    secondaryContainer = FilmoSecondary50,
    onSecondaryContainer = FilmoSecondary900,
    tertiary = FilmoPointOrange,
    onTertiary = FilmoPrimary900,
    tertiaryContainer = FilmoPointYellow,
    onTertiaryContainer = FilmoPrimary900,
    background = FilmoWhite,
    onBackground = FilmoTextBlack,
    surface = FilmoWhite,
    onSurface = FilmoTextBlack,
    surfaceVariant = FilmoGray100,
    onSurfaceVariant = FilmoPrimary400,
    outline = FilmoPrimary100,
    outlineVariant = FilmoGray200,
    error = FilmoPointRed,
    onError = FilmoWhite
)

@Composable
fun FilmoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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

    CompositionLocalProvider(LocalFilmoSpacing provides FilmoSpacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
