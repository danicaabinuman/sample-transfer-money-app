package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents.CardAcceptanceUploadDocumentsActivity
import kotlinx.android.synthetic.main.activity_yes_accept_card_payments.viewToolbar
import kotlinx.android.synthetic.main.activity_yes_accept_card_payments.*
import kotlinx.android.synthetic.main.layout_on_added_affiliation.*
import kotlinx.android.synthetic.main.layout_on_added_affiliation.view.*
import kotlinx.android.synthetic.main.spinner_card_payments_affiliation_1.*
import kotlinx.android.synthetic.main.spinner_card_payments_affiliation_1.view.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*

class YesAcceptCardPaymentsActivity : BaseActivity<YesAcceptCardPaymentsViewModel>(R.layout.activity_yes_accept_card_payments) {

    var counter = 0
    var affiliationCounter = 0
    var fieldCount = 1

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange, R.color.colorSMEMediumOrange, true)
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

//        showAffiliation1()
        buttonNextDisable()

        addAffiliation()
    }

//    private fun showAffiliation1(){
//
//        spAffiliation1.setOnClickListener {
//            counter++
//            val checker = counter % 2
//            if (checker == 1){
//                layoutAffiliation1.visibility = View.VISIBLE
//                spAffiliation1.setBackgroundResource(R.drawable.bg_transparent_orange_border_radius_8dp)
//            } else if (checker == 0){
//                layoutAffiliation1.visibility = View.GONE
//                spAffiliation1.setBackgroundResource(R.drawable.bg_rectangle_white)
//            }
//
//        }
//    }
//
//    private fun showAffiliations(){
//        spAffiliation1.visibility = View.VISIBLE
////        btnAffiliation2.visibility = View.VISIBLE
//    }
//
//    private fun hideAffiliations(){
//        spAffiliation1.visibility = View.INVISIBLE
////        btnAffiliation2.visibility = View.INVISIBLE
//    }

    private fun addAffiliation(){
        var addAffiliationFields: View
        val container: LinearLayout = findViewById(R.id.llAddedAffiliations)
        toggleAffiliation.text = "Affiliation $fieldCount"
        toggleAffiliation.setOnClickListener {
            val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            addAffiliationFields = layoutInflater.inflate(R.layout.layout_on_added_affiliation, null)
            container.addView(addAffiliationFields, container.childCount)

            fieldCount++
            affiliationCounter++

            toggleAffiliation.text = "Affiliation $fieldCount"
            addAffiliationFields.btnAffiliation.text = "Affiliation $affiliationCounter"

            val index = container.indexOfChild(addAffiliationFields)

                btnAffiliation.setOnClickListener {

                    var uniqueId = container.id
                    counter++
                    val checker = counter % 2
                    if (checker == 1){
                        addAffiliationFields.showAffiliationLayout.visibility = View.VISIBLE
                        addAffiliationFields.rlAffiliation.setBackgroundResource(R.drawable.bg_transparent_orange_border_radius_8dp)
                    } else if (checker == 0){
                        addAffiliationFields.showAffiliationLayout.visibility = View.GONE
                        addAffiliationFields.rlAffiliation.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    }

            }

            val affiliationCount = container.childCount

            if (affiliationCount == 9){
                toggleAffiliation.visibility = View.GONE
            }


        }

    }

    private fun onSelectedAffiliation(){
//        val relativeLayout: RelativeLayout = findViewById(R.id.rlAffiliation)
//        relativeLayout.viewTreeObserver.addOnPreDrawListener( object: ViewTreeObserver.OnPreDrawListener {
//                override fun onPreDraw(): Boolean {
//                    relativeLayout.viewTreeObserver.removeOnPreDrawListener(this)
//                    relativeLayout.visibility = View.GONE
//                    return true
//                }
//            }
//        )
        btnAffiliation.setOnClickListener {
            if (affiliation.visibility == View.GONE){
//                expand(relativeLayout)
                affiliation.visibility = View.VISIBLE
            } else {
//                collapse(relativeLayout)
                affiliation.visibility = View.GONE
            }
        }
    }

    private fun expand(layout: RelativeLayout){
        layout.visibility = View.VISIBLE
    }
//
    private fun collapse(layout: RelativeLayout){
        layout.visibility = View.GONE
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
//                        hideAffiliations()
                    } else {
                        buttonNextDisable()
//                        showAffiliations()
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