package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.view.View
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R

@EpoxyModelClass
abstract class DashboardBannerItemModel : EpoxyModelWithHolder<DashboardBannerItemModel.Holder>()  {

    override fun getDefaultLayout(): Int {
        return R.layout.item_dashboard_banner
    }

    override fun bind(holder: Holder) {

    }

    class Holder: EpoxyHolder() {

        override fun bindView(itemView: View) {

        }
    }
}