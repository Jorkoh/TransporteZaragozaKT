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
            DressCode("SigFig", R.style.SigFig),
            DressCode("Cyanea", R.style.Cyanea),
            DressCode("Rio", R.style.Rio),
            DressCode("VitaminSea", R.style.VitaminSea),
            DressCode("TextileDyes", R.style.TextileDyes),
            DressCode("Brave", R.style.Brave),
            DressCode("Ashen", R.style.Ashen),
            DressCode("RecognEyes", R.style.RecognEyes),
            DressCode("Monokai", R.style.Monokai),
            DressCode("SoundCloud", R.style.SoundCloud),
            DressCode("Materiallight", R.style.Materiallight),
            DressCode("Hololight", R.style.Hololight),
            DressCode("Materialdark", R.style.Materialdark),
            DressCode("Holodark", R.style.Holodark),
            DressCode("Cherrypie", R.style.Cherrypie),
            DressCode("Olive", R.style.Olive),
            DressCode("Tasty", R.style.Tasty),
            DressCode("Desert", R.style.Desert),
            DressCode("Weber", R.style.Weber),
            DressCode("Philips", R.style.Philips),
            DressCode("Instagram", R.style.Instagram),
            DressCode("Reddit", R.style.Reddit),
            DressCode("Waaark", R.style.Waaark),
            DressCode("Flax", R.style.Flax),
            DressCode("Bing", R.style.Bing),
            DressCode("Meetup", R.style.Meetup),
            DressCode("Coffee", R.style.Coffee),
            DressCode("Sepia", R.style.Sepia),
            DressCode("Oblivion", R.style.Oblivion),
            DressCode("Obsidian", R.style.Obsidian),
            DressCode("Forrest", R.style.Forrest),
            DressCode("Dawn", R.style.Dawn),
            DressCode("Twitter", R.style.Twitter),
            DressCode("Periscope", R.style.Periscope),
            DressCode("Subdued", R.style.Subdued),
            DressCode("MistyGreen", R.style.MistyGreen),
            DressCode("Waterfall", R.style.Waterfall),
            DressCode("Retro", R.style.Retro),
            DressCode("Inkpot", R.style.Inkpot),
            DressCode("VibrantInk", R.style.VibrantInk),
            DressCode("Fresh", R.style.Fresh),
            DressCode("Coteazur", R.style.Coteazur),
            DressCode("QED", R.style.QED),
            DressCode("Vintage", R.style.Vintage),
            DressCode("Spotify", R.style.Spotify),
            DressCode("Xbox", R.style.Xbox),
            DressCode("Sublime", R.style.Sublime),
            DressCode("Zenburn", R.style.Zenburn),
            DressCode("Starbucks", R.style.Starbucks),
            DressCode("Amazon", R.style.Amazon),
            DressCode("Twitch", R.style.Twitch),
            DressCode("Robinhood", R.style.Robinhood),
            DressCode("Facebook", R.style.Facebook),
            DressCode("Mint", R.style.Mint)
        )
    }
}