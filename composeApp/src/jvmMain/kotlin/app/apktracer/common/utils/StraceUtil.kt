package app.apktracer.common.utils

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StraceUtil() {
    suspend fun tracePackage(packageName: String, outputDir: String, timeout: Int = 30): Boolean =
        withContext(Dispatchers.IO) {
            try {
                pushStraceBinary()

                val pid = findProcessId(packageName)
                    ?: throw IllegalStateException("Could not find process ID for package: $packageName")

                val timestamp =
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
                val outputFile = PlatformFile(
                    PlatformFile(outputDir).also {
                        it.file.mkdirs()
                    },
                    "${timestamp}_${packageName}.txt"
                )

                val process = ProcessBuilder(
                    "adb",
                    "shell",
                    "su",
                    "-c",
                    "timeout $timeout /data/local/tmp/strace -f -tt -p $pid"
                )
                    .redirectErrorStream(true)
                    .start()

                FileOutputStream(outputFile.file).use { fileOutput ->
                    process.inputStream.bufferedReader().use { reader ->
                        val buffer = CharArray(4096)
                        var read: Int

                        while (reader.read(buffer).also { read = it } != -1) {
                            val outputString = String(buffer, 0, read)
                            fileOutput.write(outputString.toByteArray())
                        }
                    }
                }

                val exitCode = process.waitFor()

                return@withContext exitCode == 0 || exitCode == 124
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext false
            }
        }

    private suspend fun findProcessId(packageName: String): Int? = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder(
                "adb",
                "shell",
                "su",
                "-c",
                "ps -A | grep $packageName"
            ).start()
            process.waitFor()

            val output = process.inputStream.bufferedReader().use { it.readText() }

            val pid = output.split("\n")
                .filter { it.contains(packageName) }
                .firstNotNullOfOrNull { line ->
                    line.trim().split("\\s+".toRegex()).let { parts ->
                        if (1 < parts.size)
                            parts[1].toIntOrNull().takeIf { 0 < (it ?: -1) }
                        else null
                    }
                }

            return@withContext pid
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    private suspend fun pushStraceBinary() = withContext(Dispatchers.IO) {
        val binaryFile = PlatformFile(FileKit.filesDir, "binaries/strace-x64")

        if (!binaryFile.exists()) {
            extractStraceBinary()
        }

        ProcessBuilder(
            "adb",
            "push",
            binaryFile.absolutePath(),
            "/data/local/tmp/strace"
        )
            .inheritIO()
            .start()
            .waitFor()

        ProcessBuilder(
            "adb",
            "shell",
            "su",
            "-c",
            "chmod 755 /data/local/tmp/strace"
        )
            .inheritIO()
            .start()
            .waitFor()
    }

    private suspend fun extractStraceBinary() = withContext(Dispatchers.IO) {
        val binaryName = "strace-x64"
        val binaryFile = PlatformFile(FileKit.filesDir, "binaries/$binaryName")
        val resourcePath = "binaries/$binaryName"
        val inputStream: InputStream = javaClass.classLoader.getResourceAsStream(resourcePath)
            ?: throw Exception("$binaryName not found in resources")
        inputStream.use { input ->
            PlatformFile(FileKit.filesDir, "binaries").file.mkdir()
            binaryFile.file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}