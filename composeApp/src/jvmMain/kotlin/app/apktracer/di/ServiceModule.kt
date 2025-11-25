package app.apktracer.di

import app.apktracer.service.AdbService
import app.apktracer.service.AndroZooService
import app.apktracer.service.ApkService
import app.apktracer.service.AvdService
import app.apktracer.service.LdPlayerService
import app.apktracer.service.SettingsService
import app.apktracer.service.StraceService
import org.koin.dsl.module

val serviceModule = module {
    single { SettingsService() }
    single { AdbService(get()) }
    single { AvdService(get()) }
    single { LdPlayerService(get()) }
    single { ApkService(get()) }
    single { StraceService(get(), get(), get(), get()) }
    single { AndroZooService(get()) }
}