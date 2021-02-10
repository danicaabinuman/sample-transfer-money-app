package com.unionbankph.corporate.fund_transfer.data.source.local

import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.fund_transfer.data.model.Beneficiary
import io.reactivex.Single

/**
 * Created by herald25santos on 2020-01-15
 */
interface FundTransferCache {

    fun getOrganizationTransfersTestData(): Single<PagedDto<Transaction>>

    fun getScheduledTransferTutorialTestData(): Single<PagedDto<Transaction>>

    fun getBeneficiariesTestData(): Single<PagedDto<Beneficiary>>
}
