package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.payment_link.presentation.onboarding.OnboardingUploadPhotosFragment
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.YesAcceptCardPaymentsViewModel
import kotlinx.android.synthetic.main.activity_onboarding_upload_photos.*
import kotlinx.android.synthetic.main.activity_onboarding_upload_photos.viewToolbar
import kotlinx.android.synthetic.main.activity_upload_documents.*
import kotlinx.android.synthetic.main.bottom_sheet_upload_photos.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.textViewTitle
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.toolbar
import kotlinx.android.synthetic.main.widget_transparent_rmo_appbar.*

class CardAcceptanceUploadDocumentsActivity :
    BaseActivity<CardAcceptanceUploadDocumentsViewModel>(R.layout.activity_upload_documents) {

    private var onboardingUploadFragment: OnboardingUploadPhotosFragment? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(
            R.drawable.ic_close_orange,
            R.color.colorSMEMediumOrange,
            true
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

    override fun onViewsBound() {
        super.onViewsBound()

        textViewTitle.text = "Upload Document"
        btnUploadBIRDocs.setOnClickListener {
            showbottomSheetDialog()
        }
    }

    private fun showbottomSheetDialog() {

        if (onboardingUploadFragment == null) {
            onboardingUploadFragment = OnboardingUploadPhotosFragment.newInstance()
        }

        onboardingUploadFragment!!.show(supportFragmentManager, OnboardingUploadPhotosFragment.TAG)
        if (onboardingUploadFragment!!.isVisible){
            clUploadPhotoOption.visibility = View.GONE
//            clUploadBIROption.visibility = View.VISIBLE
        }

    }

}