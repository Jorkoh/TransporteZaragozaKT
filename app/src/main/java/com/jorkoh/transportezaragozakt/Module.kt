package com.jorkoh.transportezaragozakt

import androidx.room.Room
import com.jorkoh.transportezaragozakt.db.AppDatabase
import com.jorkoh.transportezaragozakt.db.Converters
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
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*


val appModule = module {

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

    single{
        Moshi.Builder().build().adapter<List<Int>>(Types.newParameterizedType(List::class.java, Integer::class.java))
    }

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .build()
    }

    single{
        get<AppDatabase>().busDao()
    }

    single<BusRepository> { BusRepositoryImplementation(get(), get()) }
    single<TramRepository> { TramRepositoryImplementation(get()) }

    viewModel { FavoritesViewModel() }

    viewModel { MapViewModel(get(), get()) }

    viewModel { SearchViewModel() }

    viewModel { MoreViewModel() }

    viewModel { StopDetailsViewModel(get(), get()) }

    viewModel { MainActivityViewModel() }
}