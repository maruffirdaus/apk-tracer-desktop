package app.apktracer.ui.convertapks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.parent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class ConvertApksViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ConvertApksUiState())
    val uiState = _uiState.asStateFlow()

    fun changeSelectedFolder(selectedFolder: String?) {
        val files = if (selectedFolder != null) {
            PlatformFile(selectedFolder).list().filter { it.isRegularFile() }
        } else {
            emptyList()
        }
        _uiState.update {
            it.copy(
                selectedFolder = selectedFolder,
                files = files
            )
        }
    }

    fun convertApks() {
        if (uiState.value.files.isNotEmpty()) {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(isLoading = true)
                }
                uiState.value.files.forEach { file ->
                    file.file.renameTo(
                        File(
                            file.parent()?.file,
                            file.nameWithoutExtension + ".apk"
                        )
                    )
                }
                uiState.value.selectedFolder.let { selectedFolder ->
                    val files = if (selectedFolder != null) {
                        PlatformFile(selectedFolder).list().filter { it.isRegularFile() }
                    } else {
                        emptyList()
                    }
                    _uiState.update {
                        it.copy(files = files)
                    }
                }
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }
}