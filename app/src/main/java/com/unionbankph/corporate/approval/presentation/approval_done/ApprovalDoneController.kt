package com.unionbankph.corporate.approval.presentation.approval_done

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.*
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.approval.data.model.TransactionDetails
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.databinding.ItemDoneApprovalBinding
import com.unionbankph.corporate.databinding.RowApprovalsDoneBinding

class ApprovalDoneController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil,
    private val autoFormatUtil: AutoFormatUtil
) : Typed3EpoxyController<MutableList<Transaction>, Pageable, Boolean>() {

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorFooterModel: ErrorFooterModel_

    private lateinit var callbacks: EpoxyAdapterCallback<Transaction>

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(
        transactions: MutableList<Transaction>,
        pageable: Pageable,
        isTableView: Boolean
    ) {
        transactions.forEachIndexed { position, transaction ->
            if (isTableView) {
                approvalsDoneRow {
                    id(transaction.id)
                    transaction(transaction)
                    position(position)
                    autoFormatUtil(this@ApprovalDoneController.autoFormatUtil)
                    viewUtil(this@ApprovalDoneController.viewUtil)
                    context(this@ApprovalDoneController.context)
                    callbacks(this@ApprovalDoneController.callbacks)
                }
            } else {
                doneApprovalItem {
                    id(transaction.id)
                    transaction(transaction)
                    position(position)
                    autoFormatUtil(this@ApprovalDoneController.autoFormatUtil)
                    viewUtil(this@ApprovalDoneController.viewUtil)
                    context(this@ApprovalDoneController.context)
                    callbacks(this@ApprovalDoneController.callbacks)
                }
            }
        }
        loadingFooterModel.loading(pageable.isLoadingPagination)
            .addIf(pageable.isLoadingPagination && !isTableView, this)
        errorFooterModel.title(pageable.errorMessage)
            .callbacks(callbacks)
            .addIf(pageable.isFailed, this)
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setAdapterCallbacks(callbacks: EpoxyAdapterCallback<Transaction>) {
        this.callbacks = callbacks
    }

}

@EpoxyModelClass(layout = R.layout.item_done_approval)
abstract class DoneApprovalItemModel : EpoxyModelWithHolder<DoneApprovalItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var transaction: Transaction

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<Transaction>

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            cardViewBatch.root.visibility =
                if (transaction.batchType == Constant.TYPE_BATCH) View.VISIBLE
                else View.GONE

            imageViewIcon.setImageResource(
                when {
                    transaction.channel.equals(ChannelBankEnum.BILLS_PAYMENT.value, true) ->
                        R.drawable.ic_bills_payment
                    transaction.channel.equals(ChannelBankEnum.CHECK_WRITER.value, true) ->
                        R.drawable.ic_check_writer
                    transaction.channel.equals(ChannelBankEnum.CASH_WITHDRAWAL.name, true) ->
                        R.drawable.ic_branch_visit_cash_withdrawal
                    transaction.channel.equals(ChannelBankEnum.CASH_DEPOSIT.name, true) ->
                        R.drawable.ic_branch_visit_cash_deposit
                    else -> R.drawable.ic_fund_transfer
                }
            )
            textViewRemarks.text = if (transaction.batchType == Constant.TYPE_BATCH) {
                if (transaction.remarks == "" || transaction.remarks == null)
                    transaction.fileName
                else
                    transaction.remarks
            } else {
                ConstantHelper.Text.getRemarks(context, transaction)
            }
            textViewCreatedBy.text = ConstantHelper.Text.getRemarksCreated(
                context,
                transaction.createdBy,
                transaction.createdDate,
                viewUtil
            )
            when {
                transaction.channel.equals(ChannelBankEnum.CHECK_WRITER.value, true) -> {
                    val field1 = getTransactionField(1, transaction.transactionDetails)
                    val field2 = getTransactionField(2, transaction.transactionDetails)
                    val field3 = getTransactionField(3, transaction.transactionDetails)
                    val field4 = getTransactionField(4, transaction.transactionDetails)
                    textViewTransferToTitle.text = field1?.header
                    textViewTransferTo.text = field1?.value
                    textViewTotalTransactionTitle.text = field2?.header
                    textViewTotalTransaction.text = transaction.totalAmount
                    textViewProposedDateTitle.text = field3?.header
                    textViewProposedDate.text =
                        field3?.value.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)
                    textViewChannelTitle.text = field4?.header
                    textViewChannel.text = field4?.value
                }
                transaction.channel.equals(ChannelBankEnum.CASH_WITHDRAWAL.name, true) -> {
                    textViewTransferToTitle.visibility = View.VISIBLE
                    textViewTransferTo.visibility = View.VISIBLE
                    textViewChannelTitle.visibility = View.VISIBLE
                    textViewChannel.visibility = View.VISIBLE
                    textViewProposedDateTitle.visibility = View.VISIBLE
                    textViewProposedDate.visibility = View.VISIBLE
                    textViewTransferToTitle.text =
                        context.formatString(R.string.title_source_account)
                    textViewTotalTransactionTitle.text = context.formatString(R.string.title_amount)
                    textViewTransferTo.text = transaction.sourceAccountNumber.formatAccountNumber()
                    textViewProposedDate.text =
                        transaction.branchTransactionDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)
                    textViewChannel.text = ChannelBankEnum.CASH_WITHDRAWAL.value
                    textViewTotalTransaction.text =
                        transaction.totalAmount.formatAmount(transaction.currency)
                }
                else -> {
                    if (transaction.channel.equals(ChannelBankEnum.BILLS_PAYMENT.value, true)) {
                        textViewTransferToTitle.visibility = View.VISIBLE
                        textViewTransferTo.visibility = View.VISIBLE
                        textViewChannelTitle.visibility = View.GONE
                        textViewChannel.visibility = View.GONE
                        textViewTransferToTitle.text =
                            context.getString(R.string.title_biller)
                        textViewTotalTransactionTitle.text =
                            context.getString(R.string.title_payment)
                        textViewProposedDateTitle.visibility = View.VISIBLE
                        textViewProposedDate.visibility = View.VISIBLE
                        textViewTransferTo.text = transaction.billerName
                    } else {
                        textViewTransferToTitle.visibility = View.VISIBLE
                        textViewTransferTo.visibility = View.VISIBLE
                        textViewChannelTitle.visibility = View.VISIBLE
                        textViewChannel.visibility = View.VISIBLE
                        textViewProposedDateTitle.visibility = View.VISIBLE
                        textViewProposedDate.visibility = View.VISIBLE
                        if (transaction.batchType == Constant.TYPE_BATCH) {
                            textViewTransferToTitle.text =
                                context.getString(R.string.title_number_of_transfers)
                            textViewTotalTransactionTitle.text =
                                context.getString(R.string.title_total_amount)
                            textViewTransferTo.text = transaction.numberOfTransactions
                        } else {
                            textViewTransferToTitle.text =
                                context.getString(R.string.title_transfer_to)
                            textViewTotalTransactionTitle.text =
                                textViewTransferToTitle.context.getString(R.string.title_amount)
                            textViewTransferTo.text = transaction.destinationAccountNumber
                        }
                    }

                    textViewProposedDate.text = if (transaction.immediate != null
                        && transaction.immediate!!
                    ) {
                        context.getString(R.string.title_immediately)
                    } else {
                        viewUtil.findDateFormatByDateString(
                            if (transaction.immediate == true)
                                transaction.proposedTransactionDate ?: transaction.startDate
                            else
                                transaction.startDate ?: transaction.proposedTransactionDate,
                            ViewUtil.DATE_FORMAT_DEFAULT
                        )
                    }
                    textViewChannel.text = transaction.channel

                    textViewTotalTransaction.text =
                        transaction.totalAmount.formatAmount(transaction.currency)
                }
            }
            textViewStatus.setContextCompatTextColor(
                ConstantHelper.Color.getTextColor(transaction.transactionStatus)
            )
            textViewStatus.text = transaction.transactionStatus?.description
            root.setOnClickListener {
                callbacks.onClickItem(
                    root,
                    transaction,
                    position
                )
            }
        }
    }

    private fun getTransactionField(
        displayIndex: Int,
        transactionDetails: MutableList<TransactionDetails>?
    ): TransactionDetails? {
        return transactionDetails?.find { displayIndex == it.displayIndex }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding : ItemDoneApprovalBinding

        override fun bindView(itemView: View) {
            binding = ItemDoneApprovalBinding.bind(itemView)
        }
    }
}

