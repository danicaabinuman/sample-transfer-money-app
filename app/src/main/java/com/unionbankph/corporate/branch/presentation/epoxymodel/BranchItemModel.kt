package com.unionbankph.corporate.branch.presentation.epoxymodel

import android.content.Context
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.branch.data.model.Branch
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.databinding.ItemBranchBinding

@EpoxyModelClass(layout = R.layout.item_branch)
abstract class BranchItemModel : EpoxyModelWithHolder<BranchItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var branch: Branch

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<Branch>

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            textViewName.text = branch.name
            textViewBranchCode.text = branch.name
            textViewAddress.text = branch.address
            cardViewBranch.setOnClickListener {
                callbacks.onClickItem(it, branch, position)
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemBranchBinding

        override fun bindView(itemView: View) {
            binding = ItemBranchBinding.bind(itemView)
        }
    }
}
