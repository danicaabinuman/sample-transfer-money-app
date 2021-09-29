package com.unionbankph.corporate.auth.presentation.otp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.TYPE_SMS
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.bus.event.EventBus
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.auth.data.AuthGateway
import com.unionbankph.corporate.auth.data.form.ECredOTPForm
import com.unionbankph.corporate.auth.data.form.LoginOTPForm
import com.unionbankph.corporate.auth.data.form.NominatePasswordOTPForm
import com.unionbankph.corporate.auth.data.form.NominatePasswordResendOTPForm
import com.unionbankph.corporate.auth.data.form.ResendOTPForm
import com.unionbankph.corporate.auth.data.form.ResetPasswordOTPForm
import com.unionbankph.corporate.auth.data.form.ResetPasswordResendOTPForm
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.auth.data.model.ECredLoginDto
import com.unionbankph.corporate.auth.data.model.ECredLoginOTPDto
import com.unionbankph.corporate.auth.data.model.UserCreationOTPVerified
import com.unionbankph.corporate.bills_payment.data.gateway.BillsPaymentGateway
import com.unionbankph.corporate.bills_payment.data.model.BillsPaymentVerify
import com.unionbankph.corporate.common.data.form.VerifyOTPForm
import com.unionbankph.corporate.common.data.model.ApiError
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.fund_transfer.data.gateway.FundTransferGateway
import com.unionbankph.corporate.fund_transfer.data.model.FundTransferVerify
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.data.model.OTPSettingsDto
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.zipWith
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class OTPViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val authGateway: AuthGateway,
    private val billsPaymentGateway: BillsPaymentGateway,
    private val settingsGateway: SettingsGateway,
    private val fundTransferGateway: FundTransferGateway,
    private val eventBus: EventBus
) : BaseViewModel() {

    private val _otpState = MutableLiveData<OTPState>()

    val state: LiveData<OTPState> get() = _otpState

    val otpType = BehaviorSubject.createDefault(TYPE_SMS)

    val isClickedResendOTP = BehaviorSubject.create<Boolean>()

    fun userOTP(loginOTPForm: LoginOTPForm) {
        authGateway.loginOTP(loginOTPForm)
            .flatMap { authGateway.saveCredential(it).toSingle { it } }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPLoading }
            .subscribe(
                {
                    _otpState.value = ShowOTPLoginSuccess(it.isPolicyAgreed ?: false)
                }, {
                    Timber.e(it, "userOTP Failed")
                    _otpState.value = ShowOTPDismissLoading
                    _otpState.value = ShowOTPError(it)
                }
            ).addTo(disposables)
    }

    fun loginMigrationOTP(request: String, codeForm: String) {
        Observable.just(request)
            .map { JsonHelper.fromJson<ECredLoginDto>(it) }
            .flatMapSingle {
                val eCredOTPForm = ECredOTPForm().apply {
                    requestId = it.requestId
                    code = codeForm
                    type = TYPE_SMS
                }
                authGateway.loginMigrationOTP(eCredOTPForm)
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    _otpState.value = ShowOTPLoginMigrationSuccess(it)
                }, {
                    Timber.e(it, "loginMigrationOTP Failed")
                    _otpState.value = ShowOTPError(it)
                }).addTo(disposables)
    }

    fun loginResendOTPMigration(request: String) {
        Observable.just(request)
            .map { JsonHelper.fromJson<ECredLoginDto>(it) }
            .flatMapSingle {
                authGateway.loginResendOTPMigration(ResendOTPForm(it.requestId))
            }
            .map {
                Auth(
                    type = it.type,
                    validity = it.validity,
                    requestId = it.requestId,
                    mobileNumber = it.mobileNumber,
                    invalidAttempts = it.invalidAttempts
                )
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    isClickedResendOTP.onNext(true)
                    _otpState.value = ShowOTPSuccessResend(it)
                }, {
                    Timber.e(it, "loginMigrationOTP Failed")
                    _otpState.value = ShowOTPError(it)
                }).addTo(disposables)
    }

    fun userChangeMobileNumberOTP(
        id: String,
        mobileNumber: String,
        requestId: String,
        code: String
    ) {
        val otpHashMap = HashMap<String, String>()
        otpHashMap["request_id"] = requestId
        otpHashMap["code"] = code
        settingsGateway.changeMobileNumberOTP(otpHashMap)
            .flatMap { settingsGateway.getCountryCode(id, mobileNumber) }
            .flatMapCompletable { settingsGateway.saveMobileNumber(mobileNumber, it) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_UPDATE_MOBILE_NUMBER)
                    )
                    _otpState.value = ShowOTPLoginSuccess(false)
                }, {
                    Timber.e(it, "userChangeMobileNumberOTP Failed")
                    _otpState.value = ShowOTPError(it)
                }).addTo(disposables)
    }

    fun resetPassOTP(resetPasswordOTPForm: ResetPasswordOTPForm) {
        authGateway.resetPassOTP(resetPasswordOTPForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    _otpState.value = ShowOTPPasswordSuccess(it)
                }, {
                    Timber.e(it, "resetPassOTP Failed")
                    _otpState.value = ShowOTPError(it)
                }).addTo(disposables)
    }

    fun nominatePasswordOTP(nominatePasswordOTPForm: NominatePasswordOTPForm) {
        authGateway.nominatePasswordOTP(nominatePasswordOTPForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    _otpState.value = ShowOTPPasswordSuccess(it)
                }, {
                    Timber.d(it, "nominatePasswordOTP Failed")
                    try {
                        val apiError = JsonHelper.fromJson<ApiError>(it.message)
                        _otpState.value = ShowOTPNominatePasswordError(apiError)
                    } catch (e: Exception) {
                        _otpState.value = ShowOTPError(it)
                    }
                }).addTo(disposables)
    }

    fun fundTransferOTP(requestId: String, code: String, otpType: String) {
        val otpHashMap = HashMap<String, String>()
        otpHashMap["request_id"] = requestId
        otpHashMap["code"] = code
        otpHashMap["otp_type"] = otpType
        fundTransferGateway.fundTransferOTP(otpHashMap)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    _otpState.value = ShowOTPSuccessFundTransfer(it)
                }, {
                    Timber.e(it, "fundTransferOTP Failed")
                    _otpState.value = ShowOTPError(it)
                }).addTo(disposables)
    }

    fun billsPaymentOTP(requestId: String, code: String) {
        val otpHashMap = HashMap<String, String>()
        otpHashMap["request_id"] = requestId
        otpHashMap["code"] = code
        billsPaymentGateway.billsPaymentOTP(otpHashMap)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    _otpState.value = ShowOTPSuccessBillsPayment(it)
                }, {
                    Timber.e(it, "fundTransferOTP Failed")
                    _otpState.value = ShowOTPError(it)
                }).addTo(disposables)
    }

    fun changeMobileNumberResendOTP(resendOTPForm: ResendOTPForm) {
        settingsGateway.changeMobileNumberResendOTP(resendOTPForm)
            .map { Auth(it.type, it.validity, it.requestId, it.type, it.mobileNumber) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPResendLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    isClickedResendOTP.onNext(true)
                    _otpState.value = ShowOTPSuccessResend(it)
                }, {
                    Timber.d(it, "changeMobileNumberResendOTP Failed")
                    _otpState.value = ShowOTPError(it)
                }).addTo(disposables)
    }

    fun resendOTPBillsPayment(resendOTPForm: ResendOTPForm) {
        billsPaymentGateway.resendOTPBillsPayment(resendOTPForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPResendLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    if (otpType.value.notNullable() == TYPE_SMS) {
                        isClickedResendOTP.onNext(true)
                    }
                    _otpState.value = ShowOTPSuccessResend(it)
                }, {
                    Timber.d(it, "resendOTPBillsPayment Failed")
                    _otpState.value = ShowOTPError(it)
                }).addTo(disposables)
    }

    fun resendOTPFundTransfer(resendOTPForm: ResendOTPForm) {
        fundTransferGateway.resendOTPFundTransfer(resendOTPForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPResendLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    if (otpType.value.notNullable() == TYPE_SMS) {
                        isClickedResendOTP.onNext(true)
                    }
                    _otpState.value = ShowOTPSuccessResend(it)
                }, {
                    Timber.d(it, "resendOTPFundTransfer Failed")
                    _otpState.value = ShowOTPError(it)
                }).addTo(disposables)
    }

    fun resendOTPLogin(resendOTPForm: ResendOTPForm) {
        authGateway.loginResendOTP(resendOTPForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPResendLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    if (otpType.value.notNullable() == TYPE_SMS) {
                        isClickedResendOTP.onNext(true)
                    }
                    _otpState.value = ShowOTPSuccessResend(it)
                }, {
                    Timber.d(it, "resendOTPLogin Failed")
                    _otpState.value = ShowOTPError(it)
                }).addTo(disposables)
    }

    fun resetPassResendOTP(resetPasswordResendOTPForm: ResetPasswordResendOTPForm) {
        authGateway.resetPassResendOTP(resetPasswordResendOTPForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPResendLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    isClickedResendOTP.onNext(true)
                    _otpState.value = ShowOTPSuccessResend(it)
                }, {
                    Timber.d(it, "resetPassResendOTP Failed")
                    _otpState.value = ShowOTPError(it)
                }).addTo(disposables)
    }

    fun nominatePasswordResendOTP(nominatePasswordResendOTPForm: NominatePasswordResendOTPForm) {
        authGateway.nominatePasswordResentOTP(nominatePasswordResendOTPForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPResendLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    isClickedResendOTP.onNext(true)
                    _otpState.value = ShowOTPSuccessResend(it)
                }, {
                    Timber.d(it, "nominatePasswordResendOTP Failed")
                    _otpState.value = ShowOTPError(it)
                }).addTo(disposables)
    }

    fun oTPSettingsVerify(verifyOTPForm: VerifyOTPForm) {
        settingsGateway.oTPSettingsVerify(verifyOTPForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    _otpState.value = ShowOTPSettingsVerifySuccess(it)
                }, {
                    Timber.e(it, "oTPSettingsVerify Failed")
                    _otpState.value = ShowOTPError(it)
                }
            ).addTo(disposables)
    }

    fun oTPSettingsVerifyResend(resendOTPForm: ResendOTPForm) {
        settingsGateway.oTPSettingsVerifyResend(resendOTPForm)
            .map { Auth(it.type, it.validity, it.requestId, it.type, it.mobileNumber) }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPResendLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    isClickedResendOTP.onNext(true)
                    _otpState.value = ShowOTPSuccessResend(it)
                }, {
                    Timber.d(it, "oTPSettingsVerifyResend Failed")
                    _otpState.value = ShowOTPError(it)
                }).addTo(disposables)
    }

    fun userCreationValidateOTP(verifyOTPForm: VerifyOTPForm) {
        authGateway.userCreationValidateOTP(verifyOTPForm)
//            .flatMap { authGateway.saveCredential(it).toSingle { it } }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPLoading }
            .subscribe(
                {
                    _otpState.value = ShowVerifyUserCreationOTPSuccess(it)
                }, {
                    Timber.e(it, "userOTP Failed")
                    _otpState.value = ShowOTPDismissLoading
                    _otpState.value = ShowOTPError(it)
                }
            ).addTo(disposables)
    }

    fun userCreationResendOTP(resendOTPForm: ResendOTPForm) {
        authGateway.userCreationResendOTP(resendOTPForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPResendLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    if (otpType.value.notNullable() == TYPE_SMS) {
                        isClickedResendOTP.onNext(true)
                    }
                    _otpState.value = ShowOTPSuccessResend(it)
                }, {
                    Timber.d(it, "userCreationResendOTP Failed")
                    _otpState.value = ShowOTPError(it)
                }
            ).addTo(disposables)
    }

    fun attemptShowTOTPDialog() {
        settingsGateway.getFingerprintToken()
            .zipWith(settingsGateway.hasTOTP())
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _otpState.value = ShowOTPLoading }
            .doFinally { _otpState.value = ShowOTPDismissLoading }
            .subscribe(
                {
                    val isTOTPEnabled = (it.first.isNotEmpty() && it.second)
                    _otpState.value = ShowTOTPBottomSheet(isTOTPEnabled)
                }, {
                    Timber.e(it, "attemptShowTOTPDialog Failed")
                    _otpState.value = ShowOTPError(it)
                })
            .addTo(disposables)
    }

    fun countDownTimer(period: Long, time: Long) {
        Observable.interval(period, TimeUnit.SECONDS)
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                val timer = time - it.toInt()
                _otpState.value = ShowOTPTimer(timer.toInt())
            }
            .takeUntil {
                it == time
            }
            .doOnComplete {
                _otpState.value = ShowOTPCompleteTimer(isClickedResendOTP.value ?: false)
            }.subscribe().addTo(disposables)
    }
}

