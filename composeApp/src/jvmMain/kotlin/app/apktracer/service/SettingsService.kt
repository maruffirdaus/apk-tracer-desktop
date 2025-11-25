package app.apktracer.service

import app.apktracer.common.model.Settings
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class SettingsService {
    suspend fun getSettings(): Settings = withContext(Dispatchers.IO) {
        val settingsJson = File(FileKit.filesDir.file, "settings.json").let {
            if (it.exists()) {
                it.readText()
            } else {
                null
            }
        }
        if (settingsJson != null) {
            val settings = Json.decodeFromString<Settings>(settingsJson)
            return@withContext settings
        }
        return@withContext Settings()
    }

    suspend fun saveSettings(settings: Settings) = withContext(Dispatchers.IO) {
        File(FileKit.filesDir.file, "settings.json").writeText(Json.encodeToString(settings))
    }
}