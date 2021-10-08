package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme

import android.view.View
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.airbnb.epoxy.*
import com.unionbankph.corporate.R
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.databinding.SmeTextviewModelBinding

@EpoxyModelClass
abstract class SMETextViewModel :
    EpoxyModelWithHolder<SMETextViewModel.Holder>() {

    override fun getDefaultLayout(): Int {
        return R.layout.sme_textview_model
    }

    @EpoxyAttribute
    lateinit var style: String

    @EpoxyAttribute
    lateinit var text: String

    override fun bind(holder: Holder) {
        super.bind(holder)

        val textStyle = when (style) {
            Constant.SMETextViewStyle.H1 -> R.style.SMEHeadlineH1
            Constant.SMETextViewStyle.H2 -> R.style.SMEHeadlineH2
            Constant.SMETextViewStyle.SUBTITLE1 -> R.style.SMESubtitle1
            Constant.SMETextViewStyle.SUBTITLE2 -> R.style.SMESubtitle2
            Constant.SMETextViewStyle.CAPTION -> R.style.SMECaption
            Constant.SMETextViewStyle.BODY_GRAY -> R.style.SMEBodyMediumGray
            else -> R.style.SMEBody // Default: Body
        }

        TextViewCompat.setTextAppearance(holder.binding.textViewContent, textStyle)

        holder.binding.textViewContent.text = text
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: SmeTextviewModelBinding

        override fun bindView(itemView: View) {
            binding = SmeTextviewModelBinding.bind(itemView)
        }
    }
}