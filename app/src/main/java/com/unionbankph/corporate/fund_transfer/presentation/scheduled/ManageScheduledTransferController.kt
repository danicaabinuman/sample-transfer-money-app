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
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ItemManageFrequentBillerBinding
import com.unionbankph.corporate.databinding.ItemManageScheduledTransferBinding
import com.unionbankph.corporate.databinding.RowManageScheduledTransferBinding

class ManageScheduledTransferController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil,
    private val autoFormatUtil: AutoFormatUtil
) : Typed4EpoxyController<MutableList<Transaction>, Boolean, Pageable, Boolean>() {

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
        holder.binding.apply {
            cardViewBatch.root.visibility =
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
            root.setOnClickListener {
                callbacks.onClickItem(root, transaction, position)
            }
            root.setOnLongClickListener {
                callbacks.onLongClickItem(root, transaction, position)
                true
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemManageScheduledTransferBinding

        override fun bindView(itemView: View) {
            binding = ItemManageScheduledTransferBinding.bind(itemView)
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
        holder.binding.apply {
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
            root.setOnClickListener {
                callbacks.onClickItem(root, transaction, position)
            }
            root.setOnLongClickListener {
                callbacks.onLongClickItem(root, transaction, position)
                true
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: RowManageScheduledTransferBinding

        override fun bindView(itemView: View) {
            binding = RowManageScheduledTransferBinding.bind(itemView)
        }
    }
}
