package com.unionbankph.corporate.app.util

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import com.unionbankph.corporate.R

class AnimUtil(private val context: Context) {

    fun startAnimation(animation: Int, view: View) {
        view.startAnimation(AnimationUtils.loadAnimation(context, animation))
    }

    fun setAnimation(animation: Int): android.view.animation.Animation {
        return AnimationUtils.loadAnimation(context, animation)
    }

    interface Animation {
        companion object {

            val FADE_IN = R.anim.anim_fade_in
            val FADE_OUT = R.anim.anim_fade_out
        }
    }
}
