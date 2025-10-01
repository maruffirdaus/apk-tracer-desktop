package app.apktracer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.composefluent.FluentTheme
import io.github.composefluent.background.Mica
import io.github.composefluent.darkColors
import io.github.composefluent.lightColors

@Composable
fun ApkTracerTheme(
    content: @Composable () -> Unit
) {
    FluentTheme(if (isSystemInDarkTheme()) darkColors() else lightColors()) {
        Mica(Modifier.fillMaxSize()) {
            content()
        }
    }
}