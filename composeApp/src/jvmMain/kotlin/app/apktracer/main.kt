package app.apktracer

import androidx.compose.ui.window.singleWindowApplication
import app.apktracer.common.model.RenderingMode
import app.apktracer.common.model.SettingsKey
import app.apktracer.di.networkModule
import app.apktracer.di.serviceModule
import app.apktracer.di.viewModelModule
import app.apktracer.service.SettingsService
import app.apktracer.ui.App
import io.github.vinceglb.filekit.FileKit
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import java.io.File

fun main() {
    FileKit.init(
        appId = "app.apktracer",
        filesDir = File("""C:\APK Tracer"""),
        cacheDir = File("""C:\APK Tracer\cache""")
    )
    startKoin {
        modules(networkModule, serviceModule, viewModelModule)
    }

    val settingsService = GlobalContext.get().get<SettingsService>()
    val renderingMode = settingsService.getValue(SettingsKey.RenderingMode)

    if (renderingMode == RenderingMode.SOFTWARE) {
        System.setProperty("skiko.renderApi", "SOFTWARE")
    }

    singleWindowApplication(
        title = "APK Tracer"
    ) {
        App()
    }
}