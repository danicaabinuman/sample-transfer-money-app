package com.unionbankph.corporate.app.dashboard

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.auth.data.model.Role
import com.unionbankph.corporate.common.data.model.BadgeCount
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum
import com.unionbankph.corporate.corporate.data.model.CorporateUsers
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.notification.data.gateway.NotificationGateway
import com.unionbankph.corporate.payment_link.domain.model.response.ValidateMerchantByOrganizationResponse
import com.unionbankph.corporate.payment_link.domain.usecase.ValidateMerchantUseCase
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.SetupPaymentLinkActivity
import com.unionbankph.corporate.settings.data.form.ManageDeviceForm
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.zipWith
import timber.log.Timber
import javax.inject.Inject

class DashboardViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val corporateGateway: CorporateGateway,
    private val settingsGateway: SettingsGateway,
    private val notificationGateway: NotificationGateway,
    private val validateMerchantUseCase: ValidateMerchantUseCase
) : BaseViewModel() {

    private val _dashBoardState = MutableLiveData<DashboardState>()

    val dashBoardState: LiveData<DashboardState> get() = _dashBoardState

    private val _authState = MutableLiveData<Event<Any>>()

    private var disposableNotification: Disposable? = null

    private var role: Role? = null

    val authState: LiveData<Event<Any>> get() = _authState

    var badgeCount: Int = 0

    private var hasUnSeenBadgeProfile: Boolean = false

    private var coloredBadgeCount: Int = 0

    init {
        getHeaderDetails()
    }

    fun getOrganizationNotification(orgId: String) {
        if (disposableNotification != null) return
        disposableNotification = notificationGateway.getOrganizationNotification()
            .map { organizations ->
                val approvalBadge = organizations.find { it.organizationId == orgId }
                var profileBadge = 0
                var hasUnSeenBadgeProfile = false
                organizations.filter { it.organizationId != orgId }.forEach {
                    profileBadge += it.persist
                    if (orgId != it.organizationId && it.colored) {
                        hasUnSeenBadgeProfile = true
                    }
                }
                Pair(
                    BadgeCount(
                        approvalBadge?.persist ?: 0,
                        approvalBadge?.colored ?: false
                    ),
                    BadgeCount(
                        profileBadge,
                        hasUnSeenBadgeProfile
                    )
                )
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    Timber.d("getOrganizationNotification")
                    disposables.remove(disposableNotification!!)
                    disposableNotification = null
                    _dashBoardState.value = NotificationSuccess(it.first, it.second)
                }, {
                    Timber.e(it, "getOrganizationNotification Failed")
                })
        disposableNotification?.addTo(disposables)
    }

    fun setOrganizationBadge(corporateUsers: MutableList<CorporateUsers>?) {
        coloredBadgeCount = 0
        badgeCount = 0
        corporateGateway.getRole()
            .doOnSuccess { role = it }
            .toObservable()
            .flatMap { Observable.just(corporateUsers) }
            .flatMapIterable { it }
            .map {
                if (it.colored) {
                    coloredBadgeCount += if (it.colored) it.badgeCount else 0
                }
                if (role?.organizationId != it.organizationId && it.colored) {
                    hasUnSeenBadgeProfile = true
                }
                if (role?.organizationId != it.organizationId) {
                    badgeCount += it.badgeCount
                }
                Timber.d("it.badgeCount: " + it.badgeCount)
            }
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _dashBoardState.value = ShowNotificationOrganization(
                        BadgeCount(
                            badgeCount,
                            hasUnSeenBadgeProfile
                        )
                    )
                }, {
                    Timber.e(it, "setOrganizationBadge Failed")
                }).addTo(disposables)
    }

    fun getNotificationLogBadgeCount() {
        notificationGateway.getNotificationLogBadgeCount()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _dashBoardState.value = ShowNotificationLogBadgeCount(
                        BadgeCount(
                            it.notificationLogBadgeCount,
                            true
                        )
                    )
                }, {
                    Timber.e(it, "getNotificationLogBadgeCount failed")
                })
            .addTo(disposables)
    }

    fun logout() {
        settingsGateway.logoutUser()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _dashBoardState.value = ShowProgressLoading }
            .doFinally { _dashBoardState.value = ShowDismissProgressLoading }
            .subscribe(
                {
                    _authState.value = Event("logout")
                }, {
                    Timber.e(it, "logout failed")
                    _dashBoardState.value = Error(it)
                })
            .addTo(disposables)
    }

    fun isNewUserDetected() {
        settingsGateway.isNewUserDetected()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _dashBoardState.value = ShowNewDeviceDetected(it)
                }, {
                    Timber.e(it, "hasNotTrustedDevice failed")
                    _dashBoardState.value = Error(it)
                }
            )
            .addTo(disposables)
    }

    fun setNewUserDetected(isNewUserDetected: Boolean) {
        settingsGateway.setNewUserDetected(isNewUserDetected)
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .doOnError {
                Timber.e(it, "setNewUserDetected failed")
                _dashBoardState.value = Error(it)
            }
            .subscribe()
            .addTo(disposables)
    }

    fun isTrustedDevice() {
        settingsGateway.isTrustedDevice()
            .zipWith(
                settingsGateway.isPromptedDialog(PromptTypeEnum.TRUST_DEVICE)
            ) { isTrustedDevice, isRecentUser ->
                !isTrustedDevice && !isRecentUser
            }
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _dashBoardState.value = ShowTrustedDevice(it)
                }, {
                    Timber.e(it, "hasNotTrustedDevice failed")
                    _dashBoardState.value = Error(it)
                })
            .addTo(disposables)
    }

    fun isReadMCDTerms() {
        settingsGateway.isReadMCDTerms()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _dashBoardState.value = ShowReadMCDTerms(it)
                }, {
                    Timber.e(it, "isReadMCDTerms failed")
                    _dashBoardState.value = Error(it)
                })
            .addTo(disposables)
    }

    fun getHeaderDetails() {
        corporateGateway.getRole()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _dashBoardState.value = Success(it)
                }, {
                    Timber.e(it, "otp details failed")
                    _dashBoardState.value = Error(it)
                })
            .addTo(disposables)
    }

    fun hasTutorialIntroduction() {
        settingsGateway.hasTutorialIntroduction()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _dashBoardState.value = ShowTutorialIntroduction(it)
                }, {
                    Timber.e(it, "hasTutorialIntroduction failed")
                    _dashBoardState.value = Error(it)
                })
            .addTo(disposables)
    }

    fun considerAsRecentUser(promptTypeEnum: PromptTypeEnum) {
        settingsGateway.considerAsRecentUser(promptTypeEnum)
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    Timber.d("considerAsRecentUser")
                }, {
                    Timber.e(it, "considerAsRecentUser failed")
                    _dashBoardState.value = Error(it)
                })
            .addTo(disposables)
    }

    fun setTutorialIntroduction(boolean: Boolean) {
        settingsGateway.setTutorialIntroduction(boolean)
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _dashBoardState.value = SuccessCompletable
                }, {
                    Timber.e(it, "setTutorialIntroduction failed")
                    _dashBoardState.value = Error(it)
                })
            .addTo(disposables)
    }

    fun setTutorialUser(boolean: Boolean) {
        settingsGateway.setTutorialUser(boolean)
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe({
                _dashBoardState.value = SuccessCompletable
            }, {
                Timber.e(it, "setTutorialUser failed")
                _dashBoardState.value = Error(it)
            })
            .addTo(disposables)
    }

    fun skipTutorial() {
        settingsGateway.skipTutorial()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _dashBoardState.value = SuccessCompletable
                }, {
                    Timber.e(it, "setTutorialBottomUser failed")
                    _dashBoardState.value = Error(it)
                })
            .addTo(disposables)
    }

    fun hasFingerPrint() {
        settingsGateway.getFingerprintToken()
            .zipWith(settingsGateway.isPromptedDialog(PromptTypeEnum.BIOMETRIC)) { fingerPrintToken, isRecentUser ->
                fingerPrintToken == "" && !isRecentUser
            }
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _dashBoardState.value = ShowBiometricDialog(it)
                }, {
                    Timber.e(it, "hasFingerPrint Failed")
                    _dashBoardState.value = Error(it)
                })
            .addTo(disposables)
    }

    fun setFingerPrint() {
        settingsGateway.setFingerprint()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _dashBoardState.value = ShowProgressLoading }
            .subscribe(
                {
                    _dashBoardState.value = ShowBiometricSuccess(it.fingerPrintToken.notNullable())
                }, {
                    Timber.d(it, "setFingerPrint Failed")
                    _dashBoardState.value = ShowDismissProgressLoading
                    _dashBoardState.value = ShowBiometricFailed(it)
                })
            .addTo(disposables)
    }

    fun setTokenFingerPrint(token: String) {
        settingsGateway.setFingerprintCredential(token)
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .doFinally { _dashBoardState.value = ShowDismissProgressLoading }
            .subscribe(
                {
                    _dashBoardState.value = ShowBiometricSetToken
                }, {
                    Timber.d(it, "setTokenFingerPrint Failed")
                    _dashBoardState.value = ShowBiometricFailed(it)
                })
            .addTo(disposables)
    }

    fun totpSubscribe(manageDeviceForm: ManageDeviceForm) {
        settingsGateway.totpSubscribe(manageDeviceForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _dashBoardState.value = ShowProgressLoading }
            .doFinally { _dashBoardState.value = ShowDismissProgressLoading }
            .subscribe(
                {
                    _dashBoardState.value = ShowTOTPSubscription
                }, {
                    Timber.e(it, "totpSubscribe failed")
                    _dashBoardState.value = Error(it)
                })
            .addTo(disposables)
    }

    fun validateMerchant(fromWhatTab: String){

        validateMerchantUseCase.execute(
            getDisposableSingleObserver(
                {
                    val merchantExists = it.merchantExists.equals("true",false)
                    if (merchantExists){
                        val merchantStatus = it.merchantStatus
                        if (merchantStatus.equals(PENDING, true)){
                            _dashBoardState.value = ShowMerchantStatusPendingScreen
                        } else if (merchantStatus.equals(ACTIVE, true)) {
                            _dashBoardState.value = ShowPaymentLinkOnBoarding(merchantExists,fromWhatTab)
                        } else if (merchantStatus.equals(INACTIVE, true)) {
                            _dashBoardState.value = ShowFeatureUnavailable
                        }else {
                            _dashBoardState.value = ShowPaymentLinkOnBoarding(merchantExists,fromWhatTab)
                        }
                    } else {
                        _dashBoardState.value = ShowPaymentLinkOnBoarding(merchantExists,fromWhatTab)
                    }
                   
                }, {
                    Timber.e(it, "validateMerchant")
                    _dashBoardState.value = Error(it)
                }
            ),
            doOnSubscribeEvent = {
                _dashBoardState.value = ShowProgressLoading
            },
            doFinallyEvent = {
                _dashBoardState.value = ShowDismissProgressLoading
            }
        ).addTo(disposables)

    }

    fun scanQR(){
        _dashBoardState.value = ShowQrScan
    }

    companion object{
        const val FROM_REQUEST_PAYMENT_BUTTON = "from_accounts_tab"
        const val FROM_TRANSACT_TAB = "from_transact_tab"
        const val FROM_DASHBOARD_TAB = "from_dashboard_tab"
        const val PENDING = "PENDING"
        const val INACTIVE = "INACTIVE"
        const val ACTIVE = "ACTIVE"
    }
}


