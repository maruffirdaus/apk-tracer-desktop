package app.apktracer.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.apktracer.common.type.ApkSource
import app.apktracer.common.type.Emulator
import app.apktracer.common.type.TraceTimeout
import app.apktracer.service.SettingsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsService: SettingsService
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(settings = settingsService.getSettings())
            }
        }
    }

    fun saveOutputDir(outputDir: String?) {
        viewModelScope.launch {
            if (outputDir != null) {
                settingsService.saveSettings(
                    settingsService.getSettings().copy(outputDir = outputDir)
                )
            }
            loadSettings()
        }
    }

    fun saveApkSource(ordinal: Int) {
        viewModelScope.launch {
            settingsService.saveSettings(
                settingsService.getSettings().copy(apkSource = ApkSource.entries[ordinal])
            )
            loadSettings()
        }
    }

    fun saveAndroZooApiKey(apiKey: String?) {
        viewModelScope.launch {
            settingsService.saveSettings(
                settingsService.getSettings().copy(androZooApiKey = apiKey)
            )
            loadSettings()
        }
    }

    fun saveEmulator(ordinal: Int) {
        viewModelScope.launch {
            settingsService.saveSettings(
                settingsService.getSettings().copy(emulator = Emulator.entries[ordinal])
            )
            loadSettings()
        }
    }

    fun saveAvdIni(avdIni: String?) {
        viewModelScope.launch {
            settingsService.saveSettings(settingsService.getSettings().copy(avdIni = avdIni))
            loadSettings()
        }
    }

    fun saveTraceTimeout(ordinal: Int) {
        viewModelScope.launch {
            settingsService.saveSettings(
                settingsService.getSettings().copy(traceTimeout = TraceTimeout.entries[ordinal])
            )
            loadSettings()
        }
    }
}