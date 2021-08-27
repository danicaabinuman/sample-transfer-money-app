package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.databinding.ActivityYesAcceptCardPaymentsBinding
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents.CardAcceptanceUploadDocumentsActivity

class YesAcceptCardPaymentsActivity :
    BaseActivity<ActivityYesAcceptCardPaymentsBinding, YesAcceptCardPaymentsViewModel>() {

    var counter = 0
    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.root)
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange, R.color.colorSMEMediumOrange, true)
    }

    override fun onViewsBound() {
        super.onViewsBound()

        showAffiliation1()
        buttonNextDisable()
    }

    private fun showAffiliation1(){

        binding.spAffiliation1.setOnClickListener {
            counter++
            val checker = counter % 2
            if (checker == 1){
                binding.layoutAffiliation1.root.visibility = View.VISIBLE
                binding.spAffiliation1.setBackgroundResource(R.drawable.bg_transparent_orange_border_radius_8dp)
            } else if (checker == 0){
                binding.layoutAffiliation1.root.visibility = View.GONE
                binding.spAffiliation1.setBackgroundResource(R.drawable.bg_rectangle_white)
            }

        }
    }

    private fun showAffiliations(){
        binding.spAffiliation1.visibility = View.VISIBLE
        binding.btnAffiliation2.visibility = View.VISIBLE
    }

    private fun hideAffiliations(){
        binding.spAffiliation1.visibility = View.INVISIBLE
        binding.btnAffiliation2.visibility = View.INVISIBLE
    }

    private fun buttonNextEnable(){
        binding.btnNext.isEnabled = true
        binding.btnNext.setBackgroundResource(R.drawable.bg_gradient_orange_radius4)
        binding.btnNext.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        navigateToUploadDocuments()
    }

    private fun buttonNextDisable(){
        binding.btnNext.isEnabled = false
        binding.btnNext.setBackgroundResource(R.drawable.bg_gray_radius4_width1)
        binding.btnNext.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorBlack10))
    }

    fun checkedNoAffiliation(view: View) {
        if (view is CheckBox){
            val checked: Boolean = view.isChecked

            when (view.id){
                R.id.cb_NoOtherAffiliation -> {
                    if (checked){
                        buttonNextEnable()
                        hideAffiliations()
                    } else {
                        buttonNextDisable()
                        showAffiliations()
                    }
                }
            }
        }
    }

    private fun navigateToUploadDocuments(){
        binding.btnNext.setOnClickListener {
            val intent = Intent(this, CardAcceptanceUploadDocumentsActivity::class.java)
            startActivity(intent)
        }

    }

    override val bindingInflater: (LayoutInflater) -> ActivityYesAcceptCardPaymentsBinding
        get() = ActivityYesAcceptCardPaymentsBinding::inflate

    override val viewModelClassType: Class<YesAcceptCardPaymentsViewModel>
        get() = YesAcceptCardPaymentsViewModel::class.java
}