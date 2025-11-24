package app.apktracer.service

import app.apktracer.common.extension.sha256
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroZooService(
    private val client: HttpClient
) {
    private val cacheDir = File(FileKit.cacheDir.file, "AndroZoo").also { it.mkdirs() }

    suspend fun downloadApk(apiKey: String, sha256: String): File? = withContext(Dispatchers.IO) {
        try {
            val outputFile = File(cacheDir, "$sha256.apk")
            val response = client.get("https://androzoo.uni.lu/api/download") {
                parameter("apikey", apiKey)
                parameter("sha256", sha256)
            }
            if (!response.status.isSuccess()) return@withContext null
            response.bodyAsChannel().copyAndClose(outputFile.writeChannel())
            return@withContext if (outputFile.sha256().equals(sha256, ignoreCase = true)) {
                outputFile
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}