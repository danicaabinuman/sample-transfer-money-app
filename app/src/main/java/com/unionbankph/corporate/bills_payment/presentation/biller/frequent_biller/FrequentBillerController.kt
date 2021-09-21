package com.unionbankph.corporate.bills_payment.presentation.biller.frequent_biller

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
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.bills_payment.data.model.FrequentBiller
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.databinding.ItemFrequentBillerBinding

class FrequentBillerController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil
) : Typed2EpoxyController<MutableList<FrequentBiller>, Pageable>() {

    private lateinit var callbacks: EpoxyAdapterCallback<FrequentBiller>

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorFooterModel: ErrorFooterModel_

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(data: MutableList<FrequentBiller>, pageable: Pageable) {
        data.forEachIndexed { position, frequentBiller ->
            frequentBillerItem {
                id(frequentBiller.id)
                frequentBiller(frequentBiller)
                position(position)
                callbacks(this@FrequentBillerController.callbacks)
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

    fun setEpoxyAdapterCallback(callbacks: EpoxyAdapterCallback<FrequentBiller>) {
        this.callbacks = callbacks
    }
}

@EpoxyModelClass(layout = R.layout.item_frequent_biller)
abstract class FrequentBillerItemModel : EpoxyModelWithHolder<FrequentBillerItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var frequentBiller: FrequentBiller

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<FrequentBiller>

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.binding.apply {
            textViewBillerAlias.text = frequentBiller.name
            textViewBillerName.text = frequentBiller.billerName
            cardViewFrequentBiller.setOnClickListener {
                callbacks.onClickItem(cardViewFrequentBiller, frequentBiller, position)
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding : ItemFrequentBillerBinding

        override fun bindView(itemView: View) {
            binding = ItemFrequentBillerBinding.bind(itemView)
        }
    }
}
