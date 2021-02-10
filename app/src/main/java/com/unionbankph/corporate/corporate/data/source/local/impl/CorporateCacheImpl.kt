package com.unionbankph.corporate.corporate.data.source.local.impl

import com.unionbankph.corporate.auth.data.model.CorporateUser
import com.unionbankph.corporate.auth.data.model.Role
import com.unionbankph.corporate.auth.data.model.RoleAccountPermissions
import com.unionbankph.corporate.auth.data.model.UserDetails
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.data.source.local.sharedpref.SharedPreferenceUtil
import com.unionbankph.corporate.common.data.source.local.tutorial.TestDataTutorial
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.corporate.data.source.local.CorporateCache
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-16
 */
class CorporateCacheImpl
@Inject
constructor(
    private val cacheManager: CacheManager,
    private val testDataTutorial: TestDataTutorial,
    private val sharedPreferenceUtil: SharedPreferenceUtil
) : CorporateCache {

    override fun getProductPermissions(): Single<MutableList<RoleAccountPermissions>> {
        return Single.fromCallable {
            val role = cacheManager.getObject(CacheManager.ROLE) as? Role
            role?.roleAccountPermissions?.toMutableList()
        }
    }

    override fun getRole(): Single<Role> {
        return Single.fromCallable {
            cacheManager.getObject(CacheManager.ROLE) as? Role
        }
    }

    override fun getCorporateUser(): Single<CorporateUser> {
        return Single.fromCallable {
            JsonHelper.fromJson<CorporateUser>(cacheManager.get(CacheManager.CORPORATE_USER))
        }
    }

    override fun getUserDetails(): Single<UserDetails> {
        return Single.fromCallable {
            UserDetails(
                null,
                cacheManager.getObject(CacheManager.ROLE) as? Role,
                JsonHelper.fromJson(cacheManager.get(CacheManager.CORPORATE_USER)),
                null
            )
        }
    }

    override fun saveRole(role: Role): Completable {
        return Completable.fromAction {
            cacheManager.put(CacheManager.ROLE, role)
        }
    }
}
