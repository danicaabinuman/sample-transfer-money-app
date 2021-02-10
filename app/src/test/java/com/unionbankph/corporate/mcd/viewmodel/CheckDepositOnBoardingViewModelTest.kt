package com.unionbankph.corporate.mcd.viewmodel

import com.unionbankph.corporate.common.BaseTest
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.mcd.presentation.onboarding.CheckDepositOnBoardingViewModel
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by herald25santos on 3/6/20
 */
class CheckDepositOnBoardingViewModelTest : BaseTest() {

    @RelaxedMockK
    lateinit var schedulerProvider: SchedulerProvider

    @RelaxedMockK
    lateinit var settingsGateway: SettingsGateway

    lateinit var viewModel: CheckDepositOnBoardingViewModel

    override fun setupBeforeTest() {
        super.setupBeforeTest()
        viewModel = CheckDepositOnBoardingViewModel(schedulerProvider, settingsGateway)
    }

    override fun isMockServerEnabled(): Boolean = false

    @Test
    fun `launch on boarding mcd, then check if it's mcd first launch`() {
        val expectedResult: Boolean = false
        every {
            settingsGateway.isFirstCheckDeposit()
        } returns Single.just(expectedResult)

        viewModel.isFirstCheckDeposit()

        assertEquals(expectedResult, viewModel.isInitialLoad.value)
    }

}