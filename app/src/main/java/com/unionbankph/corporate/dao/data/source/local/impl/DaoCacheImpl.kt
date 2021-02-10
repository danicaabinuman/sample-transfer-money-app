package com.unionbankph.corporate.dao.data.source.local.impl

import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.dao.data.model.DaoDetailsDto
import com.unionbankph.corporate.dao.data.source.local.DaoCache
import com.unionbankph.corporate.dao.domain.model.SignatoryDetail
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class DaoCacheImpl
@Inject
constructor(
    private val cacheManager: CacheManager
) : DaoCache {

    override fun getAccessToken(): Single<String> {
        return Single.fromCallable {
            if (cacheManager.getObject(CacheManager.DAO_SIGNATORY_DETAILS) != null) {
                val signatoryDetail = cacheManager.getObject(CacheManager.DAO_SIGNATORY_DETAILS) as SignatoryDetail
                signatoryDetail.userToken
            } else {
                cacheManager.get(CacheManager.DAO_ACCESS_TOKEN)
            }
        }
    }

    override fun getReferenceNumber(): Single<String> {
        return Single.fromCallable {
            if (cacheManager.getObject(CacheManager.DAO_SIGNATORY_DETAILS) != null) {
                val signatoryDetail = cacheManager.getObject(CacheManager.DAO_SIGNATORY_DETAILS) as SignatoryDetail
                signatoryDetail.referenceNumber
            } else {
                cacheManager.get(CacheManager.DAO_REFERENCE_NUMBER)
            }
        }
    }

    override fun clearDaoCache(): Completable {
        return Completable.fromAction {
            cacheManager.clear(CacheManager.DAO_ACCESS_TOKEN)
            cacheManager.clear(CacheManager.DAO_REFERENCE_NUMBER)
            cacheManager.clear(CacheManager.DAO_SIGNATORY_DETAILS)
        }
    }

    override fun saveAccessToken(token: String?): Completable {
        return Completable.fromAction {
            cacheManager.put(CacheManager.DAO_ACCESS_TOKEN, token)
        }
    }

    override fun saveReferenceNumber(referenceNumber: String?): Completable {
        return Completable.fromAction {
            cacheManager.put(CacheManager.DAO_REFERENCE_NUMBER, referenceNumber)
        }
    }

    override fun saveSignatoryDetails(signatoryDetail: SignatoryDetail): Completable {
        return Completable.fromAction {
            cacheManager.put(CacheManager.DAO_SIGNATORY_DETAILS, signatoryDetail)
        }
    }

    override fun getSignatoryDetails(): Single<SignatoryDetail> {
        return Single.fromCallable {
            cacheManager.getObject(CacheManager.DAO_SIGNATORY_DETAILS) as SignatoryDetail
        }
    }
}

