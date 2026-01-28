package app.apktracer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.apktracer.ui.convertapks.ConvertApksScreen
import app.apktracer.ui.settings.SettingsScreen
import app.apktracer.ui.traceapks.TraceApksScreen

@Composable
fun AppNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.TraceApks
    ) {
        composable<AppDestination.TraceApks> {
            TraceApksScreen()
        }

        composable<AppDestination.ConvertApks> {
            ConvertApksScreen()
        }

        composable<AppDestination.Settings> {
            SettingsScreen()
        }
    }
}