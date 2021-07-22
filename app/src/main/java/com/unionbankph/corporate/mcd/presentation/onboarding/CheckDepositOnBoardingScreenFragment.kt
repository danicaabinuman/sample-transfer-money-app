package com.unionbankph.corporate.mcd.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.databinding.FragmentSplashScreenBinding

class CheckDepositOnBoardingScreenFragment :
    BaseFragment<FragmentSplashScreenBinding, CheckDepositOnBoardingViewModel>() {

    override fun onViewsBound() {
        super.onViewsBound()
        when (arguments?.getSerializable(EXTRA_SCREEN)) {
            CheckDepositOnBoardingScreenEnum.CHECK_DEPOSIT_SCREEN -> {
                binding.imageView.setImageResource(R.drawable.logo_check_deposit_splash_1)
                binding.textViewHeader.text = formatString(R.string.title_check_deposit)
                binding.textViewDesc.text = formatString(R.string.desc_check_deposit_splash_1)
            }
            CheckDepositOnBoardingScreenEnum.GET_TO_KNOW_SCREEN -> {
                binding.imageView.setImageResource(R.drawable.logo_check_deposit_splash_2)
                binding.imageView.setPadding(
                    resources.getDimension(R.dimen.content_spacing).toInt(),
                    resources.getDimension(R.dimen.content_spacing).toInt(),
                    resources.getDimension(R.dimen.content_spacing).toInt(),
                    resources.getDimension(R.dimen.content_spacing).toInt()
                )
                binding.textViewHeader.text = formatString(R.string.title_get_to_know_your_check)
                binding.textViewDesc.text = formatString(R.string.desc_check_deposit_splash_2)
            }
            CheckDepositOnBoardingScreenEnum.FRONT_OF_CHECK_SCREEN -> {
                binding.imageView.setImageResource(R.drawable.logo_check_deposit_splash_3)
                binding.imageView.setPadding(
                    resources.getDimension(R.dimen.content_spacing).toInt(),
                    resources.getDimension(R.dimen.content_spacing).toInt(),
                    resources.getDimension(R.dimen.content_spacing).toInt(),
                    resources.getDimension(R.dimen.content_spacing).toInt()
                )
                binding.textViewHeader.text = formatString(R.string.title_front_of_check)
                binding.textViewDesc.text = formatString(R.string.desc_check_deposit_splash_3)
            }
            CheckDepositOnBoardingScreenEnum.BACK_OF_CHECK_SCREEN -> {
                binding.imageView.setImageResource(R.drawable.logo_check_deposit_splash_4)
                binding.textViewHeader.text = formatString(R.string.title_back_of_check)
                binding.textViewDesc.text = formatString(R.string.desc_check_deposit_splash_4)
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

    override val viewModelClassType: Class<CheckDepositOnBoardingViewModel>
        get() = CheckDepositOnBoardingViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSplashScreenBinding
        get() = FragmentSplashScreenBinding::inflate
}
