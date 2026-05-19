package com.echo.app.data.di

import com.echo.app.data.db.EchoDatabase
import com.echo.app.data.repository.EchoRepository
import com.echo.app.data.repository.SettingsPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single { EchoDatabase.create(androidContext()) }
    single { get<EchoDatabase>().echoRecordDao() }
    single { EchoRepository(get()) }
    single { SettingsPreferences(androidContext()) }
}
