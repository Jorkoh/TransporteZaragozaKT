package com.jorkoh.transportezaragozakt.repositories.util

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.jorkoh.transportezaragozakt.AppExecutors
import com.jorkoh.transportezaragozakt.services.common.util.ApiEmptyResponse
import com.jorkoh.transportezaragozakt.services.common.util.ApiErrorResponse
import com.jorkoh.transportezaragozakt.services.common.util.ApiResponse
import com.jorkoh.transportezaragozakt.services.common.util.ApiSuccessResponse

abstract class NetworkBoundResourceWithBackup<ResultType, PrimaryRequestType, SecondaryRequestType, TertiaryRequestType>
@MainThread constructor(private val appExecutors: AppExecutors) {

    private val result = MediatorLiveData<Resource<ResultType>>()

    init {
        result.value = Resource.loading(null)
        @Suppress("LeakingThis")
        val dbSource = loadFromDb()
        result.addSource(dbSource) { data ->
            result.removeSource(dbSource)
            if (shouldFetch(data)) {
                fetchFromPrimaryNetwork(dbSource)
            } else {
                result.addSource(dbSource) { newData ->
                    setValue(Resource.success(newData))
                }
            }
        }
    }

    @MainThread
    private fun setValue(newValue: Resource<ResultType>) {
        if (result.value != newValue) {
            result.value = newValue
        }
    }

    private fun fetchFromPrimaryNetwork(dbSource: LiveData<ResultType>) {
        val apiResponse = createPrimaryCall()
        // we re-attach dbSource as a new source, it will dispatch its latest value quickly
        result.addSource(dbSource) { newData ->
            setValue(Resource.loading(newData))
        }
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            result.removeSource(dbSource)
            when (response) {
                is ApiSuccessResponse -> {
                    appExecutors.diskIO().execute {
                        saveCallResult(processPrimaryResponse(response))
                        appExecutors.mainThread().execute {
                            // we specially request a new live data,
                            // otherwise we will get immediately last cached value,
                            // which may not be updated with latest results received from network.
                            result.addSource(loadFromDb()) { newData ->
                                setValue(Resource.success(newData))
                            }
                        }
                    }
                }
                is ApiEmptyResponse -> {
                    appExecutors.mainThread().execute {
                        // reload from disk whatever we had
                        result.addSource(loadFromDb()) { newData ->
                            setValue(Resource.success(newData))
                        }
                    }
                }
                is ApiErrorResponse -> {
                    onPrimaryFetchFailed()
                    fetchFromSecondaryNetwork(dbSource)
                }
            }
        }
    }

    private fun fetchFromSecondaryNetwork(dbSource: LiveData<ResultType>){
        val apiResponse = createSecondaryCall()
        // we re-attach dbSource as a new source, it will dispatch its latest value quickly
        result.addSource(dbSource) { newData ->
            setValue(Resource.loading(newData))
        }
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            result.removeSource(dbSource)
            when (response) {
                is ApiSuccessResponse -> {
                    appExecutors.diskIO().execute {
                        saveCallResult(processSecondaryResponse(response))
                        appExecutors.mainThread().execute {
                            // we specially request a new live data,
                            // otherwise we will get immediately last cached value,
                            // which may not be updated with latest results received from network.
                            result.addSource(loadFromDb()) { newData ->
                                setValue(Resource.success(newData))
                            }
                        }
                    }
                }
                is ApiEmptyResponse -> {
                    appExecutors.mainThread().execute {
                        // reload from disk whatever we had
                        result.addSource(loadFromDb()) { newData ->
                            setValue(Resource.success(newData))
                        }
                    }
                }
                is ApiErrorResponse -> {
                    onSecondaryFetchFailed()
                    fetchFromTertiaryNetwork(dbSource)
                }
            }
        }
    }

    private fun fetchFromTertiaryNetwork(dbSource: LiveData<ResultType>){
        val apiResponse = createTertiaryCall()
        // we re-attach dbSource as a new source, it will dispatch its latest value quickly
        result.addSource(dbSource) { newData ->
            setValue(Resource.loading(newData))
        }
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            result.removeSource(dbSource)
            when (response) {
                is ApiSuccessResponse -> {
                    appExecutors.diskIO().execute {
                        saveCallResult(processTertiaryResponse(response))
                        appExecutors.mainThread().execute {
                            // we specially request a new live data,
                            // otherwise we will get immediately last cached value,
                            // which may not be updated with latest results received from network.
                            result.addSource(loadFromDb()) { newData ->
                                setValue(Resource.success(newData))
                            }
                        }
                    }
                }
                is ApiEmptyResponse -> {
                    appExecutors.mainThread().execute {
                        // reload from disk whatever we had
                        result.addSource(loadFromDb()) { newData ->
                            setValue(Resource.success(newData))
                        }
                    }
                }
                is ApiErrorResponse -> {
                    onTertiaryFetchFailed()
                    result.addSource(dbSource) { newData ->
                        setValue(
                            Resource.error(
                                response.errorMessage,
                                newData
                            )
                        )
                    }
                }
            }
        }
    }

    protected open fun onPrimaryFetchFailed() {}

    protected open fun onSecondaryFetchFailed() {}

    protected open fun onTertiaryFetchFailed() {}

    fun asLiveData() = result as LiveData<Resource<ResultType>>

    @WorkerThread
    protected abstract fun processPrimaryResponse(response: ApiSuccessResponse<PrimaryRequestType>) : ResultType

    @WorkerThread
    protected abstract fun processSecondaryResponse(response: ApiSuccessResponse<SecondaryRequestType>): ResultType

    @WorkerThread
    protected abstract fun processTertiaryResponse(response: ApiSuccessResponse<TertiaryRequestType>): ResultType

    @WorkerThread
    protected abstract fun saveCallResult(result: ResultType)

    @MainThread
    protected abstract fun shouldFetch(data: ResultType?): Boolean

    @MainThread
    protected abstract fun loadFromDb(): LiveData<ResultType>

    @MainThread
    protected abstract fun createPrimaryCall(): LiveData<ApiResponse<PrimaryRequestType>>

    @MainThread
    protected abstract fun createSecondaryCall(): LiveData<ApiResponse<SecondaryRequestType>>

    @MainThread
    protected abstract fun createTertiaryCall(): LiveData<ApiResponse<TertiaryRequestType>>
}

