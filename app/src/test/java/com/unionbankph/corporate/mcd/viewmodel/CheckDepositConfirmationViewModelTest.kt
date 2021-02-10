package com.unionbankph.corporate.mcd.viewmodel

import com.unionbankph.corporate.common.BaseTest
import com.unionbankph.corporate.common.getOrAwaitValue
import com.unionbankph.corporate.mcd.util.CheckDepositMockResponse
import com.unionbankph.corporate.mcd.data.form.CheckDepositForm
import com.unionbankph.corporate.mcd.data.gateway.CheckDepositGateway
import com.unionbankph.corporate.mcd.presentation.confirmation.CheckDepositConfirmationViewModel
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by herald25santos on 09/21/20
 */
class CheckDepositConfirmationViewModelTest : BaseTest() {

    @RelaxedMockK
    lateinit var checkDepositGateway: CheckDepositGateway

    @RelaxedMockK
    lateinit var checkDepositMockResponse: CheckDepositMockResponse

    lateinit var viewModel: CheckDepositConfirmationViewModel

    override fun setupBeforeTest() {
        super.setupBeforeTest()
        viewModel = CheckDepositConfirmationViewModel(checkDepositGateway)
    }

    override fun isMockServerEnabled(): Boolean = false

    @Test
    fun `submit check, then it should return check deposit details`() {
        val expectedResponse = checkDepositMockResponse.checkDepositItem()
        val checkDepositForm = CheckDepositForm(id = "0001")
        every {
            checkDepositGateway.checkDeposit(checkDepositForm)
        } returns Single.just(expectedResponse)

        viewModel.checkDeposit(checkDepositForm)

        verify {
            checkDepositGateway.checkDeposit(checkDepositForm)
        }

        val checkDeposit = viewModel.checkDeposit.getOrAwaitValue()
        assertEquals(expectedResponse.id, checkDeposit.id)
    }

}