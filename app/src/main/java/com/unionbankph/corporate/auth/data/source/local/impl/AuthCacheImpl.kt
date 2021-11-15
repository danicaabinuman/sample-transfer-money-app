package com.unionbankph.corporate.auth.data.source.local.impl

import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.toDistinctByKey
import com.unionbankph.corporate.auth.data.form.ECredForm
import com.unionbankph.corporate.auth.data.model.CorporateUser
import com.unionbankph.corporate.auth.data.model.UserCreationCorporateUser
import com.unionbankph.corporate.auth.data.model.UserCreationDetails
import com.unionbankph.corporate.auth.data.model.UserDetails
import com.unionbankph.corporate.auth.data.source.local.AuthCache
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.data.source.local.sharedpref.SharedPreferenceUtil
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.settings.data.model.RecentUser
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-17
 */
class AuthCacheImpl
@Inject
constructor(
    private val cacheManager: CacheManager,
    private val sharedPreferenceUtil: SharedPreferenceUtil
) : AuthCache {

    override fun isLaunched(): Observable<Boolean> {
        return Observable.fromCallable {
            sharedPreferenceUtil.isLaunched().get()
        }
    }

    override fun isLoggedIn(): Observable<Boolean> {
        return Observable.fromCallable {
            sharedPreferenceUtil.isLoggedIn()
                .get() && cacheManager.get(CacheManager.ACCESS_TOKEN) != ""
        }
    }

    override fun clearCredential(): Completable {
        return Completable.fromAction {
            cacheManager.clear(CacheManager.ACCESS_TOKEN)
            cacheManager.clear(CacheManager.CORPORATE_USER)
            cacheManager.clear(CacheManager.ROLE)
            sharedPreferenceUtil.isLoggedIn().set(false)
        }
    }

    override fun clearLoginCredential(): Completable {
        return Completable.fromAction {
            cacheManager.clear(CacheManager.ACCESS_TOKEN)
            cacheManager.clear(CacheManager.CORPORATE_USER)
            cacheManager.clear(CacheManager.ROLE)
            sharedPreferenceUtil.isLoggedIn().set(false)
            sharedPreferenceUtil.isTrustedDevice().set(false)
            sharedPreferenceUtil.fullNameSharedPref().delete()
            sharedPreferenceUtil.emailSharedPref().delete()
            sharedPreferenceUtil.fingerPrintTokenSharedPref().delete()
            sharedPreferenceUtil.totpTokenPref().delete()
        }
    }

    override fun saveCredential(userDetails: UserDetails): Completable {
        return Completable.fromAction {
            val accessToken = userDetails.token?.accessToken.notNullable()
            val corporateUserString = JsonHelper.toJson(userDetails.corporateUser)
            val corporateUser = JsonHelper.fromJson<CorporateUser>(corporateUserString)
            val fullName = "${corporateUser.firstName} ${corporateUser.lastName}"
            if (sharedPreferenceUtil.fingerPrintTokenSharedPref().get() != "" &&
                (corporateUser.emailAddress != sharedPreferenceUtil.emailSharedPref().get())) {
                sharedPreferenceUtil.isNewUserDetectedSharedPref().set(true)
            }
            if (corporateUser.emailAddress != sharedPreferenceUtil.emailSharedPref().get()) {
                sharedPreferenceUtil.fingerPrintTokenSharedPref().delete()
                sharedPreferenceUtil.totpTokenPref().delete()
            }
            saveCorporateUser(corporateUser)
            cacheManager.put(CacheManager.ACCESS_TOKEN, accessToken)
            cacheManager.put(CacheManager.CORPORATE_USER, corporateUserString)
            cacheManager.put(CacheManager.ROLE, userDetails.role)
            sharedPreferenceUtil.isLoggedIn().set(true)
            sharedPreferenceUtil.isReadMCDTerms().set(userDetails.readMcdTerms.notNullable())
            sharedPreferenceUtil.isTrustedDevice().set(userDetails.isTrusted ?: false)
            if (userDetails.isTrusted == true) {
                sharedPreferenceUtil.totpTokenPref()
                    .set(userDetails.corporateUser?.secretToken.notNullable())
            }
            sharedPreferenceUtil.fullNameSharedPref().set(fullName)
            sharedPreferenceUtil.emailSharedPref().set(corporateUser.emailAddress.notNullable())
        }
    }


    private fun saveCorporateUser(corporateUser: CorporateUser) {
        var recentUsers = mutableListOf<RecentUser>()
        if (sharedPreferenceUtil.recentUsersSharedPref().get() != "") {
            recentUsers = JsonHelper.fromListJson(sharedPreferenceUtil.recentUsersSharedPref().get())
            val foundUser = recentUsers.find { it.userEmail == corporateUser.emailAddress }
            if (foundUser == null) {
                recentUsers.add(
                    RecentUser(
                        corporateUser.emailAddress,
                        isAskTrustDevice = false,
                        isAskBiometric = false
                    )
                )
                val recentUsersString = JsonHelper.toJson(
                    recentUsers.toDistinctByKey { it.userEmail }
                )
                sharedPreferenceUtil.recentUsersSharedPref().set(recentUsersString)
            }
        } else {
            recentUsers.add(
                RecentUser(
                    corporateUser.emailAddress,
                    isAskTrustDevice = false,
                    isAskBiometric = false
                )
            )
            val recentUsersString = JsonHelper.toJson(recentUsers)
            sharedPreferenceUtil.recentUsersSharedPref().set(recentUsersString)
        }
    }

    override fun saveCredential(userCreationDetails: UserCreationDetails): Completable {
        return Completable.fromAction {
            val accessToken = userCreationDetails.token?.accessToken.notNullable()
            val corporateUserString = JsonHelper.toJson(userCreationDetails.corporateUser)
            val corporateUser = JsonHelper.fromJson<UserCreationCorporateUser>(corporateUserString)
            val fullName = "${corporateUser.firstName} ${corporateUser.lastName}"
            if (sharedPreferenceUtil.fingerPrintTokenSharedPref().get() != "" &&
                (corporateUser.emailAddress != sharedPreferenceUtil.emailSharedPref().get())) {
                sharedPreferenceUtil.isNewUserDetectedSharedPref().set(true)
            }
            if (corporateUser.emailAddress != sharedPreferenceUtil.emailSharedPref().get()) {
                sharedPreferenceUtil.fingerPrintTokenSharedPref().delete()
                sharedPreferenceUtil.totpTokenPref().delete()
            }
            saveUserCreationCorporateUser(corporateUser)
            cacheManager.put(CacheManager.ACCESS_TOKEN, accessToken)
            cacheManager.put(CacheManager.CORPORATE_USER, corporateUserString)
            cacheManager.put(CacheManager.ROLE, userCreationDetails.role)
            sharedPreferenceUtil.isLoggedIn().set(true)
            sharedPreferenceUtil.isReadMCDTerms().set(userCreationDetails.readMcdTerms.notNullable())
            sharedPreferenceUtil.isTrustedDevice().set(userCreationDetails.isTrusted ?: false)
            if (userCreationDetails.isTrusted == true) {
                sharedPreferenceUtil.totpTokenPref()
                    .set(userCreationDetails.corporateUser?.secretToken.notNullable())
            }
            sharedPreferenceUtil.fullNameSharedPref().set(fullName)
            sharedPreferenceUtil.emailSharedPref().set(corporateUser.emailAddress.notNullable())
            sharedPreferenceUtil.isTrialMode().set(userCreationDetails.trialMode ?: false)
            sharedPreferenceUtil.trialModeDaysRemainingSharedPref().set(userCreationDetails.trialDaysRemaining.toString())
        }
    }


    private fun saveUserCreationCorporateUser(corporateUser: UserCreationCorporateUser) {
        var recentUsers = mutableListOf<RecentUser>()
        if (sharedPreferenceUtil.recentUsersSharedPref().get() != "") {
            recentUsers = JsonHelper.fromListJson(sharedPreferenceUtil.recentUsersSharedPref().get())
            val foundUser = recentUsers.find { it.userEmail == corporateUser.emailAddress }
            if (foundUser == null) {
                recentUsers.add(
                    RecentUser(
                        corporateUser.emailAddress,
                        isAskTrustDevice = false,
                        isAskBiometric = false
                    )
                )
                val recentUsersString = JsonHelper.toJson(
                    recentUsers.toDistinctByKey { it.userEmail }
                )
                sharedPreferenceUtil.recentUsersSharedPref().set(recentUsersString)
            }
        } else {
            recentUsers.add(
                RecentUser(
                    corporateUser.emailAddress,
                    isAskTrustDevice = false,
                    isAskBiometric = false
                )
            )
            val recentUsersString = JsonHelper.toJson(recentUsers)
            sharedPreferenceUtil.recentUsersSharedPref().set(recentUsersString)
        }
    }

    override fun saveECredPayload(eCredForm: ECredForm): Completable {
        return Completable.fromAction {
            if (cacheManager.get("eCredPayload") == "") {
                cacheManager.put("eCredPayload", JsonHelper.toJson(eCredForm))
            } else {
                val oldECredForm = JsonHelper.fromJson<ECredForm>(cacheManager.get("eCredPayload"))
                oldECredForm.emailAddress = eCredForm.emailAddress ?: oldECredForm.emailAddress
                oldECredForm.password = eCredForm.password ?: oldECredForm.password
                oldECredForm.mobileNumber = eCredForm.mobileNumber ?: oldECredForm.mobileNumber
                oldECredForm.countryCodeId = eCredForm.countryCodeId ?: oldECredForm.countryCodeId
                cacheManager.put("eCredPayload", JsonHelper.toJson(oldECredForm))
            }
        }
    }

    override fun getECredPayload(): Single<ECredForm> {
        return Single.fromCallable {
            JsonHelper.fromJson<ECredForm>(cacheManager.get("eCredPayload"))
        }
    }

    override fun readMCDTerms(): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.isReadMCDTerms().set(true)
        }
    }
}