sealed class OTPState

object ShowOTPLoading : OTPState()

object ShowOTPDismissLoading : OTPState()

object ShowOTPResendLoading : OTPState()

data class ShowOTPCompleteTimer(
    val isClickedResendOTP: Boolean
) : OTPState()

data class ShowTOTPBottomSheet(
    val isTOTPEnabled: Boolean
) : OTPState()


data class ShowOTPSuccessFundTransfer(
    val fundTransferVerify: FundTransferVerify
) : OTPState()

data class ShowOTPSuccessBillsPayment(
    val billsPaymentVerify: BillsPaymentVerify
) : OTPState()

data class ShowOTPLoginSuccess(val privacyAgreed: Boolean) : OTPState()

data class ShowOTPLoginMigrationSuccess(val eCredLoginOTPDto: ECredLoginOTPDto) : OTPState()

data class ShowOTPPasswordSuccess(val message: Message) : OTPState()

data class ShowOTPSuccessResend(val auth: Auth) : OTPState()

data class ShowOTPTimer(val timer: Int) : OTPState()

data class ShowOTPNominatePasswordError(val apiError: ApiError) : OTPState()

data class ShowOTPError(val throwable: Throwable) : OTPState()

data class ShowOTPSettingsVerifySuccess(
    val otpSettingsDto: OTPSettingsDto
) : OTPState()

data class ShowVerifyUserCreationOTPSuccess(
    val userCreationOTPSuccess: UserCreationOTPVerified
) : OTPState()

