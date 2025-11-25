package app.apktracer.service

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream
import kotlin.apply
import kotlin.io.copyTo
import kotlin.io.deleteRecursively
import kotlin.io.use

class AdbService(
    private val httpClient: HttpClient
) {
    private val cacheDir = FileKit.cacheDir.file.apply { mkdirs() }

    suspend fun resolvePath(): String = withContext(Dispatchers.IO) {
        val adbExecutable = "adb.exe"

        val cachedAdb = File(File(cacheDir, "platform-tools"), adbExecutable)
        if (cachedAdb.exists() && isValidAdb(cachedAdb.toString())) {
            return@withContext cachedAdb.absolutePath
        }

        downloadAdb()

        if (!isValidAdb(cachedAdb.absolutePath)) {
            throw kotlin.RuntimeException("Cached ADB is not working")
        }

        return@withContext cachedAdb.absolutePath
    }

    private suspend fun downloadAdb() = withContext(Dispatchers.IO) {
        val downloadUrl =
            "https://dl.google.com/android/repository/platform-tools-latest-windows.zip"

        val zipFile = File(cacheDir, "platform-tools.zip")

        val response = httpClient.get(downloadUrl)
        if (response.status.value != 200) {
            throw kotlin.Exception("Failed to download ADB: ${response.status.description}")
        }
        response.bodyAsChannel().toInputStream().use { input ->
            FileOutputStream(zipFile).use { output ->
                input.copyTo(output)
            }
        }

        extractZip(zipFile)

        zipFile.delete()
    }

    private suspend fun extractZip(zipFile: File) = withContext(Dispatchers.IO) {
        val platformToolsDir = File(cacheDir, "platform-tools")
        if (platformToolsDir.exists()) {
            platformToolsDir.deleteRecursively()
        }

        ZipInputStream(FileInputStream(zipFile)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val targetFile = File(cacheDir, entry.name)
                    targetFile.parentFile?.mkdirs()

                    FileOutputStream(targetFile).use { output ->
                        zip.copyTo(output)
                    }
                }
                entry = zip.nextEntry
            }
        }
    }

    private suspend fun isValidAdb(adbPath: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val process = ProcessBuilder(adbPath, "version").start()
            process.waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }
}