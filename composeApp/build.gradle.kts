import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.fluent.desktop)
            implementation(libs.fluentIconsExtended.desktop)
            implementation(libs.windowStyler)
        }
    }
}


compose.desktop {
    application {
        mainClass = "app.apktracer.MainKt"

        buildTypes.release.proguard {
            version.set("7.4.0")
            isEnabled.set(false)
        }

        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            packageName = "APK Tracer"
            packageVersion = "0.1.0"

            windows {
                upgradeUuid = "86216df0-0da7-5557-89e1-bd83e2449c5d"
                perUserInstall = true
                menu = true
                menuGroup = "APK Tracer"
                shortcut = true
            }
        }
    }
}