@EpoxyModelClass(layout = R.layout.row_approvals_done)
abstract class ApprovalsDoneRowModel : EpoxyModelWithHolder<ApprovalsDoneRowModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var transaction: Transaction

    @EpoxyAttribute
    var position: Int = 0

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<Transaction>

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.binding.apply {
            viewBorderTop.visibility(position == 0)
            imageViewRowIcon.setImageResource(
                when {
                    transaction.channel.equals(
                        ChannelBankEnum.BILLS_PAYMENT.value,
                        true
                    ) -> R.drawable.ic_bills_payment_small
                    transaction.channel.equals(
                        ChannelBankEnum.CHECK_WRITER.value,
                        true
                    ) -> R.drawable.ic_check_writer
                    else -> R.drawable.ic_fund_transfer_small
                }
            )
            textViewRowRemarks.text = if (transaction.batchType == Constant.TYPE_BATCH) {
                if (transaction.remarks == "" || transaction.remarks == null)
                    transaction.fileName
                else
                    transaction.remarks
            } else {
                ConstantHelper.Text.getRemarks(context, transaction)
            }
            when {
                transaction.channel.equals(ChannelBankEnum.CHECK_WRITER.value, true) -> {
                    val field1 = getTransactionField(1, transaction.transactionDetails)
                    val field2 = getTransactionField(2, transaction.transactionDetails)
                    val field3 = getTransactionField(3, transaction.transactionDetails)
                    val field4 = getTransactionField(4, transaction.transactionDetails)
                    textViewRowTransferTo.text = field1?.value
                    textViewRowAmount.text = transaction.totalAmount
                    textViewRowTransferDate.text =
                        field3?.value.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)
                    textViewRowChannel.text = field4?.value
                }
                transaction.channel.equals(ChannelBankEnum.CASH_WITHDRAWAL.name, true) -> {
                    textViewRowTransferTo.text = transaction.sourceAccountNumber.formatAccountNumber()
                    textViewRowAmount.text = transaction.totalAmount.formatAmount(transaction.currency)
                    textViewRowTransferDate.text =
                        transaction.branchTransactionDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)
                    textViewRowChannel.text = ChannelBankEnum.CASH_WITHDRAWAL.value
                }
                else -> {
                    if (transaction.channel.equals(ChannelBankEnum.BILLS_PAYMENT.value, true)) {
                        textViewRowTransferTo.text = transaction.billerName
                    } else {
                        if (transaction.batchType == Constant.TYPE_BATCH) {
                            textViewRowTransferTo.text = transaction.numberOfTransactions
                        } else {
                            textViewRowTransferTo.text = if (transaction.beneficiaryName != null) {
                                transaction.beneficiaryName
                            } else {
                                transaction.destinationAccountNumber
                            }
                        }
                    }
                    textViewRowAmount.text = transaction.totalAmount.formatAmount(transaction.currency)
                    textViewRowTransferDate.text = if (transaction.immediate != null
                        && transaction.immediate!!
                    ) {
                        context.getString(R.string.title_immediately)
                    } else {
                        viewUtil.findDateFormatByDateString(
                            if (transaction.immediate == true)
                                transaction.proposedTransactionDate ?: transaction.startDate
                            else
                                transaction.startDate ?: transaction.proposedTransactionDate,
                            ViewUtil.DATE_FORMAT_DEFAULT
                        )
                    }
                    textViewRowChannel.text = transaction.channel
                    root.setOnClickListener {
                        callbacks.onClickItem(root, transaction, position)
                    }
                }
            }
            textViewRowStatus.setContextCompatTextColor(
                ConstantHelper.Color.getTextColor(transaction.transactionStatus)
            )
            textViewRowStatus.text = transaction.transactionStatus?.description
        }
    }

    private fun getTransactionField(
        displayIndex: Int,
        transactionDetails: MutableList<TransactionDetails>?
    ): TransactionDetails? {
        return transactionDetails?.find { displayIndex == it.displayIndex }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: RowApprovalsDoneBinding

        override fun bindView(itemView: View) {
            binding = RowApprovalsDoneBinding.bind(itemView)
        }
    }
}