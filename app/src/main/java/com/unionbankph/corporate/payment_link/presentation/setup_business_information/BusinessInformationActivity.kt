package com.unionbankph.corporate.payment_link.presentation.setup_business_information

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_business_information.*
import kotlinx.android.synthetic.main.activity_request_payment.*
import kotlinx.android.synthetic.main.activity_request_payment.dropdownPaymentLinkExport

class BusinessInformationActivity : BaseActivity<BusinessInformationViewModel>(R.layout.activity_business_information),
    AdapterView.OnItemSelectedListener {

    var businessType = arrayOf("Manufacturer", "Wholesaler", "Service", "Importer", "Exporter", "Retailer")
    var business = "Wholesaler"
    var lazadaCounter = 0
    var shopeeCounter = 0
    var facebookCounter = 0
    var physicalStoreCounter = 0
    var instagramCounter = 0
    var websiteCounter = 0
    var otherCounter = 0

    override fun onViewsBound() {
        super.onViewsBound()

        initViews()
        natureOfBusiness()
        fromZeroCounter()
    }

    private fun initViews(){

        btn_lazada.setOnClickListener{btnLazadaClicked()}
        btn_shopee.setOnClickListener{btnShopeeClicked()}
        btn_facebook.setOnClickListener{btnFacebookClicked()}
        btn_physical_store.setOnClickListener{btnPhysicalStoreClicked()}
        btn_instagram.setOnClickListener{btnInstagramClicked()}
        btn_website.setOnClickListener{btnWebsiteClicked()}
        btn_others.setOnClickListener {btnOtherClicked()}
        btn_increment.setOnClickListener { businessYearIncrementClicked() }
    }
    private fun natureOfBusiness(){

        var aa = ArrayAdapter(this, android.R.layout.simple_list_item_1, businessType)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        with(dropdownPaymentLinkExport){
            adapter = aa
            setSelection(1, false)
            onItemSelectedListener = this@BusinessInformationActivity
            prompt = "SAMPLE PROMPT MESSAGE"
            gravity = Gravity.CENTER
        }
    }

    private fun fromZeroCounter(){

        if (tv_years_counter.toString() == "0") {
            btn_years_decrement_active.visibility = View.GONE
            btn_years_decrement_inactive.visibility = View.VISIBLE
        } else {
            btn_years_decrement_active.visibility = View.VISIBLE
            btn_years_decrement_inactive.visibility = View.GONE
        }
    }

    private  fun businessYearIncrementClicked(){

        var yearCounter = tv_years_counter.toString().toInt()

        yearCounter++

        tv_years_counter.text = yearCounter.toString()

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        business = parent?.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun btnLazadaClicked(){
        lazadaCounter++
        var stateChecker = lazadaCounter%2
        if (stateChecker  == 1){
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

    private fun btnShopeeClicked(){
        shopeeCounter++
        var stateChecker = shopeeCounter%2
        if (stateChecker  == 1){
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
    private fun btnFacebookClicked(){
        facebookCounter++
        var stateChecker = facebookCounter%2
        if (stateChecker  == 1){
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
    private fun btnPhysicalStoreClicked(){
        physicalStoreCounter++
        var stateChecker = physicalStoreCounter%2
        if (stateChecker  == 1){
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
    private fun btnInstagramClicked(){
        instagramCounter++
        var stateChecker = instagramCounter%2
        if (stateChecker  == 1){
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
    private fun btnWebsiteClicked(){
        websiteCounter++
        var stateChecker = websiteCounter%2
        if (stateChecker  == 1){
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
}