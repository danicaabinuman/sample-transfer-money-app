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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents.CardAcceptanceUploadDocumentsActivity
import kotlinx.android.synthetic.main.activity_business_information.*
import kotlinx.android.synthetic.main.activity_business_information.viewToolbar
import kotlinx.android.synthetic.main.activity_onboarding_upload_photos.*
import kotlinx.android.synthetic.main.activity_request_payment.*
import kotlinx.android.synthetic.main.item_store_facebook.*
import kotlinx.android.synthetic.main.item_store_instagram.*
import kotlinx.android.synthetic.main.item_store_lazada.*
import kotlinx.android.synthetic.main.item_store_others.*
import kotlinx.android.synthetic.main.item_store_physical.*
import kotlinx.android.synthetic.main.item_store_shopee.*
import kotlinx.android.synthetic.main.item_store_suysing.*
import kotlinx.android.synthetic.main.item_store_website.*
import kotlinx.android.synthetic.main.layout_branch_textfields.view.*
import kotlinx.android.synthetic.main.layout_gallery_preview.*
import kotlinx.android.synthetic.main.layout_store_chips.*
import kotlinx.android.synthetic.main.spinner.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.toolbar
import kotlinx.android.synthetic.main.widget_transparent_rmo_appbar.*

class BusinessInformationActivity :
    BaseActivity<BusinessInformationViewModel>(R.layout.activity_business_information),
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
            "Select"
        )

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
        disableNextButton()
        firstScreenFieldChecker()
