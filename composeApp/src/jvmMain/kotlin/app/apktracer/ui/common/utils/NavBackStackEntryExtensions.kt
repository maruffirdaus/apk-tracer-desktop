package app.apktracer.ui.common.utils

import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute
import app.apktracer.ui.AppDestination
import java.lang.Exception

fun NavBackStackEntry.toAppDestination(): AppDestination? {
    return try {
        when {
            destination.route?.contains("TraceApks") == true -> {
                toRoute<AppDestination.TraceApks>()
            }

            destination.route?.contains("ConvertApks") == true -> {
                toRoute<AppDestination.ConvertApks>()
            }

            destination.route?.contains("Settings") == true -> {
                toRoute<AppDestination.Settings>()
            }

            else -> null
        }
    } catch (_: Exception) {
        null
    }
}