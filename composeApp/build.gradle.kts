import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3.adaptive)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.core)
            implementation(libs.mutliplatformSettings)
            implementation(project.dependencies.platform(libs.koin.bom))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.fluent.desktop)
            implementation(libs.fluent.iconsExtended.desktop)
            implementation(libs.kotlinCsv.jvm)
            implementation(libs.kotlinx.coroutinesSwing)
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
            packageVersion = "0.2.4"

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
