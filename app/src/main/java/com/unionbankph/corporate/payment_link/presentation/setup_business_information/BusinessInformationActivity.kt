package com.unionbankph.corporate.payment_link.presentation.setup_business_information

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.isEmpty
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.DialogFactory
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivityBusinessInformationBinding
import com.unionbankph.corporate.payment_link.domain.model.form.RMOBusinessInformationForm
import com.unionbankph.corporate.payment_link.presentation.onboarding.upload_photos.OnboardingUploadPhotosActivity

class BusinessInformationActivity :
    BaseActivity<ActivityBusinessInformationBinding,BusinessInformationViewModel>(),
    AdapterView.OnItemSelectedListener {

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
            retrieveInformationFromFields()
            btnNextClicked()
        }
        binding.viewToolbar.btnSaveAndExit.setOnClickListener {
            retrieveInformationFromFields()
//            showDialogToDashboard()
            navigator.navigate(
                this,
                DashboardActivity::class.java,
                null,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_RIGHT
            )

        }
    }

    private fun natureOfBusiness() {

        val aa = ArrayAdapter(this, android.R.layout.simple_list_item_1, businessType)
        aa.setDropDownViewResource(R.layout.spinner)

        with(binding.dropdownBusinessInformation) {
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
        }

        if (position == 7) {
            binding.tilOthersPlsSpecify.visibility = View.VISIBLE
        } else {
            binding.tilOthersPlsSpecify.visibility = View.GONE
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun fromZeroCounter() {

        if (binding.tvYearsCounter.text == "0") {
            binding.btnYearsDecrementActive.visibility = View.GONE
            binding.btnYearsDecrementInactive.visibility = View.VISIBLE
        } else {
            binding.btnYearsDecrementActive.visibility = View.VISIBLE
            binding.btnYearsDecrementInactive.visibility = View.GONE
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

    private fun btnLazadaClicked() {
        lazadaCounter++
        val stateChecker = lazadaCounter % 2
        if (stateChecker == 1) {
            binding.btnLazada.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnLazada.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreName.visibility = View.VISIBLE
            binding.dividerDashed.visibility = View.VISIBLE
            binding.tvLazadaTitle.visibility = View.VISIBLE
            binding.tilLazada.visibility = View.VISIBLE
            binding.etLazada.visibility = View.VISIBLE
        } else if (stateChecker == 0) {
            binding.btnLazada.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnLazada.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreName.visibility = View.INVISIBLE
            binding.tvLazadaTitle.visibility = View.GONE
            binding.tilLazada.visibility = View.GONE
            binding.etLazada.visibility = View.GONE
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
            binding.tvShopeeTitle.visibility = View.VISIBLE
            binding.tilShopee.visibility = View.VISIBLE
            binding.etShopee.visibility = View.VISIBLE
        } else if (stateChecker == 0) {
            binding.btnShopee.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnShopee.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameShopee.visibility = View.INVISIBLE
            binding.tvShopeeTitle.visibility = View.GONE
            binding.tilShopee.visibility = View.GONE
            binding.etShopee.visibility = View.GONE
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
            binding.tvFacebookTitle.visibility = View.VISIBLE
            binding.tilFacebook.visibility = View.VISIBLE
            binding.etFacebook.visibility = View.VISIBLE
        } else if (stateChecker == 0) {
            binding.btnFacebook.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnFacebook.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameFacebook.visibility = View.INVISIBLE
            binding.tvFacebookTitle.visibility = View.GONE
            binding.tilFacebook.visibility = View.GONE
            binding.etFacebook.visibility = View.GONE
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
            binding.tvPhysicalStore.visibility = View.VISIBLE
            binding.tilPhysicalStore.visibility = View.VISIBLE
            binding.etPhysicalStore.visibility = View.VISIBLE
        } else if (stateChecker == 0) {
            binding.btnPhysicalStore.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnPhysicalStore.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNamePhysicalStore.visibility = View.INVISIBLE
            binding.tvPhysicalStore.visibility = View.GONE
            binding.tilPhysicalStore.visibility = View.GONE
            binding.etPhysicalStore.visibility = View.GONE
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
            binding.tvInstagramTitle.visibility = View.VISIBLE
            binding.tilInstagram.visibility = View.VISIBLE
            binding.etInstagram.visibility = View.VISIBLE
        } else if (stateChecker == 0) {
            binding.btnInstagram.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnInstagram.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameInstagram.visibility = View.INVISIBLE
            binding.tvInstagramTitle.visibility = View.GONE
            binding.tilInstagram.visibility = View.GONE
            binding.etInstagram.visibility = View.GONE
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
            binding.tvWebsiteTitle.visibility = View.VISIBLE
            binding.tilWebsite.visibility = View.VISIBLE
            binding.etWebsite.visibility = View.VISIBLE
        } else if (stateChecker == 0) {
            binding.btnWebsite.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnWebsite.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameWebsite.visibility = View.INVISIBLE
            binding.tvWebsiteTitle.visibility = View.GONE
            binding.tilWebsite.visibility = View.GONE
            binding.etWebsite.visibility = View.GONE
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
            binding.tvOthersTitle.visibility = View.VISIBLE
            binding.tilOthers.visibility = View.VISIBLE
            binding.etOthers.visibility = View.VISIBLE
        } else if (stateChecker == 0) {
            binding.btnOthers.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnOthers.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameOthers.visibility = View.INVISIBLE
            binding.tvOthersTitle.visibility = View.GONE
            binding.tilOthers.visibility = View.GONE
            binding.etOthers.visibility = View.GONE
        }
    }

    private fun retrieveInformationFromFields() {
        val businessType = business
        val storeProduct = binding.etProductOfServicesOffered.text.toString()
        val infoStatus = "draft"
        val yearsInBusiness = binding.tvYearsCounter.text.toString().toInt()
        val numberOfBranches = binding.tvBranchCounter.text.toString().toInt()
        val physicalStore = binding.etPhysicalStore.text.toString()
        val website = binding.etWebsite.text.toString()
        val lazadaUrl = binding.etLazada.text.toString()
        val shopeeUrl = binding.etShopee.text.toString()
        val facebookUrl = binding.etFacebook.text.toString()
        val instagramUrl = binding.etInstagram.text.toString()
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
        binding.btnNext?.isEnabled = true
    }

    private fun disableNextButton() {
        binding.btnNext?.isEnabled = false
    }

    private fun requiredFields() {
        binding.etProductOfServicesOffered.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        binding.etLazada.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val lazadaLink = binding.etLazada.text.toString()
                if (binding.tilLazada.isShown && lazadaLink.isEmpty()) {
                    disableNextButton()
                } else if (lazadaLink.isNotEmpty()) {
                    enableNextButton()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        binding.etShopee.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val shopeeLink = binding.etShopee.text.toString()
                if (binding.tilShopee.isShown && shopeeLink.isEmpty()) {
                    disableNextButton()
                } else if (shopeeLink.isNotEmpty()) {
                    enableNextButton()
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        binding.etFacebook.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val facebookLink = binding.etFacebook.text.toString()
                if (binding.tilFacebook.isShown && facebookLink.isEmpty()) {
                    disableNextButton()
                } else if (facebookLink.isNotEmpty()) {
                    enableNextButton()
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        binding.etInstagram.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val instagramLink = binding.etInstagram.text.toString()
                if (binding.tilInstagram.isShown && instagramLink.isEmpty()) {
                    disableNextButton()
                } else if (instagramLink.isNotEmpty()) {
                    enableNextButton()
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        binding.etPhysicalStore.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val physicalStore = binding.etPhysicalStore.text.toString()
                if (binding.tilPhysicalStore.isShown && physicalStore.isEmpty()) {
                    disableNextButton()
                } else if (physicalStore.isNotEmpty()) {
                    enableNextButton()
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        binding.etWebsite.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val websiteLink = binding.etWebsite.text.toString()
                if (binding.tilWebsite.isShown && websiteLink.isEmpty()) {
                    disableNextButton()
                } else if (websiteLink.isNotEmpty()) {
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
                navigator.navigateClearStacks(
                    this,
                    DashboardActivity::class.java,
                    null,
                    true,
                    Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
                )
            }
        ).show()
    }

    companion object {
        var TAG = this::class.java.simpleName
    }

    override val bindingInflater: (LayoutInflater) -> ActivityBusinessInformationBinding
        get() = ActivityBusinessInformationBinding::inflate
    override val viewModelClassType: Class<BusinessInformationViewModel>
        get() = BusinessInformationViewModel::class.java
}