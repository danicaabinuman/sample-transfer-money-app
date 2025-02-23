package com.unionbankph.corporate.fund_transfer.presentation.bank

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.TypedEpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.common.data.model.SectionedData
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.HeaderTitleInitialBinding
import com.unionbankph.corporate.databinding.ItemTitleBinding
import com.unionbankph.corporate.fund_transfer.data.model.Bank

class BankController
constructor(
    private val callbacks: AdapterCallbacks
) : TypedEpoxyController<MutableList<SectionedData<Bank>>>() {

    interface AdapterCallbacks {
        fun onClickItem(model: String?)
    }

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(data: MutableList<SectionedData<Bank>>) {

        data.forEachIndexed { indexHeader, bankSectioned ->
            PesoNetBankHeaderModel_()
                .id("header_${bankSectioned.header?.get(0)}_$indexHeader")
                .title(bankSectioned.header)
                .addTo(this)

            bankSectioned.data.forEachIndexed { index, bank ->
                PesoNetBankItemModel_()
                    .id("${bank.bank}_$index")
                    .bank(JsonHelper.toJson(bank))
                    .title(bank.bank)
                    .callbacks(callbacks)
                    .addTo(this)
            }
        }

    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }
}

@EpoxyModelClass(layout = R.layout.header_title_initial)
abstract class PesoNetBankHeaderModel : EpoxyModelWithHolder<PesoNetBankHeaderModel.Holder>() {

    @EpoxyAttribute
    var title: String? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.textViewHeaderTitle.text = title?.get(0).toString()
    }

    class Holder : EpoxyHolder() {
        lateinit var binding : HeaderTitleInitialBinding

        override fun bindView(itemView: View) {
            binding = HeaderTitleInitialBinding.bind(itemView)
        }
    }
}

@EpoxyModelClass(layout = R.layout.item_title)
abstract class PesoNetBankItemModel : EpoxyModelWithHolder<PesoNetBankItemModel.Holder>() {

    @EpoxyAttribute
    var title: String? = null

    @EpoxyAttribute
    var bank: String? = null

    @EpoxyAttribute
    lateinit var callbacks: BankController.AdapterCallbacks

    override fun bind(holder: Holder) {
        holder.binding.textViewItemTitle.text = title
        holder.binding.constraintLayoutItemTitle.setOnClickListener {
            callbacks.onClickItem(bank)
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemTitleBinding

        override fun bindView(itemView: View) {
            binding = ItemTitleBinding.bind(itemView)
        }
    }
}
