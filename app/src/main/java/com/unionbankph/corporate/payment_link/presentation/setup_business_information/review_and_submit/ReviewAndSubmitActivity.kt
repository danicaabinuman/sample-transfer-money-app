package com.unionbankph.corporate.payment_link.presentation.setup_business_information.review_and_submit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.databinding.ActivityReviewAndSubmitBinding
import com.unionbankph.corporate.payment_link.presentation.setup_business_information.business_information_forms.BusinessInformationActivity

class ReviewAndSubmitActivity:BaseActivity<ActivityReviewAndSubmitBinding, ReviewAndSubmitViewModel>() {

    var counter = 0
    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.root)
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

    }

    override fun onViewsBound() {
        super.onViewsBound()

        toggleReviewAndSubmitFields()

        editBusinessInfoFields()
    }

    private fun toggleReviewAndSubmitFields(){

        binding.btnReviewBusinessInfo.setOnClickListener {
            counter++
            val state = counter % 2
            if (state == 1){
                binding.layoutDetailsBusinessInfo.root.visibility = View.VISIBLE
            } else if (state == 0){
                binding.layoutDetailsBusinessInfo.root.visibility = View.GONE
            }
        }

        binding.btnReviewOtherBusinessInfo.setOnClickListener {
            counter++
            val state = counter % 2
            if (state == 1){
                binding.layoutDetailsOtherBusinessInfo.root.visibility = View.VISIBLE
            } else if (state == 0){
                binding.layoutDetailsOtherBusinessInfo.root.visibility = View.GONE
            }
        }

        binding.btnReviewProductsOrServices.setOnClickListener {
            counter++
            val state = counter % 2
            if (state == 1){
                binding.layoutDetailsProductsOrServices.root.visibility = View.VISIBLE
            } else if (state == 0){
                binding.layoutDetailsProductsOrServices.root.visibility = View.GONE
            }
        }

        binding.btnReviewCardAcceptance.setOnClickListener {
            counter++
            val state = counter % 2
            if (state == 1){
                binding.layoutDetailsCardAcceptance.root.visibility = View.VISIBLE
            } else if (state == 0){
                binding.layoutDetailsCardAcceptance.root.visibility = View.GONE
            }
        }

        binding.btnReviewTransactionInformation.setOnClickListener {
            counter++
            val state = counter % 2
            if (state == 1){
                binding.layoutDetailsTransactionInfo.root.visibility = View.VISIBLE
            } else if (state == 0){
                binding.layoutDetailsTransactionInfo.root.visibility = View.GONE
            }
        }

    }

    private fun editBusinessInfoFields(){
        binding.layoutDetailsBusinessInfo.btnEditBusinessInfo.setOnClickListener {
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

    fun readFeesAndCharges(view: View) {
        if (view is CheckBox){
            val checked: Boolean = view.isChecked

            when (view.id){
                R.id.cb_review_fnc_tnc -> {
                    binding.btnReviewAndSubmit.isEnabled = checked
                }
            }
        }

    }

    companion object{
        const val EDIT_BUSINESS_INFO_BUTTON = "edit_business_info_button"
        const val EDIT_OTHER_BUSINESS_INFO_BUTTON = "edit_other_business_info_button"
    }

    override val bindingInflater: (LayoutInflater) -> ActivityReviewAndSubmitBinding
        get() = ActivityReviewAndSubmitBinding::inflate
    override val viewModelClassType: Class<ReviewAndSubmitViewModel>
        get() = ReviewAndSubmitViewModel::class.java


}