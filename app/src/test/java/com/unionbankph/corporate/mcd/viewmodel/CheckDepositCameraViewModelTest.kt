package com.unionbankph.corporate.mcd.viewmodel

import com.unionbankph.corporate.common.BaseTest
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.getOrAwaitValue
import com.unionbankph.corporate.mcd.presentation.camera.CheckDepositCameraViewModel
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Assert.assertEquals

/**
 * Created by herald25santos on 3/6/20
 */
class CheckDepositCameraViewModelTest : BaseTest() {

    @RelaxedMockK
    lateinit var schedulerProvider: SchedulerProvider

    @RelaxedMockK
    lateinit var cacheManager: CacheManager

    lateinit var viewModel: CheckDepositCameraViewModel

    override fun setupBeforeTest() {
        super.setupBeforeTest()
        viewModel = CheckDepositCameraViewModel(schedulerProvider, cacheManager, context)
    }

    override fun isMockServerEnabled(): Boolean = false

    fun `take checks, then it should decompress file result`() {
        val checkFile = getFileFromPath("raw/cheque.jpg")

        viewModel.onPictureTaken(checkFile)

        val path = viewModel.navigateNextStep.getOrAwaitValue().peekContent().path
        assertEquals(path, checkFile.path)
    }

}