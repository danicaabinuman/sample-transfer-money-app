package com.unionbankph.corporate.branch.presentation.epoxymodel

import android.content.Context
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.setContextCompatTextColor
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.branch.presentation.model.BranchVisit
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.databinding.ItemBranchVisitBinding

@EpoxyModelClass(layout = R.layout.item_branch_visit)
abstract class BranchVisitItemModel : EpoxyModelWithHolder<BranchVisitItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var branchVisit: BranchVisit

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<BranchVisit>

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            viewCardViewBatch.root.visibility =
                if (branchVisit.numberOfTransactions > 1) View.VISIBLE
                else View.GONE
            textViewRemarks.text = branchVisit.remarks

            textViewCreatedBy.text = branchVisit.createdBy
            textViewDepositToTitle.text =
                if (branchVisit.isBatch)
                    context.formatString(
                        if (branchVisit.channel == context.formatString(R.string.title_cash_withdrawal))
                            R.string.title_number_of_withdrawals
                        else
                            R.string.title_number_of_deposits
                    )
                else {
                    context.formatString(
                        if (branchVisit.channel == context.formatString(R.string.title_cash_withdrawal))
                            R.string.title_withdraw_to
                        else
                            R.string.title_deposit_to
                    )
                }
            textViewDepositTo.text =
                if (branchVisit.isBatch)
                    branchVisit.numberOfTransactions.toString()
                else
                    branchVisit.depositTo
            textViewAmount.text = branchVisit.amount
            textViewDateTransactionDate.text = branchVisit.transactionDate
            textViewChannel.text = branchVisit.channel
            textViewStatus.setContextCompatTextColor(
                ConstantHelper.Color.getTextColorBranchVisit(branchVisit.status)
            )
            textViewStatus.text = branchVisit.status?.value
            cardViewContent.setOnClickListener {
                callbacks.onClickItem(it, branchVisit, position)
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemBranchVisitBinding

        override fun bindView(itemView: View) {
            binding = ItemBranchVisitBinding.bind(itemView)
        }
    }
}
