package com.unionbankph.corporate.payment_link.presentation.setup_business_information.business_information_forms

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.DialogFactory
import com.unionbankph.corporate.app.common.widget.edittext.autoformat.AutoFormatEditText
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.rmo.GetRMOBusinessInformationResponse
import com.unionbankph.corporate.payment_link.domain.model.rmo.RMOBusinessInformationForm
import com.unionbankph.corporate.payment_link.presentation.onboarding.upload_photos.OnboardingUploadPhotosActivity
import com.unionbankph.corporate.databinding.ActivityBusinessInformationBinding
import com.unionbankph.corporate.payment_link.presentation.setup_business_information.nature_of_business.NatureOfBusinessActivity
import com.unionbankph.corporate.payment_link.presentation.setup_business_information.nature_of_business.NatureOfBusinessActivity.Companion.SELECTED_NATURE_OF_BUSINESS
import com.unionbankph.corporate.payment_link.presentation.setup_business_information.review_and_submit.ReviewAndSubmitActivity

class BusinessInformationActivity :
    BaseActivity<ActivityBusinessInformationBinding, BusinessInformationViewModel>() {

    var rmoBusinessInformationResponse: RMOBusinessInformationForm? = null
    private var info = GetRMOBusinessInformationResponse()

    private var fromWhatButton: String? = null

    private lateinit var editTextYears : AutoFormatEditText
    private lateinit var editTextEmployee : AutoFormatEditText
    private lateinit var editTextBranch : AutoFormatEditText
    private lateinit var buttonAddYears : ImageButton
    private lateinit var buttonMinusYears : ImageButton
    private lateinit var buttonAddEmployee : ImageButton
    private lateinit var buttonMinusEmployee : ImageButton
    private lateinit var buttonAddBranch : ImageButton
    private lateinit var buttonMinusBranch : ImageButton

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
    }

    private fun initViews() {

        disableNextButton()
        firstScreenFieldChecker()
        yearsInBusiness()
        employeeCount()
        branchCount()

//        getBusinessInformation()

        fromWhatButton = intent.getStringExtra(FROM_BUSINESS_INFO_BUTTON)
        if (fromWhatButton == null) {
            fromWhatButton = FROM_BUSINESS_INFO
        } else {
            fromWhatButton = intent.getStringExtra(FROM_BUSINESS_INFO_BUTTON)

        }

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
        val productsAndServices: TextView = findViewById(R.id.tv_product_of_services_offered)
        val yearsOfBusiness: TextView = findViewById(R.id.et_years_counter)

        productsAndServices.text = info.merchantDetails!!.storeProduct.toString()
        yearsOfBusiness.text = info.merchantDetails!!.yearsInBusiness.toString()
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

    private fun yearsInBusiness() {
        editTextYears = binding.etYearsCounter
        buttonAddYears = binding.btnYearsIncrement
        buttonMinusYears = binding.btnYearsDecrementActive

        var years = editTextYears.text.toString().toInt()

        buttonAddYears.setOnClickListener {
            years++
            editTextYears.setText(years.toString())
            if (years > 0){
                binding.btnYearsDecrementInactive.visibility(false)
                binding.btnYearsDecrementActive.visibility(true)
            }
        }

        buttonMinusYears.setOnClickListener {
            years--
            editTextYears.setText(years.toString())
            if (years == 0){
                binding.btnYearsDecrementInactive.visibility(true)
                binding.btnYearsDecrementActive.visibility(false)
            }
        }

        editTextYears.doOnTextChanged { text, start, before, count ->

            buttonAddYears.setOnClickListener {
                var add = text.toString().toInt()
                add++
                editTextYears.setText(add.toString())
                editTextYears.clearFocus()
                if (add > 0){
                    binding.btnYearsDecrementInactive.visibility(false)
                    binding.btnYearsDecrementActive.visibility(true)
                }
                if (add > 99){
                    editTextYears.setText("99")
                }
            }

            buttonMinusYears.setOnClickListener {
                editTextYears.clearFocus()
                var minus = text.toString().toInt()
                minus--
                if (minus == 0){
                    editTextYears.setText(minus.toString())
                    binding.btnYearsDecrementInactive.visibility(true)
                    binding.btnYearsDecrementActive.visibility(false)
                } else {
                    editTextYears.setText(minus.toString())
                    binding.btnYearsDecrementInactive.visibility(false)
                    binding.btnYearsDecrementActive.visibility(true)
                }
            }

//            if (years > 0){
//                binding.btnYearsDecrementInactive.visibility(false)
//                binding.btnYearsDecrementActive.visibility(true)
//            } else if (years == 0){
//                binding.btnYearsDecrementInactive.visibility(true)
//                binding.btnYearsDecrementActive.visibility(false)
//            } else if (years > 99){
//                editTextYears.setText("99")
//            }

        }

    }

    private fun employeeCount(){
        editTextEmployee = binding.etEmployeeCounter
        buttonAddEmployee = binding.btnEmployeeIncrement
        buttonMinusEmployee = binding.btnEmployeeDecrementActive

        var employee = editTextEmployee.text.toString().toInt()

        buttonAddEmployee.setOnClickListener {
            employee++
            editTextEmployee.setText(employee.toString())
            if (employee > 0){
                binding.btnEmployeeDecrementInactive.visibility(false)
                binding.btnEmployeeDecrementActive.visibility(true)
            }
        }

        buttonMinusEmployee.setOnClickListener {
            employee--
            editTextEmployee.setText(employee.toString())
            if (employee == 1){
                binding.btnEmployeeDecrementInactive.visibility(true)
                binding.btnEmployeeDecrementActive.visibility(false)
            }
        }

        editTextEmployee.doOnTextChanged { text, start, before, count ->
            buttonAddEmployee.setOnClickListener {
                var add = text.toString().toInt()
                add++
                editTextEmployee.setText(add.toString())
                editTextEmployee.clearFocus()
                if (add > 1){
                    binding.btnEmployeeDecrementInactive.visibility(false)
                    binding.btnEmployeeDecrementActive.visibility(true)
                }
                if (add > 99){
                    editTextEmployee.setText("99")
                }
            }

            buttonMinusEmployee.setOnClickListener {
                editTextEmployee.clearFocus()
                var minus = text.toString().toInt()
                minus--
                if (minus == 1){
                    editTextEmployee.setText(minus.toString())
                    binding.btnEmployeeDecrementInactive.visibility(true)
                    binding.btnEmployeeDecrementActive.visibility(false)
                }
            }

//            if (employee > 0){
//                binding.btnEmployeeDecrementInactive.visibility(false)
//                binding.btnEmployeeDecrementActive.visibility(true)
//            } else if (employee == 0){
//                binding.btnEmployeeDecrementInactive.visibility(true)
//                binding.btnEmployeeDecrementActive.visibility(false)
//            } else if (employee > 99){
//                editTextYears.setText("99")
//            }

        }
    }

    private fun branchCount(){
        editTextBranch = binding.etBranchCounter
        buttonAddBranch = binding.btnIncrementBranchNumber
        buttonMinusBranch = binding.btnDecrementBranchNumberActive

        var branch = editTextBranch.text.toString().toInt()

        buttonAddBranch.setOnClickListener {
            branch++
            editTextBranch.setText(branch.toString())
            if (branch > 0){
                binding.btnDecrementBranchNumberInactive.visibility(false)
                binding.btnDecrementBranchNumberActive.visibility(true)
            }
        }

        buttonMinusBranch.setOnClickListener {
            branch--
            editTextBranch.setText(branch.toString())
            if (branch == 1){
                binding.btnDecrementBranchNumberInactive.visibility(true)
                binding.btnDecrementBranchNumberActive.visibility(false)
            }
        }

        editTextBranch.doOnTextChanged { text, start, before, count ->
            buttonAddBranch.setOnClickListener {
                var add = text.toString().toInt()
                add++
                editTextBranch.setText(add.toString())
                editTextBranch.clearFocus()
                if (add > 1){
                    binding.btnDecrementBranchNumberInactive.visibility(false)
                    binding.btnDecrementBranchNumberActive.visibility(true)
                }
                if (add > 99){
                    editTextBranch.setText("99")
                }
                editTextBranch.clearFocus()
            }

            buttonMinusBranch.setOnClickListener {
                var minus = text.toString().toInt()
                minus--
                if (minus == 1){
                    editTextBranch.setText(minus.toString())
                    binding.btnDecrementBranchNumberInactive.visibility(true)
                    binding.btnDecrementBranchNumberActive.visibility(false)
                }
                editTextBranch.clearFocus()
            }

        }
    }

    private fun putInformationFromFields() {
        val businessType = binding.searchNatureOfBusiness.text.toString()
        val storeProduct = binding.etProductOfServicesOffered.text.toString()
        val infoStatus = "draft"
        val yearsInBusiness = binding.etYearsCounter.text.toString().toInt()
        val numberOfEmployees = binding.etEmployeeCounter.text.toString().toInt()
        val numberOfBranches = binding.etBranchCounter.text.toString().toInt()
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
    }

    override val bindingInflater: (LayoutInflater) -> ActivityBusinessInformationBinding
        get() = ActivityBusinessInformationBinding::inflate
    override val viewModelClassType: Class<BusinessInformationViewModel>
        get() = BusinessInformationViewModel::class.java
}