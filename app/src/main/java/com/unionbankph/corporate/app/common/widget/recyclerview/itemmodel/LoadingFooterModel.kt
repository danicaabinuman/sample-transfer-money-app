package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewParent
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatTextView
import com.airbnb.epoxy.*
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.databinding.FooterProgressBarBinding

abstract class LoadingFooterModel : EpoxyModelWithHolder<LoadingFooterModel.Holder>() {

    override fun getDefaultLayout(): Int {
        return R.layout.footer_progress_bar
    }

    @EpoxyAttribute
    var loading: Boolean = false

    override fun createNewHolder(parent: ViewParent): Holder {
        return Holder()
    }

    override fun bind(holder: Holder) {
        holder.binding.apply {
            progressBar.visibility(loading)
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: FooterProgressBarBinding
            private set

        override fun bindView(itemView: View) {
            binding = FooterProgressBarBinding.bind(itemView)
        }
    }
}
