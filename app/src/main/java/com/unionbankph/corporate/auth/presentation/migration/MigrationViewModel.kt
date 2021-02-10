package com.unionbankph.corporate.auth.presentation.migration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.auth.data.AuthGateway
import com.unionbankph.corporate.auth.data.form.EBankingResendOTPForm
import com.unionbankph.corporate.auth.data.form.ECredEmailConfirmationForm
import com.unionbankph.corporate.auth.data.form.ECredForm
import com.unionbankph.corporate.auth.data.form.ECredMergeAccountForm
import com.unionbankph.corporate.auth.data.form.ECredMergeAccountOTPForm
import com.unionbankph.corporate.auth.data.form.ECredOTPForm
import com.unionbankph.corporate.auth.data.form.EmailConfirmationForm
import com.unionbankph.corporate.auth.data.form.LoginEBankingMigrationForm
import com.unionbankph.corporate.auth.data.form.LoginECreditingMigrationForm
import com.unionbankph.corporate.auth.data.form.MigrationForm
import com.unionbankph.corporate.auth.data.form.MigrationMergeAccountForm
import com.unionbankph.corporate.auth.data.form.MigrationNominateEmailForm
import com.unionbankph.corporate.auth.data.form.MigrationNominateMobileNumberForm
import com.unionbankph.corporate.auth.data.form.MigrationNominatePasswordForm
import com.unionbankph.corporate.auth.data.form.ResendOTPForm
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.auth.data.model.ConfirmationEmailDto
import com.unionbankph.corporate.auth.data.model.ECredLoginDto
import com.unionbankph.corporate.auth.data.model.ECredMergeSubmitDto
import com.unionbankph.corporate.auth.data.model.ECredSubmitDto
import com.unionbankph.corporate.auth.data.model.ECredSubmitOTPDto
import com.unionbankph.corporate.auth.data.model.LoginMigrationDto
import com.unionbankph.corporate.auth.data.model.MigrationSubmitDto
import com.unionbankph.corporate.auth.data.model.NominateEmailDto
import com.unionbankph.corporate.common.data.model.Message
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MigrationViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val authGateway: AuthGateway
) : BaseViewModel() {

    private val _migrationState = MutableLiveData<MigrationState>()

    val state: LiveData<MigrationState> get() = _migrationState

    var countDownDisposable: Disposable? = null

    private val isClickedResendOTP = BehaviorSubject.create<Boolean>()

    fun loginEBankingMigration(loginEBankingMigrationForm: LoginEBankingMigrationForm) {
        authGateway.loginEBankingMigration(loginEBankingMigrationForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    _migrationState.value = ShowMigrationLogin(it)
                }, {
                    Timber.e(it, "loginEBankingMigration Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun loginECreditingMigration(loginECreditingMigrationForm: LoginECreditingMigrationForm) {
        authGateway.loginECreditingMigration(loginECreditingMigrationForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    _migrationState.value = ShowMigrationECredLogin(it)
                }, {
                    Timber.e(it, "loginECreditingMigration Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun nominateEmailMigration(
        temporaryCorporateUserId: String,
        migrationNominateEmailForm: MigrationNominateEmailForm
    ) {
        authGateway.nominateEmailMigration(temporaryCorporateUserId, migrationNominateEmailForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    _migrationState.value = ShowMigrationNominateEmail(it)
                }, {
                    Timber.e(it, "nominateEmailMigration Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun nominatePasswordMigration(
        temporaryCorporateUserId: String,
        migrationNominatePasswordForm: MigrationNominatePasswordForm
    ) {
        authGateway.nominatePasswordMigration(
                temporaryCorporateUserId,
                migrationNominatePasswordForm
            )
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    _migrationState.value = ShowMigrationNominatePassword(it)
                }, {
                    Timber.e(it, "nominatePasswordMigration Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun nominateMobileNumberMigration(
        temporaryCorporateUserId: String,
        migrationNominateMobileNumberForm: MigrationNominateMobileNumberForm
    ) {
        authGateway.nominateMobileNumberMigration(
                temporaryCorporateUserId,
                migrationNominateMobileNumberForm
            )
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    _migrationState.value = ShowMigrationNominateMobileNumber(it)
                }, {
                    Timber.e(it, "nominateMobileNumberMigration Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun migrationMergeAccount(migrationMergeAccountForm: MigrationMergeAccountForm) {
        authGateway.migrationMergeAccount(migrationMergeAccountForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    _migrationState.value = ShowMigrationMergeAccount(it)
                }, {
                    Timber.e(it, "migrationMergeAccount Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun submitMigration(migrationForm: MigrationForm) {
        authGateway.submitMigration(migrationForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    _migrationState.value = ShowMigrationVerify(it)
                }, {
                    Timber.e(it, "nominateMobileNumberMigration Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun migrationConfirmEmail(emailConfirmationForm: EmailConfirmationForm) {
        authGateway.migrationConfirmEmail(emailConfirmationForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    _migrationState.value = ShowMigrationConfirmEmail(it)
                }, {
                    Timber.e(it, "migrationConfirmEmail Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun nominateECreditingEmailAddress(accessToken: String, emailAddress: String) {
        authGateway.nominateECreditingEmailAddress(accessToken, emailAddress)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    _migrationState.value = ShowMigrationNominateEmailECred(it)
                }, {
                    Timber.e(it, "nominateECreditingEmailAddress Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun nominateECredForm(accessToken: String) {
        authGateway.nominateECredForm(accessToken)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    _migrationState.value = ShowMigrationECredNominateMobileNumber(it)
                }, {
                    Timber.e(it, "nominateECredForm Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun saveECredPayload(eCredForm: ECredForm) {
        authGateway.saveECredPayload(eCredForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _migrationState.value = ShowMigrationSaveECredPayload
                }, {
                    Timber.e(it, "saveECredPayload Failed")
                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun nominateECredFormOTP(accessToken: String, eCredOTPForm: ECredOTPForm) {
        authGateway.nominateECredFormOTP(accessToken, eCredOTPForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    _migrationState.value = ShowMigrationVerifyECred(it)
                }, {
                    Timber.e(it, "nominateMobileNumberMigration Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun resendECredEmailMigration(accessToken: String) {
        authGateway.resendECredEmailMigration(accessToken)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    _migrationState.value = ShowMigrationResendEmailECred(it)
                }, {
                    Timber.e(it, "resendECredEmailMigration Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun eBankingResendOTPMigration(
        temporaryCorporateUserId: String,
        migrationToken: String?,
        requestId: String?
    ) {
        authGateway.eBankingResendOTPMigration(
                temporaryCorporateUserId,
                EBankingResendOTPForm(requestId, migrationToken)
            )
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
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    isClickedResendOTP.onNext(true)
                    _migrationState.value = ShowMigrationResendOTPSuccess(it)
                }, {
                    Timber.e(it, "resendECredEmailMigration Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun corpLoginResendOTPMigration(accessToken: String, requestId: String?) {
        authGateway.corpLoginResendOTPMigration(accessToken, ResendOTPForm(requestId))
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
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    isClickedResendOTP.onNext(true)
                    _migrationState.value = ShowMigrationResendOTPSuccess(it)
                }, {
                    Timber.e(it, "resendECredEmailMigration Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun updateDetailsResendOTPMigration(accessToken: String, requestId: String?) {
        authGateway.updateDetailsResendOTPMigration(accessToken, ResendOTPForm(requestId))
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
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    isClickedResendOTP.onNext(true)
                    _migrationState.value = ShowMigrationResendOTPSuccess(it)
                }, {
                    Timber.e(it, "resendECredEmailMigration Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun migrationECred(eCredEmailConfirmationForm: ECredEmailConfirmationForm) {
        authGateway.migrationECred(eCredEmailConfirmationForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    _migrationState.value = ShowMigrationECredConfirmEmail(it)
                }, {
                    Timber.e(it, "migrationECred Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun mergeECredAccount(accessToken: String, eCredMergeAccountForm: ECredMergeAccountForm) {
        authGateway.mergeECredAccount(accessToken, eCredMergeAccountForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    _migrationState.value = ShowMigrationECredMergeAccount(it)
                }, {
                    Timber.e(it, "migrationECred Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun mergeECredAccountOTP(
        accessToken: String,
        eCredMergeAccountOTPForm: ECredMergeAccountOTPForm
    ) {
        authGateway.mergeECredAccountOTP(accessToken, eCredMergeAccountOTPForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _migrationState.value = ShowMigrationLoading }
            .doFinally { _migrationState.value = ShowMigrationDismissLoading }
            .subscribe(
                {
                    _migrationState.value = ShowMigrationECredMergeAccountOTP(it)
                }, {
                    Timber.e(it, "migrationECred Failed")

                    _migrationState.value = ShowMigrationError(it)
                })
            .addTo(disposables)
    }

    fun countDownTimer(period: Long, time: Long) {
        countDownDisposable = Observable.interval(period, TimeUnit.SECONDS)
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                val timer = time - it.toInt()
                _migrationState.value = ShowMigrationOTPTimer(timer.toInt())
            }
            .takeUntil {
                it == time
            }
            .doOnComplete {
                _migrationState.value =
                    ShowMigrationOTPCompleteTimer(isClickedResendOTP.value ?: false)
            }.subscribe()
        countDownDisposable?.addTo(disposables)
    }

    fun clearCountDownDisposable() {
        countDownDisposable?.dispose()
    }
}

sealed class MigrationState

object ShowMigrationLoading : MigrationState()

object ShowMigrationDismissLoading : MigrationState()

object ShowMigrationSaveECredPayload : MigrationState()

data class ShowMigrationLogin(val loginMigrationDto: LoginMigrationDto) : MigrationState()

data class ShowMigrationECredLogin(val eCredLoginDto: ECredLoginDto) : MigrationState()

data class ShowMigrationNominateEmail(val loginMigrationDto: LoginMigrationDto) : MigrationState()

data class ShowMigrationNominateEmailECred(val nominateEmailDto: NominateEmailDto) : MigrationState()

data class ShowMigrationNominatePassword(val loginMigrationDto: LoginMigrationDto) : MigrationState()

data class ShowMigrationNominateMobileNumber(val auth: Auth) : MigrationState()

data class ShowMigrationECredNominateMobileNumber(val eCredSubmitDto: ECredSubmitDto) :
    MigrationState()

data class ShowMigrationMergeAccount(val migrationSubmitDto: MigrationSubmitDto) : MigrationState()

data class ShowMigrationECredMergeAccount(val eCredMergeSubmitDto: ECredMergeSubmitDto) :
    MigrationState()

data class ShowMigrationECredMergeAccountOTP(val eCredSubmitOTPDto: ECredSubmitOTPDto) :
    MigrationState()

data class ShowMigrationOTPCompleteTimer(
    val isClickedResendOTP: Boolean
) : MigrationState()

data class ShowMigrationOTPTimer(val timer: Int) : MigrationState()

data class ShowMigrationVerify(val migrationSubmitDto: MigrationSubmitDto) : MigrationState()

data class ShowMigrationVerifyECred(val eCredSubmitOTPDto: ECredSubmitOTPDto) : MigrationState()

data class ShowMigrationResendEmailECred(val message: Message) : MigrationState()

data class ShowMigrationResendOTPSuccess(val auth: Auth) : MigrationState()

data class ShowMigrationConfirmEmail(val confirmationEmailDto: ConfirmationEmailDto) :
    MigrationState()

data class ShowMigrationECredConfirmEmail(val eCredSubmitOTPDto: ECredSubmitOTPDto) :
    MigrationState()

data class ShowMigrationError(val throwable: Throwable) : MigrationState()
