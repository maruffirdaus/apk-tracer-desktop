package app.apktracer.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LdPlayerService {
    private val binary = "C:\\LDPlayer\\LDPlayer9\\ldconsole.exe"

    suspend fun duplicate() = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(
            binary,
            "copy",
            "--from",
            "0"
        ).start()
        process.waitFor()
    }

    suspend fun start() = withContext(Dispatchers.IO) {
        ProcessBuilder(
            "adb",
            "kill-server"
        )
            .start()
            .waitFor()

        ProcessBuilder(
            "adb",
            "start-server"
        )
            .start()
            .waitFor()

        ProcessBuilder(
            binary,
            "launch",
            "--index",
            "1"
        )
            .start()
            .waitFor()
    }

    suspend fun kill() = withContext(Dispatchers.IO) {
        ProcessBuilder(
            binary,
            "quit",
            "--index",
            "1"
        )
            .start()
            .waitFor()
    }

    suspend fun delete() = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(
            binary,
            "remove",
            "--index",
            "1"
        ).start()
        process.waitFor()
    }

    suspend fun launch(packageName: String) = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(
            binary,
            "runapp",
            "--index",
            "1",
            "--packagename",
            packageName
        ).start()
        process.waitFor()
    }
}