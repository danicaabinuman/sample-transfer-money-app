package com.unionbankph.corporate.loan.citizen

import android.content.Context
import android.view.View
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.TypedEpoxyController
import com.unionbankph.corporate.loan.calculator.LoanCalculatorState

class CitizenController constructor(
    private val context: Context
) : EpoxyController() {

    var whereClickListener: (Boolean) -> Unit = { }

    override fun buildModels() {

        CitizenModel_()
            .id(hashCode())
            .clickListener { whereClickListener(it) }
            .addTo(this)
    }


}