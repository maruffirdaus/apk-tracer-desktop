package app.apktracer.ui.settings

import app.apktracer.common.model.ApkSource
import app.apktracer.common.model.CsvDelimiter
import app.apktracer.common.model.Emulator
import app.apktracer.common.model.EmulatorLaunchWaitTime
import app.apktracer.common.model.RenderingMode
import app.apktracer.common.model.SettingsKey
import app.apktracer.common.model.TraceTimeout

data class SettingsUiState(
    val outputDir: String = SettingsKey.OutputDir.default,
    val traceTimeout: TraceTimeout = SettingsKey.TraceTimeout.default,
    val apkSource: ApkSource = SettingsKey.ApkSource.default,
    val androZooApiKey: String? = SettingsKey.AndroZooApiKey.default,
    val emulator: Emulator = SettingsKey.Emulator.default,
    val avdIni: String? = SettingsKey.AvdIni.default,
    val ldConsoleBinary: String = SettingsKey.LdConsoleBinary.default,
    val emulatorLaunchWaitTime: EmulatorLaunchWaitTime = SettingsKey.EmulatorLaunchWaitTime.default,
    val csvDelimiter: CsvDelimiter = SettingsKey.CsvDelimiter.default,
    val renderingMode: RenderingMode = SettingsKey.RenderingMode.default
)
