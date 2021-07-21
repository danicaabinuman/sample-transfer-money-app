package com.unionbankph.corporate.mcd.presentation.log

import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.HeaderActivityLogBinding

@EpoxyModelClass(layout = R.layout.header_activity_log)
abstract class CheckDepositActivityLogHeaderModel : EpoxyModelWithHolder<CheckDepositActivityLogHeaderModel.Holder>() {

    @EpoxyAttribute
    lateinit var header: String

    @EpoxyAttribute
    var position: Int = 0

    @EpoxyAttribute
    var size: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.textViewTitle.text = header
        if (position == 0) holder.binding.viewBorder.visibility = View.INVISIBLE
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: HeaderActivityLogBinding

        override fun bindView(itemView: View) {
            binding = HeaderActivityLogBinding.bind(itemView)
        }
    }
}
