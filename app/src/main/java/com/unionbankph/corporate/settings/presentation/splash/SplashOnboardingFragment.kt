package com.unionbankph.corporate.settings.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.databinding.FragmentSplashScreenBinding

class SplashOnboardingFragment :
    BaseFragment<FragmentSplashScreenBinding, GeneralViewModel>() {

    override fun onViewsBound() {
        super.onViewsBound()
        when (arguments?.getSerializable(EXTRA_PAGE)) {
            SplashScreenPage.PAGE_VIEW_ACCOUNT -> {
                addPaddingLargeImageView()
                binding.imageView.setImageResource(R.drawable.bg_splash_view_accounts_msme)
                binding.textViewHeader.text = getString(R.string.title_view_accounts)
                binding.textViewDesc.text = getString(R.string.desc_view_accounts)

            }
            SplashScreenPage.PAGE_TRANSFER_FUNDS -> {
                addPaddingLargeImageView()
                binding.imageView.setImageResource(R.drawable.bg_splash_transfer_funds_msme)
                binding.textViewHeader.text = getString(R.string.title_transfer_funds)
                binding.textViewDesc.text = getString(R.string.desc_transfer_funds)
            }
            SplashScreenPage.PAGE_PAY_BILLS -> {
                addPaddingLargeImageView()
                binding.imageView.setImageResource(R.drawable.bg_splash_pay_bills_msme)
                binding.textViewHeader.text = getString(R.string.title_pay_bills)
                binding.textViewDesc.text = getString(R.string.desc_pay_bills)
            }
            SplashScreenPage.PAGE_PAYMENT_LINK -> {
                addPaddingLargeImageView()
                binding.imageView.setImageResource(R.drawable.bg_splash_payment_link_msme)
                binding.textViewHeader.text = getString(R.string.title_create_unique_payment_links)
                binding.textViewDesc.text = getString(R.string.desc_create_unique_payment_links)
            }
            SplashScreenPage.PAGE_DEPOSIT_CHECK -> {
                addPaddingLargeImageView()
                binding.imageView.setImageResource(R.drawable.bg_splash_deposit_checks_msme)
                binding.textViewHeader.text = getString(R.string.title_deposit_checks)
                binding.textViewDesc.text = getString(R.string.desc_deposit_checks)
            }
        }
    }

    private fun addPaddingLargeImageView() {
        binding.imageView.setPadding(
            resources.getDimension(R.dimen.content_group_spacing).toInt(),
            resources.getDimension(R.dimen.content_group_spacing).toInt(),
            resources.getDimension(R.dimen.content_group_spacing).toInt(),
            resources.getDimension(R.dimen.content_group_spacing).toInt()
        )
    }

    private fun addPaddingImageView() {
        binding.imageView.setPadding(
            resources.getDimension(R.dimen.content_spacing).toInt(),
            resources.getDimension(R.dimen.content_spacing).toInt(),
            resources.getDimension(R.dimen.content_spacing).toInt(),
            resources.getDimension(R.dimen.content_spacing).toInt()
        )
    }

    companion object {
        fun newInstance(splashScreenPage: SplashScreenPage): SplashOnboardingFragment {
            val fragment = SplashOnboardingFragment()
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_PAGE, splashScreenPage)
            fragment.arguments = bundle
            return fragment
        }

        const val EXTRA_PAGE = "page"
    }

    enum class SplashScreenPage {
        PAGE_VIEW_ACCOUNT,
        PAGE_TRANSFER_FUNDS,
        PAGE_PAY_BILLS,
        PAGE_PAYMENT_LINK,
        PAGE_DEPOSIT_CHECK
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSplashScreenBinding
        get() = FragmentSplashScreenBinding::inflate

    override val viewModelClassType: Class<GeneralViewModel>
        get() = GeneralViewModel::class.java
}