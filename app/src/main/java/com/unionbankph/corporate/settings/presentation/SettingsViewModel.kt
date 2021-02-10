package com.unionbankph.corporate.settings.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.auth.data.model.CorporateUser
import com.unionbankph.corporate.common.data.model.Auth
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum
import com.unionbankph.corporate.corporate.data.model.RecurrenceTypes
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.settings.data.form.OTPSettingsForm
import com.unionbankph.corporate.settings.data.form.VerifyEmailAddressForm
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.data.model.OTPSettingsDto
import com.unionbankph.corporate.settings.domain.constant.FeaturesEnum
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val settingsGateway: SettingsGateway,
    private val corporateGateway: CorporateGateway
) : BaseViewModel() {

    private val _settingsState = MutableLiveData<SettingsState>()
    val state: LiveData<SettingsState> get() = _settingsState

    private val _featureToggle = MutableLiveData<Pair<FeaturesEnum, Boolean>>()
    val featureToggle: LiveData<Pair<FeaturesEnum, Boolean>> get() = _featureToggle

    val isValidFormInput = BehaviorSubject.create<Boolean>()

    fun changeMobileNumber(countryIdCode: String, mobileNumber: String, password: String) {
        val changeMobileNumberParams = HashMap<String, String>()
        changeMobileNumberParams["country_code_id"] = countryIdCode
        changeMobileNumberParams["mobile_number"] = mobileNumber
        changeMobileNumberParams["password"] = password
        settingsGateway.changeMobileNumber(changeMobileNumberParams)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _settingsState.value = ShowSettingsLoading }
            .doFinally { _settingsState.value = ShowSettingsDismissLoading }
            .subscribe(
                {
                    _settingsState.value = ShowSettingsMobileNumberSuccess(it)
                }, {
                    Timber.d(it, "changeMobileNumber Failed")
                    _settingsState.value = ShowSettingsError(it)
                }
            )
            .addTo(disposables)
    }

    fun changeEmailAddress(emailAddress: String, password: String) {
        val changeEmailAddressParams = HashMap<String, String>()
        changeEmailAddressParams["email_address"] = emailAddress
        changeEmailAddressParams["password"] = password
        var message = Message()
        settingsGateway.changeEmailAddress(changeEmailAddressParams)
            .doOnSuccess { message = it }
            .flatMapCompletable { settingsGateway.setPendingChangeEmail(true) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _settingsState.value = ShowSettingsLoading }
            .doFinally { _settingsState.value = ShowSettingsDismissLoading }
            .subscribe(
                {
                    _settingsState.value = ShowSettingsChangeEmailSuccess(message)
                }, {
                    Timber.d(it, "changeEmailAddress Failed")
                    _settingsState.value = ShowSettingsError(it)
                }
            ).addTo(disposables)
    }

    fun getCorporateUser() {
        corporateGateway.getCorporateUser()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    Timber.d(it.toString())
                    _settingsState.value = ShowSettingsGetCorporateUser(it)
                }, {
                    Timber.e(it, "otp details failed")
                    _settingsState.value = ShowSettingsError(it)
                }
            ).addTo(disposables)
    }

    fun oTPSettings(otpSettingsForm: OTPSettingsForm) {
        settingsGateway.oTPSettings(otpSettingsForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _settingsState.value = ShowSettingsProgressBarLoading }
            .doFinally { _settingsState.value = ShowSettingsDismissProgressBarLoading }
            .subscribe(
                {
                    _settingsState.value = ShowSettingsOTPSettingsSuccess(it)
                }, {
                    Timber.d(it, "oTPSettings Failed")
                    _settingsState.value = ShowSettingsError(it)
                }
            ).addTo(disposables)
    }

    fun getOTPSettings() {
        settingsGateway.getOTPSettings()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _settingsState.value = ShowSettingsLoading }
            .doOnSuccess { _settingsState.value = ShowSettingsDismissLoading }
            .subscribe(
                {
                    _settingsState.value = ShowSettingsGetOTPSettings(it)
                }, {
                    Timber.d(it, "getOTPSettings Failed")
                    _settingsState.value = ShowSettingsError(it)
                })
            .addTo(disposables)
    }

    fun verifyEmailAddress(verifyEmailAddressForm: VerifyEmailAddressForm) {
        var message = Message()
        settingsGateway.verifyEmailAddress(verifyEmailAddressForm)
            .doOnSuccess { message = it }
            .flatMapCompletable { settingsGateway.setPendingChangeEmail(false) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _settingsState.value = ShowSettingsLoading }
            .doFinally { _settingsState.value = ShowSettingsDismissLoading }
            .subscribe(
                {
                    _settingsState.value = ShowSettingsChangeEmailSuccess(message)
                }, {
                    Timber.d(it, "verifyEmailAddress Failed")
                    _settingsState.value = ShowSettingsError(it)
                }
            ).addTo(disposables)
    }

    fun hasPermissionChannel(permissionName: String, code: String) {
        settingsGateway.hasPermissionChannel(permissionName, code)
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    val permission = it.id != null
                    _settingsState.value = ShowSettingsHasPermission(permission, code)
                }, {
                    Timber.e(it, "hasPermissionChannel failed")
                }
            ).addTo(disposables)
    }

    fun isEnabledFeature(featuresEnum: FeaturesEnum) {
        settingsGateway.isEnabledFeature(featuresEnum)
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _featureToggle.value = Pair(featuresEnum, it)
                }, {
                    Timber.e(it, "isEnabledFeature failed")
                }
            ).addTo(disposables)
    }

    fun hasFundTransferTransactionsPermission(code: String) {
        settingsGateway.hasFundTransferTransactionsPermission()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _settingsState.value = ShowSettingsHasPermission(it, code)
                }, {
                    Timber.e(it, "hasFundTransferTransactionsPermission failed")
                }
            ).addTo(disposables)
    }

    fun hasBillsPaymentTransactionsPermission(code: String) {
        settingsGateway.hasBillsPaymentTransactionsPermission()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _settingsState.value = ShowSettingsHasPermission(it, code)
                }, {
                    Timber.e(it, "hasBillsPaymentTransactionsPermission failed")
                }
            ).addTo(disposables)
    }

    fun hasPendingChangeEmail() {
        settingsGateway.hasPendingChangeEmail()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _settingsState.value = ShowSettingsHasPendingChangeEmail(it)
                }, {
                    Timber.e(it, "hasPendingChangeEmail failed")
                    _settingsState.value = ShowSettingsError(it)
                }
            ).addTo(disposables)
    }

    fun getRecurrenceTypes() {
        settingsGateway.getRecurrenceTypes()
            .map {
                it.filter {
                    it.name.equals("One-time", true) ||
                            it.name.equals("Daily", true) ||
                            it.name.equals("Weekly", true) ||
                            it.name.equals("Monthly", true) ||
                            it.name.equals("Annually", true)
                }
                    .sortedWith(compareBy { recurrenceType -> recurrenceType.id })
                    .toMutableList()
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _settingsState.value = ShowSettingsLoading
            }
            .doFinally {
                _settingsState.value = ShowSettingsDismissLoading
            }
            .subscribe(
                {
                    _settingsState.value = ShowGetRecurrenceTypes(it)
                }, {
                    Timber.e(it, "getRecurrenceTypes failed")
                    _settingsState.value = ShowSettingsError(it)
                }).addTo(disposables)
    }

    fun updateRecentUser(promptTypeEnum: PromptTypeEnum, isPrompt: Boolean) {
        settingsGateway.updateRecentUser(promptTypeEnum, isPrompt)
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .doOnError {
                Timber.e(it, "updateRecentUser failed")
                _settingsState.value = ShowSettingsError(it)
            }
            .subscribe()
            .addTo(disposables)
    }

    fun deleteFingerPrint() {
        settingsGateway.deleteFingerPrint()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .doOnError {
                Timber.e(it, "deleteFingerPrint failed")
                _settingsState.value = ShowSettingsError(it)
            }
            .subscribe()
            .addTo(disposables)
    }
}

sealed class SettingsState

object ShowSettingsLoading : SettingsState()

object ShowSettingsDismissLoading : SettingsState()

object ShowSettingsProgressBarLoading : SettingsState()

object ShowSettingsDismissProgressBarLoading : SettingsState()

data class ShowSettingsHasPermission(
    val hasPermission: Boolean,
    val permissionCode: String
) : SettingsState()

data class ShowSettingsHasPendingChangeEmail(
    val hasPending: Boolean
) : SettingsState()

data class ShowGetRecurrenceTypes(
    val data: MutableList<RecurrenceTypes>
) : SettingsState()

data class ShowSettingsOTPSettingsSuccess(val auth: Auth) : SettingsState()

data class ShowSettingsGetOTPSettings(val otpSettingsDto: OTPSettingsDto) : SettingsState()

data class ShowSettingsChangeEmailSuccess(val message: Message) : SettingsState()

data class ShowSettingsMobileNumberSuccess(val auth: Auth) : SettingsState()

data class ShowSettingsError(val throwable: Throwable) : SettingsState()

data class ShowSettingsGetCorporateUser(val corporateUser: CorporateUser) : SettingsState()
