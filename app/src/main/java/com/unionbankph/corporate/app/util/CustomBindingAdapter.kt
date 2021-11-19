package com.unionbankph.corporate.app.util

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.platform.glide.GlideApp
import org.w3c.dom.Text


@BindingAdapter("setDrawable")
fun setDrawable(imageView: ImageView, source: Int?) {
    source?.let {
        Glide.with(imageView.context)
            .load(source)
            .into(imageView)
    }
}

@BindingAdapter("setBitmapImage")
fun setBitmapImage(imageView: ImageView, source: Int?) {
    source?.let {
        Glide.with(imageView.context)
            .asBitmap()
            .signature(ObjectKey("1"))
            .fitCenter()
            .load(source)
            .into(imageView)
    }
}

@BindingAdapter("backgroundColor")
fun View.setBackgroundColor(source: Int?) {
    source?.let {
        if (source  % 2 == 0) {
            this.setBackgroundColor(this.context.getColor(R.color.dsColorExtraLightGray))
        } else {
            this.setBackgroundColor(this.context.getColor(R.color.colorDisableItem))
        }
    }
}

@BindingAdapter("backgroundColorTextView")
fun setBackgroundColorTextView(textView: TextView, source: Int?) {
    source?.let {
        if (source  % 2 == 0) {
            textView.setBackgroundColor(textView.context.getColor(R.color.colorDisableItem))
        } else {
            textView.setBackgroundColor(textView.context.getColor(R.color.dsColorExtraLightGray))
        }
    }
}

@BindingAdapter("selectedRadioButton")
fun setSelectedRadioButton(radioButton: RadioButton, source: Int?) {
    source?.let {
    }
}

@BindingAdapter("setBusinessTypeLabel")
fun setBusinessTypeLabel(textView: TextView, source: Int?) {
    source?.let {
        when (source) {
            0 -> textView.text = textView.context.getString(R.string.title_individual)
            1 -> textView.text = textView.context.getString(R.string.title_sole_proprietorship_msme)
            2 -> textView.text = textView.context.getString(R.string.title_partnership)
            3 -> textView.text = textView.context.getString(R.string.title_corporation)
        }
    }
}

@BindingAdapter("setBusinessTypeDescription")
fun setBusinessTypeDescription(textView: TextView, source: Int?) {
    source?.let {
        when (source) {
            0 -> textView.text = textView.context.getString(R.string.desc_individual)
            1 -> textView.text = textView.context.getString(R.string.desc_sole_prop)
            2 -> textView.text = textView.context.getString(R.string.desc_partnership)
            3 -> textView.text = textView.context.getString(R.string.desc_corporation)
        }
    }
}

@BindingAdapter("setStrokeColor")
fun setStrokeColor(cardView: MaterialCardView, status: Boolean?) {
    cardView.apply {
        status?.let { status ->
            if (status) {
                setCardBackgroundColor(context.getColor(R.color.dsColorButtonWithIconSolidColorSelected))
                strokeColor = context.getColor(R.color.dsColorButtonWithIconBorderSelected)
            } else {
                setCardBackgroundColor(context.getColor(R.color.dsColorButtonWithIconSolidColorDefault))
                strokeColor = context.getColor(R.color.dsColorLightGray)
            }
        }
    }
}

@BindingAdapter("setTextColorValidation")
fun setTextColorValidation(textView: TextView, status: String?) {
    textView.apply {
        if (status.isNullOrEmpty()) {
            setTextColor(context.getColor(R.color.dsColorDarkGray))
        } else {
            setTextColor(context.getColor(R.color.colorErrorColor))
        }
    }
}

@BindingAdapter("setStrokeColorButtonError")
fun setStrokeColorButtonError(button: MaterialButton, status: String?) {
    button.apply {
        strokeColor = if (status.isNullOrEmpty()) {
            if (isChecked) {
                ColorStateList.valueOf(context.getColor(R.color.dsColorMediumOrange))
            } else {
                ColorStateList.valueOf(context.getColor(R.color.dsColorLightGray))
            }
        } else {
            ColorStateList.valueOf(context.getColor(R.color.colorErrorColor))
        }

    }
}

@BindingAdapter("setVisibilityByString")
fun setVisibilityByString(textView: TextView, status: String?) {
    textView.apply {
        if (status.isNullOrEmpty()) {
            this.visibility = View.INVISIBLE
        } else {
            this.visibility = View.VISIBLE
        }
    }
}


@BindingAdapter("setBackgroundColorDisableByString")
fun setBackgroundColorDisableByString(view: View, source: String?) {
    view.apply {
        if (source.isNullOrEmpty()) {
            view.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.colorTransparent))
        } else {
            view.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.dsColorLightGray))
        }
    }
}





