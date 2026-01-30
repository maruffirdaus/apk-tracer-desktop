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
                readAllAsSequence().map { row -> row.first() }.toList()
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

            if (apkSource == ApkSource.LOCAL) {
                startLocalTrace()
            }

            if (apkSource == ApkSource.ANDRO_ZOO) {
                startAndroZooTrace()
            }

            _uiState.update {
                it.copy(isTracing = false)
            }
        }
    }

    private suspend fun startLocalTrace() {
        val existing = scanExistingTraces()
        val timestamp =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))

        for (apk in uiState.value.apks) {
            if (apk.nameWithoutExtension in existing) continue

            traceApk(apk)
            logOnFailure(apk.nameWithoutExtension, timestamp)
        }
    }

    private suspend fun startAndroZooTrace() {
        androZooApiKey?.let { apiKey ->
            val existing = scanExistingTraces()
            val timestamp =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))

            for (sha256 in uiState.value.apkIdentifiers) {
                if (sha256 in existing) continue

                val apk = androZooService.downloadApk(apiKey, sha256)
                if (apk != null) {
                    traceApk(apk)
                    apk.delete()
                }
                logOnFailure(sha256, timestamp)
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

    private fun logOnFailure(apkName: String, timestamp: String) {
        var isSuccess = false

        Files.newDirectoryStream(Paths.get(outputDir)).use { stream ->
            stream.forEach { path ->
                isSuccess = path.name.contains(apkName)
            }
        }

        if (!isSuccess) {
            val failed = File(outputDir, "logs/${timestamp}_failed.csv")
            failed.parentFile?.mkdirs()

            if (!failed.exists()) {
                failed.createNewFile()
            }

            csvWriter { delimiter = csvDelimiter.value }.open(failed) {
                writeRow(apkName)
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