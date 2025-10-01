package app.apktracer.ui

import kotlinx.serialization.Serializable

@Serializable
sealed class AppDestination {
    @Serializable
    data object TraceApks : AppDestination()

    @Serializable
    data object ConvertApks : AppDestination()

    @Serializable
    data object Settings : AppDestination()
}