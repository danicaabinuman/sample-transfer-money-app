package com.unionbankph.corporate.mcd.repository.local

import com.unionbankph.corporate.common.BaseTest
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.data.source.local.tutorial.TestDataTutorial
import com.unionbankph.corporate.mcd.data.source.local.impl.CheckDepositCacheImpl
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Test

/**
 * Created by herald25santos on 09/22/20
 */
class CheckDepositCacheTest : BaseTest() {

    @RelaxedMockK
    lateinit var cacheManager: CacheManager

    @RelaxedMockK
    lateinit var testDataTutorial: TestDataTutorial

    private lateinit var checkDepositCache: CheckDepositCacheImpl

    override fun isMockServerEnabled(): Boolean = false

    override fun setupBeforeTest() {
        super.setupBeforeTest()
        checkDepositCache = CheckDepositCacheImpl(cacheManager, testDataTutorial)
    }

    @Test
    fun `get check deposit test data, then it should return 1 item`() {
        val expectedResponse = testDataTutorial.getCheckDepositTransactions()

        val responseObserver = checkDepositCache.getCheckDepositsTestData().test()

        responseObserver.assertValue {
            expectedResponse.pageSize == it.pageSize
        }
        responseObserver.dispose()
    }

}