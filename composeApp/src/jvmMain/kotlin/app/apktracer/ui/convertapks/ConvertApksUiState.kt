package app.apktracer.ui.convertapks

import io.github.vinceglb.filekit.PlatformFile

data class ConvertApksUiState(
    val selectedFolder: String? = null,
    val files: List<PlatformFile> = emptyList(),
    val isLoading: Boolean = false
)
