package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option

import com.unionbankph.corporate.app.base.BaseViewModel
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import javax.inject.Inject

class YesAcceptCardPaymentsViewModel @Inject constructor() : BaseViewModel() {
    val dateOfIncorporationInput = BehaviorSubject.create<Calendar>()

}