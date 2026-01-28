package app.apktracer.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.apktracer.common.model.ApkSource
import app.apktracer.common.model.CsvDelimiter
import app.apktracer.common.model.Emulator
import app.apktracer.common.model.EmulatorLaunchWaitTime
import app.apktracer.common.model.SettingsKey
import app.apktracer.common.model.TraceTimeout
import app.apktracer.service.SettingsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class SettingsViewModel(
    private val settingsService: SettingsService
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState
        .onStart { loadSettings() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            SettingsUiState()
        )

    private fun loadSettings() {
        _uiState.update {
            it.copy(
                outputDir = settingsService.getValue(SettingsKey.OutputDir),
                traceTimeout = settingsService.getValue(SettingsKey.TraceTimeout),
                apkSource = settingsService.getValue(SettingsKey.ApkSource),
                androZooApiKey = settingsService.getValue(SettingsKey.AndroZooApiKey),
                emulator = settingsService.getValue(SettingsKey.Emulator),
                avdIni = settingsService.getValue(SettingsKey.AvdIni),
                ldConsoleBinary = settingsService.getValue(SettingsKey.LdConsoleBinary),
                emulatorLaunchWaitTime = settingsService.getValue(SettingsKey.EmulatorLaunchWaitTime),
                csvDelimiter = settingsService.getValue(SettingsKey.CsvDelimiter)
            )
        }
    }

    fun saveOutputDir(outputDir: String?) {
        if (outputDir != null) {
            settingsService.setValue(SettingsKey.OutputDir, outputDir)
            _uiState.update {
                it.copy(outputDir = settingsService.getValue(SettingsKey.OutputDir))
            }
        }
    }

    fun saveTraceTimeout(ordinal: Int) {
        settingsService.setValue(SettingsKey.TraceTimeout, TraceTimeout.entries[ordinal])
        _uiState.update {
            it.copy(traceTimeout = settingsService.getValue(SettingsKey.TraceTimeout))
        }
    }

    fun saveApkSource(ordinal: Int) {
        settingsService.setValue(SettingsKey.ApkSource, ApkSource.entries[ordinal])
        _uiState.update {
            it.copy(apkSource = settingsService.getValue(SettingsKey.ApkSource))
        }
    }

    fun saveAndroZooApiKey(apiKey: String?) {
        settingsService.setValue(SettingsKey.AndroZooApiKey, apiKey)
        _uiState.update {
            it.copy(androZooApiKey = settingsService.getValue(SettingsKey.AndroZooApiKey))
        }
    }

    fun saveEmulator(ordinal: Int) {
        settingsService.setValue(SettingsKey.Emulator, Emulator.entries[ordinal])
        _uiState.update {
            it.copy(emulator = settingsService.getValue(SettingsKey.Emulator))
        }
    }

    fun saveAvdIni(avdIni: String) {
        settingsService.setValue(SettingsKey.AvdIni, avdIni)
        _uiState.update {
            it.copy(avdIni = settingsService.getValue(SettingsKey.AvdIni))
        }
    }

    fun saveLdConsoleBinary(ldConsoleBinary: String) {
        settingsService.setValue(SettingsKey.LdConsoleBinary, ldConsoleBinary)
        _uiState.update {
            it.copy(ldConsoleBinary = settingsService.getValue(SettingsKey.LdConsoleBinary))
        }
    }

    fun saveEmulatorLaunchWaitTime(ordinal: Int) {
        settingsService.setValue(
            SettingsKey.EmulatorLaunchWaitTime,
            EmulatorLaunchWaitTime.entries[ordinal]
        )
        _uiState.update {
            it.copy(emulatorLaunchWaitTime = settingsService.getValue(SettingsKey.EmulatorLaunchWaitTime))
        }
    }

    fun saveCsvDelimiter(ordinal: Int) {
        settingsService.setValue(SettingsKey.CsvDelimiter, CsvDelimiter.entries[ordinal])
        _uiState.update {
            it.copy(csvDelimiter = settingsService.getValue(SettingsKey.CsvDelimiter))
        }
    }
}