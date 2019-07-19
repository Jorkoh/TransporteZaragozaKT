package com.jorkoh.transportezaragozakt

import android.net.Uri
import android.preference.PreferenceManager
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jorkoh.transportezaragozakt.db.AppDatabase
import com.jorkoh.transportezaragozakt.destinations.favorites.FavoritesViewModel
import com.jorkoh.transportezaragozakt.destinations.line_details.LineDetailsViewModel
import com.jorkoh.transportezaragozakt.destinations.map.MapViewModel
import com.jorkoh.transportezaragozakt.destinations.map.MarkerIcons
import com.jorkoh.transportezaragozakt.destinations.reminders.RemindersViewModel
import com.jorkoh.transportezaragozakt.destinations.search.SearchViewModel
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsViewModel
import com.jorkoh.transportezaragozakt.repositories.*
import com.jorkoh.transportezaragozakt.repositories.util.LiveDataCallAdapterFactory
import com.jorkoh.transportezaragozakt.services.api.APIService
import com.jorkoh.transportezaragozakt.services.api.moshi_adapters.LatLngAdapter
import com.pixplicity.generate.Rate
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
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

    single {
        AppExecutors()
    }

    single<APIService> {
        Retrofit.Builder()
            .baseUrl(APIService.BASE_URL)
            .addConverterFactory(JspoonConverterFactory.create())
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .add(LatLngAdapter())
                        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                        .build()
                )
            )
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
            .create(APIService::class.java)
    }

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

    single {
        get<AppDatabase>().stopsDao()
    }

    single {
        get<AppDatabase>().remindersDao()
    }

    single {
        PreferenceManager.getDefaultSharedPreferences(androidContext())
    }

    single{
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

    single {
        MarkerIcons(androidContext())
    }

    single<SettingsRepository> { SettingsRepositoryImplementation(get(), androidContext()) }
    single<StopsRepository> { StopsRepositoryImplementation(get(), get(), get(), get()) }
    single<BusRepository> { BusRepositoryImplementation(get(), get(), get(), get(), androidContext()) }
    single<TramRepository> { TramRepositoryImplementation(get(), get(), get(), get(), androidContext()) }
    single<FavoritesRepository> { FavoritesRepositoryImplementation(get(), get(), get()) }
    single<RemindersRepository> { RemindersRepositoryImplementation(get(), get(), get(), get(), androidContext()) }

    viewModel { FavoritesViewModel(get()) }
    viewModel { MapViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { RemindersViewModel(get()) }
    viewModel { StopDetailsViewModel(get(), get(), get()) }
    viewModel { LineDetailsViewModel(get()) }
    viewModel { MainActivityViewModel(get(), get(), get()) }
    viewModel { IntroActivityViewModel(get()) }
}