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
import com.unionbankph.corporate.dao.presentation.checking_account.DaoCheckingAccountTypeFragmentDirections
import com.unionbankph.corporate.databinding.HeaderTitleOrangeBinding
import com.unionbankph.corporate.databinding.ItemTransactionHistoryBinding

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
                    callbacks(this@AccountTransactionHistoryController.callbacks)
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
        holder.binding.apply {
            textViewTitle.text = date
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: HeaderTitleOrangeBinding

        override fun bindView(itemView: View) {
            binding = HeaderTitleOrangeBinding.bind(itemView)
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

        holder.binding.apply {
            if (hasFirstPosition) {
                viewBorder.visibility = View.VISIBLE
            } else {
                viewBorder.visibility = View.GONE
            }

            textViewAmount.text = record.amount.formatAmount(record.currency)
            textViewDate.text =
                record.postedDate.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_TIME)
            textViewRemarks.text = record.tranDescription.notEmpty()

            imageViewTransferType.setImageResource(
                ConstantHelper.Drawable.getAccountTransactionType(
                    record.transactionClass
                )
            )
            constraintLayoutItemRecent.setOnClickListener {
                callbacks.onClickItem(constraintLayoutItemRecent, record, position)
            }
        }


    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemTransactionHistoryBinding

        override fun bindView(itemView: View) {
            binding = ItemTransactionHistoryBinding.bind(itemView)
        }
    }
}
