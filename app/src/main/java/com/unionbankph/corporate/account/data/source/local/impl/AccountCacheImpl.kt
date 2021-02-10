package com.unionbankph.corporate.account.data.source.local.impl

import com.unionbankph.corporate.account.data.source.local.AccountCache
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.data.source.local.tutorial.TestDataTutorial
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-14
 */
class AccountCacheImpl
@Inject
constructor(
    private val cacheManager: CacheManager,
    private val testDataTutorial: TestDataTutorial
) : AccountCache
