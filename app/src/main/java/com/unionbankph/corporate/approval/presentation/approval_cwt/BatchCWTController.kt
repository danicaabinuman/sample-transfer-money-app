package com.unionbankph.corporate.approval.presentation.approval_cwt

import android.content.Context
import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.Typed2EpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.setVisible
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.databinding.ItemCwtBinding
import com.unionbankph.corporate.fund_transfer.data.model.CWTItem

class BatchCWTController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil
) : Typed2EpoxyController<MutableList<CWTItem>, Pageable>() {

    private lateinit var callbacks: EpoxyAdapterCallback<CWTItem>

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorFooterModel: ErrorFooterModel_

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(data: MutableList<CWTItem>, pageable: Pageable) {
        data.forEachIndexed { position, cwtItem ->
            cWTDetailItem {
                id("${cwtItem.title}_$position")
                cwtItem(cwtItem)
                position(position)
                callbacks(this@BatchCWTController.callbacks)
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

    fun setEpoxyAdapterCallback(callbacks: EpoxyAdapterCallback<CWTItem>) {
        this.callbacks = callbacks
    }

}

@EpoxyModelClass(layout = R.layout.item_cwt)
abstract class CWTDetailItemModel : EpoxyModelWithHolder<CWTDetailItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var cwtItem: CWTItem

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<CWTItem>

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.binding.apply {
            viewBorderTop.visibility(position == 0)
            textViewCWTTitle.text = ("${cwtItem.title} ${position + 1}")
            textViewCWT.setVisible(false)
            this.root.setOnClickListener {
                callbacks.onClickItem(this.root, cwtItem, position)
            }
        }

    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemCwtBinding

        override fun bindView(itemView: View) {
            binding = ItemCwtBinding.bind(itemView)
        }
    }
}