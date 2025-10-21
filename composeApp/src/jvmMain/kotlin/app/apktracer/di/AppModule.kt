package app.apktracer.di

import app.apktracer.common.utils.ApkUtil
import app.apktracer.common.utils.AvdUtil
import app.apktracer.common.utils.LdPlayerUtil
import app.apktracer.common.utils.StraceUtil
import org.koin.dsl.module

val appModule = module {
    single { AvdUtil() }
    single { LdPlayerUtil() }
    single { ApkUtil() }
    single { StraceUtil() }
}