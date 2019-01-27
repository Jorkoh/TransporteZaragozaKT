package com.jorkoh.transportezaragozakt

import android.app.Application
import com.jorkoh.transportezaragozakt.DI.appModule
import org.koin.android.ext.android.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Start Koin
        startKoin(this, listOf(appModule))
    }
}