package com.jorkoh.transportezaragozakt

import androidx.room.Room
import com.jorkoh.transportezaragozakt.db.AppDatabase
import com.jorkoh.transportezaragozakt.repositories.BusRepository
import com.jorkoh.transportezaragozakt.repositories.BusRepositoryImplementation
import com.jorkoh.transportezaragozakt.repositories.TramRepository
import com.jorkoh.transportezaragozakt.repositories.TramRepositoryImplementation
import com.jorkoh.transportezaragozakt.services.api.APIService
import com.jorkoh.transportezaragozakt.services.api.moshi_adapters.LatLngAdapter
import com.jorkoh.transportezaragozakt.view_models.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors


val appModule = module {

    single<Executor>{
        Executors.newSingleThreadExecutor()
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
            .build()
            .create(APIService::class.java)
    }

    single {
        Room.databaseBuilder(androidApplication(), AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .build()
    }

    single{
        Executors.newSingleThreadExecutor()
    }

    single{
        get<AppDatabase>().stopsDao()
    }

    single<BusRepository> { BusRepositoryImplementation(get(), get(), get()) }
    single<TramRepository> { TramRepositoryImplementation(get(), get(), get()) }

    viewModel { FavoritesViewModel() }

    viewModel { MapViewModel(get(), get()) }

    viewModel { SearchViewModel() }

    viewModel { MoreViewModel() }

    viewModel { StopDetailsViewModel(get(), get()) }

    viewModel { MainActivityViewModel() }
}