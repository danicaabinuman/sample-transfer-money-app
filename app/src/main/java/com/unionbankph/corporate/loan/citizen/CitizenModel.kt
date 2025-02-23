package com.unionbankph.corporate.loan.citizen

import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.databinding.FragmentItemCitizenBinding

@EpoxyModelClass
abstract class CitizenModel(
) : EpoxyModelWithHolder<CitizenModel.Holder>() {

   override fun getDefaultLayout(): Int {
       return R.layout.fragment_item_citizen
   }

    @EpoxyAttribute
    var clickListener: (Boolean) -> Unit = { _ -> }

    override fun bind(holder: Holder) {
        holder.binding.apply {
            handler = object : CitizenHandler {
                override fun onWhere(status: Boolean) {
                    clickListener(status)
                }
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: FragmentItemCitizenBinding
        override fun bindView(itemView: View) {
            binding = FragmentItemCitizenBinding.bind(itemView)
        }
    }
}