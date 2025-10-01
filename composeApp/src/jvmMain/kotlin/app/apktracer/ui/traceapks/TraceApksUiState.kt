package app.apktracer.ui.traceapks

import io.github.vinceglb.filekit.PlatformFile

data class TraceApksUiState(
    val selectedFolder: String? = null,
    val apks: List<PlatformFile> = emptyList(),
    val isTracing: Boolean = false
)
