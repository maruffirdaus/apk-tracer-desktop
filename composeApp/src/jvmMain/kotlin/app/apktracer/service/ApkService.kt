package app.apktracer.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApkService() {
    suspend fun install(apkPath: String): String? = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(
            "adb",
            "install",
            "-r",
            apkPath
        )
            .redirectErrorStream(true)
            .start()
        process.waitFor()

        val output = process.inputStream.bufferedReader().readText()

        return@withContext if (output.contains("Failed", true)) null else getPackageName()
    }

    private suspend fun getPackageName(): String = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(
            "adb",
            "shell",
            "pm",
            "list",
            "packages",
            "-3"
        )
            .redirectErrorStream(true)
            .start()
        process.waitFor()

        val output = process.inputStream.bufferedReader().readLines().first()

        return@withContext output.substringAfter("package:").trim()
    }
}