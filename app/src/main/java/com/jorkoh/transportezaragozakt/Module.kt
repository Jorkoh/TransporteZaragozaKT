package com.jorkoh.transportezaragozakt

import com.jorkoh.transportezaragozakt.repositories.BusRepository
import com.jorkoh.transportezaragozakt.repositories.BusRepositoryImplementation
import com.jorkoh.transportezaragozakt.repositories.TramRepository
import com.jorkoh.transportezaragozakt.repositories.TramRepositoryImplementation
import com.jorkoh.transportezaragozakt.services.API.APIService
import com.jorkoh.transportezaragozakt.services.API.MoshiAdapters.LatLngAdapter
import com.jorkoh.transportezaragozakt.view_models.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
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

    single<BusRepository> { BusRepositoryImplementation(get()) }
    single<TramRepository> { TramRepositoryImplementation(get()) }

    viewModel { FavoritesViewModel(get()) }

    viewModel { MapViewModel(get(), get()) }

    viewModel { SearchViewModel() }

    viewModel { MoreViewModel() }

    viewModel { StopDetailsViewModel(get(), get()) }

    viewModel { MainActivityViewModel() }
}