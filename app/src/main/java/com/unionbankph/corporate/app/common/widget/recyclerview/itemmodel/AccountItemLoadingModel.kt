package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.view.View
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R

@EpoxyModelClass
abstract class AccountItemLoadingModel : EpoxyModelWithHolder<AccountItemLoadingModel.Holder>() {

    override fun getDefaultLayout(): Int {
        return R.layout.item_account_loading
    }

    override fun bind(holder: Holder) {
        super.bind(holder)
    }

    class Holder : EpoxyHolder() {

        override fun bindView(itemView: View) {

        }
    }
}