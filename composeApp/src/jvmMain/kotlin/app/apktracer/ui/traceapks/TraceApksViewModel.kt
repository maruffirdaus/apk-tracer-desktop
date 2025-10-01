package app.apktracer.ui.traceapks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.apktracer.common.models.Settings
import app.apktracer.common.utils.ApkUtil
import app.apktracer.common.utils.AvdUtil
import app.apktracer.common.utils.StraceUtil
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class TraceApksViewModel(
    private val avdUtil: AvdUtil,
    private val apkUtil: ApkUtil,
    private val straceUtil: StraceUtil
) : ViewModel() {
    private val _uiState = MutableStateFlow(TraceApksUiState())
    val uiState = _uiState.asStateFlow()

    fun changeSelectedFolder(selectedFolder: String?) {
        val apks = if (selectedFolder != null) {
            PlatformFile(selectedFolder).list()
                .filter { it.isRegularFile() && it.extension == "apk" }
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

    fun traceApks() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isTracing = true)
            }
            val settingsJson = PlatformFile(FileKit.filesDir, "settings.json").let {
                if (it.exists()) {
                    it.readString()
                } else {
                    null
                }
            }
            if (settingsJson != null) {
                val settings = Json.decodeFromString<Settings>(settingsJson)
                if (settings.avdIni != null) {
                    for (apk in uiState.value.apks) {
                        val duplicatedAvdIni = avdUtil.duplicate(settings.avdIni)
                        avdUtil.start(duplicatedAvdIni)
                        delay(60000L)
                        apkUtil.install(apk.absolutePath()).let { packageName ->
                            if (packageName == null) continue
                            apkUtil.launch(packageName)
                            delay(30000L)
                            straceUtil.tracePackage(
                                packageName,
                                settings.outputDir ?: PlatformFile(
                                    FileKit.filesDir,
                                    "output"
                                ).absolutePath(),
                                60
                            )
                            apkUtil.kill(packageName)
                            apkUtil.uninstall(packageName)
                        }
                        avdUtil.kill()
                        delay(30000L)
                        avdUtil.delete(duplicatedAvdIni)
                    }
                }
            }
            _uiState.update {
                it.copy(isTracing = false)
            }
        }
    }
}