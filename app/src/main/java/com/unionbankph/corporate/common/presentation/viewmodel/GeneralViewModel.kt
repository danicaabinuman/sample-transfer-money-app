package com.unionbankph.corporate.common.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.notEmpty
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class GeneralViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val corporateGateway: CorporateGateway,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _generalState = MutableLiveData<GeneralState>()

    private val badgeCountMutableLiveData = MutableLiveData<Int>()

    val state: LiveData<GeneralState> get() = _generalState

    val badgeCountLiveData: LiveData<Int> get() = badgeCountMutableLiveData

    fun switchOrganization(roleId: String, data: String? = null) {
        corporateGateway.switchOrganization(roleId)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _generalState.value = ShowGeneralLoading }
            .doFinally { _generalState.value = ShowGeneralDismissLoading }
            .subscribe(
                {
                    _generalState.value =
                        ShowGeneralSuccessSwitchOrganization(data)
                }, {
                    Timber.e(it, "switchOrganization Failed")
                    _generalState.value =
                        ShowGeneralError(it)
                })
            .addTo(disposables)
    }

    fun getOrgName() {
        corporateGateway.getRole()
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _generalState.value =
                        ShowGeneralGetOrganizationName(it.organizationName.notEmpty())
                }, {
                    Timber.e(it, "getOrgName failed")
                    _generalState.value = ShowGeneralError(it)
                })
            .addTo(disposables)
    }

    fun updateShortCutBadgeCount() {
        settingsGateway.updateShortCutBadgeCount()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    badgeCountMutableLiveData.value = it
                }, {
                    Timber.e(it, "updateShortCutBadgeCount failed")
                    _generalState.value =
                        ShowGeneralError(it)
                })
            .addTo(disposables)
    }

    fun getCacheValue(key: String) {
        settingsGateway.getCacheValue(key)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _generalState.value = ShowGeneralGetCacheValue(it, key)
                }, {
                    Timber.e(it, "getCacheValue failed")
                    _generalState.value = ShowGeneralError(it)
                })
            .addTo(disposables)
    }

    fun logout() {
        settingsGateway.logoutUser()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _generalState.value = ShowGeneralLoading }
            .doFinally { _generalState.value = ShowGeneralDismissLoading }
            .subscribe(
                {
                    _generalState.value = ShowGeneralLogoutUser
                }, {
                    Timber.e(it, "logout failed")
                    _generalState.value = ShowGeneralError(it)
                }
            )
            .addTo(disposables)
    }

    fun onClickedLogin() {
        settingsGateway.setUdid()
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _generalState.value = ShowGeneralLoading }
            .doFinally { _generalState.value = ShowGeneralDismissLoading }
            .subscribe(
                {
                    _generalState.value = ShowGeneralSuccessSetUdid
                }, {
                    Timber.e(it, "onClickedNext failed")
                    _generalState.value = ShowGeneralError(it)
                }
            )
            .addTo(disposables)
    }
}

sealed class GeneralState

object ShowGeneralLoading : GeneralState()

object ShowGeneralDismissLoading : GeneralState()

object ShowGeneralLogoutUser : GeneralState()

object ShowGeneralSuccessSetUdid : GeneralState()

data class ShowGeneralSuccessSwitchOrganization(val data: String?) : GeneralState()

data class ShowGeneralGetOrganizationName(val orgName: String) : GeneralState()

data class ShowGeneralGetCacheValue(val value: String, val key: String) : GeneralState()

data class ShowGeneralError(val throwable: Throwable) : GeneralState()
