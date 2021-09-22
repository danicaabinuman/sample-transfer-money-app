package com.unionbankph.corporate.payment_link.presentation.setup_business_information

import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.view.children
import androidx.lifecycle.Observer
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.DialogFactory
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.rmo.GetRMOBusinessInformationResponse
import com.unionbankph.corporate.payment_link.domain.model.rmo.RMOBusinessInformationForm
import com.unionbankph.corporate.payment_link.presentation.onboarding.upload_photos.OnboardingUploadPhotosActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents.CardAcceptanceUploadDocumentFragment
import com.unionbankph.corporate.databinding.ActivityBusinessInformationBinding

class BusinessInformationActivity :
    BaseActivity<ActivityBusinessInformationBinding,BusinessInformationViewModel>(),
    AdapterView.OnItemSelectedListener,
    CardAcceptanceUploadDocumentFragment.OnUploadDocs {

    var rmoBusinessInformationResponse: RMOBusinessInformationForm? = null
    private var info = GetRMOBusinessInformationResponse()
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
    var orderFulfillment =
        arrayOf(
            "Select",
            "Beyond 3 days",
            "Within 3 days",
            "Not Applicable"
        )

    var orderFulfilled = ""
    var business = "Wholesaler"
    var lazadaCounter = 0
    var shopeeCounter = 0
    var facebookCounter = 0
    var physicalStoreCounter = 0
    var instagramCounter = 0
    var websiteCounter = 0
    var suysingCounter = 0
    var otherCounter = 0
    var branchCounter = 0
    private var uploadDocumentFragment: CardAcceptanceUploadDocumentFragment? = null
    lateinit var imgView: ImageView

    private var fromWhatButton : String? = null

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

        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Loading -> showProgressAlertDialog(TAG)
                    is UiState.Complete -> dismissProgressAlertDialog()
                    is UiState.Error -> handleOnError(event.throwable)
                }
            }
        })

        viewModel.getRMOBusinessInformationResponse.observe(this, Observer {
            getBusinessInformationResponse(it)
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()

        initViews()
        natureOfBusiness()
        orderFulfillment()
        fromZeroCounter()
    }

    private fun initViews() {

        fromWhatButton = intent.getStringExtra(FROM_BUSINESS_INFO_BUTTON)
        if (fromWhatButton == null){
            fromWhatButton = FROM_BUSINESS_INFO
        } else {
            fromWhatButton = intent.getStringExtra(FROM_BUSINESS_INFO_BUTTON)

        }

        disableNextButton()
        firstScreenFieldChecker()
//        getBusinessInformation()

        binding.btnLazada.setOnClickListener { btnLazadaClicked() }
        binding.btnShopee.setOnClickListener { btnShopeeClicked() }
        binding.btnFacebook.setOnClickListener { btnFacebookClicked() }
        binding.btnPhysicalStore.setOnClickListener { btnPhysicalStoreClicked() }
        binding.btnInstagram.setOnClickListener { btnInstagramClicked() }
        binding.btnWebsite.setOnClickListener { btnWebsiteClicked() }
        binding.btnOthers.setOnClickListener { btnOtherClicked() }
        binding.btnIncrement.setOnClickListener { businessYearIncrementClicked() }
        binding.btnYearsDecrementActive.setOnClickListener { businessYearDecrementClicked() }
        binding.btnIncrementBranchNumber.setOnClickListener { branchCounterIncrementClicked() }
        binding.btnDecrementBranchNumberActive.setOnClickListener { branchCounterDecrementClicked() }
        binding.btnNext.setOnClickListener {
//            retrieveInformationFromFields()
            btnNextClicked()
        }
        binding.viewToolbar.btnSaveAndExit.setOnClickListener {
            putInformationFromFields()
            showDialogToDashboard()
        }
        binding.btnUploadBusinessPolicy.setOnClickListener {
            showUploadDocumentDialog()
        }
    }

    private fun getBusinessInformationResponse(response: GetRMOBusinessInformationResponse) {
//        rmoBusinessInformationResponse?.let {
//            viewModel.getBusinessInformation(
//                GetRMOBusinessInformationForm(
//                    it.businessType,
//                    it.storeProduct,
//                    it.infoStatus,
//                    it.yearsInBusiness,
//                    it.numberOfBranches,
//                    it.physicalStore,
//                    it.website,
//                    it.lazadaUrl,
//                    it.shopeeUrl,
//                    it.facebookUrl,
//                    it.instagramUrl,
//                    it.imageUrl1,
//                    it.imageUrl2,
//                    it.imageUrl3,
//                    it.imageUrl4,
//                    it.imageUrl5,
//                    it.imageUrl6
//                )
//            )
//        }
        displayBusinessInfo(response)
        this.info = response
    }

    private fun displayBusinessInfo(info: GetRMOBusinessInformationResponse) {
        val tvProductsAndServices: TextView = findViewById(R.id.tv_product_of_services_offered)
        val tvYearsOfBusiness: TextView = findViewById(R.id.tv_years_counter)

        tvProductsAndServices.text = info.merchantDetails!!.storeProduct.toString()
        tvYearsOfBusiness.text = info.merchantDetails!!.yearsInBusiness.toString()
    }

    private fun natureOfBusiness() {

//        val aa = ArrayAdapter(this, android.R.layout.simple_list_item_1, businessType)
//        aa.setDropDownViewResource(R.layout.spinner)
//
//        with(dropdownBusinessInformation) {
//            adapter = aa
//            setSelection(0, false)
//            onItemSelectedListener = this@BusinessInformationActivity
//            prompt = "SAMPLE PROMPT MESSAGE"
//            gravity = Gravity.CENTER
//        }

        binding.searchNatureOfBusiness.setOnClickListener {
            val intent = Intent(this, NatureOfBusinessActivity::class.java)
            startActivity(intent)
        }

        val bundle : Bundle? = intent.extras
        val selectedNatureOfBusiness = bundle?.getString("selected")
        binding.searchNatureOfBusiness.text = selectedNatureOfBusiness
    }

    private fun orderFulfillment() {

        val aa = ArrayAdapter(this, android.R.layout.simple_list_item_1, orderFulfillment)
        aa.setDropDownViewResource(R.layout.spinner)

        with(binding.dropdownOrderFulfillment) {
            adapter = aa
            setSelection(0, false)
            onItemSelectedListener = this@BusinessInformationActivity
            prompt = "SAMPLE PROMPT MESSAGE"
            gravity = Gravity.CENTER
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position > 0) {
            orderFulfilled = parent?.getItemAtPosition(position).toString()
        } else if (position == 0) {
            disableNextButton()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        val firstItem = parent?.getItemAtPosition(0)
        if (firstItem == true){
            disableNextButton()
        }
    }

    private fun fromZeroCounter() {

        if (binding.tvYearsCounter.text == "0") {
            binding.btnYearsDecrementActive.visibility = View.GONE
            binding.btnYearsDecrementInactive.visibility = View.VISIBLE
        } else {
            binding.btnYearsDecrementActive.visibility = View.VISIBLE
            binding.btnYearsDecrementInactive.visibility = View.GONE
        }

        if (binding.tvEmployeeCounter.text == "1") {
            binding.btnEmployeeDecrementActive.visibility = View.GONE
            binding.btnEmployeeDecrementInactive.visibility = View.VISIBLE
        } else {
            binding.btnEmployeeDecrementActive.visibility = View.VISIBLE
            binding.btnEmployeeDecrementInactive.visibility = View.GONE
        }

        if (binding.tvBranchCounter.text == "1") {
            binding.btnIncrementBranchNumber.visibility = View.GONE
            binding.btnDecrementBranchNumberInactive.visibility = View.VISIBLE
        } else {
            binding.btnIncrementBranchNumber.visibility = View.VISIBLE
            binding.btnDecrementBranchNumberInactive.visibility = View.GONE
        }
    }

    private fun businessYearIncrementClicked() {

        var yearCounter = binding.tvYearsCounter.text.toString().toInt()
        yearCounter++
        binding.tvYearsCounter.text = yearCounter.toString()

        fromZeroCounter()

    }

    private fun businessYearDecrementClicked() {

        var yearCounter = binding.tvYearsCounter.text.toString().toInt()
        yearCounter--
        binding.tvYearsCounter.text = yearCounter.toString()

        fromZeroCounter()

    }

    private fun branchCounterIncrementClicked() {
        var branchCounter = binding.tvBranchCounter.text.toString().toInt()
        branchCounter++
        binding.tvBranchCounter.text = branchCounter.toString()

        fromZeroCounter()
    }

    private fun branchCounterDecrementClicked() {
        var branchCounter = binding.tvBranchCounter.text.toString().toInt()
        branchCounter--
        binding.tvBranchCounter.text = branchCounter.toString()

        fromZeroCounter()
    }

    private fun employeeNumberIncrement() {
        var employeeCounter = binding.tvEmployeeCounter.text.toString().toInt()
        employeeCounter++
        binding.tvEmployeeCounter.text = employeeCounter.toString()

        fromZeroCounter()
    }

    private fun employeeNumberDecrement() {
        var employeeCounter = binding.tvEmployeeCounter.text.toString().toInt()
        employeeCounter--
        binding.tvEmployeeCounter.text = employeeCounter.toString()

        fromZeroCounter()
    }

    private fun btnLazadaClicked() {
        lazadaCounter++
        val stateChecker = lazadaCounter % 2
        if (stateChecker == 1) {
            binding.btnLazada.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnLazada.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreName.visibility = View.VISIBLE
            binding.dividerDashed.visibility = View.VISIBLE
            binding.lazada.root.visibility = View.VISIBLE
            disableNextButton()

            binding.lazada.etLazada.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
//                    enableNextButton()

                }

            })
        } else if (stateChecker == 0) {
            binding.btnLazada.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnLazada.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreName.visibility = View.GONE
            binding.view.visibility = View.INVISIBLE
            binding.lazada.root.visibility = View.GONE
//            if (et_lazada.text!!.isNotEmpty()) {
//                et_lazada.text!!.clear()
//                enableNextButton()
//            }
        }

    }

    private fun btnShopeeClicked() {
        shopeeCounter++
        val stateChecker = shopeeCounter % 2
        if (stateChecker == 1) {
            binding.btnShopee.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnShopee.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreNameShopee.visibility = View.VISIBLE
            binding.dividerDashed.visibility = View.VISIBLE
            binding.shopee.root.visibility = View.VISIBLE
            disableNextButton()

            binding.shopee.etShopee.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
//                    enableNextButton()

                }

            })

        } else if (stateChecker == 0) {
            binding.btnShopee.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnShopee.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameShopee.visibility = View.GONE
            binding.view.visibility = View.INVISIBLE
            binding.shopee.root.visibility = View.GONE
//            if (et_shopee.text!!.isNotEmpty()) {
//                et_shopee.text!!.clear()
//                enableNextButton()
//            }
        }
    }

    private fun btnFacebookClicked() {
        facebookCounter++
        val stateChecker = facebookCounter % 2
        if (stateChecker == 1) {
            binding.btnFacebook.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnFacebook.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreNameFacebook.visibility = View.VISIBLE
            binding.dividerDashed.visibility = View.VISIBLE
            binding.facebook.root.visibility = View.VISIBLE
            disableNextButton()

            binding.facebook.etFacebook.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
//                    enableNextButton()

                }

            })

        } else if (stateChecker == 0) {
            binding.btnFacebook.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnFacebook.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameFacebook.visibility = View.GONE
            binding.facebook.root.visibility = View.GONE
//            if (et_facebook.text!!.isNotEmpty()) {
//                et_facebook.text!!.clear()
//                enableNextButton()
//            }
        }
    }

    private fun btnPhysicalStoreClicked() {
        physicalStoreCounter++
        val stateChecker = physicalStoreCounter % 2
        if (stateChecker == 1) {
            binding.btnPhysicalStore.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnPhysicalStore.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreNamePhysicalStore.visibility = View.VISIBLE
            binding.dividerDashed.visibility = View.VISIBLE
            binding.physicalStore.root.visibility = View.VISIBLE
            addAnotherBranchAddress()
            disableNextButton()

            binding.physicalStore.etPhysicalStore.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
//                    enableNextButton()

                }

            })

        } else if (stateChecker == 0) {
            binding.btnPhysicalStore.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnPhysicalStore.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNamePhysicalStore.visibility = View.GONE
            binding.physicalStore.root.visibility = View.GONE
//            if (et_physical_store.text!!.isNotEmpty()) {
//                et_physical_store.text!!.clear()
//                enableNextButton()
//            }
        }
    }

    private fun btnInstagramClicked() {
        instagramCounter++
        val stateChecker = instagramCounter % 2
        if (stateChecker == 1) {
            binding.btnInstagram.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnInstagram.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreNameInstagram.visibility = View.VISIBLE
            binding.dividerDashed.visibility = View.VISIBLE
            binding.instagram.root.visibility = View.VISIBLE
            disableNextButton()

            binding.instagram.etInstagram.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
//                    enableNextButton()

                }

            })

        } else if (stateChecker == 0) {
            binding.btnInstagram.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnInstagram.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameInstagram.visibility = View.GONE
            binding.instagram.root.visibility = View.GONE
//            if (et_instagram.text!!.isNotEmpty()) {
//                et_instagram.text!!.clear()
//                enableNextButton()
//            }
        }
    }

    private fun btnWebsiteClicked() {
        websiteCounter++
        val stateChecker = websiteCounter % 2
        if (stateChecker == 1) {
            binding.btnWebsite.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnWebsite.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreNameWebsite.visibility = View.VISIBLE
            binding.dividerDashed.visibility = View.VISIBLE
            binding.website.root.visibility = View.VISIBLE
            disableNextButton()

            binding.website.etWebsite.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
//                    enableNextButton()

                }

            })

        } else if (stateChecker == 0) {
            binding.btnWebsite.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnWebsite.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameWebsite.visibility = View.GONE
            binding.website.root.visibility = View.GONE
//            if (et_website.text!!.isNotEmpty()) {
//                et_website.text!!.clear()
//                enableNextButton()
//            }
        }
    }

    private fun toggleSuysingButton() {
        suysingCounter++
        val stateChecker = suysingCounter % 2
        if (stateChecker == 1) {
            binding.btnSuysing.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnSuysing.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreNameSuysing.visibility = View.VISIBLE
            binding.dividerDashed.visibility = View.VISIBLE
            binding.suysing.root.visibility = View.VISIBLE

            binding.btnPhysicalStore.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnPhysicalStore.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreNamePhysicalStore.visibility = View.VISIBLE
            binding.physicalStore.root.visibility = View.VISIBLE
            addAnotherBranchAddress()

            disableNextButton()

            binding.suysing.etSuysing.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
//                    enableNextButton()

                }

            })

        } else if (stateChecker == 0) {
            binding.btnSuysing.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnSuysing.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameSuysing.visibility = View.GONE
            binding.suysing.root.visibility = View.GONE

            binding.btnPhysicalStore.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnPhysicalStore.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNamePhysicalStore.visibility = View.GONE
            binding.physicalStore.root.visibility = View.GONE
//            if (et_suysing.text!!.isNotEmpty()) {
//                et_suysing.text!!.clear()
//                enableNextButton()
//            }
        }
    }

    private fun btnOtherClicked() {
        otherCounter++
        val stateChecker = otherCounter % 2
        if (stateChecker == 1) {
            binding.btnOthers.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnOthers.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreNameOthers.visibility = View.VISIBLE
            binding.dividerDashed.visibility = View.VISIBLE
            binding.others.root.visibility = View.VISIBLE
            addAnotherStore()
            disableNextButton()

            binding.others.etOthers.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
//                    enableNextButton()

                }

            })

        } else if (stateChecker == 0) {
            binding.btnOthers.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnOthers.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameOthers.visibility = View.GONE
            binding.others.root.visibility = View.GONE
//            if (et_others.text!!.isNotEmpty()) {
//                et_others.text!!.clear()
//                enableNextButton()
//            }
        }
    }

    private fun putInformationFromFields() {
        val businessType = binding.searchNatureOfBusiness.text.toString()
        val storeProduct = binding.etProductOfServicesOffered.text.toString()
        val infoStatus = "draft"
        val yearsInBusiness = binding.tvYearsCounter.text.toString().toInt()
        val numberOfBranches = binding.tvBranchCounter.text.toString().toInt()
        val physicalStore = binding.physicalStore.etPhysicalStore.text.toString()
        val website = binding.website.etWebsite.text.toString()
        val lazadaUrl = binding.lazada.etLazada.text.toString()
        val shopeeUrl = binding.shopee.etShopee.text.toString()
        val facebookUrl = binding.facebook.etFacebook.text.toString()
        val instagramUrl = binding.instagram.etInstagram.text.toString()
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
        if (binding.llBusinessInformationFields1.isShown){
            if (fromWhatButton.equals(FROM_BUSINESS_INFO)){
                binding.llBusinessInformationFields1.visibility = View.GONE
                binding.scrollView2.visibility = View.VISIBLE
                disableNextButton()
            } else if (fromWhatButton.equals(ReviewAndSubmitActivity.EDIT_BUSINESS_INFO_BUTTON)){
                val intent = Intent(this, ReviewAndSubmitActivity::class.java)
                startActivity(intent)
            }

        } else if (binding.llBusinessInformationFields2.isShown){
            if (fromWhatButton.equals(FROM_BUSINESS_INFO)){
                val intent = Intent(this, OnboardingUploadPhotosActivity::class.java)
                startActivity(intent)
            }

        }

    }

    private fun navigateToUploadPhotos(response: RMOBusinessInformationForm) {
        val responseJson = JsonHelper.toJson(response)
        val intent = Intent(this, OnboardingUploadPhotosActivity::class.java)
        intent.putExtra(OnboardingUploadPhotosActivity.EXTRA_SETUP_MERCHANT_DETAILS, responseJson)
        startActivityForResult(intent, OnboardingUploadPhotosActivity.REQUEST_CODE)
    }

    private fun firstScreenFieldChecker() {
        binding.etProductOfServicesOffered.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length!! > 0) {
                    enableNextButton()
                } else {
                    disableNextButton()

                }

            }
        })
    }
    private fun enableNextButton() {
        binding.btnNext.isEnabled = true
    }

    private fun disableNextButton() {
        binding.btnNext.isEnabled = false
    }

    fun disableUploadDocs(view: View) {
        if (view is CheckBox){
            val checked: Boolean = view.isChecked

            when (view.id){
                R.id.cb_notApplicable -> {
                    if (checked) {
                        binding.btnUploadBusinessPolicy.isEnabled = false
                        binding.btnUploadBusinessPolicy.text.isEmpty()
                        enableNextButton()
                    } else {
                        binding.btnUploadBusinessPolicy.isEnabled = true
                        binding.btnUploadBusinessPolicy.text = "Upload Document"
                        disableNextButton()
                    }
                }
            }
        }

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

    private fun addAnotherBranchAddress() {
        var addBranchFields: View
        val container: LinearLayout = findViewById(R.id.branchContainer)
        binding.physicalStore.btnAddBranchAddress.setOnClickListener {
            val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            addBranchFields = layoutInflater.inflate(R.layout.layout_branch_textfields, null)
            container.addView(addBranchFields, container.childCount)

            val view = ActivityBusinessInformationBinding.inflate(LayoutInflater.from(context))
            val added = view.physicalStore.branchContainer.addView(addBranchFields, container.childCount)
            val branchCount = container.childCount
            if (branchCount <= 99){
                branchCounter++
//                addBranchFields.tv_branch_number.text = getString(R.string.branch) + " " + branchCounter
            } else if (branchCount == 99){
                binding.physicalStore.btnAddBranchAddress.visibility = View.GONE
            }
        }
    }

    private fun addAnotherStore() {
        var addOtherStoreFields: View
        val container: LinearLayout = findViewById(R.id.storeContainer)
        binding.others.btnAddAnotherStore.setOnClickListener {
            val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            addOtherStoreFields = layoutInflater.inflate(R.layout.layout_store_textfields, null)
            container.addView(addOtherStoreFields, container.childCount)

            val storeCount = container.childCount
            if (storeCount == 99){
                binding.others.btnAddAnotherStore.visibility = View.GONE
            }
        }
    }

    private fun hideLayout(){
        binding.viewToolbar.root.visibility = View.GONE
        binding.scrollView2.visibility = View.GONE
        binding.btnNext.visibility = View.GONE
    }

    private fun showLayout(){
        binding.viewToolbar.root.visibility = View.VISIBLE
        binding.scrollView2.visibility = View.VISIBLE
        binding.btnNext.visibility = View.VISIBLE
    }

    private fun showUploadDocumentDialog(){
        if (uploadDocumentFragment == null) {
            uploadDocumentFragment = CardAcceptanceUploadDocumentFragment.newInstance()
        }

        uploadDocumentFragment!!.show(supportFragmentManager, CardAcceptanceUploadDocumentFragment.TAG)
    }

    override fun openCamera() {

    }

    override fun openGallery() {
        selectImageFromGallery()
        uploadDocumentFragment?.dismiss()
    }

    override fun openFileManager() {
        selectPDFDocument()
        uploadDocumentFragment?.dismiss()
    }

    private fun selectPDFDocument(){
        if (Build.VERSION.SDK_INT < 19) {
            val intent = Intent()
            intent.type = "application/pdf"
            intent.action = Intent.ACTION_GET_CONTENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(
                Intent.createChooser(intent, "Choose file"),
                PDF_REQUEST_CODE
            )
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/pdf"
            startActivityForResult(intent, PDF_REQUEST_CODE)
        }
    }

    private fun selectImageFromGallery(){
        if (Build.VERSION.SDK_INT < 19) {
            var intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Choose pictures"), GALLERY_REQUEST_CODE
            )
        } else {
            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST_CODE
            )
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imgView = findViewById(R.id.ivPreviewBIR)
        when(requestCode){
            PDF_REQUEST_CODE ->
                if (resultCode == RESULT_OK){
                    if (data?.data != null){
                        val fileUri = data.data!!
                        val fileDescriptor : ParcelFileDescriptor = context.contentResolver.openFileDescriptor(fileUri,"r")!!
                        val fileType: String? = applicationContext.contentResolver.getType(fileUri)
                        if (fileType != DOCU_PDF){
                            DialogFactory().createSMEDialog(
                                this,
                                isNewDesign = false,
                                title = getString(R.string.item_not_uploaded),
                                description = getString(R.string.invalid_filetype_desc),
                                positiveButtonText = getString(R.string.action_try_again),
                                onPositiveButtonClicked = {
                                    imgView.setImageBitmap(null)
//                                    layoutVisibilityWhenInvalidFiles()
                                }
                            ).show()
                            val pdfRenderer = PdfRenderer(fileDescriptor)
                            val rendererPage = pdfRenderer.openPage(0)
                            val rendererPageWidth = rendererPage.width
                            val rendererPageHeight = rendererPage.height
                            val bitmap = Bitmap.createBitmap(rendererPageWidth, rendererPageHeight, Bitmap.Config.ARGB_8888)
                            rendererPage.render(bitmap,null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            imgView.setImageBitmap(bitmap)
                            rendererPage.close()
                            pdfRenderer.close()
                        }
                    }
                }
            GALLERY_REQUEST_CODE ->
                if (resultCode == RESULT_OK){
                    hideLayout()
                    binding.popupPreviewDocsFromGallery.visibility = View.VISIBLE

                    val imageUri = data?.data!!
                    val fileDescriptor: AssetFileDescriptor = applicationContext.contentResolver.openAssetFileDescriptor(imageUri, "r")!!
                    val fileType: String? = applicationContext.contentResolver.getType(imageUri)
                    val fileSize: Long = fileDescriptor.length
                    if (fileSize > MAX_FILESIZE_2MB){
                        DialogFactory().createSMEDialog(
                            this,
                            isNewDesign = false,
                            title = getString(R.string.item_not_uploaded),
                            description = getString(R.string.invalid_filesize_desc),
                            positiveButtonText = getString(R.string.action_try_again),
                            onPositiveButtonClicked = {
                                imgView.setImageBitmap(null)
//                                layoutVisibilityWhenInvalidFiles()
                            }
                        ).show()
                    }
                    if (fileType != IMAGE_JPEG && fileType != IMAGE_PNG){
                        DialogFactory().createSMEDialog(
                            this,
                            isNewDesign = false,
                            title = getString(R.string.item_not_uploaded),
                            description = getString(R.string.invalid_filetype_desc),
                            positiveButtonText = getString(R.string.action_try_again),
                            onPositiveButtonClicked = {
                                imgView.setImageBitmap(null)
//                                layoutVisibilityWhenInvalidFiles()
                            }
                        ).show()
                    }

                    imgView.setImageURI(imageUri)
                    binding.includePreviewGallery.btnNavigateBackToUploadDocs1.setOnClickListener {
                        binding.popupPreviewDocsFromGallery.visibility = View.GONE
                        showLayout()
                    }
                }
        }
    }

    override fun onBackPressed() {
        if (binding.scrollView2.isShown){
            binding.scrollView2.visibility = View.GONE
            binding.llBusinessInformationFields1.visibility = View.VISIBLE
            enableNextButton()
        } else if (binding.llBusinessInformationFields1.isShown){
            super.onBackPressed()
        }
    }

    companion object {
        var TAG = this::class.java.simpleName
        const val PDF_REQUEST_CODE = 200
        const val GALLERY_REQUEST_CODE = 201
        const val CAMERA_REQUEST_CODE = 202
        const val IMAGE_JPEG = "image/jpeg"
        const val IMAGE_PNG = "image/png"
        const val DOCU_PDF = "application/pdf"
        const val LIST_OF_IMAGES_URI = "list_of_image_uri"
        const val MAX_FILESIZE_2MB = 2097152

        const val FROM_BUSINESS_INFO_BUTTON = "from_business_info_button"
        const val FROM_BUSINESS_INFO = "from_business_info"
    }

    override val bindingInflater: (LayoutInflater) -> ActivityBusinessInformationBinding
        get() = ActivityBusinessInformationBinding::inflate
    override val viewModelClassType: Class<BusinessInformationViewModel>
        get() = BusinessInformationViewModel::class.java
}