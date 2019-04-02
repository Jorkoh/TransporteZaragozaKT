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

        //Parse
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId(this.getString(R.string.parse_application_id))
                .server(this.getString(R.string.parse_server))
                .build()
        )

        // Cyanea
        Cyanea.init(this, resources)
    }
}