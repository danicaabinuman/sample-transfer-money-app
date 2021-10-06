package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.dashboard.fragment.DashboardAdapterCallback
import com.unionbankph.corporate.databinding.ItemDashboardLoansBinding
import com.unionbankph.corporate.databinding.ItemNoAccountBinding

@EpoxyModelClass
abstract class LoansDashboardItemModel : EpoxyModelWithHolder<LoansDashboardItemModel.Holder>()  {

    @EpoxyAttribute
    lateinit var callbacks: DashboardAdapterCallback

    override fun getDefaultLayout(): Int {
        return R.layout.item_dashboard_loans
    }

    override fun bind(holder: Holder) {
        holder.binding.loansBApplyLoans.setOnClickListener {
            callbacks.onApplyLoans()
        }
    }

    class Holder: EpoxyHolder() {

        lateinit var binding : ItemDashboardLoansBinding

        override fun bindView(itemView: View) {
            binding = ItemDashboardLoansBinding.bind(itemView)
        }
    }
}