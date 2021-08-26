package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.HeaderTitleBinding

@EpoxyModelClass(layout = R.layout.header_title)
abstract class HeaderTitleModel : EpoxyModelWithHolder<HeaderTitleModel.Holder>() {

    @EpoxyAttribute
    lateinit var title: String

    @EpoxyAttribute
    var haveButton: Boolean = false

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.textViewTitle.text = title
        holder.binding.cardViewButton.visibility = if (haveButton) View.VISIBLE else View.GONE
        holder.binding.viewBorderHeader.visibility = View.GONE
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: HeaderTitleBinding

        override fun bindView(itemView: View) {
            binding = HeaderTitleBinding.bind(itemView)
        }
    }
}
