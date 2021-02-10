package com.unionbankph.corporate.bills_payment.data.source.local

import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.bills_payment.data.model.FrequentBiller
import com.unionbankph.corporate.common.data.model.PagedDto
import io.reactivex.Single

/**
 * Created by herald25santos on 2020-01-16
 */
interface BillsPaymentCache {

    fun getOrganizationPaymentsTestData(): Single<PagedDto<Transaction>>

    fun getFrequentBillersTestData(): Single<PagedDto<FrequentBiller>>
}
