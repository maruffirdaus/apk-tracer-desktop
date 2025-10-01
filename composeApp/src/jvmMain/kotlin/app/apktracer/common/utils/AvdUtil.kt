package app.apktracer.common.utils

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AvdUtil() {
    suspend fun duplicate(avdIni: String): String = withContext(Dispatchers.IO) {
        val file = PlatformFile(avdIni)
        val lines = file.readString().lines().toMutableList()
        for (i in lines.indices) {
            if (lines[i].contains("path=")) {
                val sourceDir = PlatformFile(lines[i].substringAfter("path="))
                val destinationDir = PlatformFile(
                    sourceDir.parent() ?: PlatformFile("/"),
                    "${sourceDir.name.substringBefore(".")}_Copy.avd"
                )
                sourceDir.file.copyRecursively(destinationDir.file)
                lines[i] = lines[i].replace(".avd", "_Copy.avd")
            }
            if (lines[i].contains("path.rel")) {
                lines[i] = lines[i].replace(".avd", "_Copy.avd")
            }
        }
        file.parent()?.let {
            val duplicatedFile = PlatformFile(
                it,
                "${file.nameWithoutExtension}_Copy.ini"
            )
            duplicatedFile.writeString(lines.joinToString("\n"))
            return@withContext duplicatedFile.absolutePath()
        }
        return@withContext "${file.nameWithoutExtension}_Copy.ini"
    }

    suspend fun start(avdIni: String) = withContext(Dispatchers.IO) {
        ProcessBuilder(
            "cmd",
            "/c",
            "start",
            "emulator",
            "-avd",
            PlatformFile(avdIni).nameWithoutExtension
        ).start()
    }

    suspend fun kill() = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(
            "adb",
            "emu",
            "kill"
        ).start()
        process.waitFor()
    }

    suspend fun delete(avdIni: String) = withContext(Dispatchers.IO) {
        val file = PlatformFile(avdIni)
        val lines = file.readString().lines().toMutableList()
        for (line in lines) {
            if (line.contains("path=")) {
                PlatformFile(line.substringAfter("path=")).file.deleteRecursively()
            }
        }
        file.delete()
    }
}