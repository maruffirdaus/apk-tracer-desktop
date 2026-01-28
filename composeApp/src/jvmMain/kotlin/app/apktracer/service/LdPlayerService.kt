package app.apktracer.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LdPlayerService(
    private val adbService: AdbService
) {
    private lateinit var binary: String
    private lateinit var adbPath: String
    
    suspend fun init(binary: String) {
        this.binary = binary
        adbPath = adbService.resolvePath()
    }

    suspend fun duplicate() = withContext(Dispatchers.IO) {
        ProcessBuilder(
            binary,
            "copy",
            "--from",
            "0"
        )
            .start()
            .waitFor()
    }

    suspend fun start() = withContext(Dispatchers.IO) {
        ProcessBuilder(
            adbPath,
            "kill-server"
        )
            .start()
            .waitFor()

        ProcessBuilder(
            adbPath,
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
        ProcessBuilder(
            binary,
            "remove",
            "--index",
            "1"
        )
            .start()
            .waitFor()
    }

    suspend fun launch(packageName: String) = withContext(Dispatchers.IO) {
        ProcessBuilder(
            binary,
            "runapp",
            "--index",
            "1",
            "--packagename",
            packageName
        )
            .start()
            .waitFor()
    }
}