package app.apktracer

import androidx.compose.ui.window.application
import app.apktracer.di.appModule
import app.apktracer.di.viewModelModule
import app.apktracer.ui.App
import io.github.vinceglb.filekit.FileKit
import org.koin.core.context.startKoin

fun main() {
    FileKit.init(appId = "app.apktracer")
    System.setProperty("apple.awt.application.appearance", "system")
    application {
        startKoin {
            modules(appModule, viewModelModule)
        }
        App(::exitApplication)
    }
}