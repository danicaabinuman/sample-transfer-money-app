package com.unionbankph.corporate.fund_transfer.data.source.local.impl

import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.data.source.local.tutorial.TestDataTutorial
import com.unionbankph.corporate.fund_transfer.data.model.Beneficiary
import com.unionbankph.corporate.fund_transfer.data.source.local.FundTransferCache
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-15
 */
class FundTransferCacheImpl
@Inject
constructor(
    private val cacheManager: CacheManager,
    private val testDataTutorial: TestDataTutorial
) : FundTransferCache {

    override fun getOrganizationTransfersTestData(): Single<PagedDto<Transaction>> {
        return Single.fromCallable {
            testDataTutorial.getFundTransferTransactions()
        }
    }

    override fun getScheduledTransferTutorialTestData(): Single<PagedDto<Transaction>> {
        return Single.fromCallable {
            testDataTutorial.getScheduledTransferTutorialTestData()
        }
    }

    override fun getBeneficiariesTestData(): Single<PagedDto<Beneficiary>> {
        return Single.fromCallable {
            testDataTutorial.getBeneficiariesTestData()
        }
    }
}
