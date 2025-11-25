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
    private val adbService: AdbService,
    private val avdService: AvdService,
    private val ldPlayerService: LdPlayerService,
    private val apkService: ApkService
) {
    private lateinit var adbPath: String
    private var isEmulatorRunning: Boolean = false
    private var currentAvdIni: String? = null

    suspend fun init() {
        adbPath = adbService.resolvePath()
        avdService.init()
        ldPlayerService.init()
        apkService.init()
    }

    suspend fun traceApk(
        apk: File,
        avdIni: String?,
        outputDir: String,
        timeout: Duration = 1.minutes
    ) = withContext(Dispatchers.IO) {
        val isLdPlayer = avdIni == null

        if (isLdPlayer) {
            ldPlayerService.duplicate()
            ldPlayerService.start()
        } else {
            val duplicatedAvdIni = avdService.duplicate(avdIni)
            currentAvdIni = duplicatedAvdIni
            avdService.start(duplicatedAvdIni)
        }

        isEmulatorRunning = true

        delay(2.minutes)

        apkService.install(apk.absolutePath).let { packageName ->
            if (packageName != null) {
                if (isLdPlayer) {
                    ldPlayerService.launch(packageName)
                } else {
                    avdService.launch(packageName)
                }

                delay(30.seconds)

                tracePackage(
                    packageName = packageName,
                    outputDir = outputDir,
                    timeout = timeout,
                    isLdPlayer = isLdPlayer
                )
            }
        }

        cleanupLeftovers()
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
                adbPath,
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
                    adbPath,
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
            adbPath,
            "push",
            binaryFile.absolutePath,
            "/data/local/tmp/strace"
        )
            .start()
            .waitFor()

        ProcessBuilder(
            adbPath,
            "shell",
            "chmod 755 /data/local/tmp/strace"
        )
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
        if (!isEmulatorRunning) return@withContext

        val isLdPlayer = currentAvdIni == null

        if (isLdPlayer) {
            ldPlayerService.kill()
        } else {
            avdService.kill()
        }

        delay(30.seconds)

        if (isLdPlayer) {
            ldPlayerService.delete()
        }

        currentAvdIni?.let {
            avdService.delete(it)
            currentAvdIni = null
        }

        isEmulatorRunning = false
    }
}