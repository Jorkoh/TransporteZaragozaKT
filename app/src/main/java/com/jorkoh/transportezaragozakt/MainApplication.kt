package com.jorkoh.transportezaragozakt

import android.app.Application
import com.jaredrummler.cyanea.Cyanea
import org.koin.android.ext.android.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Koin
        startKoin(this, listOf(appModule))

        // Cyanea
        Cyanea.init(this, resources)
    }
}