package com.unionbankph.corporate.corporate.presentation.organization

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.bus.data.DataBus
import com.unionbankph.corporate.auth.data.model.UserDetails
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.notification.data.gateway.NotificationGateway
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.zipWith
import timber.log.Timber
import javax.inject.Inject

class OrganizationViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val corporateGateway: CorporateGateway,
    private val notificationGateway: NotificationGateway,
    private val dataBus: DataBus
) : BaseViewModel() {

    private val _organizationState = MutableLiveData<OrganizationState>()

    val state: LiveData<OrganizationState> get() = _organizationState

    fun getCorporateUsers() {
        corporateGateway.getCorporateUserOrganization()
            .zipWith(notificationGateway.getOrganizationNotification()) { t1, t2 -> Pair(t1, t2) }
            .map {
                val corporateUsers = it.first.map { corporateUser ->
                    val isFoundSameModel = it.second.find { organizationNotification ->
                        organizationNotification.organizationId == corporateUser.organizationId
                    }
                    if (isFoundSameModel != null) {
                        corporateUser.badgeCount = isFoundSameModel.persist
                        corporateUser.colored = isFoundSameModel.colored
                    }
                    corporateUser
                }
                corporateUsers.toMutableList()
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _organizationState.value = ShowOrganizationLoading }
            .doFinally { _organizationState.value = ShowOrganizationDismissLoading }
            .subscribe(
                {
                    dataBus.corporateUserDataBus.emmit(it)
                }, {
                    Timber.e(it, "getCorporateUsers Failed")
                    _organizationState.value = ShowOrganizationError(it)
                })
            .addTo(disposables)
    }

    fun getUserDetails() {
        corporateGateway.getUserDetails()
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    Timber.d(it.toString())
                    _organizationState.value = ShowOrganizationActiveCorporate(it)
                }, {
                    Timber.e(it, "getUserDetails Failed")
                    _organizationState.value = ShowOrganizationError(it)
                })
            .addTo(disposables)
    }

    fun switchOrganization(roleId: String, data: String? = null) {
        corporateGateway.switchOrganization(roleId)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _organizationState.value = ShowOrganizationSwitchOrgLoading }
            .doFinally { _organizationState.value = ShowOrganizationSwitchOrgDismissLoading }
            .subscribe(
                {
                    _organizationState.value = ShowOrganizationSuccessSwitchOrganization(data)
                }, {
                    Timber.e(it, "switchOrganization Failed")
                    _organizationState.value = ShowOrganizationError(it)
                })
            .addTo(disposables)
    }
}

sealed class OrganizationState

object ShowOrganizationLoading : OrganizationState()

object ShowOrganizationDismissLoading : OrganizationState()

object ShowOrganizationSwitchOrgLoading : OrganizationState()

object ShowOrganizationSwitchOrgDismissLoading : OrganizationState()

data class ShowOrganizationSuccessSwitchOrganization(val data: String?) : OrganizationState()

data class ShowOrganizationError(val throwable: Throwable) : OrganizationState()

data class ShowOrganizationActiveCorporate(val userDetails: UserDetails) : OrganizationState()
