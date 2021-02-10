package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import kotlinx.android.synthetic.main.item_state.view.*

@EpoxyModelClass(layout = R.layout.item_state)
abstract class ItemStateModel : EpoxyModelWithHolder<ItemStateModel.Holder>() {

    @EpoxyAttribute
    var message: String? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.textViewEmptyState.text = message
    }

    class Holder : EpoxyHolder() {
        lateinit var textViewEmptyState: TextView

        override fun bindView(itemView: View) {
            textViewEmptyState = itemView.textViewEmptyState
        }
    }
}
