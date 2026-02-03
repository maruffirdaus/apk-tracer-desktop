package app.apktracer.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import app.apktracer.ui.main.MainScreen
import app.apktracer.ui.navigation.AppNavHost
import app.apktracer.ui.theme.ApkTracerTheme

@Composable
fun App() {
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