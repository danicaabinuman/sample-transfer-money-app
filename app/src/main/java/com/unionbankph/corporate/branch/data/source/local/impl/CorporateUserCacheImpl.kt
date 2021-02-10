package com.unionbankph.corporate.branch.data.source.local.impl

import com.unionbankph.corporate.auth.data.model.CorporateUser
import com.unionbankph.corporate.branch.data.source.local.CorporateUserCache
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import io.reactivex.Single
import javax.inject.Inject

class CorporateUserCacheImpl
@Inject
constructor(
    private val cacheManager: CacheManager
) : CorporateUserCache {

    override fun getCorporateUser(): Single<CorporateUser> {
        return Single.fromCallable {
            JsonHelper.fromJson<CorporateUser>(cacheManager.get(CacheManager.CORPORATE_USER))
        }
    }
}
