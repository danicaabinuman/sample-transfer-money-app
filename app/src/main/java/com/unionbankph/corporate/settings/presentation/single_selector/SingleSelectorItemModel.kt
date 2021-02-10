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
import com.unionbankph.corporate.settings.presentation.form.Selector
import kotlinx.android.synthetic.main.item_single_selector.view.*

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
        holder.apply {
            viewBorderTop.visibility(position == 0)
            textView.text = item.value
            itemView.setOnClickListener {
                callbacks.onClickItem(it, item, position)
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var textView: AppCompatTextView
        lateinit var viewBorderTop: View
        lateinit var viewItemState: View
        lateinit var itemView: View

        override fun bindView(itemView: View) {
            textView = itemView.textView
            viewBorderTop = itemView.viewBorderTop
            viewItemState = itemView.viewItemState
            this.itemView = itemView
        }
    }
}
