package com.unionbankph.corporate.branch.presentation.epoxymodel

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.setContextCompatTextColor
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.branch.presentation.model.BranchVisit
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.databinding.RowBranchVisitBinding

abstract class BranchVisitRowModel : EpoxyModelWithHolder<BranchVisitRowModel.Holder>() {

    override fun getDefaultLayout(): Int {
        return R.layout.row_branch_visit
    }

    @EpoxyAttribute
    lateinit var branchVisit: BranchVisit

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<BranchVisit>

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            textViewRowRemarks.text = branchVisit.remarks
            textViewRowDepositTo.text =
                if (branchVisit.isBatch)
                    branchVisit.numberOfTransactions.toString()
                else
                    branchVisit.depositTo
            textViewRowAmount.text = branchVisit.amount
            textViewRowTransactionDate.text = branchVisit.transactionDate
            textViewRowChannel.text = branchVisit.channel
            textViewRowStatus.setContextCompatTextColor(
                ConstantHelper.Color.getTextColorBranchVisit(branchVisit.status)
            )
            textViewRowStatus.text = branchVisit.status?.value
            holder.binding.root.setOnClickListener {
                callbacks.onClickItem(it, branchVisit, position)
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: RowBranchVisitBinding

        lateinit var imageViewRowIcon: ImageView
        lateinit var textViewRowRemarks: AppCompatTextView
        lateinit var textViewRowDepositTo: AppCompatTextView
        lateinit var textViewRowAmount: AppCompatTextView
        lateinit var textViewRowTransactionDate: AppCompatTextView
        lateinit var textViewRowChannel: AppCompatTextView
        lateinit var textViewRowStatus: AppCompatTextView
        lateinit var itemView: View

        override fun bindView(itemView: View) {
            binding = RowBranchVisitBinding.bind(itemView)
        }
    }
}
