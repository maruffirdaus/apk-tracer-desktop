package app.apktracer.common.type

import kotlinx.serialization.Serializable

@Serializable
enum class Emulator(val label: String) {
    AVD("AVD"),
    LD_PLAYER("LDPlayer")
}