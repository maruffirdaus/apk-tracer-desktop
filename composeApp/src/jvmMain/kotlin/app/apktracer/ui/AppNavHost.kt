package app.apktracer.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.apktracer.ui.convertapks.ConvertApksScreen
import app.apktracer.ui.convertapks.ConvertApksViewModel
import app.apktracer.ui.settings.SettingsScreen
import app.apktracer.ui.settings.SettingsViewModel
import app.apktracer.ui.traceapks.TraceApksScreen
import app.apktracer.ui.traceapks.TraceApksViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.TraceApks
    ) {
        composable<AppDestination.TraceApks> {
            val viewModel = koinViewModel<TraceApksViewModel>()

            TraceApksScreen(
                viewModel = viewModel
            )
        }

        composable<AppDestination.ConvertApks> {
            val viewModel = koinViewModel<ConvertApksViewModel>()

            ConvertApksScreen(
                viewModel = viewModel
            )
        }

        composable<AppDestination.Settings> {
            val viewModel = koinViewModel<SettingsViewModel>()

            SettingsScreen(
                viewModel = viewModel
            )
        }
    }
}