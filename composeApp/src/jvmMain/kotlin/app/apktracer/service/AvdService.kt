package app.apktracer.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AvdService() {
    suspend fun duplicate(avdIni: String): String = withContext(Dispatchers.IO) {
        val file = File(avdIni)
        val lines = file.readText().lines().toMutableList()
        for (i in lines.indices) {
            if (lines[i].contains("path=")) {
                val sourceDir = File(lines[i].substringAfter("path="))
                val destinationDir = File(
                    sourceDir.parentFile ?: File("/"),
                    "${sourceDir.name.substringBefore(".")}_Copy.avd"
                )
                sourceDir.copyRecursively(destinationDir)
                lines[i] = lines[i].replace(".avd", "_Copy.avd")
            }
            if (lines[i].contains("path.rel")) {
                lines[i] = lines[i].replace(".avd", "_Copy.avd")
            }
        }
        file.parentFile?.let {
            val duplicatedFile = File(
                it,
                "${file.nameWithoutExtension}_Copy.ini"
            )
            duplicatedFile.writeText(lines.joinToString("\n"))
            return@withContext duplicatedFile.absolutePath
        }
        return@withContext "${file.nameWithoutExtension}_Copy.ini"
    }

    suspend fun start(avdIni: String): Unit = withContext(Dispatchers.IO) {
        ProcessBuilder(
            "cmd",
            "/c",
            "start",
            "emulator",
            "-avd",
            File(avdIni).nameWithoutExtension
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
        val file = File(avdIni)
        val lines = file.readText().lines().toMutableList()
        for (line in lines) {
            if (line.contains("path=")) {
                File(line.substringAfter("path=")).deleteRecursively()
            }
        }
        file.delete()
    }

    suspend fun launch(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            ProcessBuilder(
                "adb",
                "shell",
                "monkey -p $packageName -c android.intent.category.LAUNCHER 1"
            )
                .redirectErrorStream(true)
                .inheritIO()
                .start()

            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}