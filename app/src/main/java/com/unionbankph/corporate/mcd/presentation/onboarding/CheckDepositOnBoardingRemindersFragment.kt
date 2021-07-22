package com.unionbankph.corporate.mcd.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.convertColorResourceToHex
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.toHtmlSpan
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.databinding.FragmentCheckDepositOnboardingRemindersBinding
import com.unionbankph.corporate.mcd.presentation.camera.CheckDepositCameraActivity
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositScreenEnum
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositTypeEnum
import timber.log.Timber


class CheckDepositOnBoardingRemindersFragment :
    BaseFragment<FragmentCheckDepositOnboardingRemindersBinding, CheckDepositOnBoardingViewModel>() {

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initListener()
    }

    private fun init() {
        binding.viewButtonNext.buttonPrimary.text = formatString(R.string.action_deposit_check)
        binding.viewButtonCancel.buttonSecondary.text = formatString(R.string.action_cancel)
        binding.textViewReminders1.text = formatString(R.string.desc_check_deposit_reminders_1).toHtmlSpan()
        binding.textViewReminders2.text = formatString(R.string.desc_check_deposit_reminders_2).toHtmlSpan()
        binding.textViewReminders3.text = formatString(R.string.desc_check_deposit_reminders_3).toHtmlSpan()
        binding.textViewReminders5.text = formatString(R.string.desc_check_deposit_reminders_5).toHtmlSpan()
        binding.textViewReminders5Sub.text =
            formatString(
                R.string.desc_check_deposit_reminders_5_sub,
                resources.getDimensionPixelSize(R.dimen.text_30),
                convertColorResourceToHex(getAccentColor())
            ).toHtmlSpan()
        binding.textViewReminders6.text = formatString(R.string.desc_check_deposit_reminders_6).toHtmlSpan()
        binding.textViewReminders7.text = formatString(R.string.desc_check_deposit_reminders_7).toHtmlSpan()
        binding.textViewReminders8.text = formatString(R.string.desc_check_deposit_reminders_8).toHtmlSpan()
        binding.textViewReminders9.text = formatString(R.string.desc_check_deposit_reminders_9).toHtmlSpan()
    }

    private fun initListener() {
        binding.viewButtonNext.buttonPrimary.setOnClickListener {
            navigateCheckDepositCameraScreen()
        }
        binding.viewButtonCancel.buttonSecondary.setOnClickListener {
            getAppCompatActivity().onBackPressed()
        }
        binding.fabArrow.setOnClickListener {
            binding.svReminders.post {
                binding.svReminders.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
        binding.svReminders.viewTreeObserver.addOnScrollChangedListener {
            val scrollY: Int = binding.svReminders.scrollY // For ScrollView
            val scrollX: Int = binding.svReminders.scrollX // For HorizontalScrollView
            // DO SOMETHING WITH THE SCROLL COORDINATES
            Timber.d("scrollY: $scrollY")
            if (binding.svReminders.getChildAt(0).bottom <= (binding.svReminders.height + scrollY)) {
                if (binding.clArrow.visibility == View.VISIBLE) {
                    viewUtil.startAnimateView(false, binding.clArrow, android.R.anim.fade_out)
                }
            } else {
                if (binding.clArrow.visibility == View.GONE) {
                    viewUtil.startAnimateView(true, binding.clArrow, android.R.anim.fade_in)
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

    override val viewModelClassType: Class<CheckDepositOnBoardingViewModel>
        get() = CheckDepositOnBoardingViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCheckDepositOnboardingRemindersBinding
        get() = FragmentCheckDepositOnboardingRemindersBinding::inflate
}
