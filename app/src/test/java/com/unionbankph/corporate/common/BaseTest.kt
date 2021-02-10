package com.unionbankph.corporate.common

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import io.mockk.MockKAnnotations
import io.mockk.mockk
import io.mockk.unmockkAll
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.File

abstract class BaseTest {

    // A JUnit Test Rule that swaps the background executor used by
    // the Architecture Components with a different one which executes each task synchronously.
    // You can use this rule for your host side tests that use Architecture Components.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    companion object {
        @ClassRule
        @JvmField
        val schedulers = RxImmediateSchedulerRule()
    }

    lateinit var mockServer: MockWebServer

    lateinit var retrofit: Retrofit

    val context = mockk<Context>(relaxed = true)

    @Before
    open fun setUp() {
        this.configureMockServer()
        this.configureDi()
        this.setupBeforeTest()
    }

    @After
    open fun tearDown() {
        unmockkAll()
        this.stopMockServer()
    }

    // CONFIGURATION
    open fun setupBeforeTest() {
        MockKAnnotations.init(this)
    }

    // MOCK SERVER
    abstract fun isMockServerEnabled(): Boolean // Because we don't want it always enabled on all tests

    open fun configureMockServer() {
        if (isMockServerEnabled()) {
            mockServer = MockWebServer()
            mockServer.start()
        }
    }

    private fun provideRetrofit() {
        val contentType = "application/json".toMediaType()
        retrofit = Retrofit.Builder()
            .baseUrl(mockServer.url("/").toString())
            .addConverterFactory(JsonHelper.json.asConverterFactory(contentType))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    open fun configureDi() {
        if (isMockServerEnabled()) {
            provideRetrofit()
        }
    }

    open fun stopMockServer() {
        if (isMockServerEnabled()) {
            mockServer.shutdown()
        }
    }

    open fun mockHttpResponse(fileName: String, responseCode: Int) = mockServer.enqueue(
        MockResponse()
            .setResponseCode(responseCode)
            .setBody(getJson(fileName))
    )

    fun getFileFromPath(fileName: String): File {
        val uri = this.javaClass.classLoader?.getResource(fileName)
        return File(uri?.path)
    }

    fun getJson(fileName: String): String {
        val uri = this.javaClass.classLoader?.getResource("raw/json/${fileName}.json")
        val file = File(uri?.path)
        return String(file.readBytes())
    }
}