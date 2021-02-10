package com.unionbankph.corporate.bills_payment.presentation.frequent_biller_list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.Typed3EpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.bills_payment.data.model.FrequentBiller
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.Constant
import kotlinx.android.synthetic.main.item_manage_frequent_biller.view.*
import kotlinx.android.synthetic.main.row_manage_frequent_biller.view.*

class ManageFrequentBillerController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil,
    private val autoFormatUtil: AutoFormatUtil
) : Typed3EpoxyController<MutableList<FrequentBiller>, Pageable, Boolean>() {

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorFooterModel: ErrorFooterModel_

    private lateinit var callbacks: EpoxyAdapterCallback<FrequentBiller>

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(
        frequentBillers: MutableList<FrequentBiller>,
        pageable: Pageable,
        isTableView: Boolean
    ) {
        frequentBillers.forEachIndexed { position, frequentBiller ->
            if (isTableView) {
                manageFrequentBillerRow {
                    id(frequentBiller.id)
                    frequentBiller(frequentBiller)
                    position(position)
                    viewUtil(viewUtil)
                    autoFormatUtil(autoFormatUtil)
                    context(context)
                    callbacks(callbacks)
                }
            } else {
                manageFrequentBillerItem {
                    id(frequentBiller.id)
                    frequentBiller(frequentBiller)
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

    fun setAdapterCallbacks(callbacks: EpoxyAdapterCallback<FrequentBiller>) {
        this.callbacks = callbacks
    }
}

@EpoxyModelClass(layout = R.layout.item_manage_frequent_biller)
abstract class ManageFrequentBillerItemModel :
    EpoxyModelWithHolder<ManageFrequentBillerItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var frequentBiller: FrequentBiller

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<FrequentBiller>

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        val linearInflater = LayoutInflater.from(context)
        holder.apply {
            linearLayoutFrequentBillerFields.removeAllViews()
            textViewApprovalRemarks.text = frequentBiller.name
            textViewCreatedBy.text = String.format(
                context.getString(R.string.params_created_by),
                frequentBiller.createdBy,
                viewUtil.getDateFormatByDateString(
                    frequentBiller.createdDate,
                    ViewUtil.DATE_FORMAT_ISO_WITHOUT_T,
                    ViewUtil.DATE_FORMAT_DEFAULT
                )
            )
            textViewBiller.text = frequentBiller.billerName
            if (linearLayoutFrequentBillerFields.childCount == 0) {
                frequentBiller.fields
                    .take(context.resources.getInteger(R.integer.max_item_frequent_biller_field))
                    .forEach { field ->
                        val viewFrequentBillerField =
                            linearInflater.inflate(R.layout.item_manage_frequent_biller_field, null)
                        val textViewBillerTitle =
                            viewFrequentBillerField.findViewById<TextView>(R.id.textViewBillerTitle)
                        val textViewBiller =
                            viewFrequentBillerField.findViewById<TextView>(R.id.textViewBiller)
                        textViewBillerTitle.text = field.name
                        textViewBiller.text = field.value
                        linearLayoutFrequentBillerFields.addView(viewFrequentBillerField)
                    }
            }
            cardViewItem.setOnClickListener {
                callbacks.onClickItem(itemView, frequentBiller, position)
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var constraintLayoutItemManageFrequentBiller: ConstraintLayout
        lateinit var textViewApprovalRemarks: TextView
        lateinit var textViewCreatedBy: TextView
        lateinit var textViewBillerTitle: TextView
        lateinit var textViewBiller: TextView
        lateinit var linearLayoutFrequentBillerFields: LinearLayout
        lateinit var cardViewItem: androidx.cardview.widget.CardView
        lateinit var itemView: View

        override fun bindView(itemView: View) {
            constraintLayoutItemManageFrequentBiller =
                itemView.constraintLayoutItemManageFrequentBiller
            textViewApprovalRemarks = itemView.textViewApprovalRemarks
            textViewCreatedBy = itemView.textViewCreatedBy
            textViewBillerTitle = itemView.textViewBillerTitle
            textViewBiller = itemView.textViewBiller
            linearLayoutFrequentBillerFields = itemView.linearLayoutFrequentBillerFields
            cardViewItem = itemView.cardViewItem
            this.itemView = itemView
        }
    }
}

@EpoxyModelClass(layout = R.layout.row_manage_frequent_biller)
abstract class ManageFrequentBillerRowModel :
    EpoxyModelWithHolder<ManageFrequentBillerRowModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var frequentBiller: FrequentBiller

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<FrequentBiller>

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        val linearInflater = LayoutInflater.from(context)
        holder.apply {
            viewBorderTop.visibility(position == 0)
            linearLayoutReferences.removeAllViews()
            textViewRowBillerName.text = frequentBiller.billerName
            if (linearLayoutReferences.childCount == 0) {
                if (frequentBiller.fields.isNotEmpty()) {
                    for (index in 0..4) {
                        val viewFrequentBillerField =
                            linearInflater.inflate(R.layout.row_cell, null)
                        val textViewRowCell =
                            viewFrequentBillerField.findViewById<TextView>(R.id.textViewRowCell)
                        try {
                            val field = frequentBiller.fields[index]
                            textViewRowCell.text = field.value
                        } catch (e: Exception) {
                            textViewRowCell.text = Constant.EMPTY
                        }
                        linearLayoutReferences.addView(viewFrequentBillerField)
                    }
                }
            }
            itemView.setOnClickListener {
                callbacks.onClickItem(itemView, frequentBiller, position)
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var linearLayoutRow: LinearLayout
        lateinit var textViewRowBillerAlias: TextView
        lateinit var textViewRowBillerName: TextView
        lateinit var linearLayoutReferences: LinearLayout
        lateinit var viewBorderTop: View
        lateinit var itemView: View

        override fun bindView(itemView: View) {
            linearLayoutRow = itemView.linearLayoutRow
            textViewRowBillerAlias = itemView.textViewRowBillerAlias
            textViewRowBillerName = itemView.textViewRowBillerName
            linearLayoutReferences = itemView.linearLayoutReferences
            viewBorderTop = itemView.viewBorderTop
            this.itemView = itemView
        }
    }
}
