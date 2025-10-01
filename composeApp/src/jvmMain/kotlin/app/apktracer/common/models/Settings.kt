package app.apktracer.common.models

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val outputDir: String? = null,
    val avdIni: String? = null
)
