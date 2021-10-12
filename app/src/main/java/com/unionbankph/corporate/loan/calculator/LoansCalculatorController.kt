package com.unionbankph.corporate.loan.calculator

import android.content.Context
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.TypedEpoxyController

class LoansCalculatorController
constructor(
/*    private val viewUtil: ViewUtil,
    private val autoFormatUtil: AutoFormatUtil*/
    private val context: Context
) : TypedEpoxyController<String>() {

    @AutoModel
    lateinit var monthlyPaymentModel: MonthlyPaymentModel_

    @AutoModel
    lateinit var monthlyPaymentBreakdownMainModel: MonthlyPaymentBreakdownMainModel_

    @AutoModel
    lateinit var loanInfoMainModel: LoanInfoMainModel_

    var howLongClickListener: (String) -> Unit = { _ -> }


    override fun buildModels(data: String?) {

        monthlyPaymentModel
            .howLongClickListener { howLongClickListener(it) }
            .addTo(this)

        monthlyPaymentBreakdownMainModel
            .addTo(this)

        loanInfoMainModel
            .context(context)
            .addTo(this)


    }

}

