package com.unionbankph.corporate.app.di.module

import com.unionbankph.corporate.account.data.source.local.AccountCache
import com.unionbankph.corporate.account.data.source.local.impl.AccountCacheImpl
import com.unionbankph.corporate.app.di.scope.PerApplication
import com.unionbankph.corporate.auth.data.source.local.AuthCache
import com.unionbankph.corporate.auth.data.source.local.impl.AuthCacheImpl
import com.unionbankph.corporate.bills_payment.data.source.local.BillsPaymentCache
import com.unionbankph.corporate.bills_payment.data.source.local.impl.BillsPaymentCacheImpl
import com.unionbankph.corporate.branch.data.source.local.BranchVisitCache
import com.unionbankph.corporate.branch.data.source.local.CorporateUserCache
import com.unionbankph.corporate.branch.data.source.local.impl.BranchVisitCacheImpl
import com.unionbankph.corporate.branch.data.source.local.impl.CorporateUserCacheImpl
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.data.source.local.sharedpref.SharedPreferenceUtil
import com.unionbankph.corporate.common.data.source.local.tutorial.TestDataTutorial
import com.unionbankph.corporate.corporate.data.source.local.CorporateCache
import com.unionbankph.corporate.corporate.data.source.local.impl.CorporateCacheImpl
import com.unionbankph.corporate.dao.data.source.local.DaoCache
import com.unionbankph.corporate.dao.data.source.local.impl.DaoCacheImpl
import com.unionbankph.corporate.fund_transfer.data.source.local.FundTransferCache
import com.unionbankph.corporate.fund_transfer.data.source.local.impl.FundTransferCacheImpl
import com.unionbankph.corporate.mcd.data.source.local.CheckDepositCache
import com.unionbankph.corporate.mcd.data.source.local.impl.CheckDepositCacheImpl
import com.unionbankph.corporate.settings.data.constant.SingleSelectorData
import com.unionbankph.corporate.settings.data.source.local.SettingsCache
import com.unionbankph.corporate.settings.data.source.local.impl.SettingsCacheImpl
import dagger.Module
import dagger.Provides

@Module
class LocalDataModule {

    @Provides
    @PerApplication
    fun authCache(
        cacheManager: CacheManager,
        sharedPreferenceUtil: SharedPreferenceUtil
    ): AuthCache =
        AuthCacheImpl(
            cacheManager,
            sharedPreferenceUtil
        )

    @Provides
    @PerApplication
    fun checkDepositCache(
        cacheManager: CacheManager,
        testDataTutorial: TestDataTutorial
    ): CheckDepositCache =
        CheckDepositCacheImpl(
            cacheManager,
            testDataTutorial
        )

    @Provides
    @PerApplication
    fun branchVisitCache(
        testDataTutorial: TestDataTutorial
    ): BranchVisitCache =
        BranchVisitCacheImpl(
            testDataTutorial
        )

    @Provides
    @PerApplication
    fun corporateUserCache(
        cacheManager: CacheManager
    ): CorporateUserCache = CorporateUserCacheImpl(cacheManager)

    @Provides
    @PerApplication
    fun accountCache(
        cacheManager: CacheManager,
        testDataTutorial: TestDataTutorial
    ): AccountCache = AccountCacheImpl(
        cacheManager,
        testDataTutorial
    )

    @Provides
    @PerApplication
    fun settingsCache(
        sharedPreferenceUtil: SharedPreferenceUtil,
        cacheManager: CacheManager,
        corporateCache: CorporateCache,
        singleSelectorData: SingleSelectorData
    ): SettingsCache =
        SettingsCacheImpl(sharedPreferenceUtil, cacheManager, corporateCache, singleSelectorData)

    @Provides
    @PerApplication
    fun fundTransferCache(
        cacheManager: CacheManager,
        testDataTutorial: TestDataTutorial
    ): FundTransferCache = FundTransferCacheImpl(cacheManager, testDataTutorial)

    @Provides
    @PerApplication
    fun billsPaymentCache(
        testDataTutorial: TestDataTutorial
    ): BillsPaymentCache = BillsPaymentCacheImpl(testDataTutorial)

    @Provides
    @PerApplication
    fun corporateCache(
        cacheManager: CacheManager,
        testDataTutorial: TestDataTutorial,
        sharedPreferenceUtil: SharedPreferenceUtil
    ): CorporateCache = CorporateCacheImpl(cacheManager, testDataTutorial, sharedPreferenceUtil)

    @Provides
    @PerApplication
    fun daoCache(cacheManager: CacheManager): DaoCache = DaoCacheImpl(cacheManager)
}
