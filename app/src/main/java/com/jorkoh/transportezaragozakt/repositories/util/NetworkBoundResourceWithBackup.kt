package com.jorkoh.transportezaragozakt.repositories.util

import com.jorkoh.transportezaragozakt.services.common.util.ApiErrorResponse
import com.jorkoh.transportezaragozakt.services.common.util.ApiResponse
import com.jorkoh.transportezaragozakt.services.common.util.ApiSuccessResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

abstract class NetworkBoundResourceWithBackup<ResultType, PrimaryRequestType, SecondaryRequestType, TertiaryRequestType> {

    private val result: Flow<Resource<ResultType>>

    init {
        result = flow {
            emit(Resource.loading(null))
            val dbData = loadFromDb()
            if (shouldFetch(dbData)) {
                when (val response = fetchPrimarySource()) {
                    is ApiSuccessResponse -> {
                        saveCallResult(processPrimaryResponse(response))
                        emit(Resource.success(loadFromDb()))
                    }
                    is ApiErrorResponse -> {
                        useSecondarySource(this)
                    }
                }
            } else {
                emit(Resource.success(dbData))
            }
        }
    }

    private suspend fun useSecondarySource(flowCollector: FlowCollector<Resource<ResultType>>) {
        when (val response = fetchSecondarySource()) {
            is ApiSuccessResponse -> {
                saveCallResult(processSecondaryResponse(response))
                flowCollector.emit(Resource.success(loadFromDb()))
            }
            is ApiErrorResponse -> {
                useTertiarySource(flowCollector)
            }
        }
    }

    private suspend fun useTertiarySource(flowCollector: FlowCollector<Resource<ResultType>>) {
        when (val response = fetchTertiarySource()) {
            is ApiSuccessResponse -> {
                saveCallResult(processTertiaryResponse(response))
                flowCollector.emit(Resource.success(loadFromDb()))
            }
            is ApiErrorResponse -> {
                flowCollector.emit(Resource.error(response.errorMessage, loadFromDb()))
            }
        }
    }

    fun asFlow() = result

    protected abstract fun processPrimaryResponse(response: ApiSuccessResponse<PrimaryRequestType>): ResultType

    protected abstract fun processSecondaryResponse(response: ApiSuccessResponse<SecondaryRequestType>): ResultType

    protected abstract fun processTertiaryResponse(response: ApiSuccessResponse<TertiaryRequestType>): ResultType

    protected abstract suspend fun saveCallResult(result: ResultType)

    protected abstract fun shouldFetch(data: ResultType?): Boolean

    protected abstract suspend fun loadFromDb(): ResultType

    protected abstract suspend fun fetchPrimarySource(): ApiResponse<PrimaryRequestType>

    protected abstract suspend fun fetchSecondarySource(): ApiResponse<SecondaryRequestType>

    protected abstract suspend fun fetchTertiarySource(): ApiResponse<TertiaryRequestType>
}

