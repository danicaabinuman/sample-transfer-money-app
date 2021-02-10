package com.unionbankph.corporate.fund_transfer.presentation.ubp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.auth.data.model.Auth
import com.unionbankph.corporate.auth.data.model.RoleAccountPermissions
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PermissionCollection
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.fund_transfer.data.form.FundTransferUBPForm
import com.unionbankph.corporate.fund_transfer.data.gateway.FundTransferGateway
import com.unionbankph.corporate.fund_transfer.data.model.FundTransferVerify
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import com.unionbankph.corporate.settings.domain.constant.FeaturesEnum
import io.reactivex.Single
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class UBPViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val fundTransferGateway: FundTransferGateway,
    private val accountGateway: AccountGateway,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _uBPFormState = MutableLiveData<UBPState>()

    private val _account = MutableLiveData<Account>()

    val state: LiveData<UBPState> get() = _uBPFormState

    val account: LiveData<Account> get() = _account

    val uuid = BehaviorSubject.createDefault(UUID.randomUUID().toString())

    val contextualClassFeatureToggle = BehaviorSubject.create<Boolean>()


    private fun setupPermissionCollection(accountId: Int): Single<PermissionCollection> {
        val permissionCollection = PermissionCollection()
        return settingsGateway.getPermissionCollection(accountId)
            .map { roleAccountPermission ->
                mapPermission(roleAccountPermission, accountId, permissionCollection)
                permissionCollection
            }
    }

    private fun mapPermission(
        roleAccountPermission: MutableList<RoleAccountPermissions>,
        accountId: Int,
        permissionCollection: PermissionCollection
    ) {
        roleAccountPermission.filter { it.accountId == accountId }
            .forEach {
                it.productPermissions
                    ?.forEach { productPermission ->
                        productPermission.permissions.forEach { permission ->
                            when (permission.code) {
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_UNIONBANK
                                -> permissionCollection.hasAllowToCreateTransactionUBP =
                                    true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_PESONET
                                -> permissionCollection.hasAllowToCreateTransactionPesonet =
                                    true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_PDDTS
                                -> permissionCollection.hasAllowToCreateTransactionPDDTS =
                                    true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_SWIFT
                                -> permissionCollection.hasAllowToCreateTransactionSwift =
                                    true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_INSTAPAY
                                -> permissionCollection.hasAllowToCreateTransactionInstapay =
                                    true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_EON
                                -> permissionCollection.hasAllowToCreateTransactionEon =
                                    true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_OWN
                                -> permissionCollection.hasAllowToCreateTransactionOwn =
                                    true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS
                                -> permissionCollection.hasAllowToCreateTransaction = true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_ADHOC
                                -> permissionCollection.hasAllowToCreateTransactionAdhoc =
                                    true
                                Constant.Permissions.CODE_FT_CREATETRANSACTIONS_BENEFICIARYMASTER
                                -> permissionCollection.hasAllowToCreateTransactionBeneficiaryMaster =
                                    true
                                Constant.Permissions.CODE_FT_VIEWTRANSACTIONS_VIEWDETAILS
                                -> permissionCollection.hasAllowToViewTransactionViewDetails =
                                    true
                                Constant.Permissions.CODE_FT_VIEWTRANSACTIONS
                                -> permissionCollection.hasAllowToViewTransaction = true
                                Constant.Permissions.CODE_FT_SCHEDULEDTRANSACTIONS
                                -> permissionCollection.hasAllowToCreateTransactionScheduled =
                                    true
                                Constant.Permissions.CODE_FT_DELETESCHEDULED
                                -> permissionCollection.hasAllowToViewTransactionDeleteScheduled =
                                    true
                                Constant.Permissions.CODE_FT_CHANNELS
                                -> permissionCollection.hasAllowToViewChannel = true
                                Constant.Permissions.CODE_BM_CREATEBENEFICIARYMASTER
                                -> permissionCollection.hasAllowToCreateBeneficiaryMaster =
                                    true
                                Constant.Permissions.CODE_BM_VIEWBENEFICIARYMASTER
                                -> permissionCollection.hasAllowToViewBeneficiaryMaster =
                                    true
                                Constant.Permissions.CODE_BTR_VIEWTRANSACTIONS
                                -> permissionCollection.hasAllowToBTRViewTransaction = true
                                Constant.Permissions.CODE_BTR_VIEWBALANCES
                                -> permissionCollection.hasAllowToBTRViewBalance = true
                                Constant.Permissions.CODE_BP_CREATEBILLSPAYMENT_ADHOC
                                -> permissionCollection.hasAllowToCreateBillsPaymentAdhoc =
                                    true
                                Constant.Permissions.CODE_BP_CREATEBILLSPAYMENT
                                -> permissionCollection.hasAllowToCreateBillsPayment = true
                                Constant.Permissions.CODE_BP_SCHEDULEDPAYMENTS
                                -> permissionCollection.hasAllowToScheduledBillsPayment =
                                    true
                                Constant.Permissions.CODE_BP_DELETESCHEDULEDPAYMENTS
                                -> permissionCollection.hasAllowToDeleteScheduledBillsPayment =
                                    true
                                Constant.Permissions.CODE_BP_VIEWBPHISTORY
                                -> permissionCollection.hasAllowToViewBillsPaymentHistory =
                                    true
                                Constant.Permissions.CODE_BP_CREATEBILLSPAYMENT_FREQUENT
                                -> permissionCollection.hasAllowToCreateBillsPaymentFrequent =
                                    true
                                Constant.Permissions.CODE_BP_VIEWBPHISTORY_DETAILS
                                -> permissionCollection.hasAllowToViewBillsPaymentHistoryDetails =
                                    true
                                Constant.Permissions.CODE_BP_CREATEFREQUENTBILLER
                                -> permissionCollection.hasAllowToCreateFrequentBiller =
                                    true
                                Constant.Permissions.CODE_BP_DELETEFREQUENTBILLER
                                -> permissionCollection.hasAllowToDeleteFrequentBiller =
                                    true
                            }
                        }
                    }
            }
    }

    fun getAccountsPermission(
        channelId: String? = null,
        permissionId: String? = null,
        exceptCurrency: String? = null
    ) {
        accountGateway.getAccountsPaginated(
            channelId,
            permissionId,
            exceptCurrency,
            pageable = Pageable()
        )
            .map {
                var account: Account? = null
                if (it.totalElements == 1) {
                    account = it.results[0]
                    mapAccountPermission(account)
                }
                return@map Pair(account, it.totalElements > 0 && account != null)
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uiState.value = Event(UiState.Loading) }
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    if (it.second) {
                        _account.value = it.first
                    }
                }, {
                    Timber.e(it, "getAccountsPermission")
                    _uBPFormState.value = ShowUBPError(it)
                })
            .addTo(disposables)
    }

    private fun mapAccountPermission(
        account: Account
    ) {
        setupPermissionCollection(account.id.notNullable())
            .doOnSuccess {
                account.isViewableBalance =
                    it.hasAllowToBTRViewBalance
                account.permissionCollection = it
            }
            .subscribe()
            .addTo(disposables)
    }

    fun fundTransferUBP(fundTransferUBPForm: FundTransferUBPForm) {
        fundTransferGateway.fundTransferUBP(fundTransferUBPForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _uBPFormState.value = ShowUBPLoading }
            .doFinally { _uBPFormState.value = ShowUBPDismissLoading }
            .subscribe(
                {
                    if (it.requestId != null) {
                        _uBPFormState.value = ShowUBPSuccess(
                            Auth(
                                mobileNumber = it.mobileNumber,
                                requestId = it.requestId,
                                otpType = it.otpType
                            )
                        )
                    } else {
                        _uBPFormState.value = ShowUBPSkipOTPSuccess(it)
                    }
                }, {
                    Timber.e(it, "fundTransferUBP failed")
                    _uBPFormState.value = ShowUBPError(it)
                }).addTo(disposables)
    }

    fun isEnabledFeature() {
        settingsGateway.isEnabledFeature(FeaturesEnum.TRANSACTION_CONTEXTUAL)
            .subscribeOn(schedulerProvider.newThread())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    contextualClassFeatureToggle.onNext(it)
                }, {
                    contextualClassFeatureToggle.onNext(false)
                    Timber.e(it, "isEnabledFeature failed")
                }
            ).addTo(disposables)
    }
}

sealed class UBPState

object ShowUBPLoading : UBPState()

object ShowUBPDismissLoading : UBPState()

data class ShowUBPSuccess(val auth: Auth) : UBPState()

data class ShowUBPSkipOTPSuccess(
    val fundTransferVerify: FundTransferVerify
) : UBPState()

data class ShowUBPError(val throwable: Throwable) : UBPState()
