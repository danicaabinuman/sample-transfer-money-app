package com.unionbankph.corporate.dao.presentation.online_banking_products

import android.content.Context
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Created by herald25santos on 3/17/20
 */
class DaoOnlineBankingProductsViewModel @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val context: Context
) : BaseViewModel() {

    val isValidFormInput = BehaviorSubject.create<Boolean>()

    val fundTransferInput = BehaviorSubject.createDefault(false)
    val billsPaymentInput = BehaviorSubject.createDefault(false)
    val businessCheckInput = BehaviorSubject.createDefault(false)
    val governmentPaymentsCheckInput = BehaviorSubject.createDefault(false)
    val branchTransactionCheckInput = BehaviorSubject.createDefault(false)

    fun hasValidForm() = isValidFormInput.value ?: false
}
