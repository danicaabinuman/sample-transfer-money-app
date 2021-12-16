package com.unionbankph.corporate.settings.data.source.local

import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.auth.data.model.Permissions
import com.unionbankph.corporate.auth.data.model.RoleAccountPermissions
import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.settings.data.model.RecentUser
import com.unionbankph.corporate.settings.domain.constant.FeaturesEnum
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

/**
 * Created by herald25santos on 2020-01-14
 */
interface SettingsCache {

    fun setUdid(): Completable

    fun getUdid(): Maybe<String>

    fun getAccessToken(): Single<String>

    fun getNotificationToken(): Single<String>

    fun getCacheValue(key: String): Single<String>

    fun saveMobileNumber(mobileNumber: String, countryCode: CountryCode): Completable

    fun clearFingerprintCredential(): Completable

    fun setFingerprintCredential(token: String): Completable

    fun getFingerprintToken(): Maybe<String>

    fun getFingerprintFullname(): Maybe<String>

    fun getFingerprintEmail(): Maybe<String>

    fun deleteFingerPrint(): Completable

    fun updateRecentUser(promptTypeEnum: PromptTypeEnum, isPrompt: Boolean): Completable

    fun hasNotificationToken(): Single<Boolean>

    fun setNotificationToken(): Completable

    fun isTrustedDevice(): Maybe<Boolean>

    fun isReadMCDTerms(): Maybe<Boolean>

    fun isNewUserDetected(): Maybe<Boolean>

    fun setNewUserDetected(isNewUserDetected: Boolean): Completable

    fun hasTOTP(): Maybe<Boolean>

    fun setTOTPToken(token: String): Completable

    fun logoutUser(): Completable

    fun updateShortCutBadgeCount(): Single<Int>

    /* PERMISSION */

    fun hasMakeTransferProductPermission(): Single<Boolean>

    fun hasMakePaymentProductPermission(): Single<Boolean>

    fun hasFundTransferTransactionsPermission(): Single<Boolean>

    fun hasBillsPaymentTransactionsPermission(): Single<Boolean>

    fun hasBeneficiaryCreationPermission(): Single<Boolean>

    fun hasViewBeneficiaryPermission(): Single<Boolean>

    fun hasCreateFrequentBillerPermission(): Single<Boolean>

    fun hasDeleteFrequentBillerPermission(): Single<Boolean>

    fun hasUBPProductPermission(): Single<Boolean>

    fun hasPermission(accountId: Int, permission: String, code: String): Single<Boolean>

    fun hasPermissionChannel(permission: String, code: String): Single<Permissions>

    fun hasOtherBanksProductPermission(): Single<Boolean>

    fun getPermissionCollection(accountId: Int?): Single<MutableList<RoleAccountPermissions>>

    fun hasDeleteScheduledTransferPermission(): Single<Boolean>

    fun hasCreateScheduledTransferPermission(): Single<Boolean>

    /* TUTORIAL */

    fun hasTutorialIntroduction(): Single<Boolean>
    fun hasTutorialAccount(): Single<Boolean>
    fun hasTutorialTransact(): Single<Boolean>
    fun hasTutorialApproval(): Single<Boolean>
    fun hasTutorialUser(): Single<Boolean>
    fun hasTutorialSettings(): Single<Boolean>
    fun hasTutorial(key: TutorialScreenEnum): Single<Boolean>
    fun hasTutorialApprovalDetail(): Single<Boolean>
    fun hasTutorialFundTransfer(): Single<Boolean>
    fun hasTutorialBillsPayment(): Single<Boolean>
    fun setTutorialIntroduction(boolean: Boolean): Completable
    fun setTutorial(key: TutorialScreenEnum, boolean: Boolean): Completable
    fun setTutorialTransact(boolean: Boolean): Completable
    fun setTutorialApproval(boolean: Boolean): Completable
    fun setTutorialApprovalDetail(boolean: Boolean): Completable
    fun setTutorialAccount(boolean: Boolean): Completable
    fun setTutorialUser(boolean: Boolean): Completable
    fun setTutorialSettings(boolean: Boolean): Completable
    fun setTutorialFundTransfer(boolean: Boolean): Completable
    fun setTutorialBillsPayment(boolean: Boolean): Completable
    fun resetTutorial(): Completable
    fun skipTutorial(): Completable

    fun hasPendingChangeEmail(): Single<Boolean>
    fun setPendingChangeEmail(hasPending: Boolean): Completable

    fun getRecentUsers(): Single<MutableList<RecentUser>>
    fun considerAsRecentUser(promptTypeEnum: PromptTypeEnum): Completable
    fun isPromptedDialog(promptTypeEnum: PromptTypeEnum): Maybe<Boolean>

    fun isFirstCheckDeposit(): Single<Boolean>
    fun setFirstCheckDeposit(isFirstLaunch: Boolean): Completable

    fun isTableView(): Single<Boolean>
    fun setTableView(isTableView: Boolean): Completable

    fun getGovernmentIds(): Single<MutableList<Selector>>
    fun getSalutations(): Single<MutableList<Selector>>
    fun getGenders(): Single<MutableList<Selector>>
    fun getCivilStatuses(): Single<MutableList<Selector>>
    fun getNationality(): Single<MutableList<Selector>>
    fun getRecordTypes(): Single<MutableList<Selector>>
    fun getOccupations(): Single<MutableList<Selector>>
    fun getSourceOfFunds(): Single<MutableList<Selector>>

    fun saveEnabledFeatures(enableFeatures: MutableList<String>): Completable
    fun isEnabledFeature(featuresEnum: FeaturesEnum): Single<Boolean>

    fun isTrialMode(): Maybe<Boolean>
    fun getTrialModeDaysRemaining(): Maybe<String>
    fun getOrgID(): Maybe<String>

}
