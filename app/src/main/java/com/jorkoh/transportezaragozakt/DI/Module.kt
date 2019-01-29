package com.jorkoh.transportezaragozakt.DI

import com.jorkoh.transportezaragozakt.Repositories.StopRepository
import com.jorkoh.transportezaragozakt.Repositories.StopRepositoryImplementation
import com.jorkoh.transportezaragozakt.Services.API.APIService
import com.jorkoh.transportezaragozakt.ViewModels.FavoritesViewModel
import com.jorkoh.transportezaragozakt.ViewModels.MainActivityViewModel
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

    single<StopRepository> { StopRepositoryImplementation(get()) }

    viewModel { FavoritesViewModel(get()) }

    viewModel { MainActivityViewModel() }
}