sealed class DashboardState

object ShowProgressLoading : DashboardState()

object ShowDismissProgressLoading : DashboardState()

object ShowBiometricSetToken : DashboardState()

object ShowTOTPSubscription : DashboardState()

data class NotificationSuccess(
    val approvalBadgeCount: BadgeCount,
    val profileBadgeCount: BadgeCount
) : DashboardState()

data class ShowNotificationOrganization(
    val profileBadgeCount: BadgeCount
) : DashboardState()

data class ShowNotificationLogBadgeCount(
    val badgeCount: BadgeCount
) : DashboardState()

data class ShowPaymentLinkOnBoarding(val merchantExists: Boolean, val fromWhatTab: String) : DashboardState()

data class ShowBiometricSuccess(val token: String) : DashboardState()

data class ShowBiometricFailed(val throwable: Throwable) : DashboardState()

data class ShowBiometricDialog(val hasNotBiometric: Boolean) : DashboardState()

data class Success(val role: Role) : DashboardState()

data class ShowTrustedDevice(val hasNotTrustedDevice: Boolean) : DashboardState()

data class ShowReadMCDTerms(val isReadMCDTerms: Boolean) : DashboardState()

data class ShowNewDeviceDetected(val isNewDeviceDetected: Boolean) : DashboardState()

data class ShowTutorialIntroduction(val hasTutorial: Boolean) : DashboardState()

data class Error(val throwable: Throwable) : DashboardState()

object ShowMerchantStatusPendingScreen : DashboardState()
object ShowFeatureUnavailable : DashboardState()
object SuccessCompletable : DashboardState()
object ShowSuccessBiometric : DashboardState()
object ShowQrScan : DashboardState()