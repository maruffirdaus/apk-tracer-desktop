package app.apktracer.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Window
import androidx.navigation.compose.rememberNavController
import app.apktracer.ui.main.MainScreen
import app.apktracer.ui.navigation.AppNavHost
import app.apktracer.ui.theme.ApkTracerTheme

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