package com.unionbankph.corporate.dao.presentation.type_of_business

import android.content.Context
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoTypeOfBusinessViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val context: Context
) : BaseViewModel() {

    val typeOfIndustry = BehaviorSubject.create<Selector>()

    val isValidFormInput = BehaviorSubject.create<Boolean>()

    val isInitialSubmitForm = BehaviorSubject.create<Boolean>()

    fun hasInitialSubmitted() = isInitialSubmitForm.value ?: true

    fun hasValidForm() = isValidFormInput.value ?: false
}
