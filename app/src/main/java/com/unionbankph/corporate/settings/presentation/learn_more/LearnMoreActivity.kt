package com.unionbankph.corporate.settings.presentation.learn_more

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.makeLinks
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.auth.presentation.policy.PrivacyPolicyActivity
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.settings.presentation.splash.SplashFrameActivity
import kotlinx.android.synthetic.main.activity_learn_more.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*

class LearnMoreActivity : BaseActivity<GeneralViewModel>(R.layout.activity_learn_more) {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setToolbarTitle(tvToolbar, getString(R.string.title_learn_more))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        textViewRowTwoTitle.text = formatString(
            if (isSME) {
                R.string.title_learn_more_row_two_sme
            } else {
                R.string.title_learn_more_row_two
            }
        )
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        textViewRowOne.makeLinks(
            Pair(getString(R.string.action_here), View.OnClickListener {
                navigateSplashScreen()
            })
        )
        textViewRowOne.setOnClickListener {
            navigateSplashScreen()
        }
        textViewRowThree.makeLinks(
            Pair(getString(R.string.action_here), View.OnClickListener {
                navigatePrivacyPolicy()
            })
        )
        textViewRowThree.setOnClickListener {
            navigatePrivacyPolicy()
        }
    }

    private fun navigatePrivacyPolicy() {
        val bundle = Bundle()
        bundle.putString(
            PrivacyPolicyActivity.EXTRA_REQUEST_PAGE, PrivacyPolicyActivity.PAGE_LEARN_MORE
        )
        navigator.navigate(
            this,
            PrivacyPolicyActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateSplashScreen() {
        navigator.navigate(
            this,
            SplashFrameActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
    }
}
