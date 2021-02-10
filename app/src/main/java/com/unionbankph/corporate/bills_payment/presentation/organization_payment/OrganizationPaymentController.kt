package com.unionbankph.corporate.bills_payment.presentation.organization_payment

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.Typed3EpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.setContextCompatTextColor
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import kotlinx.android.synthetic.main.item_done_approval.view.*
import kotlinx.android.synthetic.main.row_organization_payment.view.*

class OrganizationPaymentController
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
                organizationPaymentRow {
                    id(transaction.id)
                    transaction(transaction)
                    position(position)
                    autoFormatUtil(autoFormatUtil)
                    viewUtil(viewUtil)
                    context(context)
                    callbacks(callbacks)
                }
            } else {
                organizationPaymentItem {
                    id(transaction.id)
                    transaction(transaction)
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
abstract class OrganizationPaymentItemModel :
    EpoxyModelWithHolder<OrganizationPaymentItemModel.Holder>() {

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
        holder.apply {
            cardViewBatch.visibility =
                if (transaction.batchType == Constant.TYPE_BATCH) View.VISIBLE
                else View.GONE

            imageViewIcon.setImageResource(
                if (transaction.channel.equals(ChannelBankEnum.BILLS_PAYMENT.value, true)) {
                    R.drawable.ic_bills_payment
                } else {
                    R.drawable.ic_fund_transfer
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
            textViewProposedDate.text = if (transaction.immediate != null &&
                transaction.immediate!!
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

            textViewTotalTransaction.text = autoFormatUtil.formatWithTwoDecimalPlaces(
                transaction.totalAmount,
                transaction.currency
            )
            textViewStatus.setTextColor(
                ContextCompat.getColor(
                    context,
                    ConstantHelper.Color.getTextColor(transaction.transactionStatus)
                )
            )
            textViewStatus.text = transaction.transactionStatus?.description
            itemViewHolder.setOnClickListener {
                callbacks.onClickItem(
                    itemViewHolder,
                    transaction,
                    position
                )
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var constraintLayoutApprovalContent: ConstraintLayout
        lateinit var cardViewBatch: View
        lateinit var cardViewContent: androidx.cardview.widget.CardView
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
        lateinit var textViewStatus: TextView
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
            textViewStatus = itemView.textViewStatus
            itemViewHolder = itemView
        }
    }
}

@EpoxyModelClass(layout = R.layout.row_organization_payment)
abstract class OrganizationPaymentRowModel :
    EpoxyModelWithHolder<OrganizationPaymentRowModel.Holder>() {

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

        holder.apply {
            viewBorderTop.visibility(position == 0)
            imageViewRowIcon.setImageResource(
                if (transaction.channel.equals(ChannelBankEnum.BILLS_PAYMENT.value, true)) {
                    R.drawable.ic_bills_payment_small
                } else {
                    R.drawable.ic_fund_transfer_small
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
            if (transaction.channel.equals(ChannelBankEnum.BILLS_PAYMENT.value, true)) {
                textViewRowPaymentTo.text = transaction.billerName
            } else {
                if (transaction.batchType == Constant.TYPE_BATCH) {
                    textViewRowPaymentTo.text = transaction.numberOfTransactions
                } else {
                    textViewRowPaymentTo.text = if (transaction.beneficiaryName != null) {
                        transaction.beneficiaryName
                    } else {
                        transaction.destinationAccountNumber
                    }
                }
            }
            textViewRowAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
                transaction.totalAmount,
                transaction.currency
            )
            textViewRowPaymentDate.text = if (transaction.immediate != null &&
                transaction.immediate!!
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
            textViewRowStatus.setContextCompatTextColor(
                ConstantHelper.Color.getTextColor(transaction.transactionStatus)
            )
            textViewRowStatus.text = transaction.transactionStatus?.description
            itemView.setOnClickListener {
                callbacks.onClickItem(itemView, transaction, position)
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var linearLayoutRow: LinearLayout
        lateinit var viewBackgroundRow: View
        lateinit var imageViewRowIcon: ImageView
        lateinit var textViewRowRemarks: TextView
        lateinit var textViewRowPaymentTo: TextView
        lateinit var textViewRowAmount: TextView
        lateinit var textViewRowPaymentDate: TextView
        lateinit var textViewRowChannel: TextView
        lateinit var textViewRowStatus: TextView
        lateinit var viewBorderTop: View
        lateinit var itemView: View

        override fun bindView(itemView: View) {
            linearLayoutRow = itemView.linearLayoutRow
            viewBackgroundRow = itemView.viewBackgroundRow
            imageViewRowIcon = itemView.imageViewRowIcon
            textViewRowRemarks = itemView.textViewRowRemarks
            textViewRowPaymentTo = itemView.textViewRowPaymentTo
            textViewRowAmount = itemView.textViewRowAmount
            textViewRowPaymentDate = itemView.textViewRowPaymentDate
            textViewRowChannel = itemView.textViewRowChannel
            textViewRowStatus = itemView.textViewRowStatus
            viewBorderTop = itemView.viewBorderTop
            this.itemView = itemView
        }
    }
}
