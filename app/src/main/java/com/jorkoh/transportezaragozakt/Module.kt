package com.jorkoh.transportezaragozakt

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jorkoh.transportezaragozakt.db.AppDatabase
import com.jorkoh.transportezaragozakt.repositories.*
import com.jorkoh.transportezaragozakt.services.api.APIService
import com.jorkoh.transportezaragozakt.services.api.moshi_adapters.LatLngAdapter
import com.jorkoh.transportezaragozakt.util.LiveDataCallAdapterFactory
import com.jorkoh.transportezaragozakt.view_models.*
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

    single<StopsRepository> { StopsRepositoryImplementation(get(), get()) }
    single<BusRepository> { BusRepositoryImplementation(get(), get(), get(), get(), androidContext()) }
    single<TramRepository> { TramRepositoryImplementation(get(), get(), get(), get(), androidContext()) }
    single<FavoritesRepository> { FavoritesRepositoryImplementation(get(), get(), get()) }

    viewModel { FavoritesViewModel(get()) }

    viewModel { MapViewModel(get()) }

    viewModel { SearchViewModel() }

    viewModel { MoreViewModel() }

    viewModel { StopDetailsViewModel(get(), get()) }

    viewModel { MainActivityViewModel() }
}