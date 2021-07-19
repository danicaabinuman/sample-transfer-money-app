package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.common.presentation.callback.AccountAdapterCallback
import com.unionbankph.corporate.databinding.FooterErrorBinding

abstract class ErrorAccountFooterModel : EpoxyModelWithHolder<ErrorAccountFooterModel.Holder>() {

    override fun getDefaultLayout(): Int {
        return R.layout.footer_error
    }

    @EpoxyAttribute
    lateinit var title: String

    @EpoxyAttribute
    lateinit var callbacks: AccountAdapterCallback

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.apply {
            binding.tvTitle.text = title
            binding.clError.setOnClickListener {
                callbacks.onTapToRetry()
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding : FooterErrorBinding

        override fun bindView(itemView: View) {
            binding = FooterErrorBinding.bind(itemView)
        }
    }
}
