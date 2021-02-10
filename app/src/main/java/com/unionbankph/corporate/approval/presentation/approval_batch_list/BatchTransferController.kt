package com.unionbankph.corporate.approval.presentation.approval_batch_list

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.Typed2EpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.fund_transfer.data.model.Batch
import kotlinx.android.synthetic.main.item_batch_transaction.view.*
import kotlinx.android.synthetic.main.item_cwt.view.viewBorderBottom
import kotlinx.android.synthetic.main.item_cwt.view.viewBorderTop

class BatchTransferController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil,
    private val autoFormatUtil: AutoFormatUtil
) : Typed2EpoxyController<MutableList<Batch>, Pageable>() {

    private lateinit var callbacks: EpoxyAdapterCallback<Batch>

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorFooterModel: ErrorFooterModel_

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(data: MutableList<Batch>, pageable: Pageable) {
        data.forEachIndexed { position, batch ->
            batchTransferItem {
                id("${batch.referenceId}_$position")
                batch(batch)
                context(context)
                viewUtil(viewUtil)
                autoFormatUtil(autoFormatUtil)
                position(position)
                callbacks(callbacks)
            }
        }
        loadingFooterModel.loading(pageable.isLoadingPagination)
            .addIf(pageable.isLoadingPagination, this)
        errorFooterModel.title(pageable.errorMessage)
            .callbacks(callbacks)
            .addIf(pageable.isFailed, this)
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setEpoxyAdapterCallback(callbacks: EpoxyAdapterCallback<Batch>) {
        this.callbacks = callbacks
    }

}

@EpoxyModelClass(layout = R.layout.item_batch_transaction)
abstract class BatchTransferItemModel : EpoxyModelWithHolder<BatchTransferItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var batch: Batch

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<Batch>

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.apply {
            imageViewStatus.setImageResource(
                ConstantHelper.Drawable.getBatchIconStatus(batch.status)
            )
            textViewAccountName.visibility(batch.beneficiaryName != null)
            textViewAccountName.text = batch.beneficiaryName
            textViewAccountNumber.text =
                viewUtil.getAccountNumberFormat(batch.receiverAccountNumber)
            textViewAmount.text =
                autoFormatUtil.formatWithTwoDecimalPlaces(batch.amount.toString(), batch.currency)
            itemView.setOnClickListener {
                callbacks.onClickItem(itemView, batch, position)
            }
        }

    }

    class Holder : EpoxyHolder() {
        lateinit var imageViewStatus: ImageView
        lateinit var textViewAccountName: TextView
        lateinit var textViewAccountNumber: TextView
        lateinit var textViewAmount: TextView
        lateinit var viewBorderTop: View
        lateinit var viewBorderBottom: View
        lateinit var itemView: View

        override fun bindView(itemView: View) {
            imageViewStatus = itemView.imageViewStatus
            textViewAccountName = itemView.textViewAccountName
            textViewAccountNumber = itemView.textViewAccountNumber
            textViewAmount = itemView.textViewAmount
            viewBorderTop = itemView.viewBorderTop
            viewBorderBottom = itemView.viewBorderBottom
            this.itemView = itemView
        }
    }
}