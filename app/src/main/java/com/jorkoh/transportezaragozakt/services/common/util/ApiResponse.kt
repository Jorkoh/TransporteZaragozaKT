package com.jorkoh.transportezaragozakt.services.common.util

import okhttp3.HttpUrl
import retrofit2.Response

/**
 * Common class used by API responses.
 * @param <T> the type of the response object
</T> */
sealed class ApiResponse<T> {
    companion object {
        fun <T> create(error: Throwable): ApiErrorResponse<T> {
            return ApiErrorResponse(error.message ?: "unknown error")
        }

        fun <T> create(response: Response<T>): ApiResponse<T> {
            return if (response.isSuccessful) {
                val body = response.body()
                if (body == null || response.code() == 204) {
                    ApiErrorResponse("empty response")
                } else {
                    ApiSuccessResponse(
                        body = body,
                        requestURL = response.raw().request().url()
                    )
                }
            } else {
                val msg = response.errorBody()?.string()
                val errorMsg = if (msg.isNullOrEmpty()) {
                    response.message()
                } else {
                    msg
                }
                ApiErrorResponse(errorMsg ?: "unknown error")
            }
        }
    }
}

data class ApiSuccessResponse<T>(
    val body: T,
    val requestURL : HttpUrl
) : ApiResponse<T>()

data class ApiErrorResponse<T>(val errorMessage: String) : ApiResponse<T>()
