package com.unionbankph.corporate.common.data.source.remote.retrofit.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Intercepts every request, adds `clientId` and `clientSecret` into query parameters.
 */
class RequestInterceptor(private val clientId: String, private val clientSecret: String) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalHttpUrl = original.url()

        val url = originalHttpUrl
            .newBuilder()
            .build()

        val requestBuilder = original.newBuilder().url(url)
        val request = requestBuilder
            .addHeader("Content-Type", "application/json")
            .addHeader("x-client-id", clientId)
            .addHeader("x-client-secret", clientSecret)
            .build()

        return chain.proceed(request)
    }
}
