package com.jorkoh.transportezaragozakt.DI

import androidx.lifecycle.LiveData
import com.jorkoh.transportezaragozakt.Models.BusStop.BusStopModel
import com.jorkoh.transportezaragozakt.Models.BusStopLocations.BusStopLocationsModel
import com.jorkoh.transportezaragozakt.Repositories.StopRepository
import com.jorkoh.transportezaragozakt.Repositories.BusStopRepository
import com.jorkoh.transportezaragozakt.Services.API.APIService
import com.jorkoh.transportezaragozakt.ViewModels.*
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val appModule = module {

    single<APIService> {
        Retrofit.Builder()
            .baseUrl(APIService.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(APIService::class.java)
    }

    single<StopRepository> { BusStopRepository(get()) }

    viewModel { FavoritesViewModel(get()) }

    viewModel { MapViewModel(get()) }

    viewModel { SearchViewModel() }

    viewModel { MoreViewModel() }

    viewModel { MainActivityViewModel() }
}