package com.unionbankph.corporate.account_setup.presentation.account_purpose

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.data.GenderEnum
import com.unionbankph.corporate.account_setup.data.PersonalInformation
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.updateFields
import com.unionbankph.corporate.app.common.platform.events.Event
import com.unionbankph.corporate.feature.account_setup.AccountPurposeField
import com.unionbankph.corporate.feature.account_setup.AccountPurposeForm
import com.unionbankph.corporate.feature.account_setup.AccountPurposeState
import com.unionbankph.corporate.feature.loan.AddressInformationField
import com.unionbankph.corporate.feature.loan.AddressInformationForm
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
class AccountPurposeViewModel @Inject constructor(
    private val context: Context
) : BaseViewModel() {

    private val _formState = MutableLiveData<AccountPurposeState>()
    val formState: LiveData<AccountPurposeState> = _formState

    private val _form = MutableLiveData<AccountPurposeForm>()
    val form: LiveData<AccountPurposeForm> = _form

    private val dataStreamFlow = MutableSharedFlow<Pair<String, Int>>()

    private val formObserver = Observer<AccountPurposeForm> { form ->
        if (form != null) {
            _formState.updateFields {
                it.value?.let { state ->
                    state.apply {
                        when (form.formField) {
                            AccountPurposeField.PURPOSE -> {
                                purposeError = if (form.purpose.isNullOrBlank()) {
                                    context.getString(
                                        R.string.error_specific_field, context.getString(
                                            R.string.title_purpose
                                        )
                                    )
                                } else {
                                    null
                                }
                            }
                            AccountPurposeField.SPECIFICATION -> {
                                specificationError = if (form.specification.isNullOrBlank()) {
                                    context.getString(
                                        R.string.error_specific_field, context.getString(
                                            R.string.hint_please_specify
                                        )
                                    )
                                } else {
                                    null
                                }
                            }
                        }

                        form.apply {
                            isDataValid =
                                purpose?.isNotEmpty() == true && specification?.isNotEmpty() == true &&
                                        amount != 0.0
                        }
                    }
                }
            }
        }
    }

    init {

    }
}