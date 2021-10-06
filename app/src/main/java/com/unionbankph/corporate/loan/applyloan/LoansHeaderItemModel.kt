package com.unionbankph.corporate.loan.applyloan

import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.ItemHeaderApplyLoansBinding

@EpoxyModelClass
abstract class LoansHeaderItemModel : EpoxyModelWithHolder<LoansHeaderItemModel.Holder>()  {

    @EpoxyAttribute
    lateinit var callbacks: LoansAdapterCallback

    override fun getDefaultLayout(): Int {
        return R.layout.item_header_apply_loans
    }

    override fun bind(holder: Holder) {
        holder.binding.apply {
            applyLoansBApplyNow.setOnClickListener { callbacks.onApplyNow() }
            applyLoansBReferenceCode.setOnClickListener { callbacks.onReferenceCode() }
        }
    }

    class Holder: EpoxyHolder() {

        lateinit var binding : ItemHeaderApplyLoansBinding

        override fun bindView(itemView: View) {
            binding = ItemHeaderApplyLoansBinding.bind(itemView)
        }
    }
}