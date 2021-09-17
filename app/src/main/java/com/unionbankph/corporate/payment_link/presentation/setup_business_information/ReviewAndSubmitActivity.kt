package com.unionbankph.corporate.payment_link.presentation.setup_business_information

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import kotlinx.android.synthetic.main.activity_business_information.viewToolbar
import kotlinx.android.synthetic.main.activity_review_and_submit.*
import kotlinx.android.synthetic.main.item_review_business_info.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*

class ReviewAndSubmitActivity:BaseActivity<ReviewAndSubmitViewModel>(R.layout.activity_review_and_submit) {

    var counter = 0
    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(
            R.drawable.ic_msme_back_button_orange,
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

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel =
            ViewModelProviders.of(
                this,
                viewModelFactory
            )[ReviewAndSubmitViewModel::class.java]
    }

    override fun onViewsBound() {
        super.onViewsBound()

        toggleReviewAndSubmitFields()

        editBusinessInfoFields()
    }

    private fun toggleReviewAndSubmitFields(){

        btnReviewBusinessInfo.setOnClickListener {
            counter++
            val state = counter % 2
            if (state == 1){
                layout_details_business_info.visibility = View.VISIBLE
            } else if (state == 0){
                layout_details_business_info.visibility = View.GONE
            }
        }

        btnReviewOtherBusinessInfo.setOnClickListener {
            counter++
            val state = counter % 2
            if (state == 1){
                layout_details_other_business_info.visibility = View.VISIBLE
            } else if (state == 0){
                layout_details_other_business_info.visibility = View.GONE
            }
        }

        btnReviewProductsOrServices.setOnClickListener {
            counter++
            val state = counter % 2
            if (state == 1){
                layout_details_products_or_services.visibility = View.VISIBLE
            } else if (state == 0){
                layout_details_products_or_services.visibility = View.GONE
            }
        }

        btnReviewCardAcceptance.setOnClickListener {
            counter++
            val state = counter % 2
            if (state == 1){
                layout_details_card_acceptance.visibility = View.VISIBLE
            } else if (state == 0){
                layout_details_card_acceptance.visibility = View.GONE
            }
        }

        btnReviewTransactionInformation.setOnClickListener {
            counter++
            val state = counter % 2
            if (state == 1){
                layout_details_transaction_info.visibility = View.VISIBLE
            } else if (state == 0){
                layout_details_transaction_info.visibility = View.GONE
            }
        }

    }

    private fun editBusinessInfoFields(){
        btnEditBusinessInfo.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(BusinessInformationActivity.FROM_BUSINESS_INFO_BUTTON, EDIT_BUSINESS_INFO_BUTTON)

            navigator.navigate(
                this,
                BusinessInformationActivity::class.java,
                bundle,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_RIGHT
            )
        }
    }

    companion object{
        const val EDIT_BUSINESS_INFO_BUTTON = "edit_business_info_button"
        const val EDIT_OTHER_BUSINESS_INFO_BUTTON = "edit_other_business_info_button"
    }

}