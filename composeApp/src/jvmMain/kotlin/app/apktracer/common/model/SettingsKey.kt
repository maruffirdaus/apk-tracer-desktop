package app.apktracer.common.model

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import java.io.File

sealed class SettingsKey<T>(val name: String, val default: T) {
    object AndroZooApiKey : SettingsKey<String?>("andro_zoo_api_key", null)
    object ApkSource : SettingsKey<app.apktracer.common.model.ApkSource>(
        "apk_source",
        app.apktracer.common.model.ApkSource.LOCAL
    )

    object AvdIni : SettingsKey<String?>("avd_ini", null)
    object CsvDelimiter : SettingsKey<app.apktracer.common.model.CsvDelimiter>(
        "csv_delimiter",
        app.apktracer.common.model.CsvDelimiter.COMMA
    )

    object Emulator : SettingsKey<app.apktracer.common.model.Emulator>(
        "emulator",
        app.apktracer.common.model.Emulator.AVD
    )

    object EmulatorLaunchWaitTime : SettingsKey<app.apktracer.common.model.EmulatorLaunchWaitTime>(
        "emulator_launch_wait_time",
        app.apktracer.common.model.EmulatorLaunchWaitTime.MIN_2
    )

    object LdConsoleBinary :
        SettingsKey<String>("ld_console_binary", "C:\\LDPlayer\\LDPlayer9\\ldconsole.exe")

    object OutputDir :
        SettingsKey<String>("output_dir", File(FileKit.filesDir.file, "output").absolutePath)

    object RenderingMode : SettingsKey<app.apktracer.common.model.RenderingMode>(
        "rendering_mode",
        app.apktracer.common.model.RenderingMode.HARDWARE
    )

    object TraceTimeout : SettingsKey<app.apktracer.common.model.TraceTimeout>(
        "trace_timeout",
        app.apktracer.common.model.TraceTimeout.MIN_1
    )
}