package com.unionbankph.corporate.dao.presentation.company_info_2

import android.content.Context
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.settings.presentation.form.Selector
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoCompanyInformationStepTwoViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val context: Context
) : BaseViewModel() {

    val isValidFormInput = BehaviorSubject.create<Boolean>()

    val typeOfOfficeInput = BehaviorSubject.create<Selector>()
    val businessOwnershipInput = BehaviorSubject.createDefault(
        Selector(
            value = context.getString(R.string.title_filipino)
        )
    )
    val sourceOfFundsInput = BehaviorSubject.create<Selector>()
    val estimatedAmountInput = BehaviorSubject.create<Selector>()
    val purposeInput = BehaviorSubject.create<Selector>()
    val dateOfIncorporationInput = BehaviorSubject.create<Calendar>()

    fun hasValidForm() = isValidFormInput.value ?: false
}
