package com.unionbankph.corporate.approval.presentation.approval_ongoing

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import kotlinx.android.synthetic.main.item_ongoing_approval.view.*
import kotlinx.android.synthetic.main.row_approvals_ongoing.view.*

class ApprovalOngoingController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil,
    private val autoFormatUtil: AutoFormatUtil
) : Typed4EpoxyController<MutableList<Transaction>, Boolean, Pageable, Boolean>() {

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorFooterModel: ErrorFooterModel_

    private lateinit var callbacks: AdapterCallbacks

    private lateinit var callbacks2: EpoxyAdapterCallback<Transaction>

    interface AdapterCallbacks {
        fun onClickItem(id: String, result: String, position: Int)
        fun onClickItemApprove(id: String, position: Int)
        fun onClickItemReject(id: String, position: Int)
        fun onLongClickItem(id: String, position: Int)
    }

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(
        transactions: MutableList<Transaction>,
        isSelection: Boolean,
        pageable: Pageable,
        isTableView: Boolean
    ) {
        transactions.forEachIndexed { position, transaction ->
            if (isTableView) {
                approvalsOngoingRow {
                    id(transaction.id)
                    hasSelected(transaction.hasSelected)
                    hasSelection(isSelection)
                    transactionJsonString(getTransactionJsonString(transaction))
                    position(position)
                    autoFormatUtil(autoFormatUtil)
                    viewUtil(viewUtil)
                    context(context)
                    callbacks(callbacks)
                }
            } else {
                ongoingApprovalItem {
                    id(transaction.id)
                    hasSelected(transaction.hasSelected)
                    hasSelection(isSelection)
                    transactionJsonString(getTransactionJsonString(transaction))
                    position(position)
                    autoFormatUtil(autoFormatUtil)
                    viewUtil(viewUtil)
                    context(context)
                    callbacks(callbacks)
                }
            }
        }
        loadingFooterModel.loading(pageable.isLoadingPagination)
            .addIf(pageable.isLoadingPagination && !isTableView, this)
        errorFooterModel.title(pageable.errorMessage)
            .callbacks(callbacks2)
            .addIf(pageable.isFailed, this)
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    private fun getTransactionJsonString(transaction: Transaction): String {
        return JsonHelper.toJson(transaction)
    }

    fun setAdapterCallbacks(callbacks: AdapterCallbacks) {
        this.callbacks = callbacks
    }

    fun setEpoxyAdapterCallback(callbacks: EpoxyAdapterCallback<Transaction>) {
        this.callbacks2 = callbacks
    }

}

@EpoxyModelClass(layout = R.layout.item_ongoing_approval)
abstract class OngoingApprovalItemModel : EpoxyModelWithHolder<OngoingApprovalItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var transactionJsonString: String

    @EpoxyAttribute
    var position: Int = 0

    @EpoxyAttribute
    var hasSelection: Boolean = false

    @EpoxyAttribute
    var hasSelected: Boolean = false

    private val transaction by lazyFast { JsonHelper.fromJson<Transaction>(transactionJsonString) }

    @EpoxyAttribute
    lateinit var callbacks: ApprovalOngoingController.AdapterCallbacks

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.apply {
            cardViewBatch.visibility =
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

            buttonApprove.visibility = if (!hasSelection) View.VISIBLE else View.GONE
            buttonReject.visibility = if (!hasSelection) View.VISIBLE else View.GONE
            constraintLayoutApprovalContent.background = ContextCompat.getDrawable(
                context,
                if (hasSelected)
                    R.drawable.bg_cardview_border_orange
                else
                    R.drawable.bg_shadow_center
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
                            textViewTransferTo.text = if (transaction.beneficiaryName != null) {
                                transaction.beneficiaryName
                            } else {
                                transaction.destinationAccountNumber
                            }
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
            itemViewHolder.setOnClickListener {
                callbacks.onClickItem(
                    transaction.id.toString(),
                    JsonHelper.toJson(transaction),
                    position
                )
            }
            itemViewHolder.setOnLongClickListener {
                callbacks.onLongClickItem(transaction.id.toString(), position)
                true
            }
            buttonApprove.setOnClickListener {
                callbacks.onClickItemApprove(transaction.id.toString(), position)
            }
            buttonReject.setOnClickListener {
                callbacks.onClickItemReject(transaction.id.toString(), position)
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
        lateinit var constraintLayoutApprovalContent: ConstraintLayout
        lateinit var cardViewBatch: View
        lateinit var cardViewContent: CardView
        lateinit var imageViewIcon: ImageView
        lateinit var textViewRemarks: TextView
        lateinit var textViewCreatedBy: TextView
        lateinit var textViewChannelTitle: TextView
        lateinit var textViewChannel: TextView
        lateinit var textViewTotalTransactionTitle: TextView
        lateinit var textViewProposedDateTitle: TextView
        lateinit var textViewProposedDate: TextView
        lateinit var textViewTotalTransaction: TextView
        lateinit var textViewTransferTo: TextView
        lateinit var textViewTransferToTitle: TextView
        lateinit var buttonApprove: Button
        lateinit var buttonReject: Button
        lateinit var itemViewHolder: View

        override fun bindView(itemView: View) {
            constraintLayoutApprovalContent = itemView.constraintLayoutApprovalContent
            cardViewBatch = itemView.cardViewBatch
            cardViewContent = itemView.cardViewContent
            imageViewIcon = itemView.imageViewIcon
            textViewRemarks = itemView.textViewRemarks
            textViewCreatedBy = itemView.textViewCreatedBy
            textViewChannelTitle = itemView.textViewChannelTitle
            textViewChannel = itemView.textViewChannel
            textViewTotalTransactionTitle = itemView.textViewTotalTransactionTitle
            textViewProposedDate = itemView.textViewProposedDate
            textViewProposedDateTitle = itemView.textViewProposedDateTitle
            textViewTotalTransaction = itemView.textViewTotalTransaction
            textViewTransferTo = itemView.textViewTransferTo
            textViewTransferToTitle = itemView.textViewTransferToTitle
            buttonApprove = itemView.buttonApprove
            buttonReject = itemView.buttonReject
            itemViewHolder = itemView
        }
    }
}

@EpoxyModelClass(layout = R.layout.row_approvals_ongoing)
abstract class ApprovalsOngoingRowModel : EpoxyModelWithHolder<ApprovalsOngoingRowModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var transactionJsonString: String

    @EpoxyAttribute
    var position: Int = 0

    @EpoxyAttribute
    var hasSelection: Boolean = false

    @EpoxyAttribute
    var hasSelected: Boolean = false

    @EpoxyAttribute
    lateinit var callbacks: ApprovalOngoingController.AdapterCallbacks

    private val transaction by lazyFast { JsonHelper.fromJson<Transaction>(transactionJsonString) }

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.apply {
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
                }
            }
            linearLayoutRow.setContextCompatBackgroundColor(
                if (hasSelected) {
                    R.color.colorOrangeSelectedSourceAccount
                } else {
                    R.color.colorTransparent
                }
            )
            itemView.setOnClickListener {
                callbacks.onClickItem(
                    transaction.id.toString(),
                    JsonHelper.toJson(transaction),
                    position
                )
            }
            itemView.setOnLongClickListener {
                callbacks.onLongClickItem(transaction.id.toString(), position)
                true
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
        lateinit var linearLayoutRow: LinearLayout
        lateinit var viewBackgroundRow: View
        lateinit var imageViewRowIcon: ImageView
        lateinit var textViewRowRemarks: TextView
        lateinit var textViewRowTransferTo: TextView
        lateinit var textViewRowAmount: TextView
        lateinit var textViewRowTransferDate: TextView
        lateinit var textViewRowChannel: TextView
        lateinit var viewBorderTop: View
        lateinit var itemView: View

        override fun bindView(itemView: View) {
            linearLayoutRow = itemView.linearLayoutRow
            viewBackgroundRow = itemView.viewBackgroundRow
            imageViewRowIcon = itemView.imageViewRowIcon
            textViewRowRemarks = itemView.textViewRowRemarks
            textViewRowTransferTo = itemView.textViewRowTransferTo
            textViewRowAmount = itemView.textViewRowAmount
            textViewRowTransferDate = itemView.textViewRowTransferDate
            textViewRowChannel = itemView.textViewRowChannel
            viewBorderTop = itemView.viewBorderTop
            this.itemView = itemView
        }
    }
}
