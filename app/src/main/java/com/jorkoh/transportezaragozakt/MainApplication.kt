package com.jorkoh.transportezaragozakt

import android.app.Application
import com.parse.Parse
import daio.io.dresscode.DressCode
import daio.io.dresscode.declareDressCode
import org.koin.android.ext.android.startKoin

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Koin
        startKoin(this, listOf(appModule))

        //Parse
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId(this.getString(R.string.parse_application_id))
                .server(this.getString(R.string.parse_server))
                .build()
        )

        //Dresscode
        declareDressCode(
            DressCode("ThemeOne", R.style.ThemeOne),
            DressCode("ThemeTwo", R.style.ThemeTwo)
        )
    }
}