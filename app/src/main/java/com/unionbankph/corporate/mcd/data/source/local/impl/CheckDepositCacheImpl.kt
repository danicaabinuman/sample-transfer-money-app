package com.unionbankph.corporate.mcd.data.source.local.impl

import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.mcd.data.source.local.CheckDepositCache
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.data.source.local.tutorial.TestDataTutorial
import io.reactivex.Single
import javax.inject.Inject

class CheckDepositCacheImpl
@Inject
constructor(
    private val cacheManager: CacheManager,
    private val testDataTutorial: TestDataTutorial
) : CheckDepositCache {

    override fun getCheckDepositsTestData(): Single<PagedDto<CheckDeposit>> {
        return Single.fromCallable {
            testDataTutorial.getCheckDepositTransactions()
        }
    }
}
