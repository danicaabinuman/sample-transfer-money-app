package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.view.View
import androidx.cardview.widget.CardView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.dashboard.fragment.DashboardAdapterCallback
import kotlinx.android.synthetic.main.item_no_account.view.*

@EpoxyModelClass
abstract class NoAccountItemModel : EpoxyModelWithHolder<NoAccountItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var callbacks: DashboardAdapterCallback

    override fun getDefaultLayout(): Int {
        return R.layout.item_no_account
    }

    override fun bind(holder: Holder) {
        holder.cardViewRoot.setOnClickListener {
            callbacks.onContinueAccountSetup()
        }
    }

    class Holder: EpoxyHolder() {

        lateinit var cardViewRoot: CardView

        override fun bindView(itemView: View) {
            cardViewRoot = itemView.cardViewSetupAccount
        }
    }
}