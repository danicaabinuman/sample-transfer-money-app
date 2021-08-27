package com.unionbankph.corporate.settings.presentation.selector

import android.view.View
import com.google.android.material.checkbox.MaterialCheckBox
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.setContextCompatBackgroundColor
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.databinding.ItemSelectorBinding
import com.unionbankph.corporate.settings.presentation.form.Selector

@EpoxyModelClass(layout = R.layout.item_selector)
abstract class SelectorItemModel : EpoxyModelWithHolder<SelectorItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var item: Selector

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<Selector>

    @EpoxyAttribute
    var hasSelected: Boolean = false

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            viewBorderTop.visibility(position == 0)
            checkBox.text = item.value
            checkBox.isChecked = hasSelected
            viewItemState.setContextCompatBackgroundColor(
                if (checkBox.isChecked) {
                    R.color.colorOrangeSelectedSourceAccount
                } else {
                    R.color.colorTransparent
                }
            )
            checkBox.setOnClickListener {
                callbacks.onClickItem(it, item, position)
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding : ItemSelectorBinding

        override fun bindView(itemView: View) {
            binding = ItemSelectorBinding.bind(itemView)
        }
    }
}
