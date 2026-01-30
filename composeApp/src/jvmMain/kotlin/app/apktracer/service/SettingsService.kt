package app.apktracer.service

import app.apktracer.common.model.ApkSource
import app.apktracer.common.model.CsvDelimiter
import app.apktracer.common.model.Emulator
import app.apktracer.common.model.EmulatorLaunchWaitTime
import app.apktracer.common.model.RenderingMode
import app.apktracer.common.model.SettingsKey
import app.apktracer.common.model.TraceTimeout
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences

class SettingsService {
    private val settings = PreferencesSettings(Preferences.userRoot().node("apktracer"))

    @Suppress("UNCHECKED_CAST")
    fun <T> getValue(key: SettingsKey<T>): T {
        return when (key) {
            SettingsKey.AndroZooApiKey -> settings.getStringOrNull(key.name)
            SettingsKey.ApkSource -> getEnum<ApkSource>(key.name)
            SettingsKey.AvdIni -> settings.getStringOrNull(key.name)
            SettingsKey.CsvDelimiter -> getEnum<CsvDelimiter>(key.name)
            SettingsKey.Emulator -> getEnum<Emulator>(key.name)
            SettingsKey.EmulatorLaunchWaitTime -> getEnum<EmulatorLaunchWaitTime>(key.name)
            SettingsKey.LdConsoleBinary -> settings.getStringOrNull(key.name)
            SettingsKey.OutputDir -> settings.getStringOrNull(key.name)
            SettingsKey.RenderingMode -> getEnum<RenderingMode>(key.name)
            SettingsKey.TraceTimeout -> getEnum<TraceTimeout>(key.name)
        } as T? ?: key.default
    }

    private inline fun <reified E : Enum<E>> getEnum(key: String): E? {
        val value = settings.getStringOrNull(key)
        return enumValues<E>().firstOrNull { it.name == value }
    }

    fun <T> setValue(key: SettingsKey<T>, value: T) {
        when (key) {
            SettingsKey.AndroZooApiKey -> if (value != null) {
                settings.putString(key.name, value as String)
            } else {
                settings.remove(key.name)
            }

            SettingsKey.ApkSource -> settings.putString(key.name, (value as ApkSource).name)
            SettingsKey.AvdIni -> if (value != null) {
                settings.putString(key.name, value as String)
            } else {
                settings.remove(key.name)
            }

            SettingsKey.CsvDelimiter -> settings.putString(key.name, (value as CsvDelimiter).name)
            SettingsKey.Emulator -> settings.putString(key.name, (value as Emulator).name)
            SettingsKey.EmulatorLaunchWaitTime -> settings.putString(
                key.name,
                (value as EmulatorLaunchWaitTime).name
            )

            SettingsKey.LdConsoleBinary -> settings.putString(key.name, value as String)
            SettingsKey.OutputDir -> settings.putString(key.name, value as String)
            SettingsKey.RenderingMode -> settings.putString(key.name, (value as RenderingMode).name)
            SettingsKey.TraceTimeout -> settings.putString(key.name, (value as TraceTimeout).name)
        }
    }
}