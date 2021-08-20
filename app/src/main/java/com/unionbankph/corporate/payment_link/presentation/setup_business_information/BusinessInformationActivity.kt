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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.DialogFactory
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.rmo.GetRMOBusinessInformationForm
import com.unionbankph.corporate.payment_link.presentation.onboarding.OnboardingUploadPhotosActivity
import com.unionbankph.corporate.payment_link.domain.model.rmo.RMOBusinessInformationForm
import kotlinx.android.synthetic.main.activity_business_information.*
import kotlinx.android.synthetic.main.activity_business_information.viewToolbar
import kotlinx.android.synthetic.main.activity_onboarding_upload_photos.*
import kotlinx.android.synthetic.main.activity_request_payment.*
import kotlinx.android.synthetic.main.spinner.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.toolbar
import kotlinx.android.synthetic.main.widget_transparent_rmo_appbar.*

class BusinessInformationActivity :
    BaseActivity<BusinessInformationViewModel>(R.layout.activity_business_information),
    AdapterView.OnItemSelectedListener {

    var rmoBusinessInformationResponse: RMOBusinessInformationForm? = null
    var businessType =
        arrayOf(
            "Select",
            "Manufacturer",
            "Wholesaler",
            "Service",
            "Importer",
            "Exporter",
            "Retailer",
            "Others"
        )
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

        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Loading -> showProgressAlertDialog(TAG)
                    is UiState.Complete -> dismissProgressAlertDialog()
                    is UiState.Error -> handleOnError(event.throwable)
                }
            }
        })

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
        getBusinessInformation()

        btn_lazada.setOnClickListener { btnLazadaClicked() }
        btn_shopee.setOnClickListener { btnShopeeClicked() }
        btn_facebook.setOnClickListener { btnFacebookClicked() }
        btn_physical_store.setOnClickListener { btnPhysicalStoreClicked() }
        btn_instagram.setOnClickListener { btnInstagramClicked() }
        btn_website.setOnClickListener { btnWebsiteClicked() }
        btn_others.setOnClickListener { btnOtherClicked() }
        btn_increment.setOnClickListener { businessYearIncrementClicked() }
        btn_years_decrement_active.setOnClickListener { businessYearDecrementClicked() }
        btn_increment_branch_number.setOnClickListener { branchCounterIncrementClicked() }
        btn_decrement_branch_number_active.setOnClickListener { branchCounterDecrementClicked() }
        btn_next.setOnClickListener {
//            retrieveInformationFromFields()
            btnNextClicked()
        }
        btnSaveAndExit.setOnClickListener {
            retrieveInformationFromFields()
            showDialogToDashboard()
        }
    }

    private fun getBusinessInformation(){
        rmoBusinessInformationResponse?.let {
            viewModel.getBusinessInformation(
                GetRMOBusinessInformationForm(
                    it.businessType,
                    it.storeProduct,
                    it.infoStatus,
                    it.yearsInBusiness,
                    it.numberOfBranches,
                    it.physicalStore,
                    it.website,
                    it.lazadaUrl,
                    it.shopeeUrl,
                    it.facebookUrl,
                    it.instagramUrl,
                    it.imageUrl1,
                    it.imageUrl2,
                    it.imageUrl3,
                    it.imageUrl4,
                    it.imageUrl5,
                    it.imageUrl6
                )
            )
        }
    }
    private fun natureOfBusiness() {

        val aa = ArrayAdapter(this, android.R.layout.simple_list_item_1, businessType)
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
        if (position > 0) {
            business = parent?.getItemAtPosition(position).toString()
        } else if (position == 0){
            disableNextButton()
        }

        if (position == 7) {
            til_others_pls_specify.visibility = View.VISIBLE
        } else {
            til_others_pls_specify.visibility = View.GONE
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        disableNextButton()
    }

    private fun fromZeroCounter() {

        if (tv_years_counter.text == "0") {
            btn_years_decrement_active.visibility = View.GONE
            btn_years_decrement_inactive.visibility = View.VISIBLE
        } else {
            btn_years_decrement_active.visibility = View.VISIBLE
            btn_years_decrement_inactive.visibility = View.GONE
        }

        if (tv_branch_counter.text == "1") {
            btn_decrement_branch_number_active.visibility = View.GONE
            btn_decrement_branch_number_inactive.visibility = View.VISIBLE
        } else {
            btn_decrement_branch_number_active.visibility = View.VISIBLE
            btn_decrement_branch_number_inactive.visibility = View.GONE
        }
    }

    private fun businessYearIncrementClicked() {

        var yearCounter = tv_years_counter.text.toString().toInt()
        yearCounter++
        tv_years_counter.text = yearCounter.toString()

        fromZeroCounter()

    }

    private fun businessYearDecrementClicked() {

        var yearCounter = tv_years_counter.text.toString().toInt()
        yearCounter--
        tv_years_counter.text = yearCounter.toString()

        fromZeroCounter()

    }

    private fun branchCounterIncrementClicked() {
        var branchCounter = tv_branch_counter.text.toString().toInt()
        branchCounter++
        tv_branch_counter.text = branchCounter.toString()

        fromZeroCounter()
    }

    private fun branchCounterDecrementClicked() {
        var branchCounter = tv_branch_counter.text.toString().toInt()
        branchCounter--
        tv_branch_counter.text = branchCounter.toString()

        fromZeroCounter()
    }

    private fun btnLazadaClicked() {
        lazadaCounter++
        val stateChecker = lazadaCounter % 2
        if (stateChecker == 1) {
            btn_lazada.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_lazada.setTextColor(Color.parseColor("#FF8200"))
            tv_input_store_name.visibility = View.VISIBLE
            divider_dashed.visibility = View.VISIBLE
            tv_lazada_title.visibility = View.VISIBLE
            til_lazada.visibility = View.VISIBLE
            et_lazada.visibility = View.VISIBLE
            disableNextButton()
        } else if (stateChecker == 0) {
            btn_lazada.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_lazada.setTextColor(Color.parseColor("#4A4A4A"))
            tv_input_store_name.visibility = View.INVISIBLE
            tv_lazada_title.visibility = View.GONE
            til_lazada.visibility = View.GONE
            et_lazada.visibility = View.GONE
            if (et_lazada.text!!.isNotEmpty()){
                et_lazada.text!!.clear()
                enableNextButton()
            }
        }

    }

    private fun btnShopeeClicked() {
        shopeeCounter++
        val stateChecker = shopeeCounter % 2
        if (stateChecker == 1) {
            btn_shopee.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_shopee.setTextColor(Color.parseColor("#FF8200"))
            tv_input_store_name_shopee.visibility = View.VISIBLE
            divider_dashed.visibility = View.VISIBLE
            tv_shopee_title.visibility = View.VISIBLE
            til_shopee.visibility = View.VISIBLE
            et_shopee.visibility = View.VISIBLE
            disableNextButton()
        } else if (stateChecker == 0) {
            btn_shopee.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_shopee.setTextColor(Color.parseColor("#4A4A4A"))
            tv_input_store_name_shopee.visibility = View.INVISIBLE
            tv_shopee_title.visibility = View.GONE
            til_shopee.visibility = View.GONE
            et_shopee.visibility = View.GONE
            if (et_shopee.text!!.isNotEmpty()){
                et_shopee.text!!.clear()
                enableNextButton()
            }
        }
    }

    private fun btnFacebookClicked() {
        facebookCounter++
        val stateChecker = facebookCounter % 2
        if (stateChecker == 1) {
            btn_facebook.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_facebook.setTextColor(Color.parseColor("#FF8200"))
            tv_input_store_name_facebook.visibility = View.VISIBLE
            divider_dashed.visibility = View.VISIBLE
            tv_facebook_title.visibility = View.VISIBLE
            til_facebook.visibility = View.VISIBLE
            et_facebook.visibility = View.VISIBLE
            disableNextButton()
        } else if (stateChecker == 0) {
            btn_facebook.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_facebook.setTextColor(Color.parseColor("#4A4A4A"))
            tv_input_store_name_facebook.visibility = View.INVISIBLE
            tv_facebook_title.visibility = View.GONE
            til_facebook.visibility = View.GONE
            et_facebook.visibility = View.GONE
            if (et_facebook.text!!.isNotEmpty()){
                et_facebook.text!!.clear()
                enableNextButton()
            }
        }
    }

    private fun btnPhysicalStoreClicked() {
        physicalStoreCounter++
        val stateChecker = physicalStoreCounter % 2
        if (stateChecker == 1) {
            btn_physical_store.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_physical_store.setTextColor(Color.parseColor("#FF8200"))
            tv_input_store_name.visibility = View.VISIBLE
            tv_input_store_name_physical_store.visibility = View.VISIBLE
            divider_dashed.visibility = View.VISIBLE
            tv_physical_store.visibility = View.VISIBLE
            til_physical_store.visibility = View.VISIBLE
            et_physical_store.visibility = View.VISIBLE
            disableNextButton()
        } else if (stateChecker == 0) {
            btn_physical_store.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_physical_store.setTextColor(Color.parseColor("#4A4A4A"))
            tv_input_store_name_physical_store.visibility = View.GONE
            tv_physical_store.visibility = View.GONE
            til_physical_store.visibility = View.GONE
            et_physical_store.visibility = View.GONE
            if (et_physical_store.text!!.isNotEmpty()){
                et_physical_store.text!!.clear()
                enableNextButton()
            }
        }
    }

    private fun btnInstagramClicked() {
        instagramCounter++
        val stateChecker = instagramCounter % 2
        if (stateChecker == 1) {
            btn_instagram.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_instagram.setTextColor(Color.parseColor("#FF8200"))
            tv_input_store_name_instagram.visibility = View.VISIBLE
            divider_dashed.visibility = View.VISIBLE
            tv_instagram_title.visibility = View.VISIBLE
            til_instagram.visibility = View.VISIBLE
            et_instagram.visibility = View.VISIBLE
            disableNextButton()
        } else if (stateChecker == 0) {
            btn_instagram.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_instagram.setTextColor(Color.parseColor("#4A4A4A"))
            tv_input_store_name_instagram.visibility = View.INVISIBLE
            tv_instagram_title.visibility = View.GONE
            til_instagram.visibility = View.GONE
            et_instagram.visibility = View.GONE
            if (et_instagram.text!!.isNotEmpty()){
                et_instagram.text!!.clear()
                enableNextButton()
            }
        }
    }

    private fun btnWebsiteClicked() {
        websiteCounter++
        val stateChecker = websiteCounter % 2
        if (stateChecker == 1) {
            btn_website.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_website.setTextColor(Color.parseColor("#FF8200"))
            tv_input_store_name_website.visibility = View.VISIBLE
            divider_dashed.visibility = View.VISIBLE
            tv_website_title.visibility = View.VISIBLE
            til_website.visibility = View.VISIBLE
            til_website.visibility = View.VISIBLE
            disableNextButton()
        } else if (stateChecker == 0) {
            btn_website.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_website.setTextColor(Color.parseColor("#4A4A4A"))
            tv_input_store_name_website.visibility = View.INVISIBLE
            tv_website_title.visibility = View.GONE
            til_website.visibility = View.GONE
            et_website.visibility = View.GONE
            if (et_website.text!!.isNotEmpty()){
                et_website.text!!.clear()
                enableNextButton()
            }
        }
    }

    private fun btnOtherClicked() {
        otherCounter++
        val stateChecker = otherCounter % 2
        if (stateChecker == 1) {
            btn_others.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_others.setTextColor(Color.parseColor("#FF8200"))
            tv_input_store_name_others.visibility = View.VISIBLE
            divider_dashed.visibility = View.VISIBLE
            tv_others_title.visibility = View.VISIBLE
            til_others.visibility = View.VISIBLE
            et_others.visibility = View.VISIBLE
            disableNextButton()
        } else if (stateChecker == 0) {
            btn_others.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_others.setTextColor(Color.parseColor("#4A4A4A"))
            tv_input_store_name_others.visibility = View.INVISIBLE
            tv_others_title.visibility = View.GONE
            til_others.visibility = View.GONE
            et_others.visibility = View.GONE
            if (et_others.text!!.isNotEmpty()){
                et_others.text!!.clear()
                enableNextButton()
            }
        }
    }

    private fun retrieveInformationFromFields() {
        val businessType = business
        val storeProduct = et_product_of_services_offered.text.toString()
        val infoStatus = "draft"
        val yearsInBusiness = tv_years_counter.text.toString().toInt()
        val numberOfBranches = tv_branch_counter.text.toString().toInt()
        val physicalStore = et_physical_store.text.toString()
        val website = et_website.text.toString()
        val lazadaUrl = et_lazada.text.toString()
        val shopeeUrl = et_shopee.text.toString()
        val facebookUrl = et_facebook.text.toString()
        val instagramUrl = et_instagram.text.toString()
        val imageUrl1 = "testimage.jpg"
        val imageUrl2 = "testimage.jpg"
        val imageUrl3 = "testimage.jpg"
        val imageUrl4 = "testimage.jpg"
        val imageUrl5 = "testimage.jpg"
        val imageUrl6 = "testimage.jpg"


        viewModel.submitBusinessInformation(
            RMOBusinessInformationForm(
                businessType,
                storeProduct,
                infoStatus,
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

    private fun btnNextClicked() {
        val intent = Intent(this, OnboardingUploadPhotosActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToUploadPhotos(response: RMOBusinessInformationForm) {
        val responseJson = JsonHelper.toJson(response)
        val intent = Intent(this, OnboardingUploadPhotosActivity::class.java)
        intent.putExtra(OnboardingUploadPhotosActivity.EXTRA_SETUP_MERCHANT_DETAILS, responseJson)
        startActivityForResult(intent, OnboardingUploadPhotosActivity.REQUEST_CODE)
    }

    private fun enableNextButton() {
        btn_next?.isEnabled = true
    }

    private fun disableNextButton() {
        btn_next?.isEnabled = false
    }

    private fun requiredFields() {
        et_product_of_services_offered.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                val servicesOffered = s?.length
//                if (servicesOffered == 0){
//                    disableNextButton()
//                } else {
//                    enableNextButton()
//                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        et_lazada.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val lazadaLink = s?.length
                if (lazadaLink == 0) {
                    disableNextButton()
                } else {
                    enableNextButton()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        et_shopee.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val shopeeLink = s?.length
                if (shopeeLink == 0) {
                    disableNextButton()
                } else {
                    enableNextButton()
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        et_facebook.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val facebookLink = s?.length
                if (facebookLink == 0) {
                    disableNextButton()
                } else {
                    enableNextButton()
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        et_instagram.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val instagramLink = s?.length
                if (instagramLink == 0) {
                    disableNextButton()
                } else {
                    enableNextButton()
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        et_physical_store.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val physicalStore = s?.length
                if (physicalStore == 0) {
                    disableNextButton()
                } else {
                    enableNextButton()
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        et_website.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val websiteLink = s?.length
                if (websiteLink == 0) {
                    disableNextButton()
                } else {
                    enableNextButton()
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

    }

    private fun showDialogToDashboard() {
        DialogFactory().createSMEDialog(
            this,
            isNewDesign = false,
            title = getString(R.string.progress_saved),
            iconResource = R.drawable.ic_money_box,
            description = getString(R.string.progress_saved),
            positiveButtonText = getString(R.string.btn_back_to_dashboard),
            onPositiveButtonClicked = {
                navigator.navigate(
                    this,
                    DashboardActivity::class.java,
                    null,
                    isClear = false,
                    isAnimated = true,
                    transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_RIGHT
                )
            }
        ).show()
    }

    companion object {
        var TAG = this::class.java.simpleName
    }
}