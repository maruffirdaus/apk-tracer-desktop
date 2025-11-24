package app.apktracer.service

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class StraceService(
    private val avdService: AvdService,
    private val ldPlayerService: LdPlayerService,
    private val apkService: ApkService
) {
    private var isEmulatorRunning: Boolean = false
    private var currentAvdIni: String? = null

    suspend fun traceApk(
        apk: File,
        avdIni: String?,
        outputDir: String,
        timeout: Duration = 1.minutes
    ) = withContext(Dispatchers.IO) {
        val isLdPlayer = avdIni == null
        currentAvdIni = avdIni
        if (isLdPlayer) {
            ldPlayerService.duplicate()
            ldPlayerService.start()
            isEmulatorRunning = true
            delay(1.minutes)
            apkService.install(apk.absolutePath).let { packageName ->
                if (packageName != null) {
                    ldPlayerService.launch(packageName)
                    delay(30.seconds)
                    tracePackage(
                        packageName = packageName,
                        outputDir = outputDir,
                        timeout = timeout,
                        isLdPlayer = true
                    )
                }
            }
            ldPlayerService.kill()
            delay(30.seconds)
            ldPlayerService.delete()
        } else {
            val duplicatedAvdIni = avdService.duplicate(avdIni)
            avdService.start(duplicatedAvdIni)
            isEmulatorRunning = true
            delay(1.minutes)
            apkService.install(apk.absolutePath).let { packageName ->
                if (packageName != null) {
                    avdService.launch(packageName)
                    delay(30.seconds)
                    tracePackage(
                        packageName = packageName,
                        outputDir = outputDir,
                        timeout = timeout
                    )
                }
            }
            avdService.kill()
            delay(30.seconds)
            avdService.delete(duplicatedAvdIni)
            currentAvdIni = null
        }
        isEmulatorRunning = false
    }

    private suspend fun tracePackage(
        packageName: String,
        outputDir: String,
        isLdPlayer: Boolean = false,
        timeout: Duration = 1.minutes,
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            pushStraceBinary()

            val pid = findProcessId(packageName, isLdPlayer)
                ?: throw IllegalStateException("Could not find process ID for package: $packageName")

            val timestamp =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
            val outputFile = File(
                File(outputDir).also {
                    it.mkdirs()
                },
                "${timestamp}_${packageName}.txt"
            )

            val process = ProcessBuilder(
                "adb",
                "shell",
                "su",
                "-c",
                if (isLdPlayer) {
                    "sh -c 'timeout ${timeout.inWholeSeconds}s /data/local/tmp/strace -f -tt -p $pid'"
                } else {
                    "timeout ${timeout.inWholeSeconds} /data/local/tmp/strace -f -tt -p $pid"
                }
            )
                .redirectErrorStream(true)
                .start()

            outputFile.bufferedWriter().use { writer ->
                process.inputStream.bufferedReader().forEachLine { line ->
                    if (line.startsWith("[pid")) {
                        writer.write(line)
                        writer.newLine()
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

    private suspend fun findProcessId(packageName: String, isLdPlayer: Boolean = false): Int? =
        withContext(Dispatchers.IO) {
            try {
                val process = ProcessBuilder(
                    "adb",
                    "shell",
                    "su",
                    "-c",
                    if (isLdPlayer) {
                        "ps | grep $packageName"
                    } else {
                        "ps -A | grep $packageName"
                    }
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
        val binaryFile = File(FileKit.filesDir.file, "binaries/strace-x64")

        if (!binaryFile.exists()) {
            extractStraceBinary()
        }

        ProcessBuilder(
            "adb",
            "push",
            binaryFile.absolutePath,
            "/data/local/tmp/strace"
        )
            .inheritIO()
            .start()
            .waitFor()

        ProcessBuilder(
            "adb",
            "shell",
            "chmod 755 /data/local/tmp/strace"
        )
            .inheritIO()
            .start()
            .waitFor()
    }

    private suspend fun extractStraceBinary() = withContext(Dispatchers.IO) {
        val binaryName = "strace-x64"
        val binaryFile = File(FileKit.filesDir.file, "binaries/$binaryName")
        val resourcePath = "binaries/$binaryName"
        val inputStream: InputStream = javaClass.classLoader.getResourceAsStream(resourcePath)
            ?: throw Exception("$binaryName not found in resources")
        inputStream.use { input ->
            File(FileKit.filesDir.file, "binaries").mkdir()
            binaryFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    suspend fun cleanupLeftovers() = withContext(Dispatchers.IO) {
        if (isEmulatorRunning) {
            if (currentAvdIni != null) {
                avdService.kill()
                delay(30.seconds)
                currentAvdIni?.let {
                    avdService.delete(it)
                    currentAvdIni = null
                }
            } else {
                ldPlayerService.kill()
                delay(30.seconds)
                ldPlayerService.delete()
            }
            isEmulatorRunning = false
        }
    }
}