package com.unionbankph.corporate.mcd.viewmodel

import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.BaseTest
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.data.source.local.tutorial.TestDataTutorial
import com.unionbankph.corporate.common.domain.exception.SomethingWentWrongException
import com.unionbankph.corporate.common.getOrAwaitValue
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.mcd.util.CheckDepositMockResponse
import com.unionbankph.corporate.mcd.data.gateway.CheckDepositGateway
import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.mcd.domain.model.CheckDepositFilter
import com.unionbankph.corporate.mcd.presentation.list.CheckDepositViewModel
import com.unionbankph.corporate.mcd.presentation.list.ShowCheckDepositDismissLoading
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test


/**
 * Created by herald25santos on 3/6/20
 */
class CheckDepositViewModelTest : BaseTest() {

    @RelaxedMockK
    lateinit var checkDepositGateway: CheckDepositGateway

    @RelaxedMockK
    lateinit var mockResponse: CheckDepositMockResponse

    lateinit var viewModel: CheckDepositViewModel

    override fun setupBeforeTest() {
        super.setupBeforeTest()
        viewModel = CheckDepositViewModel(checkDepositGateway)
    }

    override fun isMockServerEnabled(): Boolean = false

    @Test
    fun `get check deposit list, then return with result`() {
        val response = mockResponse.getMockResponseCheckDepositTransactions()
        every {
            checkDepositGateway.getCheckDeposits(
                viewModel.pageable,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
        } returns Single.just(response)

        viewModel.getCheckDeposits(true)

        verify {
            checkDepositGateway.getCheckDeposits(
                viewModel.pageable,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
        }
        val checkDeposits = viewModel.checkDeposits.getOrAwaitValue()
        assertEquals(ShowCheckDepositDismissLoading, viewModel.state.getOrAwaitValue())
        assertEquals(response.results.size, checkDeposits.size)
        assertEquals(response.results, checkDeposits)
    }

    @Test
    fun `get check deposit list, then it return empty result`() {
        val response = PagedDto<CheckDeposit>(results = mutableListOf())
        every {
            checkDepositGateway.getCheckDeposits(
                viewModel.pageable,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
        } returns Single.just(response)
        viewModel.getCheckDeposits(true)

        verify {
            checkDepositGateway.getCheckDeposits(
                viewModel.pageable,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
        }

        assertEquals(ShowCheckDepositDismissLoading, viewModel.state.getOrAwaitValue())
        assertEquals(response.results.size, viewModel.checkDeposits.getOrAwaitValue().size)
    }

    @Test
    fun `get tutorial check deposit list, then return with result`() {
        val viewUtil = mockk<ViewUtil>(relaxed = true)
        val testDataTutorial = TestDataTutorial(viewUtil)
        val response = testDataTutorial.getCheckDepositTransactions()
        every {
            checkDepositGateway.getCheckDepositsTestData()
        } returns Single.just(response)

        viewModel.getCheckDepositsTestData()

        verify {
            checkDepositGateway.getCheckDepositsTestData()
        }

        val testCheckDeposits = viewModel.testCheckDeposits.getOrAwaitValue()
        assertEquals(response.results.size, testCheckDeposits.size)
        assertEquals(response.results, testCheckDeposits)
    }

    @Test
    fun `get paginated check deposit list, then return error response`() {
        every {
            checkDepositGateway.getCheckDeposits(
                viewModel.pageable,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
        } returns Single.error(SomethingWentWrongException(context))

        viewModel.getCheckDeposits(false)

        verify {
            checkDepositGateway.getCheckDeposits(
                viewModel.pageable,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
        }
        assertEquals(
            true,
            viewModel.pageable.isFailed
        )
    }

    @Test
    fun `apply filter, then match the count item`() {
        val filterJson = getJson("check_deposit_filter")
        val filter = JsonHelper.fromJson<CheckDepositFilter>(filterJson)

        viewModel.onApplyFilter(filterJson)

        assertEquals(
            filter.filterCount,
            viewModel.checkDepositFilterCount.value
        )
        assertEquals(
            filter,
            viewModel.checkDepositFilter.value
        )
    }

}