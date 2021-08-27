package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.view.View
import androidx.cardview.widget.CardView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.dashboard.fragment.DashboardAdapterCallback
import com.unionbankph.corporate.databinding.ItemNoAccountBinding

@EpoxyModelClass
abstract class NoAccountItemModel : EpoxyModelWithHolder<NoAccountItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var callbacks: DashboardAdapterCallback

    override fun getDefaultLayout(): Int {
        return R.layout.item_no_account
    }

    override fun bind(holder: Holder) {
        holder.binding.root.setOnClickListener {
            callbacks.onContinueAccountSetup()
        }
    }

    class Holder: EpoxyHolder() {

        lateinit var binding : ItemNoAccountBinding

        override fun bindView(itemView: View) {
            binding = ItemNoAccountBinding.bind(itemView)
        }
    }
}