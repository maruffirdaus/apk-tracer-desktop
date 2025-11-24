package app.apktracer

import androidx.compose.ui.window.application
import app.apktracer.di.networkModule
import app.apktracer.di.serviceModule
import app.apktracer.di.viewModelModule
import app.apktracer.ui.App
import io.github.vinceglb.filekit.FileKit
import org.koin.core.context.startKoin
import java.io.File

fun main() {
    FileKit.init(
        appId = "app.apktracer",
        filesDir = File("""C:\APK Tracer"""),
        cacheDir = File("""C:\APK Tracer\cache""")
    )
    application {
        startKoin {
            modules(networkModule, serviceModule, viewModelModule)
        }
        App(::exitApplication)
    }
}