package com.unionbankph.corporate.settings.presentation.splash

import android.os.Bundle
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import kotlinx.android.synthetic.main.fragment_splash_screen.*

class SplashFragment : BaseFragment<GeneralViewModel>(R.layout.fragment_splash_screen) {

    override fun onViewsBound() {
        super.onViewsBound()
        when (arguments?.getSerializable(EXTRA_PAGE)) {
            SplashScreenPage.PAGE_SAFE_FIRST -> {
                if (App.isSME()) {
                    addPaddingLargeImageView()
                    imageView.setImageResource(R.drawable.bg_splash_safety_first_sme)
                } else {
                    imageView.setImageResource(R.drawable.bg_splash_safety_first)
                }
                textViewHeader.text = getString(R.string.title_safety_first)
                textViewDesc.text = getString(R.string.desc_safety_first)
            }
            SplashScreenPage.PAGE_TRANSFER -> {
                if (App.isSME()) {
                    imageView.setImageResource(R.drawable.bg_splash_transfers_sme)
                } else {
                    imageView.setImageResource(R.drawable.bg_splash_transfers)
                }
                textViewHeader.text = getString(R.string.title_transfers)
                textViewDesc.text = getString(R.string.desc_transfers)
            }
            SplashScreenPage.PAGE_BILLS_PAYMENT -> {
                if (App.isSME()) {
                    addPaddingLargeImageView()
                    imageView.setImageResource(R.drawable.bg_splash_bills_payment_sme)
                } else {
                    imageView.setImageResource(R.drawable.bg_splash_bills_payment)
                }
                textViewHeader.text = getString(R.string.title_bills_payment_splash)
                textViewDesc.text = getString(R.string.desc_bills_payment)
            }
            SplashScreenPage.PAGE_APPROVAL -> {
                if (App.isSME()) {
                    imageView.setImageResource(R.drawable.bg_splash_approval_sme)
                    addPaddingImageView()
                } else {
                    addPaddingLargeImageView()
                    imageView.setImageResource(R.drawable.bg_splash_approval)
                }
                textViewHeader.text = getString(R.string.title_approve_transactions)
                textViewDesc.text = getString(R.string.desc_approve_transactions)
            }
            SplashScreenPage.PAGE_ORGANIZATION -> {
                if (App.isSME()) {
                    addPaddingLargeImageView()
                    imageView.setImageResource(R.drawable.bg_splash_organizations_sme)
                } else {
                    addPaddingImageView()
                    imageView.setImageResource(R.drawable.bg_splash_organizations)
                }
                textViewHeader.text = getString(R.string.title_organization_splash)
                textViewDesc.text = getString(R.string.desc_organization)
            }
            SplashScreenPage.PAGE_SUMMARY -> {
            }
        }
    }

    private fun addPaddingLargeImageView() {
        imageView.setPadding(
            resources.getDimension(R.dimen.content_group_spacing).toInt(),
            resources.getDimension(R.dimen.content_group_spacing).toInt(),
            resources.getDimension(R.dimen.content_group_spacing).toInt(),
            resources.getDimension(R.dimen.content_group_spacing).toInt()
        )
    }

    private fun addPaddingImageView() {
        imageView.setPadding(
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
        PAGE_SUMMARY
    }
}
