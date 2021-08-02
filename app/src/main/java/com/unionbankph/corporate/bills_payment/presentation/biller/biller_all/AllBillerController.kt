package com.unionbankph.corporate.bills_payment.presentation.biller.biller_all

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.Typed3EpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.bills_payment.data.model.Biller
import com.unionbankph.corporate.bills_payment.presentation.biller.BillerMainActivity
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.databinding.HeaderAllBillerBinding
import com.unionbankph.corporate.databinding.ItemAllBillerBinding

class AllBillerController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil
) : Typed3EpoxyController<MutableList<Biller>, MutableList<Biller>, String>() {

    private lateinit var callbacks: EpoxyAdapterCallback<Biller>

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(
        listHeader: MutableList<Biller>,
        list: MutableList<Biller>,
        page: String
    ) {
        listHeader
            .sortedWith(compareBy { it.name?.get(0) })
            .forEachIndexed { _, billerHeader ->
                billerHeader {
                    id("biller_header_" + billerHeader.name?.get(0))
                    title(billerHeader.name.notNullable())
                }
                list.filter {
                    it.name?.get(0).toString() in billerHeader.name?.get(0).toString()
                }
                    .sortedWith(compareBy { it.name })
                    .forEachIndexed { position, biller ->
                        billerItem {
                            id("${biller.code}_${biller.name}")
                            title(biller.name.notNullable())
                            biller(biller)
                            page(page)
                            position(position)
                            callbacks(this@AllBillerController.callbacks)
                        }
                    }
            }
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setEpoxyAdapterCallback(epoxyAdapterCallback: EpoxyAdapterCallback<Biller>) {
        this.callbacks = epoxyAdapterCallback
    }
}

@EpoxyModelClass(layout = R.layout.header_all_biller)
abstract class BillerHeaderModel : EpoxyModelWithHolder<BillerHeaderModel.Holder>() {

    @EpoxyAttribute
    lateinit var title: String

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.textViewHeaderTitle.text = title.get(0).toString()
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: HeaderAllBillerBinding

        override fun bindView(itemView: View) {
            binding = HeaderAllBillerBinding.bind(itemView)
        }
    }
}

@EpoxyModelClass(layout = R.layout.item_all_biller)
abstract class BillerItemModel : EpoxyModelWithHolder<BillerItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var title: String

    @EpoxyAttribute
    lateinit var page: String

    @EpoxyAttribute
    lateinit var biller: Biller

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<Biller>

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)

        holder.binding.apply {
            textViewItemTitle.text = title
            constraintLayoutItemTitle.alpha =
                if (!biller.canAddAsFrequentBiller &&
                    page == BillerMainActivity.PAGE_FREQUENT_BILLER_FORM)
                    0.5F
                else
                    1.0F
            constraintLayoutItemTitle.setOnClickListener {
                callbacks.onClickItem(constraintLayoutItemTitle, biller, position)
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemAllBillerBinding

        override fun bindView(itemView: View) {
            binding = ItemAllBillerBinding.bind(itemView)
        }
    }
}
