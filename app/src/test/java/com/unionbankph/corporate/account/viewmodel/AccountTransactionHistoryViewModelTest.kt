package com.unionbankph.corporate.account.viewmodel

import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.RecentTransaction
import com.unionbankph.corporate.account.presentation.account_history.AccountTransactionHistoryViewModel
import com.unionbankph.corporate.common.BaseTest
import com.unionbankph.corporate.common.getOrAwaitValue
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by herald25santos on 3/6/20
 */
class AccountTransactionHistoryViewModelTest : BaseTest() {

    @RelaxedMockK
    lateinit var accountGateway: AccountGateway

    lateinit var viewModel: AccountTransactionHistoryViewModel

    override fun setupBeforeTest() {
        super.setupBeforeTest()
        viewModel = AccountTransactionHistoryViewModel(accountGateway)
    }

    override fun isMockServerEnabled(): Boolean = false

    @Test
    fun `get initial accounts transactions history, then return it's result`() {
        val stringResponse = getJson("account_transaction_history")
        val expectedResponse = JsonHelper.fromJson<RecentTransaction>(stringResponse)

        every {
            accountGateway.getRecentTransactions(
                "1",
                viewModel.pageable,
                null
            )
        } returns Single.just(expectedResponse)

        viewModel.getRecentTransactions("1", true)

        verify {
            accountGateway.getRecentTransactions(
                "1",
                viewModel.pageable,
                null
            )
        }

        val sectionedTransactions = viewModel.sectionedTransactions.getOrAwaitValue()

        assertEquals(1, sectionedTransactions.size)
    }

    @Test
    fun `get second page of accounts transactions history, then return it's result`() {
        val stringResponse = getJson("account_transaction_history")
        val expectedResponse = JsonHelper.fromJson<RecentTransaction>(stringResponse)
        val lastItemResult = expectedResponse.records.last()
        viewModel.lastItemRecord.onNext(lastItemResult)
        every {
            accountGateway.getRecentTransactions(
                "1",
                viewModel.pageable,
                viewModel.lastItemRecord.value
            )
        } returns Single.just(expectedResponse)

        viewModel.getRecentTransactions("1", false)

        verify {
            accountGateway.getRecentTransactions(
                "1",
                viewModel.pageable,
                viewModel.lastItemRecord.value
            )
        }

        val sectionedTransactions = viewModel.sectionedTransactions.getOrAwaitValue()

        assertEquals(1, sectionedTransactions.size)
    }

}