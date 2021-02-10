package com.unionbankph.corporate.bills_payment.presentation.bills_payment_form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.auth.data.model.RoleAccountPermissions
import com.unionbankph.corporate.bills_payment.data.form.BillsPaymentForm
import com.unionbankph.corporate.bills_payment.data.gateway.BillsPaymentGateway
import com.unionbankph.corporate.bills_payment.data.model.Amount
import com.unionbankph.corporate.bills_payment.data.model.BillerField
import com.unionbankph.corporate.bills_payment.data.model.BillsPaymentValidate
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PermissionCollection
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Single
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class BillsPaymentFormViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val billsPaymentGateway: BillsPaymentGateway,
    private val accountGateway: AccountGateway,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _billsPaymentFormState = MutableLiveData<BillsPaymentFormState>()

    private val _account = MutableLiveData<Account>()

    val formState: LiveData<BillsPaymentFormState> get() = _billsPaymentFormState

    val account: LiveData<Account> get() = _account

    val uuid = BehaviorSubject.createDefault(UUID.randomUUID().toString())

    fun validateBillsPayment(billsPaymentForm: BillsPaymentForm, currency: String) {
        billsPaymentGateway.validateBillsPayment(billsPaymentForm)
            .doOnSuccess {
                billsPaymentForm.references = it.details?.references ?: mutableListOf()
                billsPaymentForm.amount = Amount(
                    currency,
                    it.details?.amount?.toDouble()
                )
            }
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                _billsPaymentFormState.value =
                    ShowBillsPaymentFormLoading
            }
            .doFinally {
                _billsPaymentFormState.value =
                    ShowBillsPaymentFormDismissLoading
            }
            .subscribe(
                {
                    _billsPaymentFormState.value =
                        ShowBillsPaymentFormValidate(
                            it,
                            billsPaymentForm
                        )
                }, {
                    Timber.e(it, "fundTransferUBP failed")
                    _billsPaymentFormState.value =
                        ShowBillsPaymentFormError(
                            it
                        )
                }).addTo(disposables)
    }

    fun getBillerFields(billerId: String) {
        billsPaymentGateway.getBillerFields(billerId)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doFinally { _uiState.value = Event(UiState.Complete) }
            .subscribe(
                {
                    _billsPaymentFormState.value = ShowBillsPaymentFormGetBillerFields(it)
                }, {
                    Timber.e(it, "getOrgDetails failed")
                    _billsPaymentFormState.value = ShowBillsPaymentFormError(it)
                })
            .addTo(disposables)
    }

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
                it.productPermissions?.forEach { productPermission ->
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
        sourceAccountId: String? = null,
        destinationId: String? = null,
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
            .subscribe(
                {
                    if (it.second) {
                        _account.value = it.first
                    }
                    when (channelId) {
                        ChannelBankEnum.BIR.getChannelId().toString() -> {
                            getBillerFields(Constant.IntegerValues.BIR_SERVICE_ID)
                        }
                        ChannelBankEnum.SSS.getChannelId().toString() -> {
                            getBillerFields(Constant.IntegerValues.SSS_SERVICE_ID)
                        }
                        else -> {
                            _uiState.value = Event(UiState.Complete)
                        }
                    }
                }, {
                    Timber.e(it, "getAccountsPermission")
                    _uiState.value = Event(UiState.Complete)
                    _billsPaymentFormState.value = ShowBillsPaymentFormError(it)
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

}

sealed class BillsPaymentFormState

object ShowBillsPaymentFormLoading : BillsPaymentFormState()

object ShowBillsPaymentFormDismissLoading : BillsPaymentFormState()

data class ShowBillsPaymentFormValidate(
    val billsPaymentValidate: BillsPaymentValidate,
    val billsPaymentForm: BillsPaymentForm
) : BillsPaymentFormState()

data class ShowBillsPaymentFormGetBillerFields(val billerFields: MutableList<BillerField>) :
    BillsPaymentFormState()

data class ShowBillsPaymentFormError(val throwable: Throwable) : BillsPaymentFormState()
