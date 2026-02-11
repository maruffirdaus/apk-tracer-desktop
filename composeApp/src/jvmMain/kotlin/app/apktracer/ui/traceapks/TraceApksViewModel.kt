package app.apktracer.ui.traceapks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.apktracer.common.model.ApkSource
import app.apktracer.common.model.CsvDelimiter
import app.apktracer.common.model.Emulator
import app.apktracer.common.model.EmulatorLaunchWaitTime
import app.apktracer.common.model.SettingsKey
import app.apktracer.common.model.TraceTimeout
import app.apktracer.service.AndroZooService
import app.apktracer.service.SettingsService
import app.apktracer.service.StraceService
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.name

class TraceApksViewModel(
    private val settingsService: SettingsService,
    private val androZooService: AndroZooService,
    private val straceService: StraceService
) : ViewModel() {
    private val _uiState = MutableStateFlow(TraceApksUiState())
    val uiState = _uiState
        .onStart {
            loadSettings()
            initStraceService()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            TraceApksUiState()
        )

    private lateinit var outputDir: String
    private lateinit var traceTimeout: TraceTimeout
    private lateinit var apkSource: ApkSource
    private var androZooApiKey: String? = null
    private lateinit var emulator: Emulator
    private var avdIni: String? = null
    private lateinit var ldConsoleBinary: String
    private lateinit var emulatorLaunchWaitTime: EmulatorLaunchWaitTime
    private lateinit var csvDelimiter: CsvDelimiter

    private var traceStartTime: String? = null
    private var traceJob: Job? = null

    private fun loadSettings() {
        outputDir = settingsService.getValue(SettingsKey.OutputDir)
        traceTimeout = settingsService.getValue(SettingsKey.TraceTimeout)
        apkSource = settingsService.getValue(SettingsKey.ApkSource)
        androZooApiKey = settingsService.getValue(SettingsKey.AndroZooApiKey)
        emulator = settingsService.getValue(SettingsKey.Emulator)
        avdIni = settingsService.getValue(SettingsKey.AvdIni)
        ldConsoleBinary = settingsService.getValue(SettingsKey.LdConsoleBinary)
        emulatorLaunchWaitTime = settingsService.getValue(SettingsKey.EmulatorLaunchWaitTime)
        csvDelimiter = settingsService.getValue(SettingsKey.CsvDelimiter)

        _uiState.update {
            it.copy(apkSource = apkSource)
        }
    }

    private fun initStraceService() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Preparing ADB"
                )
            }
            straceService.init(ldConsoleBinary)
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
            csvReader { delimiter = csvDelimiter.value }.open(selectedCsv) {
                readAllAsSequence()
                    .mapNotNull { row -> row.first().takeIf { it.isNotBlank() } }
                    .toList()
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

    private fun getCurrentTime(): String =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))

    fun startTrace() {
        traceJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isTracing = true)
            }

            if (emulator == Emulator.AVD && avdIni == null) {
                _uiState.update {
                    it.copy(
                        isTracing = false,
                        errorMessage = "No AVD INI selected."
                    )
                }
                return@launch
            }

            if (apkSource == ApkSource.ANDRO_ZOO && androZooApiKey.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        isTracing = false,
                        errorMessage = "No API key provided."
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    completedTraceCount = 0,
                    failedTraceCount = 0
                )
            }

            if (apkSource == ApkSource.LOCAL) {
                _uiState.update {
                    it.copy(totalTraceCount = it.apks.size)
                }
                startLocalTrace()
            }

            if (apkSource == ApkSource.ANDRO_ZOO) {
                _uiState.update {
                    it.copy(totalTraceCount = it.apkIdentifiers.size)
                }
                startAndroZooTrace()
            }

            _uiState.update {
                it.copy(isTracing = false)
            }
        }
    }

    private suspend fun startLocalTrace() {
        val existing = scanExistingTraces()
        traceStartTime = getCurrentTime()

        for (apk in uiState.value.apks) {
            if (apk.nameWithoutExtension in existing) {
                _uiState.update {
                    it.copy(completedTraceCount = it.completedTraceCount + 1)
                }
                continue
            }

            _uiState.update {
                it.copy(traceMessage = "Tracing ${apk.nameWithoutExtension}")
            }
            traceApk(apk)
            checkTraceResult(apk.nameWithoutExtension)
            _uiState.update {
                it.copy(traceMessage = null)
            }
        }
    }

    private suspend fun startAndroZooTrace() {
        androZooApiKey?.let { apiKey ->
            val existing = scanExistingTraces()
            traceStartTime = getCurrentTime()

            for (sha256 in uiState.value.apkIdentifiers) {
                if (sha256 in existing) {
                    _uiState.update {
                        it.copy(completedTraceCount = it.completedTraceCount + 1)
                    }
                    continue
                }

                _uiState.update {
                    it.copy(traceMessage = "Downloading $sha256")
                }
                val apk = androZooService.downloadApk(apiKey, sha256)
                if (apk != null) {
                    _uiState.update {
                        it.copy(traceMessage = "Tracing $sha256")
                    }
                    traceApk(apk)
                    apk.delete()
                }
                checkTraceResult(sha256)
                _uiState.update {
                    it.copy(traceMessage = null)
                }
            }
        }
    }

    private suspend fun scanExistingTraces(): Set<String> = withContext(Dispatchers.IO) {
        File(outputDir).listFiles()
            ?.asSequence()
            ?.filter { it.isFile }
            ?.mapNotNull {
                val name = it.name
                if (name.contains("[") && name.contains("]")) {
                    name.substringAfter("[").substringBefore("]")
                } else {
                    null
                }
            }
            ?.toSet()
            ?: emptySet()
    }

    private suspend fun traceApk(apk: File) {
        if (emulator == Emulator.AVD) {
            if (avdIni != null) {
                straceService.traceApk(
                    apk = apk,
                    avdIni = avdIni,
                    outputDir = outputDir,
                    timeout = traceTimeout.duration,
                    emulatorLaunchWaitTime = emulatorLaunchWaitTime.duration
                )
            }
        }

        if (emulator == Emulator.LD_PLAYER) {
            straceService.traceApk(
                apk = apk,
                avdIni = null,
                outputDir = outputDir,
                timeout = traceTimeout.duration,
                emulatorLaunchWaitTime = emulatorLaunchWaitTime.duration
            )
        }
    }

    private fun checkTraceResult(apkName: String) {
        val isSuccess = Files.newDirectoryStream(Paths.get(outputDir)).use { stream ->
            stream.any { path -> path.name.contains(apkName) }
        }

        if (isSuccess) {
            _uiState.update {
                it.copy(completedTraceCount = it.completedTraceCount + 1)
            }
        } else {
            val failed = File(outputDir, "logs/${traceStartTime}_failed.csv")
            failed.parentFile?.mkdirs()

            if (!failed.exists()) {
                failed.createNewFile()
            }

            csvWriter { delimiter = csvDelimiter.value }.open(failed, append = true) {
                writeRow(apkName)
            }

            _uiState.update {
                it.copy(failedTraceCount = it.failedTraceCount + 1)
            }
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