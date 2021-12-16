package com.unionbankph.corporate.settings.data.source.local.impl

import com.google.firebase.iid.FirebaseInstanceId
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.auth.data.model.Permissions
import com.unionbankph.corporate.auth.data.model.Role
import com.unionbankph.corporate.auth.data.model.RoleAccountPermissions
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.data.source.local.sharedpref.SharedPreferenceUtil
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.corporate.data.source.local.CorporateCache
import com.unionbankph.corporate.settings.data.constant.PermissionNameEnum
import com.unionbankph.corporate.settings.data.constant.SingleSelectorData
import com.unionbankph.corporate.settings.data.model.RecentUser
import com.unionbankph.corporate.settings.data.source.local.SettingsCache
import com.unionbankph.corporate.settings.domain.constant.FeaturesEnum
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by herald25santos on 2020-01-14
 */
class SettingsCacheImpl
@Inject
constructor(
    private val sharedPreferenceUtil: SharedPreferenceUtil,
    private val cacheManager: CacheManager,
    private val corporateCache: CorporateCache,
    private val singleSelectorData: SingleSelectorData
) : SettingsCache {

    override fun setUdid(): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.udidSharedPref().set(UUID.randomUUID().toString())
            sharedPreferenceUtil.isLaunched().set(true)
        }
    }

    override fun getUdid(): Maybe<String> {
        return Maybe.fromCallable {
            val udid = sharedPreferenceUtil.udidSharedPref().get()
            if (udid == "") {
                sharedPreferenceUtil.udidSharedPref().set(UUID.randomUUID().toString())
                return@fromCallable sharedPreferenceUtil.udidSharedPref().get()
            } else {
                return@fromCallable udid
            }

        }
    }

    override fun getAccessToken(): Single<String> {
        return Single.fromCallable { cacheManager.accessToken() }
    }

    override fun getNotificationToken(): Single<String> {
        return Single.fromCallable {
            sharedPreferenceUtil.notificationTokenPref().get()
        }
    }

    override fun getCacheValue(key: String): Single<String> {
        return Single.fromCallable { cacheManager.get(key) }
    }

    override fun saveMobileNumber(mobileNumber: String, countryCode: CountryCode): Completable {
        return corporateCache.getCorporateUser()
            .flatMapCompletable {
                Completable.fromAction {
                    it.mobileNumber = mobileNumber
                    it.countryCode = countryCode
                    cacheManager.put(
                        CacheManager.CORPORATE_USER,
                        JsonHelper.toJson(it)
                    )
                }
            }
    }

    override fun clearFingerprintCredential(): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.fingerPrintTokenSharedPref().delete()
        }
    }

    override fun setFingerprintCredential(token: String): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.fingerPrintTokenSharedPref().set(token)
        }
    }

    override fun getFingerprintToken(): Maybe<String> {
        return Maybe.fromCallable {
            sharedPreferenceUtil.fingerPrintTokenSharedPref().get()
        }
    }

    override fun getFingerprintFullname(): Maybe<String> {
        return Maybe.fromCallable {
            sharedPreferenceUtil.fullNameSharedPref().get()
        }
    }

    override fun getFingerprintEmail(): Maybe<String> {
        return Maybe.fromCallable {
            sharedPreferenceUtil.emailSharedPref().get()
        }
    }

    override fun deleteFingerPrint(): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.fingerPrintTokenSharedPref().delete()
        }
    }

    override fun updateRecentUser(promptTypeEnum: PromptTypeEnum, isPrompt: Boolean): Completable {
        return Completable.fromAction {
            if (sharedPreferenceUtil.recentUsersSharedPref().get() != "") {
                val recentUsers = JsonHelper.fromListJson<RecentUser>(
                    sharedPreferenceUtil.recentUsersSharedPref().get()
                )
                recentUsers.forEach {
                    if (it.userEmail == sharedPreferenceUtil.emailSharedPref().get()) {
                        if (promptTypeEnum == PromptTypeEnum.BIOMETRIC) {
                            it.isAskBiometric = isPrompt
                        } else {
                            it.isAskTrustDevice = isPrompt
                        }
                        return@forEach
                    }
                }
                val recentUsersString = JsonHelper.toJson(recentUsers)
                sharedPreferenceUtil.recentUsersSharedPref().set(recentUsersString)
            }
        }
    }

    override fun hasNotificationToken(): Single<Boolean> {
        return Single.fromCallable {
            sharedPreferenceUtil.notificationTokenPref().get() != ""
        }
    }

    override fun setNotificationToken(): Completable {
        return Completable.fromAction {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
                if (!it.isSuccessful) {
                    return@addOnCompleteListener
                }
                val notificationToken = it.result?.token.notNullable()
                Timber.d("Regenerate tokenId: $notificationToken")
                sharedPreferenceUtil.notificationTokenPref().set(notificationToken)
            }
        }
    }

    override fun isTrustedDevice(): Maybe<Boolean> {
        return Maybe.fromCallable {
            sharedPreferenceUtil.isTrustedDevice().get()
        }
    }

    override fun isReadMCDTerms(): Maybe<Boolean> {
        return Maybe.fromCallable {
            sharedPreferenceUtil.isReadMCDTerms().get()
        }
    }

    override fun setNewUserDetected(isNewUserDetected: Boolean): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.isNewUserDetectedSharedPref().set(isNewUserDetected)
        }
    }

    override fun isNewUserDetected(): Maybe<Boolean> {
        return Maybe.fromCallable {
            sharedPreferenceUtil.isNewUserDetectedSharedPref().get()
        }
    }

    override fun hasTOTP(): Maybe<Boolean> {
        return Maybe.fromCallable {
            sharedPreferenceUtil.totpTokenPref().get() != ""
        }
    }

    override fun setTOTPToken(token: String): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.totpTokenPref().set(token)
        }
    }

    override fun logoutUser(): Completable {
        return Completable.fromAction {
            cacheManager.clear(CacheManager.ACCESS_TOKEN)
            cacheManager.clear(CacheManager.CORPORATE_USER)
            cacheManager.clear(CacheManager.ROLE)
            sharedPreferenceUtil.isLoggedIn().set(false)
        }
    }

    override fun updateShortCutBadgeCount(): Single<Int> {
        return Single.fromCallable {
            val badgeCountSharedAlertsPref = sharedPreferenceUtil.badgeCountAlertsSharedPref()
            val badgeCountSharedApprovalsPref = sharedPreferenceUtil.badgeCountApprovalsSharedPref()
            val totalShortCutBadge =
                badgeCountSharedAlertsPref.get().plus(badgeCountSharedApprovalsPref.get())
            totalShortCutBadge
        }
    }

    override fun hasTutorial(key: TutorialScreenEnum): Single<Boolean> {
        return Single.fromCallable {
            when (key) {
                TutorialScreenEnum.INTRODUCTION -> {
                    sharedPreferenceUtil.tutorialIntroductionSharedPref().get()
                }
                TutorialScreenEnum.ACCOUNTS -> {
                    sharedPreferenceUtil.tutorialAccountSharedPref().get()
                }
                TutorialScreenEnum.TRANSACT -> {
                    sharedPreferenceUtil.tutorialTransactSharedPref().get()
                }
                TutorialScreenEnum.APPROVALS -> {
                    sharedPreferenceUtil.tutorialApprovalSharedPref().get()
                }
                TutorialScreenEnum.SETTINGS -> {
                    sharedPreferenceUtil.tutorialSettingsSharedPref().get()
                }
                TutorialScreenEnum.PROFILE -> {
                    sharedPreferenceUtil.tutorialUserSharedPref().get()
                }
                TutorialScreenEnum.ALERTS -> {
                    sharedPreferenceUtil.tutorialAlertsSharedPref().get()
                }
                TutorialScreenEnum.ACCOUNT_DETAILS -> {
                    sharedPreferenceUtil.tutorialAccountDetailSharedPref().get()
                }
                TutorialScreenEnum.FUND_TRANSFER -> {
                    sharedPreferenceUtil.tutorialFundTransferSharedPref().get()
                }
                TutorialScreenEnum.BILLS_PAYMENT -> {
                    sharedPreferenceUtil.tutorialBillsPaymentSharedPref().get()
                }
                TutorialScreenEnum.CHECK_DEPOSIT -> {
                    sharedPreferenceUtil.tutorialCheckDepositSharedPref().get()
                }
                TutorialScreenEnum.APPROVAL_DETAILS -> {
                    sharedPreferenceUtil.tutorialApprovalDetailSharedPref().get()
                }
                TutorialScreenEnum.CHANNEL -> {
                    sharedPreferenceUtil.tutorialChannelSharedPref().get()
                }
                TutorialScreenEnum.UBP_FORM -> {
                    sharedPreferenceUtil.tutorialUBPFormSharedPref().get()
                }
                TutorialScreenEnum.UBP_CONFIRMATION -> {
                    sharedPreferenceUtil.tutorialUBPConfirmationSharedPref().get()
                }
                TutorialScreenEnum.UBP_SUMMARY -> {
                    sharedPreferenceUtil.tutorialUBPSummarySharedPref().get()
                }
                TutorialScreenEnum.PESONET_FORM -> {
                    sharedPreferenceUtil.tutorialPesoNetFormSharedPref().get()
                }
                TutorialScreenEnum.PESONET_CONFIRMATION -> {
                    sharedPreferenceUtil.tutorialPesoNetConfirmationSharedPref().get()
                }
                TutorialScreenEnum.PESONET_SUMMARY -> {
                    sharedPreferenceUtil.tutorialPesoNetSummarySharedPref().get()
                }
                TutorialScreenEnum.PDDTS_FORM -> {
                    sharedPreferenceUtil.tutorialPDDTSFormSharedPref().get()
                }
                TutorialScreenEnum.PDDTS_CONFIRMATION -> {
                    sharedPreferenceUtil.tutorialPDDTSConfirmationSharedPref().get()
                }
                TutorialScreenEnum.PDDTS_SUMMARY -> {
                    sharedPreferenceUtil.tutorialPDDTSSummarySharedPref().get()
                }
                TutorialScreenEnum.INSTAPAY_FORM -> {
                    sharedPreferenceUtil.tutorialInstapayFormSharedPref().get()
                }
                TutorialScreenEnum.INSTAPAY_CONFIRMATION -> {
                    sharedPreferenceUtil.tutorialInstapayConfirmationSharedPref().get()
                }
                TutorialScreenEnum.INSTAPAY_SUMMARY -> {
                    sharedPreferenceUtil.tutorialInstapaySummarySharedPref().get()
                }
                TutorialScreenEnum.SWIFT_FORM -> {
                    sharedPreferenceUtil.tutorialSwiftFormSharedPref().get()
                }
                TutorialScreenEnum.SWIFT_CONFIRMATION -> {
                    sharedPreferenceUtil.tutorialSwiftConfirmationSharedPref().get()
                }
                TutorialScreenEnum.SWIFT_SUMMARY -> {
                    sharedPreferenceUtil.tutorialSwiftSummarySharedPref().get()
                }
                TutorialScreenEnum.BILLS_PAYMENT_FORM -> {
                    sharedPreferenceUtil.tutorialBillsPaymentFormSharedPref().get()
                }
                TutorialScreenEnum.BILLS_PAYMENT_CONFIRMATION -> {
                    sharedPreferenceUtil.tutorialBillsPaymentConfirmationSharedPref().get()
                }
                TutorialScreenEnum.BILLS_PAYMENT_SUMMARY -> {
                    sharedPreferenceUtil.tutorialBillsPaymentSummarySharedPref().get()
                }
            }
        }
    }

    override fun setTutorial(key: TutorialScreenEnum, boolean: Boolean): Completable {
        return Completable.fromAction {
            when (key) {
                TutorialScreenEnum.INTRODUCTION -> {
                    sharedPreferenceUtil.tutorialIntroductionSharedPref().set(boolean)
                }
                TutorialScreenEnum.ACCOUNTS -> {
                    sharedPreferenceUtil.tutorialAccountSharedPref().set(boolean)
                }
                TutorialScreenEnum.TRANSACT -> {
                    sharedPreferenceUtil.tutorialTransactSharedPref().set(boolean)
                }
                TutorialScreenEnum.APPROVALS -> {
                    sharedPreferenceUtil.tutorialApprovalSharedPref().set(boolean)
                }
                TutorialScreenEnum.SETTINGS -> {
                    sharedPreferenceUtil.tutorialSettingsSharedPref().set(boolean)
                }
                TutorialScreenEnum.PROFILE -> {
                    sharedPreferenceUtil.tutorialUserSharedPref().set(boolean)
                }
                TutorialScreenEnum.ALERTS -> {
                    sharedPreferenceUtil.tutorialAlertsSharedPref().set(boolean)
                }
                TutorialScreenEnum.ACCOUNT_DETAILS -> {
                    sharedPreferenceUtil.tutorialAccountDetailSharedPref().set(boolean)
                }
                TutorialScreenEnum.FUND_TRANSFER -> {
                    sharedPreferenceUtil.tutorialFundTransferSharedPref().set(boolean)
                }
                TutorialScreenEnum.BILLS_PAYMENT -> {
                    sharedPreferenceUtil.tutorialBillsPaymentSharedPref().set(boolean)
                }
                TutorialScreenEnum.CHECK_DEPOSIT -> {
                    sharedPreferenceUtil.tutorialCheckDepositSharedPref().set(boolean)
                }
                TutorialScreenEnum.APPROVAL_DETAILS -> {
                    sharedPreferenceUtil.tutorialApprovalDetailSharedPref().set(boolean)
                }
                TutorialScreenEnum.CHANNEL -> {
                    sharedPreferenceUtil.tutorialChannelSharedPref().set(boolean)
                }
                TutorialScreenEnum.UBP_FORM -> {
                    sharedPreferenceUtil.tutorialUBPFormSharedPref().set(boolean)
                }
                TutorialScreenEnum.UBP_CONFIRMATION -> {
                    sharedPreferenceUtil.tutorialUBPConfirmationSharedPref().set(boolean)
                }
                TutorialScreenEnum.UBP_SUMMARY -> {
                    sharedPreferenceUtil.tutorialUBPSummarySharedPref().set(boolean)
                }
                TutorialScreenEnum.PESONET_FORM -> {
                    sharedPreferenceUtil.tutorialPesoNetFormSharedPref().set(boolean)
                }
                TutorialScreenEnum.PESONET_CONFIRMATION -> {
                    sharedPreferenceUtil.tutorialPesoNetConfirmationSharedPref().set(boolean)
                }
                TutorialScreenEnum.PESONET_SUMMARY -> {
                    sharedPreferenceUtil.tutorialPesoNetSummarySharedPref().set(boolean)
                }
                TutorialScreenEnum.PDDTS_FORM -> {
                    sharedPreferenceUtil.tutorialPDDTSFormSharedPref().set(boolean)
                }
                TutorialScreenEnum.PDDTS_CONFIRMATION -> {
                    sharedPreferenceUtil.tutorialPDDTSConfirmationSharedPref().set(boolean)
                }
                TutorialScreenEnum.PDDTS_SUMMARY -> {
                    sharedPreferenceUtil.tutorialPDDTSSummarySharedPref().set(boolean)
                }
                TutorialScreenEnum.INSTAPAY_FORM -> {
                    sharedPreferenceUtil.tutorialInstapayFormSharedPref().set(boolean)
                }
                TutorialScreenEnum.INSTAPAY_CONFIRMATION -> {
                    sharedPreferenceUtil.tutorialInstapayConfirmationSharedPref().set(boolean)
                }
                TutorialScreenEnum.INSTAPAY_SUMMARY -> {
                    sharedPreferenceUtil.tutorialInstapaySummarySharedPref().set(boolean)
                }
                TutorialScreenEnum.SWIFT_FORM -> {
                    sharedPreferenceUtil.tutorialSwiftFormSharedPref().set(boolean)
                }
                TutorialScreenEnum.SWIFT_CONFIRMATION -> {
                    sharedPreferenceUtil.tutorialSwiftConfirmationSharedPref().set(boolean)
                }
                TutorialScreenEnum.SWIFT_SUMMARY -> {
                    sharedPreferenceUtil.tutorialSwiftSummarySharedPref().set(boolean)
                }
                TutorialScreenEnum.BILLS_PAYMENT_FORM -> {
                    sharedPreferenceUtil.tutorialBillsPaymentFormSharedPref().set(boolean)
                }
                TutorialScreenEnum.BILLS_PAYMENT_CONFIRMATION -> {
                    sharedPreferenceUtil.tutorialBillsPaymentConfirmationSharedPref().set(boolean)
                }
                TutorialScreenEnum.BILLS_PAYMENT_SUMMARY -> {
                    sharedPreferenceUtil.tutorialBillsPaymentSummarySharedPref().set(boolean)
                }
            }
        }
    }

    override fun hasTutorialIntroduction(): Single<Boolean> {
        return Single.fromCallable {
            sharedPreferenceUtil.tutorialIntroductionSharedPref().get()
        }
    }

    override fun hasTutorialAccount(): Single<Boolean> {
        return Single.fromCallable {
            sharedPreferenceUtil.tutorialAccountSharedPref().get()
        }
    }

    override fun hasTutorialTransact(): Single<Boolean> {
        return Single.fromCallable {
            sharedPreferenceUtil.tutorialTransactSharedPref().get()
        }
    }

    override fun hasTutorialApproval(): Single<Boolean> {
        return Single.fromCallable {
            sharedPreferenceUtil.tutorialApprovalSharedPref().get()
        }
    }

    override fun hasTutorialApprovalDetail(): Single<Boolean> {
        return Single.fromCallable {
            sharedPreferenceUtil.tutorialApprovalDetailSharedPref().get()
        }
    }

    override fun hasTutorialUser(): Single<Boolean> {
        return Single.fromCallable {
            sharedPreferenceUtil.tutorialUserSharedPref().get()
        }
    }

    override fun hasTutorialSettings(): Single<Boolean> {
        return Single.fromCallable {
            sharedPreferenceUtil.tutorialSettingsSharedPref().get()
        }
    }

    override fun hasTutorialBillsPayment(): Single<Boolean> {
        return Single.fromCallable {
            sharedPreferenceUtil.tutorialBillsPaymentSharedPref().get()
        }
    }

    override fun hasTutorialFundTransfer(): Single<Boolean> {
        return Single.fromCallable {
            sharedPreferenceUtil.tutorialFundTransferSharedPref().get()
        }
    }

    override fun setTutorialIntroduction(boolean: Boolean): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.tutorialIntroductionSharedPref().set(boolean)
        }
    }

    override fun setTutorialTransact(boolean: Boolean): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.tutorialTransactSharedPref().set(boolean)
        }
    }

    override fun setTutorialApprovalDetail(boolean: Boolean): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.tutorialApprovalDetailSharedPref().set(boolean)
        }
    }

    override fun setTutorialApproval(boolean: Boolean): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.tutorialApprovalSharedPref().set(boolean)
        }
    }

    override fun setTutorialAccount(boolean: Boolean): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.tutorialAccountSharedPref().set(boolean)
        }
    }

    override fun setTutorialUser(boolean: Boolean): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.tutorialUserSharedPref().set(boolean)
        }
    }

    override fun setTutorialSettings(boolean: Boolean): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.tutorialSettingsSharedPref().set(boolean)
        }
    }

    override fun setTutorialFundTransfer(boolean: Boolean): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.tutorialFundTransferSharedPref().set(boolean)
        }
    }

    override fun setTutorialBillsPayment(boolean: Boolean): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.tutorialBillsPaymentSharedPref().set(boolean)
        }
    }

    override fun resetTutorial(): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.tutorialIntroductionSharedPref().set(true)
            sharedPreferenceUtil.tutorialAccountSharedPref().set(true)
            sharedPreferenceUtil.tutorialTransactSharedPref().set(true)
            sharedPreferenceUtil.tutorialApprovalSharedPref().set(true)
            sharedPreferenceUtil.tutorialAlertsSharedPref().set(true)
            sharedPreferenceUtil.tutorialSettingsSharedPref().set(true)
            sharedPreferenceUtil.tutorialUserSharedPref().set(true)
            sharedPreferenceUtil.tutorialApprovalDetailSharedPref().set(true)
            sharedPreferenceUtil.tutorialFundTransferSharedPref().set(true)
            sharedPreferenceUtil.tutorialBillsPaymentSharedPref().set(true)
        }
    }

    override fun skipTutorial(): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.tutorialIntroductionSharedPref().set(false)
            sharedPreferenceUtil.tutorialAccountSharedPref().set(false)
            sharedPreferenceUtil.tutorialTransactSharedPref().set(false)
            sharedPreferenceUtil.tutorialApprovalSharedPref().set(false)
            sharedPreferenceUtil.tutorialAlertsSharedPref().set(false)
            sharedPreferenceUtil.tutorialSettingsSharedPref().set(false)
            sharedPreferenceUtil.tutorialUserSharedPref().set(false)
            sharedPreferenceUtil.tutorialApprovalDetailSharedPref().set(false)
            sharedPreferenceUtil.tutorialFundTransferSharedPref().set(false)
            sharedPreferenceUtil.tutorialBillsPaymentSharedPref().set(false)
        }
    }

    override fun isFirstCheckDeposit(): Single<Boolean> {
        return Single.fromCallable {
            sharedPreferenceUtil.firstDepositCheckSharedPref().get()
        }
    }

    override fun setFirstCheckDeposit(isFirstLaunch: Boolean): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.firstDepositCheckSharedPref().set(isFirstLaunch)
        }
    }

    override fun considerAsRecentUser(promptTypeEnum: PromptTypeEnum): Completable {
        return Completable.fromAction {
            if (sharedPreferenceUtil.recentUsersSharedPref().get() != "") {
                val recentUsers = JsonHelper.fromListJson<RecentUser>(
                    sharedPreferenceUtil.recentUsersSharedPref().get()
                )
                recentUsers.forEach {
                    if (it.userEmail == sharedPreferenceUtil.emailSharedPref().get()) {
                        if (promptTypeEnum == PromptTypeEnum.BIOMETRIC) {
                            it.isAskBiometric = true
                        } else {
                            it.isAskTrustDevice = true
                        }
                        return@forEach
                    }
                }
                val recentUsersString = JsonHelper.toJson(recentUsers)
                sharedPreferenceUtil.recentUsersSharedPref().set(recentUsersString)
            }
        }
    }

    override fun getRecentUsers(): Single<MutableList<RecentUser>> {
        return Single.fromCallable {
            JsonHelper.fromListJson<RecentUser>(sharedPreferenceUtil.recentUsersSharedPref().get())
        }
    }

    override fun isPromptedDialog(promptTypeEnum: PromptTypeEnum): Maybe<Boolean> {
        return Maybe.fromCallable {
            if (sharedPreferenceUtil.recentUsersSharedPref().get() != "") {
                val recentUsers = JsonHelper.fromListJson<RecentUser>(
                    sharedPreferenceUtil.recentUsersSharedPref().get()
                ).toMutableList()

                val foundUser = recentUsers
                    .find { it.userEmail == sharedPreferenceUtil.emailSharedPref().get() }
                if (promptTypeEnum == PromptTypeEnum.BIOMETRIC) {
                    foundUser?.isAskBiometric
                } else {
                    foundUser?.isAskTrustDevice
                }
            } else {
                false
            }
        }
    }

    override fun hasPendingChangeEmail(): Single<Boolean> {
        return Single.fromCallable {
            sharedPreferenceUtil.hasPendingEmailChangedSharedPref().get()
        }
    }

    override fun setPendingChangeEmail(hasPending: Boolean): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.hasPendingEmailChangedSharedPref().set(hasPending)
        }
    }

    override fun hasMakeTransferProductPermission(): Single<Boolean> {
        return Single.fromCallable {
            val role = cacheManager.getObject(CacheManager.ROLE) as? Role
            role?.roleAccountPermissions?.forEach { roleAccountPermission ->
                roleAccountPermission.productPermissions?.forEach { productPermission ->
                    if (productPermission.name == PermissionNameEnum.FUND_TRANSFER.value &&
                        productPermission.permissions.size > 0
                    ) {
                        productPermission.permissions.forEach {
                            if (it.code == Constant.Permissions.CODE_FT_CREATETRANSACTIONS) {
                                return@fromCallable true
                            }
                        }
                    }
                }
            }
            return@fromCallable false
        }
    }

    override fun hasMakePaymentProductPermission(): Single<Boolean> {
        return Single.fromCallable {
            val role = cacheManager.getObject(CacheManager.ROLE) as? Role
            role?.roleAccountPermissions?.forEach {
                it.productPermissions?.forEach { productPermission ->
                    if (productPermission.name == PermissionNameEnum.BILLS_PAYMENT.value &&
                        productPermission.permissions.size > 0
                    ) {
                        productPermission.permissions.forEach {
                            if (it.code == Constant.Permissions.CODE_BP_CREATEBILLSPAYMENT) {
                                return@fromCallable true
                            }
                        }
                    }
                }
            }
            return@fromCallable false
        }
    }

    override fun hasUBPProductPermission(): Single<Boolean> {
        return Single.fromCallable {
            val role = cacheManager.getObject(CacheManager.ROLE) as? Role
            role?.roleAccountPermissions?.forEach {
                it.productPermissions?.forEach { productPermission ->
                    if (productPermission.name == PermissionNameEnum.FUND_TRANSFER.value &&
                        productPermission.permissions.size > 0
                    ) {
                        productPermission.permissions.forEach {
                            if (it.code == Constant.Permissions.CODE_FT_CREATETRANSACTIONS_UNIONBANK) {
                                return@fromCallable true
                            }
                        }
                    }
                }
            }
            return@fromCallable false
        }
    }

    override fun hasOtherBanksProductPermission(): Single<Boolean> {
        return Single.fromCallable {
            val role = cacheManager.getObject(CacheManager.ROLE) as? Role
            role?.roleAccountPermissions?.forEach {
                it.productPermissions?.forEach { productPermission ->
                    if (productPermission.name == PermissionNameEnum.FUND_TRANSFER.value &&
                        productPermission.permissions.size > 0
                    ) {
                        productPermission.permissions.forEach {
                            if (it.code == Constant.Permissions.CODE_FT_CREATETRANSACTIONS_UNIONBANK ||
                                it.code == Constant.Permissions.CODE_FT_CREATETRANSACTIONS_PESONET ||
                                it.code == Constant.Permissions.CODE_FT_CREATETRANSACTIONS_PDDTS ||
                                it.code == Constant.Permissions.CODE_FT_CREATETRANSACTIONS_SWIFT
                            ) {
                                return@fromCallable true
                            }
                        }
                    }
                }
            }
            return@fromCallable false
        }
    }

    override fun hasFundTransferTransactionsPermission(): Single<Boolean> {
        return Single.fromCallable {
            val role = cacheManager.getObject(CacheManager.ROLE) as? Role
            role?.roleAccountPermissions?.forEach {
                it.productPermissions?.forEach { productPermission ->
                    if (productPermission.name == PermissionNameEnum.FUND_TRANSFER.value &&
                        productPermission.permissions.size > 0
                    ) {
                        productPermission.permissions.forEach {
                            if (it.code == Constant.Permissions.CODE_FT_CREATETRANSACTIONS ||
                                it.code == Constant.Permissions.CODE_FT_VIEWTRANSACTIONS
                            ) {
                                return@fromCallable true
                            }
                        }
                    }
                }
            }
            return@fromCallable false
        }
    }

    override fun hasViewBeneficiaryPermission(): Single<Boolean> {
        return Single.fromCallable {
            val role = cacheManager.getObject(CacheManager.ROLE) as? Role
            role?.roleAccountPermissions?.forEach {
                it.productPermissions?.forEach { productPermission ->
                    if (productPermission.name == PermissionNameEnum.FUND_TRANSFER.value &&
                        productPermission.permissions.size > 0
                    ) {
                        productPermission.permissions.forEach {
                            if (it.code == Constant.Permissions.CODE_BM_VIEWBENEFICIARYMASTER) {
                                return@fromCallable true
                            }
                        }
                    }
                }
            }
            return@fromCallable false
        }
    }

    override fun hasCreateFrequentBillerPermission(): Single<Boolean> {
        return Single.fromCallable {
            val role = cacheManager.getObject(CacheManager.ROLE) as? Role
            role?.roleAccountPermissions?.forEach {
                it.productPermissions?.forEach { productPermission ->
                    if (productPermission.name == PermissionNameEnum.BILLS_PAYMENT.value &&
                        productPermission.permissions.size > 0
                    ) {
                        productPermission.permissions.forEach {
                            if (it.code == Constant.Permissions.CODE_BP_CREATEFREQUENTBILLER) {
                                return@fromCallable true
                            }
                        }
                    }
                }
            }
            return@fromCallable false
        }
    }

    override fun hasDeleteFrequentBillerPermission(): Single<Boolean> {
        return Single.fromCallable {
            val role = cacheManager.getObject(CacheManager.ROLE) as? Role
            role?.roleAccountPermissions?.forEach {
                it.productPermissions?.forEach { productPermission ->
                    if (productPermission.name == PermissionNameEnum.BILLS_PAYMENT.value &&
                        productPermission.permissions.size > 0
                    ) {
                        productPermission.permissions.forEach {
                            if (it.code == Constant.Permissions.CODE_BP_DELETEFREQUENTBILLER) {
                                return@fromCallable true
                            }
                        }
                    }
                }
            }
            return@fromCallable false
        }
    }

    override fun hasBeneficiaryCreationPermission(): Single<Boolean> {
        return Single.fromCallable {
            val role = cacheManager.getObject(CacheManager.ROLE) as? Role
            role?.roleAccountPermissions?.forEach {
                it.productPermissions?.forEach { productPermission ->
                    if (productPermission.name == PermissionNameEnum.FUND_TRANSFER.value &&
                        productPermission.permissions.size > 0
                    ) {
                        productPermission.permissions.forEach {
                            if (it.code == Constant.Permissions.CODE_BM_CREATEBENEFICIARYMASTER) {
                                return@fromCallable true
                            }
                        }
                    }
                }
            }
            return@fromCallable false
        }
    }

    override fun hasPermission(accountId: Int, permission: String, code: String): Single<Boolean> {
        return Single.fromCallable {
            val role = cacheManager.getObject(CacheManager.ROLE) as? Role
            role?.roleAccountPermissions?.forEach {
                if (it.accountId == accountId) {
                    it.productPermissions?.forEach { productPermission ->
                        if (productPermission.name == permission &&
                            productPermission.permissions.size > 0
                        ) {
                            productPermission.permissions.forEach {
                                if (code == Constant.Permissions.CODE_FT_CREATETRANSACTIONS_UNIONBANK) {
                                    if (it.code == Constant.Permissions.CODE_FT_CREATETRANSACTIONS_UNIONBANK ||
                                        it.code == Constant.Permissions.CODE_FT_CREATETRANSACTIONS_OWN
                                    ) {
                                        return@fromCallable true
                                    }
                                } else {
                                    if (it.code == code) {
                                        return@fromCallable true
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return@fromCallable false
        }
    }

    override fun hasPermissionChannel(permission: String, code: String): Single<Permissions> {
        return Single.fromCallable {
            val role = (cacheManager.getObject(CacheManager.ROLE) as? Role)
            role?.roleAccountPermissions?.forEach {
                it.productPermissions
                    ?.filter { it.name == permission }
                    ?.forEach { productPermission ->
                        return@fromCallable productPermission.permissions
                            .find { it.code == code } ?: Permissions()
                    }
            }
            return@fromCallable Permissions()
        }
    }

    override fun hasBillsPaymentTransactionsPermission(): Single<Boolean> {
        return Single.fromCallable {
            val role = (cacheManager.getObject(CacheManager.ROLE) as? Role)
            role?.roleAccountPermissions?.forEach {
                it.productPermissions?.forEach { productPermission ->
                    if (productPermission.name == PermissionNameEnum.BILLS_PAYMENT.value &&
                        productPermission.permissions.size > 0
                    ) {
                        productPermission.permissions.forEach {
                            if (it.code == Constant.Permissions.CODE_BP_CREATEBILLSPAYMENT ||
                                it.code == Constant.Permissions.CODE_BP_VIEWBPHISTORY
                            ) {
                                return@fromCallable true
                            }
                        }
                    }
                }
            }
            return@fromCallable false
        }
    }

    override fun getPermissionCollection(
        accountId: Int?
    ): Single<MutableList<RoleAccountPermissions>> {
        return Single.fromCallable {
            val roleAccountPermissions =
                (cacheManager.getObject(CacheManager.ROLE) as? Role)?.roleAccountPermissions
            if (accountId != null) {
                roleAccountPermissions
                    ?.filter { it.accountId == accountId }
                    ?.toMutableList()
            }
            return@fromCallable roleAccountPermissions
        }
    }

    override fun hasDeleteScheduledTransferPermission(): Single<Boolean> {
        return Single.fromCallable {
            val role = cacheManager.getObject(CacheManager.ROLE) as? Role
            role?.roleAccountPermissions?.forEach {
                it.productPermissions?.forEach { productPermission ->
                    if (productPermission.name == PermissionNameEnum.FUND_TRANSFER.value &&
                        productPermission.permissions.size > 0
                    ) {
                        productPermission.permissions.forEach {
                            if (it.code == Constant.Permissions.CODE_FT_DELETESCHEDULED) {
                                return@fromCallable true
                            }
                        }
                    }
                }
            }
            return@fromCallable false
        }
    }

    override fun hasCreateScheduledTransferPermission(): Single<Boolean> {
        return Single.fromCallable {
            val role = cacheManager.getObject(CacheManager.ROLE) as? Role
            role?.roleAccountPermissions?.forEach {
                it.productPermissions?.forEach { productPermission ->
                    if (productPermission.name == PermissionNameEnum.FUND_TRANSFER.value &&
                        productPermission.permissions.size > 0
                    ) {
                        productPermission.permissions.forEach {
                            if (it.code == Constant.Permissions.CODE_FT_SCHEDULEDTRANSACTIONS) {
                                return@fromCallable true
                            }
                        }
                    }
                }
            }
            return@fromCallable false
        }
    }

    override fun isTableView(): Single<Boolean> {
        return Single.fromCallable {
            sharedPreferenceUtil.isTableView().get()
        }
    }

    override fun setTableView(isTableView: Boolean): Completable {
        return Completable.fromAction {
            sharedPreferenceUtil.isTableView().set(isTableView)
        }
    }

    override fun getGovernmentIds(): Single<MutableList<Selector>> {
        return Single.fromCallable {
            singleSelectorData.getGovernmentIds()
        }
    }

    override fun getSalutations(): Single<MutableList<Selector>> {
        return Single.fromCallable {
            singleSelectorData.getSalutations()
        }
    }

    override fun getGenders(): Single<MutableList<Selector>> {
        return Single.fromCallable {
            singleSelectorData.getGenders()
        }
    }

    override fun getCivilStatuses(): Single<MutableList<Selector>> {
        return Single.fromCallable {
            singleSelectorData.getCivilStatuses()
        }
    }

    override fun getNationality(): Single<MutableList<Selector>> {
        return Single.fromCallable {
            singleSelectorData.getNationality()
        }
    }

    override fun getRecordTypes(): Single<MutableList<Selector>> {
        return Single.fromCallable {
            singleSelectorData.getUSRecordTypes()
        }
    }

    override fun getOccupations(): Single<MutableList<Selector>> {
        return Single.fromCallable {
            singleSelectorData.getOccupations()
        }
    }

    override fun getSourceOfFunds(): Single<MutableList<Selector>> {
        return Single.fromCallable {
            singleSelectorData.getSourceOfFunds()
        }
    }

    override fun saveEnabledFeatures(enableFeatures: MutableList<String>): Completable {
        return Completable.fromAction {
            val enabledFeaturesString = JsonHelper.toJson(enableFeatures)
            cacheManager.put(CacheManager.ENABLED_FEATURES, enabledFeaturesString)
        }
    }

    override fun isEnabledFeature(featuresEnum: FeaturesEnum): Single<Boolean> {
        return Single.fromCallable {
            val enabledFeaturesString = cacheManager.get(CacheManager.ENABLED_FEATURES)
            val enabledFeatures = JsonHelper.fromListJson<String>(enabledFeaturesString)
            val isFindFeature = enabledFeatures.find { featuresEnum.name == it }
            isFindFeature != null
        }
    }

    override fun isTrialMode(): Maybe<Boolean> {
        return Maybe.fromCallable {
            sharedPreferenceUtil.isTrialMode().get()
        }
    }

    override fun getTrialModeDaysRemaining(): Maybe<String> {
        return Maybe.fromCallable {
            sharedPreferenceUtil.trialModeDaysRemainingSharedPref().get()
        }
    }

    override fun getOrgID(): Maybe<String> {
        return Maybe.fromCallable {
            sharedPreferenceUtil.getOrgID().get()
        }
    }
}
