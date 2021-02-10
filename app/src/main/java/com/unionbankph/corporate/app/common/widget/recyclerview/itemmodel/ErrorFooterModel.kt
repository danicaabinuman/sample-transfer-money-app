package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel

import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import kotlinx.android.synthetic.main.footer_error.view.*
import kotlinx.android.synthetic.main.footer_progress_bar.view.*

@EpoxyModelClass(layout = R.layout.footer_error)
abstract class ErrorFooterModel : EpoxyModelWithHolder<ErrorFooterModel.Holder>() {

    @EpoxyAttribute
    lateinit var title: String

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<*>

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.apply {
            tv_title.text = title
            cl_error.setOnClickListener {
                callbacks.onTapToRetry()
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var cl_error: ConstraintLayout
        lateinit var tv_title: AppCompatTextView
        lateinit var tv_desc: AppCompatTextView

        override fun bindView(itemView: View) {
            cl_error = itemView.cl_error
            tv_title = itemView.tv_title
            tv_desc = itemView.tv_desc
        }
    }
}
