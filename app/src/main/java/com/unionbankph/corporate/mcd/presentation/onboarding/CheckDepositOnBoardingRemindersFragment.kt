package com.unionbankph.corporate.mcd.presentation.onboarding

import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.convertColorResourceToHex
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.toHtmlSpan
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.mcd.presentation.camera.CheckDepositCameraActivity
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositScreenEnum
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositTypeEnum
import kotlinx.android.synthetic.main.fragment_check_deposit_onboarding_reminders.*
import kotlinx.android.synthetic.main.widget_button_gray.*
import kotlinx.android.synthetic.main.widget_button_orange.*
import timber.log.Timber


class CheckDepositOnBoardingRemindersFragment :
    BaseFragment<CheckDepositOnBoardingViewModel>(R.layout.fragment_check_deposit_onboarding_reminders) {

    override fun onViewModelBound() {
        super.onViewModelBound()
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[CheckDepositOnBoardingViewModel::class.java]
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initListener()
    }

    private fun init() {
        buttonPrimary.text = formatString(R.string.action_deposit_check)
        buttonSecondary.text = formatString(R.string.action_cancel)
        textViewReminders1.text = formatString(R.string.desc_check_deposit_reminders_1).toHtmlSpan()
        textViewReminders2.text = formatString(R.string.desc_check_deposit_reminders_2).toHtmlSpan()
        textViewReminders3.text = formatString(R.string.desc_check_deposit_reminders_3).toHtmlSpan()
        textViewReminders5.text = formatString(R.string.desc_check_deposit_reminders_5).toHtmlSpan()
        textViewReminders5Sub.text =
            formatString(
                R.string.desc_check_deposit_reminders_5_sub,
                resources.getDimensionPixelSize(R.dimen.text_30),
                convertColorResourceToHex(getAccentColor())
            ).toHtmlSpan()
        textViewReminders6.text = formatString(R.string.desc_check_deposit_reminders_6).toHtmlSpan()
        textViewReminders7.text = formatString(R.string.desc_check_deposit_reminders_7).toHtmlSpan()
        textViewReminders8.text = formatString(R.string.desc_check_deposit_reminders_8).toHtmlSpan()
        textViewReminders9.text = formatString(R.string.desc_check_deposit_reminders_9).toHtmlSpan()
    }

    private fun initListener() {
        buttonPrimary.setOnClickListener {
            navigateCheckDepositCameraScreen()
        }
        buttonSecondary.setOnClickListener {
            getAppCompatActivity().onBackPressed()
        }
        fab_arrow.setOnClickListener {
            sv_reminders.post { sv_reminders.fullScroll(ScrollView.FOCUS_DOWN) }
        }
        sv_reminders.viewTreeObserver.addOnScrollChangedListener {
            val scrollY: Int = sv_reminders.scrollY // For ScrollView
            val scrollX: Int = sv_reminders.scrollX // For HorizontalScrollView
            // DO SOMETHING WITH THE SCROLL COORDINATES
            Timber.d("scrollY: $scrollY")
            if (sv_reminders.getChildAt(0).bottom <= (sv_reminders.height + scrollY)) {
                if (cl_arrow.visibility == View.VISIBLE) {
                    viewUtil.startAnimateView(false, cl_arrow, android.R.anim.fade_out)
                }
            } else {
                if (cl_arrow.visibility == View.GONE) {
                    viewUtil.startAnimateView(true, cl_arrow, android.R.anim.fade_in)
                }
            }
        }
    }

    private fun navigateCheckDepositCameraScreen() {
        viewModel.setFirstCheckDeposit(false)
        val bundle = Bundle().apply {
            putString(
                CheckDepositCameraActivity.EXTRA_SCREEN,
                CheckDepositScreenEnum.FRONT_OF_CHECK.name
            )
            putString(
                CheckDepositCameraActivity.EXTRA_CHECK_DEPOSIT_TYPE,
                CheckDepositTypeEnum.FRONT_OF_CHECK.name
            )
        }
        navigator.navigate(
            getAppCompatActivity(),
            CheckDepositCameraActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }
}
