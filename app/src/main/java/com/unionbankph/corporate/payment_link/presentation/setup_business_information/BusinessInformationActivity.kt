package com.unionbankph.corporate.payment_link.presentation.setup_business_information

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
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
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashActivity
import com.unionbankph.corporate.payment_link.presentation.setup_business_information.NatureOfBusinessActivity.Companion.SELECTED_NATURE_OF_BUSINESS

class BusinessInformationActivity :
    BaseActivity<ActivityBusinessInformationBinding, BusinessInformationViewModel>() {

    var rmoBusinessInformationResponse: RMOBusinessInformationForm? = null
    private var info = GetRMOBusinessInformationResponse()

    private var fromWhatButton: String? = null

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
        fromZeroCounter()
    }

    private fun initViews() {

        fromWhatButton = intent.getStringExtra(FROM_BUSINESS_INFO_BUTTON)
        if (fromWhatButton == null) {
            fromWhatButton = FROM_BUSINESS_INFO
        } else {
            fromWhatButton = intent.getStringExtra(FROM_BUSINESS_INFO_BUTTON)

        }

        disableNextButton()
        firstScreenFieldChecker()
//        getBusinessInformation()


        binding.btnIncrement.setOnClickListener { businessYearIncrementClicked() }
        binding.btnYearsDecrementActive.setOnClickListener { businessYearDecrementClicked() }
        binding.btnIncrementBranchNumber.setOnClickListener { branchCounterIncrementClicked() }
        binding.btnDecrementBranchNumberActive.setOnClickListener { branchCounterDecrementClicked() }
        binding.btnEmployeeIncrement.setOnClickListener { employeeNumberIncrement() }
        binding.btnEmployeeDecrementActive.setOnClickListener { employeeNumberDecrement() }
        binding.btnNext.setOnClickListener {
//            retrieveInformationFromFields()
            btnNextClicked()
        }
        binding.viewToolbar.btnSaveAndExit.setOnClickListener {
            putInformationFromFields()
            showDialogToDashboard()
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
        binding.searchNatureOfBusiness.setOnClickListener {
            val intent = Intent(this, NatureOfBusinessActivity::class.java)
            startActivity(intent)
        }

        val bundle: Bundle? = intent.extras
        val selectedNatureOfBusiness = bundle?.getString(SELECTED_NATURE_OF_BUSINESS)
        binding.searchNatureOfBusiness.text = selectedNatureOfBusiness
    }

    private fun firstScreenFieldChecker() {
        binding.etProductOfServicesOffered.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.searchNatureOfBusiness.text.isNotEmpty() && s?.length!! > 0) {
                    enableNextButton()
                } else {
                    disableNextButton()

                }

            }
        })
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
            binding.btnDecrementBranchNumberActive.visibility = View.GONE
            binding.btnDecrementBranchNumberInactive.visibility = View.VISIBLE
        } else {
            binding.btnDecrementBranchNumberActive.visibility = View.VISIBLE
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

    private fun putInformationFromFields() {
//        val businessType = binding.searchNatureOfBusiness.text.toString()
//        val storeProduct = binding.etProductOfServicesOffered.text.toString()
//        val infoStatus = "draft"
//        val yearsInBusiness = binding.tvYearsCounter.text.toString().toInt()
//        val numberOfBranches = binding.tvBranchCounter.text.toString().toInt()
//        val physicalStore = binding.physicalStore.etPhysicalStore.text.toString()
//        val website = binding.website.etWebsite.text.toString()
//        val lazadaUrl = binding.lazada.etLazada.text.toString()
//        val shopeeUrl = binding.shopee.etShopee.text.toString()
//        val facebookUrl = binding.facebook.etFacebook.text.toString()
//        val instagramUrl = binding.instagram.etInstagram.text.toString()
//        val imageUrl1 = "testimage.jpg"
//        val imageUrl2 = "testimage.jpg"
//        val imageUrl3 = "testimage.jpg"
//        val imageUrl4 = "testimage.jpg"
//        val imageUrl5 = "testimage.jpg"
//        val imageUrl6 = "testimage.jpg"
//
//
//        viewModel.submitBusinessInformation(
//            RMOBusinessInformationForm(
//                businessType,
//                storeProduct,
//                infoStatus,
//                yearsInBusiness,
//                numberOfBranches,
//                physicalStore,
//                website,
//                lazadaUrl,
//                shopeeUrl,
//                facebookUrl,
//                instagramUrl,
//                imageUrl1,
//                imageUrl2,
//                imageUrl3,
//                imageUrl4,
//                imageUrl5,
//                imageUrl6
//            )
//        )

    }

    private fun btnNextClicked() {
        if (fromWhatButton.equals(FROM_BUSINESS_INFO)) {
            hideKeyboard(this, binding.etProductOfServicesOffered)
            navigator.navigate(
                this,
                BusinessInformation2ndScreenActivity::class.java,
                null,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )

        } else if (fromWhatButton.equals(ReviewAndSubmitActivity.EDIT_BUSINESS_INFO_BUTTON)) {
            val intent = Intent(this, ReviewAndSubmitActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToUploadPhotos(response: RMOBusinessInformationForm) {
        val responseJson = JsonHelper.toJson(response)
        val intent = Intent(this, OnboardingUploadPhotosActivity::class.java)
        intent.putExtra(OnboardingUploadPhotosActivity.EXTRA_SETUP_MERCHANT_DETAILS, responseJson)
        startActivityForResult(intent, OnboardingUploadPhotosActivity.REQUEST_CODE)
    }


    private fun enableNextButton() {
        binding.btnNext.isEnabled = true
    }

    private fun disableNextButton() {
        binding.btnNext.isEnabled = false
    }

    private fun hideKeyboard(context: Context, view: View) {
        val hideKb = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        hideKb.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
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

        const val FROM_BUSINESS_INFO_BUTTON = "from_business_info_button"
        const val FROM_BUSINESS_INFO = "from_business_info"

        const val SHAREDPREF_IS_ONBOARDED = "sharedpref_is_onboarded"
    }

    override val bindingInflater: (LayoutInflater) -> ActivityBusinessInformationBinding
        get() = ActivityBusinessInformationBinding::inflate
    override val viewModelClassType: Class<BusinessInformationViewModel>
        get() = BusinessInformationViewModel::class.java
}