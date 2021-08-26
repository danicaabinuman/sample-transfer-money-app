package com.unionbankph.corporate.fund_transfer.presentation.swift_bank

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
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.databinding.ItemSwiftBankBinding
import com.unionbankph.corporate.fund_transfer.data.model.SwiftBank

class SwiftBankController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil
) : Typed2EpoxyController<MutableList<SwiftBank>, Pageable>() {

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorFooterModel: ErrorFooterModel_

    private lateinit var callbacks: EpoxyAdapterCallback<SwiftBank>

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(data: MutableList<SwiftBank>, pageable: Pageable) {
        data.forEachIndexed { position, swiftBank ->
            swiftBankItem {
                id("${swiftBank.swiftBicCode}_$position")
                swiftBank(swiftBank)
                callbacks(this@SwiftBankController.callbacks)
                context(this@SwiftBankController.context)
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

    fun setEpoxyAdapterCallback(callbacks: EpoxyAdapterCallback<SwiftBank>) {
        this.callbacks = callbacks
    }

}

@EpoxyModelClass(layout = R.layout.item_swift_bank)
abstract class SwiftBankItemModel : EpoxyModelWithHolder<SwiftBankItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var swiftBank: SwiftBank

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<SwiftBank>

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.binding.apply {
            textViewSwiftCode.text = swiftBank.swiftBicCode
            textViewName.text = swiftBank.bankName
            textViewAddress.text = swiftBank.let {
                if (it.address1 == null && it.address2 == null) {
                    Constant.EMPTY
                } else {
                    it.address1.notNullable() + it.address2.notNullable()
                }
            }
            root.setOnClickListener {
                callbacks.onClickItem(root, swiftBank, position)
            }
        }

    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemSwiftBankBinding

        override fun bindView(itemView: View) {
            binding = ItemSwiftBankBinding.bind(itemView)
        }
    }

}
