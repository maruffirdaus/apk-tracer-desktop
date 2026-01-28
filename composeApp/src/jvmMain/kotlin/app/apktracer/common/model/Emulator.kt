package app.apktracer.common.model

import kotlinx.serialization.Serializable

@Serializable
enum class Emulator(val label: String) {
    AVD("AVD"),
    LD_PLAYER("LDPlayer")
}