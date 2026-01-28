package app.apktracer.ui.traceapks

import app.apktracer.common.model.ApkSource
import java.io.File

data class TraceApksUiState(
    val apkSource: ApkSource = ApkSource.LOCAL,
    val selectedFolder: String? = null,
    val apks: List<File> = emptyList(),
    val selectedCsv: String? = null,
    val apkIdentifiers: List<String> = emptyList(),
    val isTracing: Boolean = false,
    val isStoppingTrace: Boolean = false,
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
    val errorMessage: String? = null
)
