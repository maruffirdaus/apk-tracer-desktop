package app.apktracer.common.model

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Serializable
enum class TraceTimeout(
    val label: String,
    val duration: Duration
) {
    SEC_30("30 sec", 30.seconds),
    SEC_45("45 sec", 45.seconds),
    MIN_1("1 min", 1.minutes),
    MIN_2("2 min", 2.minutes),
    MIN_3("3 min", 3.minutes)
}