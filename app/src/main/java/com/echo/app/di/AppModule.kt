package com.echo.app.di

import com.echo.app.ui.calendar.CalendarViewModel
import com.echo.app.ui.home.HomeViewModel
import com.echo.app.ui.settings.SettingsViewModel
import com.echo.app.ui.trend.TrendViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { CalendarViewModel(get()) }
    viewModel { TrendViewModel(get()) }
    viewModel { SettingsViewModel(androidContext(), get(), get()) }
}
