package app.apktracer.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.apktracer.common.models.Settings
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class SettingsViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadSettings() {
        viewModelScope.launch {
            val settingsJson = PlatformFile(FileKit.filesDir, "settings.json").let {
                if (it.exists()) {
                    it.readString()
                } else {
                    null
                }
            }
            if (settingsJson != null) {
                val settings = Json.decodeFromString<Settings>(settingsJson)
                _uiState.update {
                    it.copy(settings = settings)
                }
            }
        }
    }

    fun saveOutputDir(outputDir: String?) {
        viewModelScope.launch {
            val settings = uiState.value.settings.copy(outputDir = outputDir)
            PlatformFile(
                FileKit.filesDir,
                "settings.json"
            ).writeString(Json.encodeToString(settings))
            _uiState.update {
                it.copy(settings = settings)
            }
        }
    }

    fun saveLdPlayerSelected(isSelected: Boolean) {
        viewModelScope.launch {
            val settings = uiState.value.settings.copy(ldPlayerSelected = isSelected)
            PlatformFile(
                FileKit.filesDir,
                "settings.json"
            ).writeString(Json.encodeToString(settings))
            _uiState.update {
                it.copy(settings = settings)
            }
        }
    }

    fun saveAvdIni(avdIni: String?) {
        viewModelScope.launch {
            val settings = uiState.value.settings.copy(avdIni = avdIni)
            PlatformFile(
                FileKit.filesDir,
                "settings.json"
            ).writeString(Json.encodeToString(settings))
            _uiState.update {
                it.copy(settings = settings)
            }
        }
    }
}