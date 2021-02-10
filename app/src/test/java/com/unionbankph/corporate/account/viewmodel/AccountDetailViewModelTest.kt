package com.unionbankph.corporate.account.viewmodel

import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.RecentTransaction
import com.unionbankph.corporate.account.presentation.account_detail.AccountDetailViewModel
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
class AccountDetailViewModelTest : BaseTest() {

    @RelaxedMockK
    lateinit var accountGateway: AccountGateway

    lateinit var viewModel: AccountDetailViewModel

    override fun setupBeforeTest() {
        super.setupBeforeTest()
        viewModel = AccountDetailViewModel(accountGateway)
    }

    override fun isMockServerEnabled(): Boolean = false

    @Test
    fun `get accounts details, then return with result`() {
        val stringResponse = getJson("account_recent_transactions")
        val expectedResponse = JsonHelper.fromJson<RecentTransaction>(stringResponse)
        every {
            accountGateway.getRecentTransactionsWithPagination("1", 2)
        } returns Single.just(expectedResponse)

        viewModel.getRecentTransactions("1", 2)

        verify {
            accountGateway.getRecentTransactionsWithPagination("1", 2)
        }

        val records = viewModel.records.getOrAwaitValue()
        assertEquals(expectedResponse.records, records)
    }

}