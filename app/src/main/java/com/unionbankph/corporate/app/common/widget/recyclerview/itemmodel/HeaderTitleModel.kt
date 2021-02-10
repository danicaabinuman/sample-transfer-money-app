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
import kotlinx.android.synthetic.main.header_title.view.*

@EpoxyModelClass(layout = R.layout.header_title)
abstract class HeaderTitleModel : EpoxyModelWithHolder<HeaderTitleModel.Holder>() {

    @EpoxyAttribute
    lateinit var title: String

    @EpoxyAttribute
    var haveButton: Boolean = false

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.textViewTitle.text = title
        holder.cardViewButton.visibility = if (haveButton) View.VISIBLE else View.GONE
        holder.viewBorderHeader.visibility = View.GONE
    }

    class Holder : EpoxyHolder() {
        lateinit var viewBorderHeader: View
        lateinit var imageViewPresence: ImageView
        lateinit var textViewTitle: TextView
        lateinit var cardViewButton: CardView

        override fun bindView(itemView: View) {
            viewBorderHeader = itemView.viewBorderHeader
            imageViewPresence = itemView.imageViewPresence
            textViewTitle = itemView.textViewTitle
            cardViewButton = itemView.cardViewButton
        }
    }
}
