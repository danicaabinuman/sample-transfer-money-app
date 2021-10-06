package com.unionbankph.corporate.settings.data.gateway

import com.unionbankph.corporate.auth.data.form.ChangePasswordForm
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.auth.data.model.Permissions
import com.unionbankph.corporate.auth.data.model.RoleAccountPermissions
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.form.VerifyOTPForm
import com.unionbankph.corporate.common.data.model.Auth
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.corporate.data.model.RecurrenceTypes
import com.unionbankph.corporate.settings.data.form.ManageDeviceForm
import com.unionbankph.corporate.settings.data.form.OTPSettingsForm
import com.unionbankph.corporate.settings.data.form.OTPTypeForm
import com.unionbankph.corporate.settings.data.form.VerifyEmailAddressForm
import com.unionbankph.corporate.settings.data.model.*
import com.unionbankph.corporate.settings.domain.constant.FeaturesEnum
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

interface SettingsGateway {

    fun setUdid(): Completable

    fun getUdid(): Maybe<String>

    fun getAccessToken(): Single<String>

    fun getNotificationToken(): Single<String>

    fun getCountryCodes(): Single<MutableList<CountryCode>>

    fun getCountryCode(id: String, mobileNumber: String): Single<CountryCode>

    fun setFingerprint(): Single<FingerPrintToken>

    fun getCacheValue(key: String): Single<String>

    fun clearFingerprintCredential(): Completable

    fun setFingerprintCredential(token: String): Completable

    fun getFingerprintToken(): Maybe<String>

    fun getFingerprintFullname(): Maybe<String>

    fun getFingerprintEmail(): Maybe<String>

    fun deleteFingerPrint(): Completable

    fun oTPSettings(otpSettingsForm: OTPSettingsForm): Single<Auth>

    fun oTPSettingsVerify(verifyOTPForm: VerifyOTPForm): Single<OTPSettingsDto>

    fun oTPSettingsVerifyResend(
        resendOTPForm: com.unionbankph.corporate.auth.data.form.ResendOTPForm
    ): Single<Auth>

    fun getOTPSettings(): Single<OTPSettingsDto>

    fun hasTOTP(): Maybe<Boolean>

    fun hasNotificationToken(): Single<Boolean>

    fun setNotificationToken(): Completable

    fun isTrustedDevice(): Maybe<Boolean>

    fun isReadMCDTerms(): Maybe<Boolean>

    fun isNewUserDetected(): Maybe<Boolean>

    fun setNewUserDetected(isNewUserDetected: Boolean): Completable

    fun totpSubscribe(manageDeviceForm: ManageDeviceForm): Completable

    fun getOTPTypes(): Single<OTPTypeDto>

    fun setOTPType(otpTypeForm: OTPTypeForm): Single<OTPTypeDto>

    fun setTOTPToken(token: String): Completable

    fun unTrustDevice(manageDeviceForm: ManageDeviceForm): Single<Message>

    fun forgetDevice(id: String): Single<Message>

    fun getManageDevicesList(): Single<ManageDevicesDto>

    fun getManageDeviceDetail(id: String): Single<ManageDeviceDetailDto>

    fun getLoginHistory(id: String, pageable: Pageable): Single<PagedDto<LastAccessed>>

    fun logoutUser(): Completable

    fun updateShortCutBadgeCount(): Single<Int>

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

    fun getPermissionCollection(accountId: Int? = null): Single<MutableList<RoleAccountPermissions>>

    fun hasDeleteScheduledTransferPermission(): Single<Boolean>

    fun hasCreateScheduledTransferPermission(): Single<Boolean>

    fun hasPendingChangeEmail(): Single<Boolean>
    fun setPendingChangeEmail(hasPending: Boolean): Completable
    fun verifyEmailAddress(verifyEmailAddressForm: VerifyEmailAddressForm): Single<Message>

    fun getRecentUsers(): Single<MutableList<RecentUser>>
    fun isPromptedDialog(promptTypeEnum: PromptTypeEnum): Maybe<Boolean>
    fun considerAsRecentUser(promptTypeEnum: PromptTypeEnum): Completable
    fun updateRecentUser(promptTypeEnum: PromptTypeEnum, isPrompt: Boolean): Completable
    fun getRecurrenceTypes(): Single<MutableList<RecurrenceTypes>>

    fun isFirstCheckDeposit(): Single<Boolean>
    fun setFirstCheckDeposit(isFirstLaunch: Boolean): Completable

    fun saveMobileNumber(mobileNumber: String, countryCode: CountryCode): Completable
    fun changePassword(changePasswordForm: ChangePasswordForm): Single<Message>
    fun changeEmailAddress(changeEmailAddressParams: HashMap<String, String>): Single<Message>
    fun changeMobileNumber(changeMobileNumberParams: HashMap<String, String>): Single<Auth>
    fun changeMobileNumberOTP(changeMobileNumberOTPParams: HashMap<String, String>): Single<Message>
    fun changeMobileNumberResendOTP(
        resendOTPForm: com.unionbankph.corporate.auth.data.form.ResendOTPForm
    ): Single<Auth>

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

    fun getEnabledFeatures(): Completable
    fun saveEnabledFeatures(enabledFeatures: MutableList<String>): Completable
    fun isEnabledFeature(featuresEnum: FeaturesEnum): Single<Boolean>
}
