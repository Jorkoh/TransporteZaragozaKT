package com.jorkoh.transportezaragozakt

import android.app.Application
import com.jaredrummler.cyanea.Cyanea
import com.parse.Parse
import org.koin.android.ext.android.startKoin

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Koin
        startKoin(this, listOf(appModule))

        //TODO: Make this a string resource or value
        //Parse
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId("transporte-zaragoza")
                .server("https://transporte-zaragoza.herokuapp.com/parse/")
                .build()
        )

        // Cyanea
        Cyanea.init(this, resources)
    }
}