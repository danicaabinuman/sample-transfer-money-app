package com.unionbankph.corporate.payment_link.presentation.setup_business_information

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.payment_link.presentation.onboarding.OnboardingUploadPhotosActivity
import com.unionbankph.corporate.payment_link.domain.model.form.RMOBusinessInformationForm
import kotlinx.android.synthetic.main.activity_business_information.*
import kotlinx.android.synthetic.main.activity_business_information.viewToolbar
import kotlinx.android.synthetic.main.activity_onboarding_upload_photos.*
import kotlinx.android.synthetic.main.activity_request_payment.*
import kotlinx.android.synthetic.main.spinner.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*

class BusinessInformationActivity :
    BaseActivity<BusinessInformationViewModel>(R.layout.activity_business_information),
    AdapterView.OnItemSelectedListener {

    var businessType =
        arrayOf("Select", "Manufacturer", "Wholesaler", "Service", "Importer", "Exporter", "Retailer")
    var business = "Wholesaler"
    var lazadaCounter = 0
    var shopeeCounter = 0
    var facebookCounter = 0
    var physicalStoreCounter = 0
    var instagramCounter = 0
    var websiteCounter = 0
    var otherCounter = 0

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
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[BusinessInformationViewModel::class.java]
    }

    override fun onViewsBound() {
        super.onViewsBound()

        initViews()
        natureOfBusiness()
        fromZeroCounter()
    }

    private fun initViews() {
        disableNextButton()
        requiredFields()

        btn_lazada.setOnClickListener{btnLazadaClicked()}
        btn_shopee.setOnClickListener{btnShopeeClicked()}
        btn_facebook.setOnClickListener{btnFacebookClicked()}
        btn_physical_store.setOnClickListener{btnPhysicalStoreClicked()}
        btn_instagram.setOnClickListener{btnInstagramClicked()}
        btn_website.setOnClickListener{btnWebsiteClicked()}
        btn_others.setOnClickListener {btnOtherClicked()}
        btn_increment.setOnClickListener { businessYearIncrementClicked() }
        btn_years_decrement_active.setOnClickListener { businessYearDecrementClicked() }
        btn_increment_branch_number.setOnClickListener { branchCounterIncrementClicked() }
        btn_decrement_branch_number_active.setOnClickListener { branchCounterDecrementClicked() }
        btn_next.setOnClickListener {
            btnNextClicked()
//            validateForm()
        }
    }

    private fun natureOfBusiness() {

        var aa = ArrayAdapter(this, android.R.layout.simple_list_item_1, businessType)
        aa.setDropDownViewResource(R.layout.spinner)

        with(dropdownBusinessInformation) {
            adapter = aa
            setSelection(0, false)
            onItemSelectedListener = this@BusinessInformationActivity
            prompt = "SAMPLE PROMPT MESSAGE"
            gravity = Gravity.CENTER
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position > 0){
            business = parent?.getItemAtPosition(position).toString()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun fromZeroCounter(){

        if (tv_years_counter.text == "0") {
            btn_years_decrement_active.visibility = View.GONE
            btn_years_decrement_inactive.visibility = View.VISIBLE
        } else {
            btn_years_decrement_active.visibility = View.VISIBLE
            btn_years_decrement_inactive.visibility = View.GONE
        }

        if (tv_branch_counter.text == "0") {
            btn_decrement_branch_number_active.visibility = View.GONE
            btn_decrement_branch_number_inactive.visibility = View.VISIBLE
        } else {
            btn_decrement_branch_number_active.visibility = View.VISIBLE
            btn_decrement_branch_number_inactive.visibility = View.GONE
        }
    }

    private  fun businessYearIncrementClicked(){

        var yearCounter = tv_years_counter.text.toString().toInt()
        yearCounter++
        tv_years_counter.text = yearCounter.toString()

        fromZeroCounter()

    }

    private  fun businessYearDecrementClicked(){

        var yearCounter = tv_years_counter.text.toString().toInt()
        yearCounter--
        tv_years_counter.text = yearCounter.toString()

        fromZeroCounter()

    }

    private  fun branchCounterIncrementClicked(){
        var branchCounter = tv_branch_counter.text.toString().toInt()
        branchCounter++
        tv_branch_counter.text = branchCounter.toString()

        fromZeroCounter()
    }

    private  fun branchCounterDecrementClicked(){
        var branchCounter = tv_branch_counter.text.toString().toInt()
        branchCounter--
        tv_branch_counter.text = branchCounter.toString()

        fromZeroCounter()
    }

    private fun btnLazadaClicked() {
        lazadaCounter++
        var stateChecker = lazadaCounter%2
        if (stateChecker  == 1) {
            btn_lazada.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_lazada.setTextColor(Color.parseColor("#FF8200"))
            tv_lazada_title.visibility = View.VISIBLE
            til_lazada.visibility = View.VISIBLE
            et_lazada.visibility = View.VISIBLE
        } else if (stateChecker  == 0) {
            btn_lazada.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_lazada.setTextColor(Color.parseColor("#4A4A4A"))
            tv_lazada_title.visibility = View.GONE
            til_lazada.visibility = View.GONE
            et_lazada.visibility = View.GONE
        }

    }

    private fun btnShopeeClicked() {
        shopeeCounter++
        var stateChecker = shopeeCounter%2
        if (stateChecker  == 1) {
            btn_shopee.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_shopee.setTextColor(Color.parseColor("#FF8200"))
            tv_shopee_title.visibility = View.VISIBLE
            til_shopee.visibility = View.VISIBLE
            et_shopee.visibility = View.VISIBLE
        } else if (stateChecker  == 0) {
            btn_shopee.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_shopee.setTextColor(Color.parseColor("#4A4A4A"))
            tv_shopee_title.visibility = View.GONE
            til_shopee.visibility = View.GONE
            et_shopee.visibility = View.GONE
        }
    }

    private fun btnFacebookClicked() {
        facebookCounter++
        var stateChecker = facebookCounter%2
        if (stateChecker  == 1) {
            btn_facebook.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_facebook.setTextColor(Color.parseColor("#FF8200"))
            tv_facebook_title.visibility = View.VISIBLE
            til_facebook.visibility = View.VISIBLE
            et_facebook.visibility = View.VISIBLE
        } else if (stateChecker  == 0) {
            btn_facebook.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_facebook.setTextColor(Color.parseColor("#4A4A4A"))
            tv_facebook_title.visibility = View.GONE
            til_facebook.visibility = View.GONE
            et_facebook.visibility = View.GONE
        }
    }

    private fun btnPhysicalStoreClicked() {
        physicalStoreCounter++
        var stateChecker = physicalStoreCounter%2
        if (stateChecker  == 1) {
            btn_physical_store.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_physical_store.setTextColor(Color.parseColor("#FF8200"))
            tv_physical_store.visibility = View.VISIBLE
            til_physical_store.visibility = View.VISIBLE
            et_physical_store.visibility = View.VISIBLE
        } else if (stateChecker  == 0) {
            btn_physical_store.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_physical_store.setTextColor(Color.parseColor("#4A4A4A"))
            tv_physical_store.visibility = View.GONE
            til_physical_store.visibility = View.GONE
            et_physical_store.visibility = View.GONE
        }
    }

    private fun btnInstagramClicked() {
        instagramCounter++
        var stateChecker = instagramCounter%2
        if (stateChecker  == 1) {
            btn_instagram.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_instagram.setTextColor(Color.parseColor("#FF8200"))
            tv_instagram_title.visibility = View.VISIBLE
            til_instagram.visibility = View.VISIBLE
            et_instagram.visibility = View.VISIBLE
        } else if (stateChecker  == 0) {
            btn_instagram.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_instagram.setTextColor(Color.parseColor("#4A4A4A"))
            tv_instagram_title.visibility = View.GONE
            til_instagram.visibility = View.GONE
            et_instagram.visibility = View.GONE
        }
    }

    private fun btnWebsiteClicked() {
        websiteCounter++
        var stateChecker = websiteCounter%2
        if (stateChecker  == 1) {
            btn_website.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_website.setTextColor(Color.parseColor("#FF8200"))
            tv_website_title.visibility = View.VISIBLE
            til_website.visibility = View.VISIBLE
            til_website.visibility = View.VISIBLE
        } else if (stateChecker  == 0) {
            btn_website.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_website.setTextColor(Color.parseColor("#4A4A4A"))
            tv_website_title.visibility = View.GONE
            til_website.visibility = View.GONE
            et_website.visibility = View.GONE
        }
    }

    private fun btnOtherClicked(){
        otherCounter++
        var stateChecker = otherCounter%2
        if (stateChecker  == 1){
            btn_others.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_others.setTextColor(Color.parseColor("#FF8200"))

            tv_others_title.visibility = View.VISIBLE
            til_others.visibility = View.VISIBLE
            et_others.visibility = View.VISIBLE
        } else if (stateChecker  == 0) {
            btn_others.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_others.setTextColor(Color.parseColor("#4A4A4A"))

            tv_others_title.visibility = View.GONE
            til_others.visibility = View.GONE
            et_others.visibility = View.GONE
        }
    }

    private fun btnNextClicked() {

        val businessType = business.toString()
        val storeProduct = et_product_of_services_offered.text.toString()
        val yearsInBusiness = tv_years_counter.text.toString().toInt()
        val numberOfBranches = tv_branch_counter.text.toString().toInt()
        val physicalStore = et_physical_store.text.toString()
        val website = et_website.text.toString()
        val lazadaUrl = et_lazada.text.toString()
        val shopeeUrl = et_shopee.text.toString()
        val facebookUrl = et_facebook.text.toString()
        val instagramUrl = et_instagram.text.toString()
        val imageUrl1 = "0"
        val imageUrl2 = "0"
        val imageUrl3 = "0"
        val imageUrl4 = "0"
        val imageUrl5 = "0"
        val imageUrl6 = "0"


        viewModel.submitBusinessInformation(
            RMOBusinessInformationForm(
                businessType!!,
                storeProduct,
                yearsInBusiness,
                numberOfBranches,
                physicalStore,
                website,
                lazadaUrl,
                shopeeUrl,
                facebookUrl,
                instagramUrl,
                imageUrl1,
                imageUrl2,
                imageUrl3,
                imageUrl4,
                imageUrl5,
                imageUrl6
            )
        )

        val intent = Intent(this, OnboardingUploadPhotosActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToUploadPhotos(response: RMOBusinessInformationForm){
        val responseJson = JsonHelper.toJson(response)
        val intent = Intent(this, OnboardingUploadPhotosActivity::class.java)
        intent.putExtra(OnboardingUploadPhotosActivity.EXTRA_SETUP_MERCHANT_DETAILS, responseJson)
        startActivityForResult(intent, OnboardingUploadPhotosActivity.REQUEST_CODE)
    }

    private fun disableNextButton(){
        btn_next?.isEnabled = false
    }

    private fun enableNextButton(){
        btn_next?.isEnabled = true
    }

    private fun validateForm(){
        val productsOrServicesOffered = et_product_of_services_offered.text.toString()
        val numberOfBranches = tv_branch_counter.text

        if (productsOrServicesOffered.isNotEmpty()){
            enableNextButton()
        } else {
            disableNextButton()
        }
    }

    private fun requiredFields(){
        et_product_of_services_offered.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }
}