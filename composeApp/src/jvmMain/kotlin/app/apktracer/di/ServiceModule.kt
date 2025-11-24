package app.apktracer.di

import app.apktracer.service.AndroZooService
import app.apktracer.service.ApkService
import app.apktracer.service.AvdService
import app.apktracer.service.LdPlayerService
import app.apktracer.service.SettingsService
import app.apktracer.service.StraceService
import org.koin.dsl.module

val serviceModule = module {
    single { SettingsService() }
    single { AvdService() }
    single { LdPlayerService() }
    single { ApkService() }
    single { StraceService(get(), get(), get()) }
    single { AndroZooService(get()) }
}