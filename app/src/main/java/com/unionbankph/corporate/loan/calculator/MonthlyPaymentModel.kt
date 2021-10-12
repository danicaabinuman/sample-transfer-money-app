package com.unionbankph.corporate.loan.calculator

import android.content.Context
import android.view.View
import com.airbnb.epoxy.*
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.ItemMonthlyPaymentMainBinding
import com.unionbankph.corporate.loan.applyloan.ReadyToBusinessMainModel

@EpoxyModelClass(layout = R.layout.item_monthly_payment_main)
abstract class MonthlyPaymentModel : EpoxyModelWithHolder<MonthlyPaymentModel.Holder>() {

    @EpoxyAttribute
    var howLongClickListener: (String) -> Unit = { _ -> }

    override fun bind(holder: Holder) {
        holder.binding.apply {
            monthlyPaymentMbtgHowLong.setOnClickListener {
                howLongClickListener(it.display.name)
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemMonthlyPaymentMainBinding
        override fun bindView(itemView: View) {
            binding = ItemMonthlyPaymentMainBinding.bind(itemView)
        }
    }

}