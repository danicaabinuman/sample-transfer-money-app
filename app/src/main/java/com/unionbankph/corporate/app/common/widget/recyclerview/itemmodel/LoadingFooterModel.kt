package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatTextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.visibility
import kotlinx.android.synthetic.main.footer_progress_bar.view.*

@EpoxyModelClass(layout = R.layout.footer_progress_bar)
abstract class LoadingFooterModel : EpoxyModelWithHolder<LoadingFooterModel.Holder>() {

    @EpoxyAttribute
    var loading: Boolean = false

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.progressBar.visibility(loading)
    }

    class Holder : EpoxyHolder() {
        lateinit var linearLayoutProgressBar: LinearLayout
        lateinit var progressBar: ProgressBar
        lateinit var textViewProgressBar: AppCompatTextView

        override fun bindView(itemView: View) {
            linearLayoutProgressBar = itemView.linearLayoutProgressBar
            progressBar = itemView.progressBar
            textViewProgressBar = itemView.textViewProgressBar
        }
    }
}
