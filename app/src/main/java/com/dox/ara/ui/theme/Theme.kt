package com.dox.ara.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val darkColorScheme = darkColorScheme(
    primary = AccentColorDark,
    secondary = ChatBubbleSenderColorDark,
    tertiary = BarsColorDark,
    surfaceVariant = ChatBubbleReceiverColorDark,
    background = BackgroundColorDark,

    onPrimary = BarsColorDark,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onSurfaceVariant =Color.White,
    onBackground = Color.White,
)

private val lightColorScheme = lightColorScheme(
    primary = AccentColorLight,
    secondary = ChatBubbleSenderColorLight,
    tertiary = BarsColorLight,
    surfaceVariant = ChatBubbleReceiverColorLight,
    background = BackgroundColorLight,

    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onSurfaceVariant =Color.White,
    onBackground = Color.Black,
)

@Composable
fun ARATheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if(darkTheme) darkColorScheme else lightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        val systemUIController = rememberSystemUiController()
        val statusBarColor = colorScheme.tertiary
        val navigationBarColor = colorScheme.tertiary

        SideEffect {
            systemUIController.setStatusBarColor(statusBarColor)
            systemUIController.setNavigationBarColor(navigationBarColor)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}