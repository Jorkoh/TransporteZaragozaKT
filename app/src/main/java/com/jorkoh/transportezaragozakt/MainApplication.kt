package com.jorkoh.transportezaragozakt

import android.app.Application
import com.parse.Parse
import daio.io.dresscode.DressCode
import daio.io.dresscode.declareDressCode
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Koin
        startKoin {
            androidContext(this@MainApplication)
            modules(appModule)
        }

        //Parse
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId(this.getString(R.string.parse_application_id))
                .server(this.getString(R.string.parse_server))
                .build()
        )

        //Dresscode
        declareDressCode(
            DressCode(getString(R.string.theme_zaragoza_transport), R.style.ZaragozaTransport),
            DressCode(getString(R.string.theme_kingfisher), R.style.Kingfisher),
            DressCode(getString(R.string.theme_porcelain), R.style.Porcelain),
            DressCode(getString(R.string.theme_cyanea), R.style.Cyanea),
            DressCode(getString(R.string.theme_coral), R.style.Coral),
            DressCode(getString(R.string.theme_ashen), R.style.Ashen),
            DressCode(getString(R.string.theme_watermelon), R.style.Watermelon),
            DressCode(getString(R.string.theme_deep_sea), R.style.DeepSea),
            DressCode(getString(R.string.theme_midnight), R.style.Midnight),
            DressCode(getString(R.string.theme_cerulean), R.style.Cerulean),
            DressCode(getString(R.string.theme_flax), R.style.Flax),
            DressCode(getString(R.string.theme_coffee), R.style.Coffee),
            DressCode(getString(R.string.theme_sky), R.style.Sky),
            DressCode(getString(R.string.theme_breeze), R.style.Breeze),
            DressCode(getString(R.string.theme_inkpot), R.style.Inkpot),
            DressCode(getString(R.string.theme_vintage), R.style.Vintage),
            DressCode(getString(R.string.theme_jade), R.style.Jade),
            DressCode(getString(R.string.theme_grape), R.style.Grape),
            DressCode(getString(R.string.theme_cobalt), R.style.Cobalt),
            DressCode(getString(R.string.theme_mint), R.style.Mint)
        )
    }
}