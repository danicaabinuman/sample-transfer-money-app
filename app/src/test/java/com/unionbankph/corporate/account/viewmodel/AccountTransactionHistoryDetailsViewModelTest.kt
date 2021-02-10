package com.unionbankph.corporate.account.viewmodel

import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.AccountTransactionHistoryDetails
import com.unionbankph.corporate.account.data.model.RecentTransaction
import com.unionbankph.corporate.account.domain.form.GetAccountTransactionHistoryDetailsForm
import com.unionbankph.corporate.account.domain.interactor.GetAccountTransactionHistoryDetails
import com.unionbankph.corporate.account.presentation.account_history_detail.AccountTransactionHistoryDetailsViewModel
import com.unionbankph.corporate.app.common.extension.convertDateToDesireFormat
import com.unionbankph.corporate.common.BaseTest
import com.unionbankph.corporate.common.getOrAwaitValue
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import io.reactivex.observers.DisposableSingleObserver
import org.junit.Assert
import org.junit.Test

/**
 * Created by herald25santos on 3/6/20
 */
class AccountTransactionHistoryDetailsViewModelTest : BaseTest() {

    @RelaxedMockK
    lateinit var accountGateway: AccountGateway

    @RelaxedMockK
    lateinit var getAccountTransactionHistoryDetails: GetAccountTransactionHistoryDetails

    lateinit var viewModel: AccountTransactionHistoryDetailsViewModel

    override fun setupBeforeTest() {
        super.setupBeforeTest()
        viewModel = AccountTransactionHistoryDetailsViewModel(getAccountTransactionHistoryDetails)
    }

    override fun isMockServerEnabled(): Boolean = false

    @Test
    fun `get accounts history details, then return with result`() {
        val stringResponse = getJson("account_history_details")
        val expectedResponse = JsonHelper.fromJson<AccountTransactionHistoryDetails>(stringResponse)
        val accountTransactionHistoryString = getJson("account_transaction_history")
        val accountTransactionHistory =
            JsonHelper.fromJson<RecentTransaction>(accountTransactionHistoryString)
        val record = accountTransactionHistory.records[0]
        val recordString = JsonHelper.toJson(record)
        val accountId = "1"
        viewModel.initBundleData(recordString, accountId)

        val getAccountTransactionHistoryDetailsForm =
            GetAccountTransactionHistoryDetailsForm().apply {
                id = accountId
                referenceNumber = record.tranId
                serialNo = record.serialNo
                transactionDate = record.tranDate.convertDateToDesireFormat(
                    DateFormatEnum.DATE_FORMAT_ISO_DATE
                )
            }

        val captor =
            slot<DisposableSingleObserver<AccountTransactionHistoryDetails>>()
        verify {
            getAccountTransactionHistoryDetails.execute(
                capture(captor),
                any(),
                any(),
                getAccountTransactionHistoryDetailsForm
            )
        }
        captor.captured.onSuccess(expectedResponse)

        Assert.assertEquals(
            record.tranId,
            viewModel.record.value?.tranId
        )
        Assert.assertEquals(
            expectedResponse.account?.id,
            viewModel.accountTransactionHistoryDetails.getOrAwaitValue().account?.id
        )
    }

}