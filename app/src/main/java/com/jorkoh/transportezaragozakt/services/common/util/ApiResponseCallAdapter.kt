package com.jorkoh.transportezaragozakt.services.common.util

import okhttp3.Request
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response


class ApiResponseCallAdapter<T>(
    private val clazz: Class<T>
) : CallAdapter<T, Call<ApiResponse<T>>> {
    override fun responseType() = clazz
    override fun adapt(call: Call<T>): Call<ApiResponse<T>> = ApiResponseCall(call)
}

class ApiResponseCall<T>(proxy: Call<T>) : CallDelegate<T, ApiResponse<T>>(proxy) {
    override fun enqueueImpl(callback: Callback<ApiResponse<T>>) = proxy.enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            callback.onResponse(this@ApiResponseCall, Response.success(ApiResponse.create(response)))
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            callback.onResponse(this@ApiResponseCall, Response.success(ApiResponse.create(t)))
        }
    })

    override fun cloneImpl() = ApiResponseCall(proxy.clone())
}

abstract class CallDelegate<TIn, TOut>(
    protected val proxy: Call<TIn>
) : Call<TOut> {
    override fun execute(): Response<TOut> = throw NotImplementedError()
    final override fun enqueue(callback: Callback<TOut>) = enqueueImpl(callback)
    final override fun clone(): Call<TOut> = cloneImpl()

    override fun cancel() = proxy.cancel()
    override fun request(): Request = proxy.request()
    override fun isExecuted() = proxy.isExecuted
    override fun isCanceled() = proxy.isCanceled

    abstract fun enqueueImpl(callback: Callback<TOut>)
    abstract fun cloneImpl(): Call<TOut>
}