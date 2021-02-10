package com.unionbankph.corporate.bills_payment.data.source.local.impl

import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.bills_payment.data.model.FrequentBiller
import com.unionbankph.corporate.bills_payment.data.source.local.BillsPaymentCache
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.data.source.local.tutorial.TestDataTutorial
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-16
 */
class BillsPaymentCacheImpl
@Inject
constructor(
    private val testDataTutorial: TestDataTutorial
) : BillsPaymentCache {

    override fun getOrganizationPaymentsTestData(): Single<PagedDto<Transaction>> {
        return Single.fromCallable {
            testDataTutorial.getBillsPaymentTransactions()
        }
    }

    override fun getFrequentBillersTestData(): Single<PagedDto<FrequentBiller>> {
        return Single.fromCallable {
            testDataTutorial.getfrequentBillersTestData()
        }
    }
}
