package com.unionbankph.corporate.settings.data.gateway.impl

import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.auth.data.form.ChangePasswordForm
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.auth.data.model.Permissions
import com.unionbankph.corporate.auth.data.model.RoleAccountPermissions
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.form.VerifyOTPForm
import com.unionbankph.corporate.common.data.model.Auth
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.domain.provider.ResponseProvider
import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.corporate.data.model.RecurrenceTypes
import com.unionbankph.corporate.settings.data.form.ManageDeviceForm
import com.unionbankph.corporate.settings.data.form.OTPSettingsForm
import com.unionbankph.corporate.settings.data.form.OTPTypeForm
import com.unionbankph.corporate.settings.data.form.VerifyEmailAddressForm
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.data.model.*
import com.unionbankph.corporate.settings.data.source.local.SettingsCache
import com.unionbankph.corporate.settings.data.source.remote.SettingsRemote
import com.unionbankph.corporate.settings.domain.constant.FeaturesEnum
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject

class SettingsGatewayImpl
@Inject
constructor(
    private val responseProvider: ResponseProvider,
    private val settingsRemote: SettingsRemote,
    private val settingsCache: SettingsCache
) : SettingsGateway {

    override fun setUdid(): Completable {
        return settingsCache.setUdid()
    }

    override fun getUdid(): Maybe<String> {
        return settingsCache.getUdid()
    }

    override fun getAccessToken(): Single<String> {
        return settingsCache.getAccessToken()
    }

    override fun getNotificationToken(): Single<String> {
        return settingsCache.getNotificationToken()
    }

    override fun getCountryCodes(): Single<MutableList<CountryCode>> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.getCountryCodes(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getCountryCode(id: String, mobileNumber: String): Single<CountryCode> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.getCountryCode(it, id, mobileNumber) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun setFingerprint(): Single<FingerPrintToken> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.setFingerprint(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getCacheValue(key: String): Single<String> {
        return settingsCache.getCacheValue(key)
    }

    override fun setFingerprintCredential(token: String): Completable {
        return settingsCache.setFingerprintCredential(token)
    }

    override fun getFingerprintToken(): Maybe<String> {
        return settingsCache.getFingerprintToken()
    }

    override fun getFingerprintFullname(): Maybe<String> {
        return settingsCache.getFingerprintFullname()
    }

    override fun getFingerprintEmail(): Maybe<String> {
        return settingsCache.getFingerprintEmail()
    }

    override fun deleteFingerPrint(): Completable {
        return settingsCache.deleteFingerPrint()
    }

    override fun clearFingerprintCredential(): Completable {
        return settingsCache.clearFingerprintCredential()
    }

    override fun hasTutorialIntroduction(): Single<Boolean> {
        return settingsCache.hasTutorialIntroduction()
    }

    override fun hasTutorialAccount(): Single<Boolean> {
        return settingsCache.hasTutorialAccount()
    }

    override fun hasTutorialTransact(): Single<Boolean> {
        return settingsCache.hasTutorialTransact()
    }

    override fun hasTutorialApproval(): Single<Boolean> {
        return settingsCache.hasTutorialApproval()
    }

    override fun hasTutorialApprovalDetail(): Single<Boolean> {
        return settingsCache.hasTutorialApprovalDetail()
    }

    override fun hasTutorialUser(): Single<Boolean> {
        return settingsCache.hasTutorialUser()
    }

    override fun hasTutorialSettings(): Single<Boolean> {
        return settingsCache.hasTutorialSettings()
    }

    override fun hasTutorialBillsPayment(): Single<Boolean> {
        return settingsCache.hasTutorialBillsPayment()
    }

    override fun hasTutorialFundTransfer(): Single<Boolean> {
        return settingsCache.hasTutorialFundTransfer()
    }

    override fun setTutorialIntroduction(boolean: Boolean): Completable {
        return settingsCache.setTutorialIntroduction(boolean)
    }

    override fun setTutorialTransact(boolean: Boolean): Completable {
        return settingsCache.setTutorialTransact(boolean)
    }

    override fun setTutorialApprovalDetail(boolean: Boolean): Completable {
        return settingsCache.setTutorialApprovalDetail(boolean)
    }

    override fun setTutorialApproval(boolean: Boolean): Completable {
        return settingsCache.setTutorialApproval(boolean)
    }

    override fun setTutorialAccount(boolean: Boolean): Completable {
        return settingsCache.setTutorialAccount(boolean)
    }

    override fun setTutorialUser(boolean: Boolean): Completable {
        return settingsCache.setTutorialUser(boolean)
    }

    override fun setTutorialSettings(boolean: Boolean): Completable {
        return settingsCache.setTutorialSettings(boolean)
    }

    override fun setTutorialFundTransfer(boolean: Boolean): Completable {
        return settingsCache.setTutorialFundTransfer(boolean)
    }

    override fun setTutorialBillsPayment(boolean: Boolean): Completable {
        return settingsCache.setTutorialBillsPayment(boolean)
    }

    override fun resetTutorial(): Completable {
        return settingsCache.resetTutorial()
    }

    override fun skipTutorial(): Completable {
        return settingsCache.skipTutorial()
    }

    override fun oTPSettings(otpSettingsForm: OTPSettingsForm): Single<Auth> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.oTPSettings(it, otpSettingsForm) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun isTrustedDevice(): Maybe<Boolean> {
        return settingsCache.isTrustedDevice()
    }

    override fun isReadMCDTerms(): Maybe<Boolean> {
        return settingsCache.isReadMCDTerms()
    }

    override fun isNewUserDetected(): Maybe<Boolean> {
        return settingsCache.isNewUserDetected()
    }

    override fun setNewUserDetected(isNewUserDetected: Boolean): Completable {
        return settingsCache.setNewUserDetected(isNewUserDetected)
    }

    override fun hasTOTP(): Maybe<Boolean> {
        return settingsCache.hasTOTP()
    }

    override fun hasNotificationToken(): Single<Boolean> {
        return settingsCache.hasNotificationToken()
    }

    override fun setNotificationToken(): Completable {
        return settingsCache.setNotificationToken()
    }

    override fun getOTPSettings(): Single<OTPSettingsDto> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.getOTPSettings(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun oTPSettingsVerify(verifyOTPForm: VerifyOTPForm): Single<OTPSettingsDto> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.oTPSettingsVerify(it, verifyOTPForm) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun oTPSettingsVerifyResend(
        resendOTPForm: com.unionbankph.corporate.auth.data.form.ResendOTPForm
    ): Single<Auth> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.oTPSettingsVerifyResend(it, resendOTPForm) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun hasTutorial(key: TutorialScreenEnum): Single<Boolean> {
        return settingsCache.hasTutorial(key)
    }

    override fun setTutorial(key: TutorialScreenEnum, boolean: Boolean): Completable {
        return settingsCache.setTutorial(key, boolean)
    }

    override fun totpSubscribe(manageDeviceForm: ManageDeviceForm): Completable {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.totpSubscribe(it, manageDeviceForm) }
            .flatMap { responseProvider.executeResponseSingle(it) }
            .flatMapCompletable { settingsCache.setTOTPToken(it.token.notNullable()) }
    }

    override fun getOTPTypes(): Single<OTPTypeDto> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.getOTPTypes(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun setOTPType(otpTypeForm: OTPTypeForm): Single<OTPTypeDto> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.setOTPType(it, otpTypeForm) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun setTOTPToken(token: String): Completable {
        return settingsCache.setTOTPToken(token)
    }

    override fun unTrustDevice(manageDeviceForm: ManageDeviceForm): Single<Message> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.unTrustDevice(it, manageDeviceForm) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun forgetDevice(id: String): Single<Message> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.forgetDevice(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getManageDevicesList(): Single<ManageDevicesDto> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.getManageDevicesList(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getManageDeviceDetail(id: String): Single<ManageDeviceDetailDto> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.getManageDeviceDetail(it, id) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun getLoginHistory(id: String, pageable: Pageable): Single<PagedDto<LastAccessed>> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.getLoginHistory(it, id, pageable) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun hasPendingChangeEmail(): Single<Boolean> {
        return settingsCache.hasPendingChangeEmail()
    }

    override fun setPendingChangeEmail(hasPending: Boolean): Completable {
        return settingsCache.setPendingChangeEmail(hasPending)
    }

    override fun verifyEmailAddress(verifyEmailAddressForm: VerifyEmailAddressForm): Single<Message> {
        return settingsRemote.verifyEmailAddress(verifyEmailAddressForm)
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun isPromptedDialog(promptTypeEnum: PromptTypeEnum): Maybe<Boolean> {
        return settingsCache.isPromptedDialog(promptTypeEnum)
    }

    override fun getRecentUsers(): Single<MutableList<RecentUser>> {
        return settingsCache.getRecentUsers()
    }

    override fun considerAsRecentUser(promptTypeEnum: PromptTypeEnum): Completable {
        return settingsCache.considerAsRecentUser(promptTypeEnum)
    }

    override fun updateRecentUser(promptTypeEnum: PromptTypeEnum, isPrompt: Boolean): Completable {
        return settingsCache.updateRecentUser(promptTypeEnum, isPrompt)
    }

    override fun getRecurrenceTypes(): Single<MutableList<RecurrenceTypes>> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.getRecurrenceTypes(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun logoutUser(): Completable {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.logoutUser(it) }
            .flatMap { responseProvider.executeResponseSingle(it) }
            .flatMapCompletable { settingsCache.logoutUser() }
    }

    override fun updateShortCutBadgeCount(): Single<Int> {
        return settingsCache.updateShortCutBadgeCount()
    }

    override fun isFirstCheckDeposit(): Single<Boolean> {
        return settingsCache.isFirstCheckDeposit()
    }

    override fun setFirstCheckDeposit(isFirstLaunch: Boolean): Completable {
        return settingsCache.setFirstCheckDeposit(isFirstLaunch)
    }

    override fun changePassword(changePasswordForm: ChangePasswordForm): Single<Message> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.changePassword(it, changePasswordForm) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun changeEmailAddress(changeEmailAddressParams: HashMap<String, String>): Single<Message> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.changeEmailAddress(it, changeEmailAddressParams) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun changeMobileNumber(changeMobileNumberParams: HashMap<String, String>): Single<Auth> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.changeMobileNumber(it, changeMobileNumberParams) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun changeMobileNumberOTP(
        changeMobileNumberOTPParams: HashMap<String, String>
    ): Single<Message> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.changeMobileNumberOTP(it, changeMobileNumberOTPParams) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun changeMobileNumberResendOTP(
        resendOTPForm: com.unionbankph.corporate.auth.data.form.ResendOTPForm
    ): Single<Auth> {
        return settingsCache.getAccessToken()
            .flatMap { settingsRemote.changeMobileNumberResendOTP(it, resendOTPForm) }
            .flatMap { responseProvider.executeResponseSingle(it) }
    }

    override fun saveMobileNumber(mobileNumber: String, countryCode: CountryCode): Completable {
        return settingsCache.saveMobileNumber(mobileNumber, countryCode)
    }

    override fun hasMakeTransferProductPermission(): Single<Boolean> {
        return settingsCache.hasMakeTransferProductPermission()
    }

    override fun hasMakePaymentProductPermission(): Single<Boolean> {
        return settingsCache.hasMakePaymentProductPermission()
    }

    override fun hasFundTransferTransactionsPermission(): Single<Boolean> {
        return settingsCache.hasFundTransferTransactionsPermission()
    }

    override fun hasBillsPaymentTransactionsPermission(): Single<Boolean> {
        return settingsCache.hasBillsPaymentTransactionsPermission()
    }

    override fun hasBeneficiaryCreationPermission(): Single<Boolean> {
        return settingsCache.hasBeneficiaryCreationPermission()
    }

    override fun hasViewBeneficiaryPermission(): Single<Boolean> {
        return settingsCache.hasViewBeneficiaryPermission()
    }

    override fun hasCreateFrequentBillerPermission(): Single<Boolean> {
        return settingsCache.hasCreateFrequentBillerPermission()
    }

    override fun hasDeleteFrequentBillerPermission(): Single<Boolean> {
        return settingsCache.hasDeleteFrequentBillerPermission()
    }

    override fun hasUBPProductPermission(): Single<Boolean> {
        return settingsCache.hasUBPProductPermission()
    }

    override fun hasPermission(accountId: Int, permission: String, code: String): Single<Boolean> {
        return settingsCache.hasPermission(accountId, permission, code)
    }

    override fun hasPermissionChannel(permission: String, code: String): Single<Permissions> {
        return settingsCache.hasPermissionChannel(permission, code)
    }

    override fun hasOtherBanksProductPermission(): Single<Boolean> {
        return settingsCache.hasOtherBanksProductPermission()
    }

    override fun getPermissionCollection(accountId: Int?): Single<MutableList<RoleAccountPermissions>> {
        return settingsCache.getPermissionCollection(accountId)
    }

    override fun hasDeleteScheduledTransferPermission(): Single<Boolean> {
        return settingsCache.hasDeleteScheduledTransferPermission()
    }

    override fun hasCreateScheduledTransferPermission(): Single<Boolean> {
        return settingsCache.hasCreateScheduledTransferPermission()
    }

    override fun isTableView(): Single<Boolean> {
        return settingsCache.isTableView()
    }

    override fun setTableView(isTableView: Boolean): Completable {
        return settingsCache.setTableView(isTableView)
    }

    override fun getGovernmentIds(): Single<MutableList<Selector>> {
        return settingsCache.getGovernmentIds()
    }

    override fun getCivilStatuses(): Single<MutableList<Selector>> {
        return settingsCache.getCivilStatuses()
    }

    override fun getGenders(): Single<MutableList<Selector>> {
        return settingsCache.getGenders()
    }

    override fun getNationality(): Single<MutableList<Selector>> {
        return settingsCache.getNationality()
    }

    override fun getRecordTypes(): Single<MutableList<Selector>> {
        return settingsCache.getRecordTypes()
    }

    override fun getSalutations(): Single<MutableList<Selector>> {
        return settingsCache.getSalutations()
    }

    override fun getOccupations(): Single<MutableList<Selector>> {
        return settingsCache.getOccupations()
    }

    override fun getSourceOfFunds(): Single<MutableList<Selector>> {
        return settingsCache.getSourceOfFunds()
    }

    override fun getEnabledFeatures(): Completable {
        return settingsRemote.getEnabledFeatures()
            .flatMap { responseProvider.executeResponseSingle(it) }
            .flatMapCompletable {
                settingsCache.saveEnabledFeatures(it.enabledfeatures.notNullable())
            }
    }

    override fun saveEnabledFeatures(enabledFeatures: MutableList<String>): Completable {
        return settingsCache.saveEnabledFeatures(enabledFeatures)
    }

    override fun isEnabledFeature(featuresEnum: FeaturesEnum): Single<Boolean> {
        return settingsCache.isEnabledFeature(featuresEnum)
    }
}
