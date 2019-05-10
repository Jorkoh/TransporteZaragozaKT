package com.jorkoh.transportezaragozakt

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jorkoh.transportezaragozakt.db.AppDatabase
import com.jorkoh.transportezaragozakt.destinations.favorites.FavoritesViewModel
import com.jorkoh.transportezaragozakt.destinations.map.MapViewModel
import com.jorkoh.transportezaragozakt.destinations.more.MoreViewModel
import com.jorkoh.transportezaragozakt.destinations.reminders.RemindersViewModel
import com.jorkoh.transportezaragozakt.destinations.search.SearchViewModel
import com.jorkoh.transportezaragozakt.repositories.*
import com.jorkoh.transportezaragozakt.services.api.APIService
import com.jorkoh.transportezaragozakt.services.api.moshi_adapters.LatLngAdapter
import com.jorkoh.transportezaragozakt.destinations.stop_details.StopDetailsViewModel
import com.jorkoh.transportezaragozakt.repositories.util.LiveDataCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*


val appModule = module {

    single {
        AppExecutors()
    }

    single<APIService> {
        Retrofit.Builder()
            .baseUrl(APIService.BASE_URL)
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
        androidContext().getSharedPreferences(
            androidContext().getString(R.string.preferences_version_number_key),
            Context.MODE_PRIVATE
        )
    }

    single<SettingsRepository> { SettingsRepositoryImplementation(get(), androidContext()) }
    single<StopsRepository> { StopsRepositoryImplementation(get(), get(), get(), get()) }
    single<BusRepository> { BusRepositoryImplementation(get(), get(), get(), get(), androidContext()) }
    single<TramRepository> { TramRepositoryImplementation(get(), get(), get(), get(), androidContext()) }
    single<FavoritesRepository> { FavoritesRepositoryImplementation(get(), get(), get()) }
    single<RemindersRepository> { RemindersRepositoryImplementation(get(), get(), get(), get(), androidContext()) }

    viewModel { FavoritesViewModel(get()) }

    viewModel { MapViewModel(get(), get()) }

    viewModel { SearchViewModel() }

    viewModel { RemindersViewModel(get()) }

    viewModel { MoreViewModel() }

    viewModel { StopDetailsViewModel(get(), get(), get()) }

    viewModel { MainActivityViewModel() }
}