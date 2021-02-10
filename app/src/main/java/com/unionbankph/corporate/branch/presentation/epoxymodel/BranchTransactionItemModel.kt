package com.unionbankph.corporate.branch.presentation.epoxymodel

import android.content.Context
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.google.android.material.checkbox.MaterialCheckBox
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.setContextCompatBackgroundColor
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.branch.presentation.constant.BranchVisitTypeEnum
import com.unionbankph.corporate.branch.presentation.model.BranchTransactionForm
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import kotlinx.android.synthetic.main.item_branch_transaction.view.*

@EpoxyModelClass(layout = R.layout.item_branch_transaction)
abstract class BranchTransactionItemModel :
    EpoxyModelWithHolder<BranchTransactionItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var branchTransactionFormString: String

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<BranchTransactionForm>

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    var hasSelected: Boolean = false

    @EpoxyAttribute
    var hasSelection: Boolean = false

    @EpoxyAttribute
    var position: Int = 0

    private val branchTransactionForm by lazyFast {
        JsonHelper.fromJson<BranchTransactionForm>(branchTransactionFormString)
    }

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.apply {
            viewBorderTop.visibility(position == 0)
            checkBoxBranchTransaction.isChecked = hasSelected
            viewItemState.setContextCompatBackgroundColor(
                if (checkBoxBranchTransaction.isChecked) {
                    R.color.colorOrangeSelectedSourceAccount
                } else {
                    R.color.colorTransparent
                }
            )
            checkBoxBranchTransaction.visibility(hasSelection)
            textViewAccountNumber.visibility(
                branchTransactionForm.type != BranchVisitTypeEnum.CASH_DEPOSIT.value
            )
            textViewTypeOfCheck.text = mapBranchType(branchTransactionForm.type)
            textViewAccountNumber.text = branchTransactionForm.checkNumber
            textViewAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
                branchTransactionForm.amount,
                branchTransactionForm.currency
            )
            textViewRemarks.text = branchTransactionForm.remarks ?: "--"
            checkBoxBranchTransaction.setOnClickListener {
                callbacks.onClickItem(it, branchTransactionForm, position)
            }
            constraintLayoutBranchTransaction.setOnClickListener {
                callbacks.onClickItem(it, branchTransactionForm, position)
            }
            constraintLayoutBranchTransaction.setOnLongClickListener {
                callbacks.onLongClickItem(it, branchTransactionForm, position)
                return@setOnLongClickListener true
            }
        }
    }

    private fun mapBranchType(type: String?): String {
        return context.formatString(
            when (type) {
                BranchVisitTypeEnum.CASH_WITHDRAW.value -> {
                    R.string.title_cash_withdrawal
                }
                BranchVisitTypeEnum.CASH_DEPOSIT.value -> {
                    R.string.title_cash_deposit
                }
                BranchVisitTypeEnum.CHECK_DEPOSIT_ON_US.value -> {
                    R.string.title_check_deposit_on_us
                }
                BranchVisitTypeEnum.CHECK_DEPOSIT_OFF_US.value -> {
                    R.string.title_check_deposit_off_us
                }
                else -> {
                    R.string.title_unknown
                }
            }
        )
    }

    class Holder : EpoxyHolder() {
        lateinit var constraintLayoutBranchTransaction: ConstraintLayout
        lateinit var checkBoxBranchTransaction: MaterialCheckBox
        lateinit var textViewTypeOfCheck: AppCompatTextView
        lateinit var textViewAccountNumber: AppCompatTextView
        lateinit var textViewRemarks: AppCompatTextView
        lateinit var textViewAmount: AppCompatTextView
        lateinit var viewBorderTop: View
        lateinit var viewItemState: View

        override fun bindView(itemView: View) {
            constraintLayoutBranchTransaction = itemView.constraintLayoutBranchTransaction
            checkBoxBranchTransaction = itemView.checkBoxBranchTransaction
            textViewTypeOfCheck = itemView.textViewTypeOfCheck
            textViewAccountNumber = itemView.textViewAccountNumber
            textViewRemarks = itemView.textViewRemarks
            textViewAmount = itemView.textViewAmount
            viewBorderTop = itemView.viewBorderTop
            viewItemState = itemView.viewItemState
        }
    }
}
