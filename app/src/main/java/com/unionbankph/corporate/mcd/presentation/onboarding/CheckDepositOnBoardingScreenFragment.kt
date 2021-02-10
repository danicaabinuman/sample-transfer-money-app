package com.unionbankph.corporate.mcd.presentation.onboarding

import android.os.Bundle
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import kotlinx.android.synthetic.main.fragment_splash_screen.*

class CheckDepositOnBoardingScreenFragment :
    BaseFragment<CheckDepositOnBoardingViewModel>(R.layout.fragment_splash_screen) {

    override fun onViewsBound() {
        super.onViewsBound()
        when (arguments?.getSerializable(EXTRA_SCREEN)) {
            CheckDepositOnBoardingScreenEnum.CHECK_DEPOSIT_SCREEN -> {
                imageView.setImageResource(R.drawable.logo_check_deposit_splash_1)
                textViewHeader.text = formatString(R.string.title_check_deposit)
                textViewDesc.text = formatString(R.string.desc_check_deposit_splash_1)
            }
            CheckDepositOnBoardingScreenEnum.GET_TO_KNOW_SCREEN -> {
                imageView.setImageResource(R.drawable.logo_check_deposit_splash_2)
                imageView.setPadding(
                    resources.getDimension(R.dimen.content_spacing).toInt(),
                    resources.getDimension(R.dimen.content_spacing).toInt(),
                    resources.getDimension(R.dimen.content_spacing).toInt(),
                    resources.getDimension(R.dimen.content_spacing).toInt()
                )
                textViewHeader.text = formatString(R.string.title_get_to_know_your_check)
                textViewDesc.text = formatString(R.string.desc_check_deposit_splash_2)
            }
            CheckDepositOnBoardingScreenEnum.FRONT_OF_CHECK_SCREEN -> {
                imageView.setImageResource(R.drawable.logo_check_deposit_splash_3)
                imageView.setPadding(
                    resources.getDimension(R.dimen.content_spacing).toInt(),
                    resources.getDimension(R.dimen.content_spacing).toInt(),
                    resources.getDimension(R.dimen.content_spacing).toInt(),
                    resources.getDimension(R.dimen.content_spacing).toInt()
                )
                textViewHeader.text = formatString(R.string.title_front_of_check)
                textViewDesc.text = formatString(R.string.desc_check_deposit_splash_3)
            }
            CheckDepositOnBoardingScreenEnum.BACK_OF_CHECK_SCREEN -> {
                imageView.setImageResource(R.drawable.logo_check_deposit_splash_4)
                textViewHeader.text = formatString(R.string.title_back_of_check)
                textViewDesc.text = formatString(R.string.desc_check_deposit_splash_4)
            }
        }
    }

    companion object {
        fun newInstance(
            checkDepositOnBoardingScreenEnum: CheckDepositOnBoardingScreenEnum
        ): CheckDepositOnBoardingScreenFragment {
            val fragment =
                CheckDepositOnBoardingScreenFragment()
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_SCREEN, checkDepositOnBoardingScreenEnum)
            fragment.arguments = bundle
            return fragment
        }

        const val EXTRA_SCREEN = "screen"
    }

    enum class CheckDepositOnBoardingScreenEnum {
        CHECK_DEPOSIT_SCREEN,
        GET_TO_KNOW_SCREEN,
        FRONT_OF_CHECK_SCREEN,
        BACK_OF_CHECK_SCREEN,
        REMINDERS_SCREEN,
        IMPORTANT_SCREEN
    }
}
