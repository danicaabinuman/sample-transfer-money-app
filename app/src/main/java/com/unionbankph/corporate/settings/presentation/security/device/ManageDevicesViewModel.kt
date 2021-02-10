package com.unionbankph.corporate.settings.presentation.security.device

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.auth.data.AuthGateway
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PagedDto
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.settings.data.form.ManageDeviceForm
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.data.model.Device
import com.unionbankph.corporate.settings.data.model.LastAccessed
import com.unionbankph.corporate.settings.data.model.ManageDevicesDto
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class ManageDevicesViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val settingsGateway: SettingsGateway,
    private val authGateway: AuthGateway
) : BaseViewModel() {

    private val _manageDevicesState = MutableLiveData<ManageDevicesState>()

    private val lastAccessedMutableLiveData = MutableLiveData<PagedDto<LastAccessed>>()

    val state: LiveData<ManageDevicesState> get() = _manageDevicesState

    val lastAccessedLiveData: LiveData<PagedDto<LastAccessed>> get() = lastAccessedMutableLiveData

    fun trustDevice(manageDeviceForm: ManageDeviceForm) {
        settingsGateway.totpSubscribe(manageDeviceForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _manageDevicesState.value =
                    ShowManageDevicesProgressLoading
            }
            .subscribe(
                {
                    _manageDevicesState.value =
                        ShowManageDevicesProgressDismissLoading
                    _manageDevicesState.value =
                        ShowManageDeviceTrustDevice
                }, {
                    Timber.e(it, "trustDevice Failed")
                    _manageDevicesState.value =
                        ShowManageDevicesProgressDismissLoading
                    _manageDevicesState.value =
                        ShowManageDevicesError(
                            it
                        )
                })
            .addTo(disposables)
    }

    fun unTrustDevice(manageDeviceForm: ManageDeviceForm) {
        settingsGateway.unTrustDevice(manageDeviceForm)
            .flatMapCompletable { settingsGateway.setTOTPToken("") }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _manageDevicesState.value =
                    ShowManageDevicesProgressLoading
            }
            .subscribe(
                {
                    _manageDevicesState.value =
                        ShowManageDevicesProgressDismissLoading
                    _manageDevicesState.value =
                        ShowManageDeviceUnTrustDevice
                }, {
                    Timber.e(it, "unTrustDevice Failed")
                    _manageDevicesState.value =
                        ShowManageDevicesProgressDismissLoading
                    _manageDevicesState.value =
                        ShowManageDevicesError(
                            it
                        )
                })
            .addTo(disposables)
    }

    fun forgetDevice(device: Device) {
        settingsGateway.forgetDevice(device.id.notNullable())
            .flatMap { settingsGateway.getUdid().toSingle() }
            .map { device.udid.notNullable() == it }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _manageDevicesState.value = ShowManageDevicesProgressLoading
            }
            .subscribe(
                {
                    _manageDevicesState.value = ShowManageDevicesProgressDismissLoading
                    _manageDevicesState.value = ShowManageDeviceForget(it)
                }, {
                    Timber.e(it, "forgetDevice Failed")
                    _manageDevicesState.value = ShowManageDevicesProgressDismissLoading
                    _manageDevicesState.value = ShowManageDevicesError(it)
                }
            )
            .addTo(disposables)
    }

    fun getManageDevicesList() {
        settingsGateway.getManageDevicesList()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _manageDevicesState.value =
                    ShowManageDevicesLoading
            }
            .doFinally {
                _manageDevicesState.value =
                    ShowManageDevicesDismissLoading
            }
            .subscribe(
                {
                    _manageDevicesState.value =
                        ShowManageDeviceGetDevices(
                            it
                        )
                }, {
                    Timber.e(it, "getManageDevicesList Failed")
                    _manageDevicesState.value =
                        ShowManageDevicesError(
                            it
                        )
                })
            .addTo(disposables)
    }

    fun getManageDeviceDetail(id: String) {
        settingsGateway.getManageDeviceDetail(id)
            .map {
                Device(
                    it.userAgent,
                    it.id,
                    it.loginDate,
                    it.location,
                    it.udid,
                    it.deviceType,
                    it.deviceLogins,
                    it.devicePlatform
                )
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _manageDevicesState.value =
                    ShowManageDevicesLoading
            }
            .subscribe(
                {
                    _manageDevicesState.value =
                        ShowManageDeviceGetDeviceDetail(
                            it
                        )
                }, {
                    Timber.e(it, "getManageDeviceDetail Failed")
                    _manageDevicesState.value =
                        ShowManageDevicesDismissLoading
                    _manageDevicesState.value =
                        ShowManageDevicesError(
                            it
                        )
                })
            .addTo(disposables)
    }

    fun getLoginHistory(id: String, pageable: Pageable, isInitialLoading: Boolean) {
        settingsGateway.getLoginHistory(id, pageable)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                if (!isInitialLoading)
                    _manageDevicesState.value =
                        ShowManageDevicesEndlessLoading
            }
            .doFinally {
                if (!isInitialLoading)
                    _manageDevicesState.value =
                        ShowManageDevicesEndlessDismissLoading
                else
                    _manageDevicesState.value =
                        ShowManageDevicesDismissLoading
            }
            .subscribe(
                {
                    lastAccessedMutableLiveData.value = it
                }, {
                    Timber.e(it, "getLoginHistory Failed")
                    if (!isInitialLoading) {
                        pageable.errorPagination(it.message)
                    } else {
                        _manageDevicesState.value = ShowManageDevicesError(it)
                    }
                }).addTo(disposables)
    }

    fun clearTOTPToken() {
        settingsGateway.setTOTPToken("")
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _manageDevicesState.value =
                        ShowManageClearTOTPToken
                }, {
                    Timber.e(it, "logout failed")
                    _manageDevicesState.value =
                        ShowManageDevicesError(
                            it
                        )
                })
            .addTo(disposables)
    }

    fun logout() {
        authGateway.clearLoginCredential()
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _manageDevicesState.value =
                        ShowManageLogoutUser
                }, {
                    Timber.e(it, "logout failed")
                    _manageDevicesState.value =
                        ShowManageDevicesError(
                            it
                        )
                })
            .addTo(disposables)
    }
}

sealed class ManageDevicesState
object ShowManageDevicesProgressLoading : ManageDevicesState()
object ShowManageDevicesProgressDismissLoading : ManageDevicesState()
object ShowManageDevicesLoading : ManageDevicesState()
object ShowManageDevicesDismissLoading : ManageDevicesState()
object ShowManageDevicesEndlessLoading : ManageDevicesState()
object ShowManageDevicesEndlessDismissLoading : ManageDevicesState()
object ShowManageDeviceTrustDevice : ManageDevicesState()
object ShowManageLogoutUser : ManageDevicesState()
object ShowManageClearTOTPToken : ManageDevicesState()
object ShowManageDeviceUnTrustDevice : ManageDevicesState()
data class ShowManageDeviceGetDevices(val manageDevicesDto: ManageDevicesDto) : ManageDevicesState()
data class ShowManageDeviceGetDeviceDetail(
    val device: Device
) : ManageDevicesState()

data class ShowManageDeviceForget(val sameUdid: Boolean) : ManageDevicesState()
data class ShowManageDevicesError(val throwable: Throwable) : ManageDevicesState()
data class ShowManageDevicesEndlessError(val throwable: Throwable) : ManageDevicesState()
