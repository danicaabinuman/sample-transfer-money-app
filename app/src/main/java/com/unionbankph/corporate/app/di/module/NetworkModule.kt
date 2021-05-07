package com.unionbankph.corporate.app.di.module

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.di.scope.PerApplication
import com.unionbankph.corporate.app.util.SettingsUtil
import com.unionbankph.corporate.common.data.source.remote.retrofit.interceptor.ConnectivityInterceptor
import com.unionbankph.corporate.common.data.source.remote.retrofit.interceptor.UserAgentInterceptor
import com.unionbankph.corporate.common.presentation.constant.Environment
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import dagger.Module
import dagger.Provides
import okhttp3.CertificatePinner
import okhttp3.ConnectionSpec
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.security.GeneralSecurityException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

@Module
class NetworkModule {

    @Provides
    @PerApplication
    internal fun provideRestAdapter(
        @Named("prodProvideOkHttpClient")
        okHttpClient: OkHttpClient
    ): Retrofit {
        val contentType = MediaType.get("application/json")
        return Retrofit.Builder()
            .baseUrl(BuildConfig.HOST)
            .addConverterFactory(JsonHelper.json.asConverterFactory(contentType))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @PerApplication
    @Named("prodProvideOkHttpClient")
    internal fun prodProvideOkHttpClient(
        context: Context,
        settingsUtil: SettingsUtil
    ): OkHttpClient {
        val builder: OkHttpClient.Builder =
            if (Environment.PRODUCTION || Environment.STAGING) {
                safeOkHttpClient()
            } else {
                unsafeOkHttpClient()
            }
        builder.addInterceptor(ConnectivityInterceptor(context))
        builder.addInterceptor(UserAgentInterceptor(settingsUtil.getDefaultUserAgent()))
        builder.readTimeout(
            context.resources.getInteger(R.integer.time_api_request).toLong(),
            TimeUnit.SECONDS
        )
        builder.connectTimeout(
            context.resources.getInteger(R.integer.time_api_request).toLong(),
            TimeUnit.SECONDS
        )
        builder.writeTimeout(
            context.resources.getInteger(R.integer.time_api_request).toLong(),
            TimeUnit.SECONDS
        )
        if (BuildConfig.DEBUG) {
            builder.addNetworkInterceptor(StethoInterceptor())
            val logInterceptor = HttpLoggingInterceptor()
            logInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(logInterceptor)
        }
        builder.cache(null)
        return builder.build()
    }

    private fun safeOkHttpClient(): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()
        val connectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2, TlsVersion.TLS_1_1)
            .build()
        val prodHostName = "business.unionbankph.com"
        val stagingHostName = "demo-business.unionbankph.com"
        builder.connectionSpecs(mutableListOf(connectionSpec, ConnectionSpec.CLEARTEXT))
        builder.certificatePinner(
            CertificatePinner.Builder().apply {
                add(prodHostName, "sha256/sM9MvJYFTDkfa8skjJY8De54DGR7ZkZS5F5/vMVKbO4=")
                add(prodHostName, "sha256/86fLIetopQLDNxFZ0uMI66Xpl1pFgLlHHn9v6kT0i4I=")
                add(prodHostName, "sha256/cGuxAXyFXFkWm61cF4HPWX8S0srS9j0aSqN0k4AP+4A=")
                add(stagingHostName, "sha256/KEdBIsJmdeJuy5JJa1cm2u/rx+odZaI3HlgS8pZP3m0=")
                add(stagingHostName, "sha256/86fLIetopQLDNxFZ0uMI66Xpl1pFgLlHHn9v6kT0i4I=")
                add(stagingHostName, "sha256/cGuxAXyFXFkWm61cF4HPWX8S0srS9j0aSqN0k4AP+4A=")
            }.build()
        )
        return builder
    }

    private fun unsafeOkHttpClient(): OkHttpClient.Builder {
        val trustManager: X509TrustManager
        val ssLSocketFactory: SSLSocketFactory
        try {
            trustManager = unsafeTrustManager()
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(trustManager), java.security.SecureRandom())
            ssLSocketFactory = sslContext.socketFactory
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        return OkHttpClient.Builder().apply {
            sslSocketFactory(ssLSocketFactory, trustManager)
        }
    }

    @Throws(GeneralSecurityException::class)
    private fun unsafeTrustManager(): X509TrustManager {
        val trustManager: X509TrustManager
        try {
            trustManager = object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                    // checkClientTrusted
                }

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                    // checkServerTrusted
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return emptyArray()
                }
            }
        } catch (e: Exception) {
            throw GeneralSecurityException(e)
        }
        return trustManager
    }
}
