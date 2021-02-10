package com.unionbankph.corporate.mcd.viewmodel

import com.unionbankph.corporate.common.BaseTest
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.getOrAwaitValue
import com.unionbankph.corporate.mcd.util.CheckDepositMockResponse
import com.unionbankph.corporate.mcd.data.gateway.CheckDepositGateway
import com.unionbankph.corporate.mcd.presentation.preview.CheckDepositPreviewViewModel
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by herald25santos on 3/6/20
 */
class CheckDepositPreviewViewModelTest : BaseTest() {

    @RelaxedMockK
    lateinit var schedulerProvider: SchedulerProvider

    @RelaxedMockK
    lateinit var checkDepositGateway: CheckDepositGateway

    @RelaxedMockK
    lateinit var cacheManager: CacheManager

    @RelaxedMockK
    lateinit var mockResponse: CheckDepositMockResponse

    lateinit var viewModel: CheckDepositPreviewViewModel

    override fun setupBeforeTest() {
        super.setupBeforeTest()
        viewModel =
            CheckDepositPreviewViewModel(schedulerProvider, checkDepositGateway, cacheManager)
    }

    override fun isMockServerEnabled(): Boolean = false

    @Test
    fun `upload check file, then it should return check details`() {
        val checkFile = getFileFromPath("raw/cheque.jpg")
        val response = mockResponse.getUploadFileResponse()

        every {
            checkDepositGateway.checkDepositUploadFile(
                checkFile, "front", ""
            )
        } returns Single.just(response)

        viewModel.uploadCheckDeposit(checkFile, "front")

        verify {
            checkDepositGateway.checkDepositUploadFile(
                checkFile, "front", ""
            )
        }
        val actualResponse = viewModel.checkDepositUpload.getOrAwaitValue()
        assertEquals(response, actualResponse)
    }

}