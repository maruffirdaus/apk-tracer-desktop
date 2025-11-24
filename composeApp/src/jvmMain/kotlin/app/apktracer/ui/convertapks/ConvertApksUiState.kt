package app.apktracer.ui.convertapks

import java.io.File

data class ConvertApksUiState(
    val selectedFolder: String? = null,
    val files: List<File> = emptyList(),
    val isLoading: Boolean = false
)
