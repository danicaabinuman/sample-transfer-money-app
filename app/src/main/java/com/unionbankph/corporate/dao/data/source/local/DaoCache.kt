package com.unionbankph.corporate.dao.data.source.local

import com.unionbankph.corporate.dao.domain.model.SignatoryDetail
import io.reactivex.Completable
import io.reactivex.Single

interface DaoCache {

    fun getAccessToken(): Single<String>

    fun getReferenceNumber(): Single<String>

    fun clearDaoCache(): Completable

    fun saveAccessToken(token: String?): Completable

    fun saveReferenceNumber(referenceNumber: String?): Completable

    fun saveSignatoryDetails(signatoryDetail: SignatoryDetail): Completable

    fun getSignatoryDetails(): Single<SignatoryDetail>
}