//        getBusinessInformation()

        btn_lazada.setOnClickListener { btnLazadaClicked() }
        btn_shopee.setOnClickListener { btnShopeeClicked() }
        btn_facebook.setOnClickListener { btnFacebookClicked() }
        btn_physical_store.setOnClickListener { btnPhysicalStoreClicked() }
        btn_instagram.setOnClickListener { btnInstagramClicked() }
        btn_website.setOnClickListener { btnWebsiteClicked() }
        btn_suysing.setOnClickListener { toggleSuysingButton() }
        btn_others.setOnClickListener { btnOtherClicked() }
        btn_increment.setOnClickListener { businessYearIncrementClicked() }
        btn_years_decrement_active.setOnClickListener { businessYearDecrementClicked() }
        btn_increment_branch_number.setOnClickListener { branchCounterIncrementClicked() }
        btn_decrement_branch_number_active.setOnClickListener { branchCounterDecrementClicked() }
        btn_employee_increment.setOnClickListener { employeeNumberIncrement() }
        btn_employee_decrement_active.setOnClickListener { employeeNumberDecrement() }
        btn_next.setOnClickListener {
//            retrieveInformationFromFields()
            btnNextClicked()
        }
        btnSaveAndExit.setOnClickListener {
            retrieveInformationFromFields()
            showDialogToDashboard()
        }
        btnUploadBusinessPolicy.setOnClickListener {
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

    private fun orderFulfillment() {

        val aa = ArrayAdapter(this, android.R.layout.simple_list_item_1, orderFulfillment)
        aa.setDropDownViewResource(R.layout.spinner)

        with(dropdownOrderFulfillment) {
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
        } else if (position == 0) {
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

        if (tv_employee_counter.text == "1") {
            btn_employee_decrement_active.visibility = View.GONE
            btn_employee_decrement_inactive.visibility = View.VISIBLE
        } else {
            btn_employee_decrement_active.visibility = View.VISIBLE
            btn_employee_decrement_inactive.visibility = View.GONE
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

    private fun employeeNumberIncrement() {
        var employeeCounter = tv_employee_counter.text.toString().toInt()
        employeeCounter++
        tv_employee_counter.text = employeeCounter.toString()

        fromZeroCounter()
    }

    private fun employeeNumberDecrement() {
        var employeeCounter = tv_employee_counter.text.toString().toInt()
        employeeCounter--
        tv_employee_counter.text = employeeCounter.toString()

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
            lazada.visibility = View.VISIBLE
            disableNextButton()

            et_lazada.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    enableNextButton()

                }

            })
        } else if (stateChecker == 0) {
            btn_lazada.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_lazada.setTextColor(Color.parseColor("#4A4A4A"))
            tv_input_store_name.visibility = View.GONE
            view.visibility = View.INVISIBLE
            lazada.visibility = View.GONE
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
            btn_shopee.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_shopee.setTextColor(Color.parseColor("#FF8200"))
            tv_input_store_name_shopee.visibility = View.VISIBLE
            divider_dashed.visibility = View.VISIBLE
            shopee.visibility = View.VISIBLE
            disableNextButton()

            et_shopee.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    enableNextButton()

                }

            })

        } else if (stateChecker == 0) {
            btn_shopee.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_shopee.setTextColor(Color.parseColor("#4A4A4A"))
            tv_input_store_name_shopee.visibility = View.GONE
            view.visibility = View.INVISIBLE
            shopee.visibility = View.GONE
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
            btn_facebook.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_facebook.setTextColor(Color.parseColor("#FF8200"))
            tv_input_store_name_facebook.visibility = View.VISIBLE
            divider_dashed.visibility = View.VISIBLE
            facebook.visibility = View.VISIBLE
            disableNextButton()

            et_facebook.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    enableNextButton()

                }

            })

        } else if (stateChecker == 0) {
            btn_facebook.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_facebook.setTextColor(Color.parseColor("#4A4A4A"))
            tv_input_store_name_facebook.visibility = View.GONE
            facebook.visibility = View.GONE
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
            btn_physical_store.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_physical_store.setTextColor(Color.parseColor("#FF8200"))
            tv_input_store_name_physical_store.visibility = View.VISIBLE
            divider_dashed.visibility = View.VISIBLE
            physical_store.visibility = View.VISIBLE
            addAnotherBranchAddress()
            disableNextButton()

            et_physical_store.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    enableNextButton()

                }

            })

        } else if (stateChecker == 0) {
            btn_physical_store.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_physical_store.setTextColor(Color.parseColor("#4A4A4A"))
            tv_input_store_name_physical_store.visibility = View.GONE
            physical_store.visibility = View.GONE
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
            btn_instagram.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_instagram.setTextColor(Color.parseColor("#FF8200"))
            tv_input_store_name_instagram.visibility = View.VISIBLE
            divider_dashed.visibility = View.VISIBLE
            instagram.visibility = View.VISIBLE
            disableNextButton()

            et_instagram.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    enableNextButton()

                }

            })

        } else if (stateChecker == 0) {
            btn_instagram.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_instagram.setTextColor(Color.parseColor("#4A4A4A"))
            tv_input_store_name_instagram.visibility = View.GONE
            instagram.visibility = View.GONE
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
            btn_website.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_website.setTextColor(Color.parseColor("#FF8200"))
            tv_input_store_name_website.visibility = View.VISIBLE
            divider_dashed.visibility = View.VISIBLE
            website.visibility = View.VISIBLE
            disableNextButton()

            et_website.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    enableNextButton()

                }

            })

        } else if (stateChecker == 0) {
            btn_website.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_website.setTextColor(Color.parseColor("#4A4A4A"))
            tv_input_store_name_website.visibility = View.GONE
            website.visibility = View.GONE
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
            btn_suysing.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_suysing.setTextColor(Color.parseColor("#FF8200"))
            tv_input_store_name_suysing.visibility = View.VISIBLE
            divider_dashed.visibility = View.VISIBLE
            suysing.visibility = View.VISIBLE

            btn_physical_store.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_physical_store.setTextColor(Color.parseColor("#FF8200"))
            tv_input_store_name_physical_store.visibility = View.VISIBLE
            physical_store.visibility = View.VISIBLE
            addAnotherBranchAddress()

            disableNextButton()

            et_suysing.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    enableNextButton()

                }

            })

        } else if (stateChecker == 0) {
            btn_suysing.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_suysing.setTextColor(Color.parseColor("#4A4A4A"))
            tv_input_store_name_suysing.visibility = View.GONE
            suysing.visibility = View.GONE

            btn_physical_store.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_physical_store.setTextColor(Color.parseColor("#4A4A4A"))
            tv_input_store_name_physical_store.visibility = View.GONE
            physical_store.visibility = View.GONE
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
            btn_others.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            btn_others.setTextColor(Color.parseColor("#FF8200"))
            tv_input_store_name_others.visibility = View.VISIBLE
            divider_dashed.visibility = View.VISIBLE
            others.visibility = View.VISIBLE
            addAnotherStore()
            disableNextButton()

            et_others.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    enableNextButton()

                }

            })

        } else if (stateChecker == 0) {
            btn_others.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            btn_others.setTextColor(Color.parseColor("#4A4A4A"))
            tv_input_store_name_others.visibility = View.GONE
            others.visibility = View.GONE
//            if (et_others.text!!.isNotEmpty()) {
//                et_others.text!!.clear()
//                enableNextButton()
//            }
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
        if (llBusinessInformationFields1.isShown){
            llBusinessInformationFields1.visibility = View.GONE
            scrollView2.visibility = View.VISIBLE
            disableNextButton()
        } else if (llBusinessInformationFields2.isShown){
//            validation()
        }

    }

    private fun navigateToUploadPhotos(response: RMOBusinessInformationForm) {
        val responseJson = JsonHelper.toJson(response)
        val intent = Intent(this, OnboardingUploadPhotosActivity::class.java)
        intent.putExtra(OnboardingUploadPhotosActivity.EXTRA_SETUP_MERCHANT_DETAILS, responseJson)
        startActivityForResult(intent, OnboardingUploadPhotosActivity.REQUEST_CODE)
    }

    private fun firstScreenFieldChecker(){
        et_product_of_services_offered.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length!! > 0){
                    enableNextButton()
                } else {
                    disableNextButton()
                }
            }

        })
    }
    private fun enableNextButton() {
        btn_next?.isEnabled = true
    }

    private fun disableNextButton() {
        btn_next?.isEnabled = false
    }

    fun disableUploadDocs(view: View) {
        if (view is CheckBox){
            val checked: Boolean = view.isChecked

            when (view.id){
                R.id.cb_notApplicable -> {
                    btnUploadBusinessPolicy?.isEnabled = !checked
                    enableNextButton()
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
        btnAddBranchAddress.setOnClickListener {
            val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            addBranchFields = layoutInflater.inflate(R.layout.layout_branch_textfields, null)
            container.addView(addBranchFields, container.childCount)

            val branchCount = container.childCount
            if (branchCount <= 99){
                branchCounter++
                addBranchFields.tv_branch_number.text = getString(R.string.branch) + " " + branchCounter
            } else if (branchCount == 99){
                btnAddBranchAddress.visibility = View.GONE
            }
        }
    }

    private fun addAnotherStore() {
        var addOtherStoreFields: View
        val container: LinearLayout = findViewById(R.id.storeContainer)
        btnAddAnotherStore.setOnClickListener {
            val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            addOtherStoreFields = layoutInflater.inflate(R.layout.layout_store_textfields, null)
            container.addView(addOtherStoreFields, container.childCount)

            val storeCount = container.childCount
            if (storeCount == 99){
                btnAddAnotherStore.visibility = View.GONE
            }
        }
    }

    private fun hideLayout(){
        viewToolbar.visibility = View.GONE
        scrollView2.visibility = View.GONE
        btn_next.visibility = View.GONE
    }

    private fun showLayout(){
        viewToolbar.visibility = View.VISIBLE
        scrollView2.visibility = View.VISIBLE
        btn_next.visibility = View.VISIBLE
    }

    private fun showUploadDocumentDialog(){
        if (uploadDocumentFragment == null) {
            uploadDocumentFragment = CardAcceptanceUploadDocumentFragment.newInstance()
        }

        uploadDocumentFragment!!.show(supportFragmentManager, CardAcceptanceUploadDocumentFragment.TAG)
    }

    override fun openCamera() {
        TODO("Not yet implemented")
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
                    popupPreviewDocsFromGallery.visibility = View.VISIBLE

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
                    btnNavigateBackToUploadDocs1.setOnClickListener {
                        popupPreviewDocsFromGallery.visibility = View.GONE
                        showLayout()
                    }
                }
        }
    }

    override fun onBackPressed() {
        if (scrollView2.isShown){
            scrollView2.visibility = View.GONE
            llBusinessInformationFields1.visibility = View.VISIBLE
            enableNextButton()
        } else if (llBusinessInformationFields1.isShown){
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
    }
}