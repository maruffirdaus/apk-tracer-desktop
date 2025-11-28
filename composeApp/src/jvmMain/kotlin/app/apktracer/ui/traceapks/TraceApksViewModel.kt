package app.apktracer.ui.traceapks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.apktracer.common.model.Settings
import app.apktracer.common.type.ApkSource
import app.apktracer.common.type.Emulator
import app.apktracer.service.AndroZooService
import app.apktracer.service.SettingsService
import app.apktracer.service.StraceService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class TraceApksViewModel(
    private val settingsService: SettingsService,
    private val androZooService: AndroZooService,
    private val straceService: StraceService
) : ViewModel() {
    private val _uiState = MutableStateFlow(TraceApksUiState())
    val uiState = _uiState.asStateFlow()

    private lateinit var settings: Settings

    private var traceJob: Job? = null

    fun loadSettings() {
        viewModelScope.launch {
            settings = settingsService.getSettings()
            _uiState.update {
                it.copy(apkSource = settings.apkSource)
            }
        }
    }

    fun initStraceService() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Preparing ADB"
                )
            }
            straceService.init()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    loadingMessage = null
                )
            }
        }
    }

    fun changeSelectedFolder(selectedFolder: String?) {
        val apks = if (selectedFolder != null) {
            File(selectedFolder).listFiles().filter { it.isFile && it.extension == "apk" }
        } else {
            emptyList()
        }
        _uiState.update {
            it.copy(
                selectedFolder = selectedFolder,
                apks = apks
            )
        }
    }

    fun changeSelectedCsv(selectedCsv: String?) {
        val identifiers = if (selectedCsv != null) {
            File(selectedCsv).readLines().map {
                it.substringBefore(",").removePrefix("\uFEFF")
            }
        } else {
            emptyList()
        }
        _uiState.update {
            it.copy(
                selectedCsv = selectedCsv,
                apkIdentifiers = identifiers
            )
        }
    }

    fun startTrace() {
        traceJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isTracing = true)
            }

            if (settings.emulator == Emulator.AVD && settings.avdIni == null) {
                _uiState.update {
                    it.copy(
                        isTracing = false,
                        errorMessage = "No AVD INI selected."
                    )
                }
                return@launch
            }

            if (settings.apkSource == ApkSource.ANDRO_ZOO && settings.androZooApiKey.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        isTracing = false,
                        errorMessage = "No API key provided."
                    )
                }
                return@launch
            }

            if (settings.apkSource == ApkSource.LOCAL) {
                startLocalTrace()
            }

            if (settings.apkSource == ApkSource.ANDRO_ZOO) {
                startAndroZooTrace()
            }

            _uiState.update {
                it.copy(isTracing = false)
            }
        }
    }

    private suspend fun startLocalTrace() {
        for (apk in uiState.value.apks) {
            traceApk(apk)
        }
    }

    private suspend fun startAndroZooTrace() {
        settings.androZooApiKey?.let {
            for (sha256 in uiState.value.apkIdentifiers) {
                val apk = androZooService.downloadApk(it, sha256)
                if (apk != null) {
                    traceApk(apk)
                    apk.delete()
                }
            }
        }
    }

    private suspend fun traceApk(apk: File) {
        if (settings.emulator == Emulator.AVD) {
            if (settings.avdIni != null) {
                straceService.traceApk(
                    apk = apk,
                    avdIni = settings.avdIni,
                    outputDir = settings.outputDir,
                    timeout = settings.traceTimeout.duration,
                    emulatorLaunchWaitTime = settings.emulatorLaunchWaitTime.duration
                )
            }
        }

        if (settings.emulator == Emulator.LD_PLAYER) {
            straceService.traceApk(
                apk = apk,
                avdIni = null,
                outputDir = settings.outputDir,
                timeout = settings.traceTimeout.duration,
                emulatorLaunchWaitTime = settings.emulatorLaunchWaitTime.duration
            )
        }
    }

    fun stopTrace() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isStoppingTrace = true)
            }
            traceJob?.cancel()
            traceJob = null
            straceService.cleanupLeftovers()
            _uiState.update {
                it.copy(
                    isTracing = false,
                    isStoppingTrace = false
                )
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }
}