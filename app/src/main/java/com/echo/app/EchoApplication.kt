package com.echo.app

import android.app.Application
import com.echo.app.di.appModule
import com.echo.app.data.di.dataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EchoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@EchoApplication)
            modules(dataModule, appModule)
        }
    }
}
