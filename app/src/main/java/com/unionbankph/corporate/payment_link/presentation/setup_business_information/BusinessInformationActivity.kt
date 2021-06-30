package com.unionbankph.corporate.payment_link.presentation.setup_business_information

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.payment_link.domain.model.form.RMOBusinessInformationForm
import kotlinx.android.synthetic.main.activity_business_information.*
import kotlinx.android.synthetic.main.activity_request_payment.*

class BusinessInformationActivity :
    BaseActivity<BusinessInformationViewModel>(R.layout.activity_business_information),
    AdapterView.OnItemSelectedListener {

    var businessType =
        arrayOf("Manufacturer", "Wholesaler", "Service", "Importer", "Exporter", "Retailer")
    var business = "Wholesaler"
    var lazadaCounter = 0
    var shopeeCounter = 0
    var facebookCounter = 0
    var physicalStoreCounter = 0
    var instagramCounter = 0
    var websiteCounter = 0

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
    }

    private fun initViews() {

        btn_lazada.setOnClickListener { btnLazadaClicked() }
        btn_shopee.setOnClickListener { btnShopeeClicked() }
        btn_facebook.setOnClickListener { btnFacebookClicked() }
        btn_physical_store.setOnClickListener { btnPhysicalStoreClicked() }
        btn_instagram.setOnClickListener { btnInstagramClicked() }
        btn_website.setOnClickListener { btnWebsiteClicked() }
        btn_next.setOnClickListener { btnNextClicked() }
    }

    private fun natureOfBusiness() {

        var aa = ArrayAdapter(this, android.R.layout.simple_list_item_1, businessType)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        with(dropdownPaymentLinkExport) {
            adapter = aa
            setSelection(1, false)
            onItemSelectedListener = this@BusinessInformationActivity
            prompt = "SAMPLE PROMPT MESSAGE"
            gravity = Gravity.CENTER
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        business = parent?.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun btnLazadaClicked() {
        lazadaCounter++
        var stateChecker = lazadaCounter % 2
        if (stateChecker == 1) {
            btn_lazada.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_lazada.setTextColor(Color.parseColor("#FF8200"))
        } else if (stateChecker == 0) {
            btn_lazada.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_lazada.setTextColor(Color.parseColor("#4A4A4A"))
        }

    }

    private fun btnShopeeClicked() {
        shopeeCounter++
        var stateChecker = shopeeCounter % 2
        if (stateChecker == 1) {
            btn_shopee.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_shopee.setTextColor(Color.parseColor("#FF8200"))
        } else if (stateChecker == 0) {
            btn_shopee.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_shopee.setTextColor(Color.parseColor("#4A4A4A"))
        }
    }

    private fun btnFacebookClicked() {
        facebookCounter++
        var stateChecker = facebookCounter % 2
        if (stateChecker == 1) {
            btn_facebook.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_facebook.setTextColor(Color.parseColor("#FF8200"))
        } else if (stateChecker == 0) {
            btn_facebook.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_facebook.setTextColor(Color.parseColor("#4A4A4A"))
        }
    }

    private fun btnPhysicalStoreClicked() {
        physicalStoreCounter++
        var stateChecker = physicalStoreCounter % 2
        if (stateChecker == 1) {
            btn_physical_store.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_physical_store.setTextColor(Color.parseColor("#FF8200"))
        } else if (stateChecker == 0) {
            btn_physical_store.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_physical_store.setTextColor(Color.parseColor("#4A4A4A"))
        }
    }

    private fun btnInstagramClicked() {
        instagramCounter++
        var stateChecker = instagramCounter % 2
        if (stateChecker == 1) {
            btn_instagram.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_instagram.setTextColor(Color.parseColor("#FF8200"))
        } else if (stateChecker == 0) {
            btn_instagram.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_instagram.setTextColor(Color.parseColor("#4A4A4A"))
        }
    }

    private fun btnWebsiteClicked() {
        websiteCounter++
        var stateChecker = websiteCounter % 2
        if (stateChecker == 1) {
            btn_website.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_website.setTextColor(Color.parseColor("#FF8200"))
        } else if (stateChecker == 0) {
            btn_website.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_website.setTextColor(Color.parseColor("#4A4A4A"))
        }
    }

    private fun btnNextClicked() {

        val businessType = business.toString()
        val storeProduct = et_product_of_services_offered.text.toString()
        val yearsInBusiness = tv_business_years.text.toString().toInt()
        val numberOfBranches = tv_number_of_branch.text.toString().toInt()
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
    }

}