package com.unionbankph.corporate.settings.presentation.single_selector

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.databinding.ItemSingleSelectorBinding
import com.unionbankph.corporate.settings.presentation.form.Selector

@EpoxyModelClass(layout = R.layout.item_single_selector)
abstract class SingleSelectorItemModel : EpoxyModelWithHolder<SingleSelectorItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var item: Selector

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<Selector>

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            viewBorderTop.visibility(position == 0)
            tvReferenceNo.text = item.value
            root.setOnClickListener {
                callbacks.onClickItem(it, item, position)
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemSingleSelectorBinding

        override fun bindView(itemView: View) {
            binding = ItemSingleSelectorBinding.bind(itemView)
        }
    }
}
