package app.apktracer.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Window
import androidx.navigation.compose.rememberNavController
import app.apktracer.ui.main.MainScreen
import app.apktracer.ui.navigation.AppNavHost
import app.apktracer.ui.theme.ApkTracerTheme
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.mayakapps.compose.windowstyler.WindowFrameStyle
import com.mayakapps.compose.windowstyler.WindowStyle
import io.github.composefluent.FluentTheme

@Composable
@Preview
fun App(
    onCloseRequest: () -> Unit
) {
    Window(
        onCloseRequest = onCloseRequest,
        title = "APK Tracer"
    ) {
        ApkTracerTheme {
            val windowColor = FluentTheme.colors.background.mica.base

            WindowStyle(
                isDarkTheme = isSystemInDarkTheme(),
                backdropType = WindowBackdrop.Solid(windowColor),
                frameStyle = WindowFrameStyle(
                    titleBarColor = windowColor
                )
            )

            val navController = rememberNavController()

            MainScreen(
                navHost = {
                    AppNavHost(navController)
                },
                navController = navController
            )
        }
    }
}