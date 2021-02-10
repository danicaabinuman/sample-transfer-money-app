package com.unionbankph.corporate.dao.presentation.welcome

import android.content.Context
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoWelcomeViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val context: Context
) : BaseViewModel() {

    val isValidFormInput = BehaviorSubject.create<Boolean>()

    val salutationInput = BehaviorSubject.createDefault(
        Selector(value = context.formatString(R.string.title_mr))
    )

    var countryCodeInput =
        BehaviorSubject.createDefault(Constant.getDefaultCountryCode())

    fun hasValidForm() = isValidFormInput.value ?: false
}
