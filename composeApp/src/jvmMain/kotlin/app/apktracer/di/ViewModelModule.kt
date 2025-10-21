package app.apktracer.di

import app.apktracer.ui.convertapks.ConvertApksViewModel
import app.apktracer.ui.settings.SettingsViewModel
import app.apktracer.ui.traceapks.TraceApksViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { TraceApksViewModel(get(), get(), get(), get()) }
    viewModel { ConvertApksViewModel() }
    viewModel { SettingsViewModel() }
}