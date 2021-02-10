package com.unionbankph.corporate.bills_payment.presentation.frequent_biller_form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.presentation.source_account.SourceAccountForm
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.auth.data.model.RoleAccountPermissions
import com.unionbankph.corporate.bills_payment.data.form.FrequentBillerForm
import com.unionbankph.corporate.bills_payment.data.gateway.BillsPaymentGateway
import com.unionbankph.corporate.bills_payment.data.model.BillerField
import com.unionbankph.corporate.bills_payment.data.model.CreationFrequentBillerDto
import com.unionbankph.corporate.bills_payment.data.model.FrequentBiller
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.PermissionCollection
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.corporate.domain.gateway.CorporateGateway
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import javax.inject.Inject

class ManageFrequentBillerFormViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val billsPaymentGateway: BillsPaymentGateway,
    private val corporateGateway: CorporateGateway,
    private val accountGateway: AccountGateway,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    private val _beneficiaryFormState = MutableLiveData<ManageFrequentBillerFormState>()

    val state: LiveData<ManageFrequentBillerFormState> get() = _beneficiaryFormState

    private val _accounts = MutableLiveData<Pair<MutableList<Account>, Int>>()

    val accounts: LiveData<Pair<MutableList<Account>, Int>> = _accounts

    val pageable = Pageable()

    var isAllSelectedAccounts = BehaviorSubject.createDefault(true)

    var totalElements = BehaviorSubject.createDefault(0)

    val sourceAccountForm = BehaviorSubject.createDefault(SourceAccountForm())

    val frequentBiller = BehaviorSubject.create<FrequentBiller>()

    fun createFrequentBiller(frequentBillerForm: FrequentBillerForm) {
        billsPaymentGateway.createFrequentBiller(frequentBillerForm)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _beneficiaryFormState.value = ShowManageFrequentBillerFormLoading }
            .doFinally { _beneficiaryFormState.value = ShowManageFrequentBillerFormDismissLoading }
            .subscribe(
                {
                    _beneficiaryFormState.value = ShowManageFrequentBillerFormSubmit(it)
                }, {
                    Timber.e(it, "createFrequentBiller Failed")
                    _beneficiaryFormState.value = ShowManageFrequentBillerFormError(it)
                }
            )
            .addTo(disposables)
    }

    fun updateFrequentBiller(frequentBillerForm: FrequentBillerForm) {
        billsPaymentGateway.updateFrequentBiller(
            frequentBiller.value?.id.toString(),
            frequentBillerForm
        )
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _beneficiaryFormState.value = ShowManageFrequentBillerFormLoading }
            .doFinally { _beneficiaryFormState.value = ShowManageFrequentBillerFormDismissLoading }
            .subscribe(
                {
                    _beneficiaryFormState.value = ShowManageFrequentBillerFormSubmit(it)
                }, {
                    Timber.e(it, "updateBeneficiary Failed")
                    _beneficiaryFormState.value = ShowManageFrequentBillerFormError(it)
                }
            )
            .addTo(disposables)
    }

    fun getBillerFields(billerId: String) {
        billsPaymentGateway.getBillerFields(billerId)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { _beneficiaryFormState.value = ShowManageFrequentBillerFormLoading }
            .doFinally { _beneficiaryFormState.value = ShowManageFrequentBillerFormDismissLoading }
            .subscribe(
                {
                    _beneficiaryFormState.value = ShowManageFrequentBillerGetBillerFields(it)
                }, {
                    Timber.e(it, "getBillerFields failed")
                    _beneficiaryFormState.value = ShowManageFrequentBillerFormError(it)
                })
            .addTo(disposables)
    }

    fun getOrgName() {
        corporateGateway.getRole()
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {
                    _beneficiaryFormState.value = ShowManageFrequentBillerFormGetOrganizationName(
                        it.organizationName ?: ""
                    )
                }, {
                    Timber.e(it, "getOrgDetails failed")
                    _beneficiaryFormState.value = ShowManageFrequentBillerFormError(it)
                }).addTo(disposables)
    }

    fun getSourceAccounts(
        channelId: String,
        permissionId: String
    ) {
        accountGateway.getAccountsPaginated(
            channelId,
            permissionId,
            pageable = pageable
        )
            .doOnSuccess {
                pageable.apply {
                    totalPageCount = it.totalPages
                    isLastPage = !it.hasNextPage
                    increasePage()
                }
                totalElements.onNext(it.totalElements)
                if (it.totalElements < 5) {
                    sourceAccountForm.onNext(
                        SourceAccountForm(
                            allAccountsSelected = false,
                            totalSelected = it.totalElements,
                            selectedAccounts = it.results
                        )
                    )
                } else {
                    sourceAccountForm.onNext(
                        SourceAccountForm(
                            totalSelected = it.totalElements
                        )
                    )
                }
                setPermissionEachAccount(it.results)
            }
            .map {
                Pair(
                    pageable.combineList(accounts.value?.first, it.results),
                    it.totalElements
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _beneficiaryFormState.value = ShowManageFrequentBillerFormLoading
            }
            .doFinally {
                _beneficiaryFormState.value = ShowManageFrequentBillerFormDismissLoading
            }
            .subscribe(
                {
                    _accounts.value = it
                }, {
                    Timber.e(it, "getSourceAccounts Failed")
                    _beneficiaryFormState.value = ShowManageFrequentBillerFormError(it)
                })
            .addTo(disposables)
    }

    private fun setPermissionEachAccount(
        accounts: MutableList<Account>
    ) {
        settingsGateway.getPermissionCollection()
            .subscribe(
                {
                    accounts.forEach { account ->
                        val permissionCollection = mapPermission(account.id, it)
                        account.isLoading = false
                        account.isError = false
                        account.permissionCollection = permissionCollection
                        account.isViewableBalance = permissionCollection.hasAllowToBTRViewBalance
                    }
                }, {
                    Timber.e(it, "getPermissionCollection")
                }
            ).addTo(disposables)
    }

    private fun mapPermission(
        accountId: Int?,
        roleAccountPermissions: MutableList<RoleAccountPermissions>
    ): PermissionCollection {
        val permissionCollection = PermissionCollection()
        roleAccountPermissions
            .filter { it.accountId == accountId }
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
        return permissionCollection
    }

    fun clearSourceAccounts() {
        getSourceAccounts().clear()
        sourceAccountForm.onNext(SourceAccountForm())
    }

    fun getSourceAccounts(): MutableList<Account> {
        return accounts.value?.first.notNullable()
    }

    fun onSetSourceAccounts(sourceAccountsForm: String?) {
        val sourceAccountForm = JsonHelper.fromJson<SourceAccountForm>(sourceAccountsForm)
        this.sourceAccountForm.onNext(sourceAccountForm)
        this.isAllSelectedAccounts.onNext(
            sourceAccountForm.totalSelected == totalElements.value.notNullable()
        )
        val pair = if (sourceAccountForm.allAccountsSelected) {
            Pair(mutableListOf(), sourceAccountForm.totalSelected)
        } else {
            Pair(
                sourceAccountForm.selectedAccounts,
                sourceAccountForm.totalSelected
            )
        }
        _accounts.value = pair
    }

    fun onSetSourceAccountsFromEdit(accounts: MutableList<Account>) {
        val sourceAccountForm = SourceAccountForm()
        sourceAccountForm.allAccountsSelected = false
        sourceAccountForm.totalSelected = accounts.size
        sourceAccountForm.selectedAccounts = accounts
        this.isAllSelectedAccounts.onNext(
            sourceAccountForm.totalSelected == totalElements.value.notNullable()
        )
        this.sourceAccountForm.onNext(sourceAccountForm)
        val pair = if (sourceAccountForm.allAccountsSelected) {
            Pair(mutableListOf(), sourceAccountForm.totalSelected)
        } else {
            Pair(
                sourceAccountForm.selectedAccounts,
                sourceAccountForm.totalSelected
            )
        }
        _accounts.value = pair
    }

}

sealed class ManageFrequentBillerFormState

object ShowManageFrequentBillerFormLoading : ManageFrequentBillerFormState()

object ShowManageFrequentBillerFormDismissLoading : ManageFrequentBillerFormState()

data class ShowManageFrequentBillerFormSubmit(
    val creationFrequentBillerDto: CreationFrequentBillerDto
) : ManageFrequentBillerFormState()

data class ShowManageFrequentBillerGetBillerFields(val billerFields: MutableList<BillerField>) :
    ManageFrequentBillerFormState()

data class ShowManageFrequentBillerFormGetOrganizationName(val orgName: String) :
    ManageFrequentBillerFormState()

data class ShowManageFrequentBillerFormError(val throwable: Throwable) :
    ManageFrequentBillerFormState()
