package com.unionbankph.corporate.mcd.presentation.form

import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.fund_transfer.data.model.Bank
import com.unionbankph.corporate.mcd.data.form.CheckDepositForm
import com.unionbankph.corporate.mcd.data.model.CheckDepositUpload
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositTypeEnum
import com.unionbankph.corporate.settings.data.constant.PermissionNameEnum
import com.unionbankph.corporate.settings.data.gateway.SettingsGateway
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class CheckDepositFormViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val cacheManager: CacheManager,
    private val viewUtil: ViewUtil,
    private val settingsGateway: SettingsGateway
) : BaseViewModel() {

    val checkDepositForm = BehaviorSubject.create<CheckDepositForm>()
    val bankInput = BehaviorSubject.create<Bank>()
    val depositToAccount = BehaviorSubject.create<Account>()
    val dateOnCheckInput = BehaviorSubject.create<Calendar>()
    val checkDepositFormOutput = BehaviorSubject.create<String>()
    val permissionId = BehaviorSubject.create<String>()
    val remarks = BehaviorSubject.create<String>()

    init {
        getAccountMCDPermission(
            PermissionNameEnum.CHECK_DEPOSIT.value,
            Constant.Permissions.CODE_RCD_MOBILE_CHECK
        )
    }

    fun setCheckDepositUpload(checkDepositUploadString: String?) {
        checkDepositUploadString?.let {
            Single.fromCallable { JsonHelper.fromJson<CheckDepositUpload>(it) }
                .map {
                    CheckDepositForm().apply {
                        id = it.id
                        frontKey = it.frontKey
                        backKey = it.backKey
                        issuer = it.issuer
                        brstn = it.brstn
                        issuerName = it.issuer
                        checkNumber = it.checkNumber
                        sourceAccount = it.sourceAccount
                        sourceAccountName = it.sourceAccountName
                        frontOfCheckFilePath =
                            cacheManager.get(CheckDepositTypeEnum.CROPPED_FRONT_OF_CHECK.name.toLowerCase())
                        backOfCheckFilePath =
                            cacheManager.get(CheckDepositTypeEnum.CROPPED_BACK_OF_CHECK.name.toLowerCase())
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        bankInput.onNext(Bank(bank = it.issuerName, pesonetBrstn = it.brstn))
                        checkDepositForm.onNext(it)
                    }, {
                        Timber.e(it, "setCheckDepositUpload")
                        _uiState.value = Event(UiState.Error(it))
                    }
                ).addTo(disposables)
        }
    }

    fun getAccountMCDPermission(permissionName: String, code: String) {
        settingsGateway.hasPermissionChannel(permissionName, code)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if (it.id != null) {
                        permissionId.onNext(it.id.toString())
                    }
                }, {
                    Timber.e(it, "getAccountMCDPermission failed")
                }
            ).addTo(disposables)
    }

    fun submitForm() {
        checkDepositForm.value?.let { checkDepositForm ->
            checkDepositForm.currency = depositToAccount.value?.currency
            checkDepositForm.targetAccountName = depositToAccount.value?.name
            checkDepositForm.targetAccount = depositToAccount.value?.accountNumber
            checkDepositForm.accountType = depositToAccount.value?.productCodeDesc
            checkDepositForm.brstn = bankInput.value?.pesonetBrstn
            checkDepositForm.issuerName = bankInput.value?.bank
            checkDepositForm.issuer = bankInput.value?.bank
            checkDepositForm.checkDate = viewUtil.getDateFormatByCalendar(
                dateOnCheckInput.value,
                DateFormatEnum.DATE_FORMAT_ISO_Z.value
            )
            Single.fromCallable { JsonHelper.toJson(checkDepositForm) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        checkDepositFormOutput.onNext(it)
                    }, {
                        Timber.e(it, "submitForm")
                        _uiState.value = Event(UiState.Error(it))
                    }
                )
        }
    }
}
