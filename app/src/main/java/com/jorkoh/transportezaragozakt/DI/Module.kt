package com.jorkoh.transportezaragozakt.DI

import com.jorkoh.transportezaragozakt.Repositories.BusRepository
import com.jorkoh.transportezaragozakt.Repositories.BusRepositoryImplementation
import com.jorkoh.transportezaragozakt.Repositories.TramRepository
import com.jorkoh.transportezaragozakt.Repositories.TramRepositoryImplementation
import com.jorkoh.transportezaragozakt.Services.API.APIService
import com.jorkoh.transportezaragozakt.Services.API.MoshiAdapters.LatLngAdapter
import com.jorkoh.transportezaragozakt.ViewModels.*
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

    viewModel { MainActivityViewModel() }
}