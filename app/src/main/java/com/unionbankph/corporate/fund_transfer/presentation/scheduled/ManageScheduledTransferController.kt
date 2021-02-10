package com.unionbankph.corporate.fund_transfer.presentation.scheduled

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
import com.airbnb.epoxy.Typed4EpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.setContextCompatBackgroundColor
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
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import kotlinx.android.synthetic.main.item_manage_scheduled_transfer.view.*
import kotlinx.android.synthetic.main.row_manage_scheduled_transfer.view.*

class ManageScheduledTransferController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil,
    private val autoFormatUtil: AutoFormatUtil
) : Typed4EpoxyController<MutableList<Transaction>, Boolean, Pageable, Boolean>() {

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
        isSelection: Boolean,
        pageable: Pageable,
        isTableView: Boolean
    ) {
        transactions.forEachIndexed { position, transaction ->
            if (isTableView) {
                manageScheduledTransferRow {
                    id(transaction.id)
                    transactionJsonString(
                        JsonHelper.toJson(transaction)
                    )
                    hasSelected(transaction.hasSelected)
                    position(position)
                    viewUtil(viewUtil)
                    autoFormatUtil(autoFormatUtil)
                    context(context)
                    callbacks(callbacks)
                }
            } else {
                manageScheduledTransferItem {
                    id(transaction.id)
                    transactionJsonString(JsonHelper.toJson(transaction))
                    hasSelected(transaction.hasSelected)
                    position(position)
                    viewUtil(viewUtil)
                    autoFormatUtil(autoFormatUtil)
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

@EpoxyModelClass(layout = R.layout.item_manage_scheduled_transfer)
abstract class ManageScheduledTransferItemModel :
    EpoxyModelWithHolder<ManageScheduledTransferItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<Transaction>

    @EpoxyAttribute
    lateinit var transactionJsonString: String

    @EpoxyAttribute
    var position: Int = 0

    @EpoxyAttribute
    var hasSelected: Boolean = false

    private val transaction by lazy {
        JsonHelper.fromJson<Transaction>(transactionJsonString)
    }

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.apply {
            cardViewBatch.visibility =
                if (transaction.batchType == Constant.TYPE_BATCH)
                    View.VISIBLE
                else
                    View.GONE

            constraintLayoutManageScheduledTransfer.background = ContextCompat.getDrawable(
                context,
                if (hasSelected)
                    R.drawable.bg_cardview_border_orange
                else
                    R.drawable.bg_shadow_center
            )

            imageViewIcon.setImageResource(
                if (transaction.channel.equals(ChannelBankEnum.BILLS_PAYMENT.value, true)) {
                    R.drawable.ic_bills_payment
                } else {
                    R.drawable.ic_fund_transfer
                }
            )

            textViewApprovalRemarks.text = ConstantHelper.Text.getRemarks(context, transaction)

            textViewCreatedBy.text = ConstantHelper.Text.getRemarksCreated(
                context,
                transaction.createdBy,
                transaction.createdDate,
                viewUtil
            )
            textViewTransferTo.text = if (transaction.beneficiaryName != null) {
                transaction.beneficiaryName
            } else {
                transaction.destinationAccountNumber
            }
            textViewAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
                transaction.totalAmount, transaction.currency
            )
            textViewFrequency.text = transaction.frequency
            textViewChannel.text = transaction.channel
            itemViewHolder.setOnClickListener {
                callbacks.onClickItem(itemViewHolder, transaction, position)
            }
            itemViewHolder.setOnLongClickListener {
                callbacks.onLongClickItem(itemViewHolder, transaction, position)
                true
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var constraintLayoutManageScheduledTransfer: ConstraintLayout
        lateinit var cardViewBatch: View
        lateinit var cardViewContent: androidx.cardview.widget.CardView
        lateinit var imageViewIcon: ImageView
        lateinit var textViewApprovalRemarks: TextView
        lateinit var textViewCreatedBy: TextView
        lateinit var textViewChannel: TextView
        lateinit var textViewFrequency: TextView
        lateinit var textViewAmount: TextView
        lateinit var textViewTransferTo: TextView
        lateinit var itemViewHolder: View

        override fun bindView(itemView: View) {
            constraintLayoutManageScheduledTransfer =
                itemView.constraintLayoutManageScheduledTransfer
            cardViewBatch = itemView.cardViewBatch
            cardViewContent = itemView.cardViewContent
            imageViewIcon = itemView.imageViewIcon
            textViewApprovalRemarks = itemView.textViewApprovalRemarks
            textViewCreatedBy = itemView.textViewCreatedBy
            textViewChannel = itemView.textViewChannel
            textViewAmount = itemView.textViewAmount
            textViewFrequency = itemView.textViewFrequency
            textViewTransferTo = itemView.textViewTransferTo
            itemViewHolder = itemView
        }
    }
}

@EpoxyModelClass(layout = R.layout.row_manage_scheduled_transfer)
abstract class ManageScheduledTransferRowModel :
    EpoxyModelWithHolder<ManageScheduledTransferRowModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<Transaction>

    @EpoxyAttribute
    lateinit var transactionJsonString: String

    @EpoxyAttribute
    var position: Int = 0

    @EpoxyAttribute
    var hasSelected: Boolean = false

    private val transaction by lazy {
        JsonHelper.fromJson<Transaction>(transactionJsonString)
    }

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.apply {
            viewBorderTop.visibility(position == 0)
            textViewRowRemarks.text = if (transaction.batchType == Constant.TYPE_BATCH) {
                if (transaction.remarks == "" || transaction.remarks == null)
                    transaction.fileName
                else
                    transaction.remarks
            } else {
                ConstantHelper.Text.getRemarks(context, transaction)
            }

            textViewRowTransferTo.text = if (transaction.beneficiaryName != null) {
                transaction.beneficiaryName
            } else {
                transaction.destinationAccountNumber
            }
            textViewRowAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
                transaction.totalAmount, transaction.currency
            )
            textViewRowFrequency.text = transaction.frequency
            textViewRowChannel.text = transaction.channel
            linearLayoutRow.setContextCompatBackgroundColor(
                if (hasSelected) {
                    R.color.colorOrangeSelectedSourceAccount
                } else {
                    R.color.colorTransparent
                }
            )
            itemView.setOnClickListener {
                callbacks.onClickItem(itemView, transaction, position)
            }
            itemView.setOnLongClickListener {
                callbacks.onLongClickItem(itemView, transaction, position)
                true
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var linearLayoutRow: LinearLayout
        lateinit var textViewRowRemarks: TextView
        lateinit var textViewRowTransferTo: TextView
        lateinit var textViewRowAmount: TextView
        lateinit var textViewRowFrequency: TextView
        lateinit var textViewRowChannel: TextView
        lateinit var viewBorderTop: View
        lateinit var itemView: View

        override fun bindView(itemView: View) {
            linearLayoutRow = itemView.linearLayoutRow
            textViewRowRemarks = itemView.textViewRowRemarks
            textViewRowTransferTo = itemView.textViewRowTransferTo
            textViewRowAmount = itemView.textViewRowAmount
            textViewRowFrequency = itemView.textViewRowFrequency
            textViewRowChannel = itemView.textViewRowChannel
            viewBorderTop = itemView.viewBorderTop
            this.itemView = itemView
        }
    }
}
