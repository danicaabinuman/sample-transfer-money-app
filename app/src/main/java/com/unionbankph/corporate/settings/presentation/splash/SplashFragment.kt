package com.unionbankph.corporate.settings.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.databinding.FragmentSplashScreenBinding

class SplashFragment :
    BaseFragment<FragmentSplashScreenBinding, GeneralViewModel>() {

    override fun onViewsBound() {
        super.onViewsBound()
        when (arguments?.getSerializable(EXTRA_PAGE)) {
            SplashScreenPage.PAGE_SAFE_FIRST -> {
                if (App.isSME()) {
                    addPaddingLargeImageView()
                    binding.imageView.setImageResource(R.drawable.bg_splash_safety_first_sme)
                } else {
                    binding.imageView.setImageResource(R.drawable.bg_splash_safety_first)
                }
                binding.textViewHeader.text = getString(R.string.title_safety_first)
                binding.textViewDesc.text = getString(R.string.desc_safety_first)
            }
            SplashScreenPage.PAGE_TRANSFER -> {
                if (App.isSME()) {
                    binding.imageView.setImageResource(R.drawable.bg_splash_transfers_sme)
                } else {
                    binding.imageView.setImageResource(R.drawable.bg_splash_transfers)
                }
                binding.textViewHeader.text = getString(R.string.title_transfers)
                binding.textViewDesc.text = getString(R.string.desc_transfers)
            }
            SplashScreenPage.PAGE_BILLS_PAYMENT -> {
                if (App.isSME()) {
                    addPaddingLargeImageView()
                    binding.imageView.setImageResource(R.drawable.bg_splash_bills_payment_sme)
                } else {
                    binding.imageView.setImageResource(R.drawable.bg_splash_bills_payment)
                }
                binding.textViewHeader.text = getString(R.string.title_bills_payment_splash)
                binding.textViewDesc.text = getString(R.string.desc_bills_payment)
            }
            SplashScreenPage.PAGE_APPROVAL -> {
                if (App.isSME()) {
                    binding.imageView.setImageResource(R.drawable.bg_splash_approval_sme)
                    addPaddingImageView()
                } else {
                    addPaddingLargeImageView()
                    binding.imageView.setImageResource(R.drawable.bg_splash_approval)
                }
                binding.textViewHeader.text = getString(R.string.title_approve_transactions)
                binding.textViewDesc.text = getString(R.string.desc_approve_transactions)
            }
            SplashScreenPage.PAGE_ORGANIZATION -> {
                if (App.isSME()) {
                    addPaddingLargeImageView()
                    binding.imageView.setImageResource(R.drawable.bg_splash_organizations_sme)
                } else {
                    addPaddingImageView()
                    binding.imageView.setImageResource(R.drawable.bg_splash_organizations)
                }
                binding.textViewHeader.text = getString(R.string.title_organization_splash)
                binding.textViewDesc.text = getString(R.string.desc_organization)
            }
            SplashScreenPage.PAGE_SUMMARY -> {
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
        fun newInstance(splashScreenPage: SplashScreenPage): SplashFragment {
            val fragment = SplashFragment()
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_PAGE, splashScreenPage)
            fragment.arguments = bundle
            return fragment
        }

        const val EXTRA_PAGE = "page"
    }

    enum class SplashScreenPage {
        PAGE_SAFE_FIRST,
        PAGE_APPROVAL,
        PAGE_TRANSFER,
        PAGE_BILLS_PAYMENT,
        PAGE_ORGANIZATION,
        PAGE_SUMMARY,
        PAGE_LOGIN
    }

    override val viewModelClassType: Class<GeneralViewModel>
        get() = GeneralViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSplashScreenBinding
        get() = FragmentSplashScreenBinding::inflate
}
