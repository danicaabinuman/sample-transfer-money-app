package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents.CardAcceptanceUploadDocumentsActivity
import kotlinx.android.synthetic.main.activity_yes_accept_card_payments.viewToolbar
import kotlinx.android.synthetic.main.activity_yes_accept_card_payments.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*

class YesAcceptCardPaymentsActivity : BaseActivity<YesAcceptCardPaymentsViewModel>(R.layout.activity_yes_accept_card_payments) {

    var counter = 0
    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange, R.color.colorSMEMediumOrange, true)
    }

    override fun onViewsBound() {
        super.onViewsBound()

        showAffiliation1()
        buttonNextDisable()
    }

    private fun showAffiliation1(){

        spAffiliation1.setOnClickListener {
            counter++
            val checker = counter % 2
            if (checker == 1){
                layoutAffiliation1.visibility = View.VISIBLE
                spAffiliation1.setBackgroundResource(R.drawable.bg_transparent_orange_border_radius_8dp)
            } else if (checker == 0){
                layoutAffiliation1.visibility = View.GONE
                spAffiliation1.setBackgroundResource(R.drawable.bg_rectangle_white)
            }

        }
    }

    private fun showAffiliations(){
        spAffiliation1.visibility = View.VISIBLE
        btnAffiliation2.visibility = View.VISIBLE
    }

    private fun hideAffiliations(){
        spAffiliation1.visibility = View.INVISIBLE
        btnAffiliation2.visibility = View.INVISIBLE
    }

    private fun buttonNextEnable(){
        btnNext.isEnabled = true
        btnNext.setBackgroundResource(R.drawable.bg_gradient_orange_radius4)
        btnNext.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        navigateToUploadDocuments()
    }

    private fun buttonNextDisable(){
        btnNext.isEnabled = false
        btnNext.setBackgroundResource(R.drawable.bg_gray_radius4_width1)
        btnNext.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorBlack10))
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
        btnNext.setOnClickListener {
            val intent = Intent(this, CardAcceptanceUploadDocumentsActivity::class.java)
            startActivity(intent)
        }

    }
}