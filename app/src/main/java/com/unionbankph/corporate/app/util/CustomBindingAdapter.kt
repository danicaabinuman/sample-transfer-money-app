package com.unionbankph.corporate.app.util

import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.google.android.material.card.MaterialCardView
import com.unionbankph.corporate.R


@BindingAdapter("setDrawable")
fun setDrawable(imageView: ImageView, source: Int?) {
    source?.let {
        Glide.with(imageView.context)
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




