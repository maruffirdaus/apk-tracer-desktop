package app.apktracer.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApkService() {
    suspend fun install(apkPath: String): String? = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(
            "adb",
            "install",
            "-g",
            "-i",
            "app.apktracer",
            apkPath
        )
            .redirectErrorStream(true)
            .start()
        process.waitFor()

        val output = process.inputStream.bufferedReader().readText()

        println(output)

        return@withContext if (output.contains("Failed", true)) null else getPackageName()
    }

    private suspend fun getPackageName(): String = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(
            "adb",
            "shell",
            "\"pm list packages -i | grep app.apktracer\""
        )
            .redirectErrorStream(true)
            .start()
        process.waitFor()

        val output = process.inputStream.bufferedReader().readLines().first()

        return@withContext output.trim().split(Regex("\\s+")).first().substringAfter("package:")
    }
}