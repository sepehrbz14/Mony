package com.tolou.mony.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = RoyalBlue,
    secondary = RoyalBlue,
    tertiary = AlertRed,
    background = CharcoalBlack,
    surface = CharcoalBlack,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = PureWhite,
    onBackground = PureWhite,
    onSurface = PureWhite,
    error = AlertRed,
    onError = PureWhite
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    secondary = RoyalBlue,
    tertiary = AlertRed,
    background = PureWhite,
    surface = PureWhite,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = PureWhite,
    onBackground = PureBlack,
    onSurface = PureBlack,
    error = AlertRed,
    onError = PureWhite
)

@Composable
fun MonyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
