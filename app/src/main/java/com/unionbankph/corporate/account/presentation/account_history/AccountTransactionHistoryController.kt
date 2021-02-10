package com.unionbankph.corporate.account.presentation.account_history

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.Typed2EpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Record
import com.unionbankph.corporate.account.domain.model.SectionedAccountTransactionHistory
import com.unionbankph.corporate.app.common.extension.convertDateToDesireFormat
import com.unionbankph.corporate.app.common.extension.formatAmount
import com.unionbankph.corporate.app.common.extension.notEmpty
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import kotlinx.android.synthetic.main.header_title_orange.view.*
import kotlinx.android.synthetic.main.item_transaction_history.view.*

class AccountTransactionHistoryController :
    Typed2EpoxyController<MutableList<SectionedAccountTransactionHistory>, Pageable>() {

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorFooterModel: ErrorFooterModel_

    private lateinit var callbacks: EpoxyAdapterCallback<Record>

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(
        sectionedAccountTransactionHistory: MutableList<SectionedAccountTransactionHistory>,
        pageable: Pageable
    ) {

        var isFirstPosition = false

        sectionedAccountTransactionHistory.forEachIndexed { position, transactions ->
            transactionHistoryHeader {
                id(transactions.header)
                date(transactions.header.notNullable())
            }
            if (!isFirstPosition) isFirstPosition = true
            transactions.records?.forEachIndexed { positionRecord, record ->
                transactionHistoryItem {
                    id("${record.tranId}_$positionRecord")
                    hasFirstPosition(isFirstPosition && positionRecord == 0)
                    record(record)
                    position(positionRecord)
                    callbacks(callbacks)
                }
                if (isFirstPosition) isFirstPosition = false
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

    fun setAdapterCallbacks(callbacks: EpoxyAdapterCallback<Record>) {
        this.callbacks = callbacks
    }
}

@EpoxyModelClass(layout = R.layout.header_title_orange)
abstract class TransactionHistoryHeaderModel :
    EpoxyModelWithHolder<TransactionHistoryHeaderModel.Holder>() {

    @EpoxyAttribute
    lateinit var date: String

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.textViewTitle.text = date
    }

    class Holder : EpoxyHolder() {
        lateinit var constraintLayoutHeaderTitle: ConstraintLayout
        lateinit var textViewTitle: TextView

        override fun bindView(itemView: View) {
            textViewTitle = itemView.textViewTitle
            constraintLayoutHeaderTitle = itemView.constraintLayoutHeaderTitle
        }
    }
}

@EpoxyModelClass(layout = R.layout.item_transaction_history)
abstract class TransactionHistoryItemModel :
    EpoxyModelWithHolder<TransactionHistoryItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var record: Record

    @EpoxyAttribute
    var position: Int = 0

    @EpoxyAttribute
    var hasFirstPosition: Boolean = false

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<Record>

    override fun bind(holder: Holder) {
        super.bind(holder)

        if (hasFirstPosition) {
            holder.viewBorder.visibility = View.VISIBLE
        } else {
            holder.viewBorder.visibility = View.GONE
        }

        holder.textViewAmount.text = record.amount.formatAmount(record.currency)
        holder.textViewDate.text =
            record.postedDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_TIME)
        holder.textViewRemarks.text = record.tranDescription.notEmpty()

        holder.imageViewTransferType.setImageResource(
            ConstantHelper.Drawable.getAccountTransactionType(
                record.transactionClass
            )
        )
        holder.constraintLayoutItemRecent.setOnClickListener {
            callbacks.onClickItem(holder.constraintLayoutItemRecent, record, position)
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var viewBorder: View
        lateinit var viewBorder2: View
        lateinit var constraintLayoutItemRecent: ConstraintLayout
        lateinit var textViewAmount: TextView
        lateinit var textViewDate: TextView
        lateinit var textViewRemarks: TextView
        lateinit var imageViewTransferType: ImageView

        override fun bindView(itemView: View) {
            constraintLayoutItemRecent = itemView.constraintLayoutItemRecent
            textViewAmount = itemView.textViewAmount
            textViewDate = itemView.textViewDate
            textViewRemarks = itemView.textViewRemarks
            viewBorder = itemView.viewBorder
            viewBorder2 = itemView.viewBorder2
            imageViewTransferType = itemView.imageViewTransferType
        }
    }
}
