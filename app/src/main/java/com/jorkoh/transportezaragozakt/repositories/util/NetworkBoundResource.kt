package com.jorkoh.transportezaragozakt.repositories.util

import com.jorkoh.transportezaragozakt.services.common.util.ApiErrorResponse
import com.jorkoh.transportezaragozakt.services.common.util.ApiResponse
import com.jorkoh.transportezaragozakt.services.common.util.ApiSuccessResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@ExperimentalCoroutinesApi
abstract class NetworkBoundResource<ResultType, RequestType> {

    private val result: Flow<Resource<ResultType>>

    init {
        result = flow {
            emit(Resource.loading(null))
            val dbData = loadFromDb()
            if (shouldFetch(dbData)) {
                when (val response = fetchData()) {
                    is ApiSuccessResponse -> {
                        saveCallResult(processResponse(response))
                        emit(Resource.success(loadFromDb()))
                    }
                    is ApiErrorResponse -> {
                        emit(Resource.error(response.errorMessage, loadFromDb()))
                    }
                }
            } else {
                emit(Resource.success(dbData))
            }
        }
    }

    fun asFlow() = result

    protected abstract fun processResponse(response: ApiSuccessResponse<RequestType>): ResultType

    protected abstract suspend fun saveCallResult(result: ResultType)

    protected abstract fun shouldFetch(data: ResultType?): Boolean

    protected abstract suspend fun loadFromDb(): ResultType

    protected abstract suspend fun fetchData(): ApiResponse<RequestType>
}

