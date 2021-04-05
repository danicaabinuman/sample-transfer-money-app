package com.unionbankph.corporate.request_payment_link.presentation.setup_payment_link

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.account.data.gateway.AccountGateway
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.domain.form.GetAccountsBalances
import com.unionbankph.corporate.account.presentation.account_list.ShowAccountDismissLoading
import com.unionbankph.corporate.account.presentation.account_list.ShowAccountError
import com.unionbankph.corporate.account.presentation.account_list.ShowAccountLoading
import com.unionbankph.corporate.account.presentation.account_selection.ShowAccountsSelectionDetailError
import com.unionbankph.corporate.account.presentation.own_account.ShowOwnAccountDismissLoading
import com.unionbankph.corporate.account.presentation.own_account.ShowOwnAccountError
import com.unionbankph.corporate.account.presentation.own_account.ShowOwnAccountLoading
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.getDisposableSingleObserver
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.request_payment_link.data.form.CreateMerchantForm
import com.unionbankph.corporate.request_payment_link.data.model.CreateMerchantResponse
import com.unionbankph.corporate.request_payment_link.domain.interactor.GetAccounts
import com.unionbankph.corporate.request_payment_link.domain.interactor.PostMerchantDetails
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class SetupPaymentLinkViewModel
@Inject constructor(
    private val postMerchantDetails: PostMerchantDetails,
    private val getAccounts: GetAccounts
) : BaseViewModel() {

    private val _createMerchantResponse = MutableLiveData<CreateMerchantResponse>()
    private val _accounts = MutableLiveData<MutableList<Account>>()

    val createMerchantResponse: LiveData<CreateMerchantResponse>
        get() =
            _createMerchantResponse

    val accounts: LiveData<MutableList<Account>> = _accounts


    fun createMerchant(form: CreateMerchantForm){

        postMerchantDetails.execute(
            getDisposableSingleObserver(
                {
                    _createMerchantResponse.value = it
                }, {
                    Timber.e(it, "createMerchant")
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = form
        ).addTo(disposables)
    }

    fun getAccounts() {
        getAccounts.execute(
            getDisposableSingleObserver(
                {
                    _accounts.value = it
                }, {
                    Timber.e(it, "getAccounts")
                    _uiState.value = Event(UiState.Error(it))
                }
            ),
            doOnSubscribeEvent = {
                _uiState.value = Event(UiState.Loading)
            },
            doFinallyEvent = {
                _uiState.value = Event(UiState.Complete)
            },
            params = null
        ).addTo(disposables)
    }


}
