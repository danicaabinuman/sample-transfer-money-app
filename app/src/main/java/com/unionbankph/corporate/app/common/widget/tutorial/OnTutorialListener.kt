package com.unionbankph.corporate.app.common.widget.tutorial

import android.view.View
import com.takusemba.spotlight.Spotlight

interface OnTutorialListener {

    fun onClickSkipButtonTutorial(spotlight: Spotlight)
    fun onClickOkButtonTutorial(spotlight: Spotlight)
    fun onStartedTutorial(view: View?, viewTarget: View)
    fun onEndedTutorial(view: View?, viewTarget: View)
}
