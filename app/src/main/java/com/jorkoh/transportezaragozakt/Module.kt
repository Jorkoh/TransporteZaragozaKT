package com.jorkoh.transportezaragozakt

import android.net.Uri
import android.preference.PreferenceManager
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jorkoh.transportezaragozakt.db.AppDatabase
import com.jorkoh.transportezaragozakt.destinations.favorites.FavoritesViewModel
import com.jorkoh.transportezaragozakt.destinations.line_details.LineDetailsViewModel
import com.jorkoh.transportezaragozakt.destinations.map.MapSettingsViewModel
import com.jorkoh.transportezaragozakt.destinations.map.MapViewModel
import com.jorkoh.transportezaragozakt.destinations.map.MarkerIcons
import com.jorkoh.transportezaragozakt.destinations.reminders.RemindersViewModel
import com.jorkoh.transportezaragozakt.destinations.search.SearchViewModel
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsViewModel
import com.jorkoh.transportezaragozakt.repositories.*
import com.jorkoh.transportezaragozakt.services.bus_web.BusWebService
import com.jorkoh.transportezaragozakt.services.common.util.LiveDataCallAdapterFactory
import com.jorkoh.transportezaragozakt.services.ctaz_api.CtazAPIService
import com.jorkoh.transportezaragozakt.services.official_api.OfficialAPIService
import com.jorkoh.transportezaragozakt.services.official_api.moshi_adapters.LatLngAdapter
import com.jorkoh.transportezaragozakt.services.tram_api.TramAPIService
import com.pixplicity.generate.Rate
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pl.droidsonroids.retrofit2.JspoonConverterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

const val MILLISECONDS_PER_DAY = 86400000

val appModule = module {

    // Executors
    single { AppExecutors() }

    // Services (OkHttp and some converters/factories are shared)
    single { OkHttpClient() }
    single { LiveDataCallAdapterFactory() }
    single<OfficialAPIService> {
        Retrofit.Builder()
            .baseUrl(OfficialAPIService.BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .add(LatLngAdapter())
                        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                        .build()
                )
            )
            .addCallAdapterFactory(get<LiveDataCallAdapterFactory>())
            .build()
            .create(OfficialAPIService::class.java)
    }
    single<BusWebService> {
        Retrofit.Builder()
            .baseUrl(BusWebService.BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(JspoonConverterFactory.create())
            .addCallAdapterFactory(get<LiveDataCallAdapterFactory>())
            .build()
            .create(BusWebService::class.java)
    }
    single<TramAPIService> {
        Retrofit.Builder()
            .baseUrl(TramAPIService.BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .add(LatLngAdapter())
                        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                        .build()
                )
            )
            .addCallAdapterFactory(get<LiveDataCallAdapterFactory>())
            .build()
            .create(TramAPIService::class.java)
    }
    single<CtazAPIService> {
        Retrofit.Builder()
            .baseUrl(CtazAPIService.BASE_URL)
            .client(OkHttpClient())
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .add(LatLngAdapter())
                        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                        .build()
                )
            )
            .addCallAdapterFactory(get<LiveDataCallAdapterFactory>())
            .build()
            .create(CtazAPIService::class.java)
    }

    // Room
    single {
        Room.databaseBuilder(androidApplication(), AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    get<AppExecutors>().diskIO().execute {
                        get<AppDatabase>().stopsDao().insertInitialData(androidContext())
                    }
                }
            })
            .build()
    }
    single { get<AppDatabase>().stopsDao() }
    single { get<AppDatabase>().remindersDao() }
    single { get<AppDatabase>().trackingsDao() }

    single { PreferenceManager.getDefaultSharedPreferences(androidContext()) }

    single {
        // First rate request after 5 launches, 4 days passed and positive action (loading stop details).
        // Next rate request (if the user pressed later or dismissed) after 6 launches and positive action
        Rate.Builder(androidContext())
            .setTriggerCount(5)
            .setRepeatCount(6)
            .setSwipeToDismissVisible(true)
            .setMinimumInstallTime(MILLISECONDS_PER_DAY * 4)
            .setFeedbackAction(Uri.parse(androidContext().getString(R.string.feedback_mail_uri)))
            .build()
    }

    single { MarkerIcons(androidContext()) }

    // Repositories
    single<SettingsRepository> { SettingsRepositoryImplementation(get(), androidContext()) }
    single<StopsRepository> { StopsRepositoryImplementation(get(), get(), get()) }
    single<BusRepository> { BusRepositoryImplementation(get(), get(), get(), get(), get(), get()) }
    single<TramRepository> { TramRepositoryImplementation(get(), get(), get(), get(), get(), get()) }
    single <RuralRepository> { RuralRepositoryImplementation(get(), get(), get(), get()) }
    single<FavoritesRepository> { FavoritesRepositoryImplementation(get(), get(), get()) }
    single<RemindersRepository> { RemindersRepositoryImplementation(get(), get(), get(), get(), androidContext()) }

    // ViewModels
    viewModel { FavoritesViewModel(get()) }
    viewModel { MapViewModel(get(), get()) }
    viewModel { MapSettingsViewModel(get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { RemindersViewModel(get()) }
    viewModel { StopDetailsViewModel(get(), get(), get()) }
    viewModel { LineDetailsViewModel(get()) }
    viewModel { MainActivityViewModel(get(), get(), get()) }
    viewModel { IntroActivityViewModel(get()) }
}