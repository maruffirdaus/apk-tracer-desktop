package app.apktracer.common.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApkUtil() {
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

        println("Output")
        println(output)

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

        println(output.substringAfter("package:").trim())

        return@withContext output.substringAfter("package:").trim()
    }

    suspend fun kill(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            ProcessBuilder(
                "adb",
                "shell",
                "am",
                "force-stop",
                packageName
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

    suspend fun uninstall(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder(
                "adb",
                "uninstall",
                packageName
            )
                .redirectErrorStream(true)
                .start()
            process.waitFor()

            val output = process.inputStream.bufferedReader().readText()

            return@withContext output.contains("Success") || output.isEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}