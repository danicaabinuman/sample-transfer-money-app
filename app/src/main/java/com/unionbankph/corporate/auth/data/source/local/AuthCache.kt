package com.unionbankph.corporate.auth.data.source.local

import com.unionbankph.corporate.auth.data.form.ECredForm
import com.unionbankph.corporate.auth.data.model.UserCreationDetails
import com.unionbankph.corporate.auth.data.model.UserDetails
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by herald25santos on 2020-01-17
 */
interface AuthCache {

    fun isLoggedIn(): Observable<Boolean>

    fun isLaunched(): Observable<Boolean>

    fun clearCredential(): Completable

    fun clearLoginCredential(): Completable

    fun saveCredential(userDetails: UserDetails): Completable

    fun saveCredential(userCreationDetails: UserCreationDetails): Completable

    fun saveECredPayload(eCredForm: ECredForm): Completable

    fun getECredPayload(): Single<ECredForm>

    fun readMCDTerms(): Completable
}
