package com.unionbankph.corporate.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.unionbankph.corporate.app.di.scope.PerApplication
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import dagger.Module
import dagger.Provides
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import javax.inject.Singleton

@Module
class NetworkModuleTest(private val baseUrl: String) {

    @Provides
    @PerApplication
    internal fun provideRestAdapter(): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(JsonHelper.json.asConverterFactory(contentType))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

}