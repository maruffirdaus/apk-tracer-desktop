package app.apktracer.common.model

import app.apktracer.common.type.ApkSource
import app.apktracer.common.type.Emulator
import app.apktracer.common.type.TraceTimeout
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Settings(
    val outputDir: String = File(FileKit.filesDir.file, "output").absolutePath,
    val traceTimeout: TraceTimeout = TraceTimeout.MIN_1,
    val apkSource: ApkSource = ApkSource.LOCAL,
    val androZooApiKey: String? = null,
    val emulator: Emulator = Emulator.AVD,
    val avdIni: String? = null
)
