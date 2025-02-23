package com.unionbankph.corporate.payment_link.presentation.setup_business_information.business_information_forms

import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.DialogFactory
import com.unionbankph.corporate.databinding.ActivityBusinessInformation2ndScreenBinding
import com.unionbankph.corporate.payment_link.presentation.onboarding.camera.BusinessPolicyCameraActivity
import com.unionbankph.corporate.payment_link.presentation.onboarding.upload_photos.OnboardingUploadPhotosActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents.CardAcceptanceUploadDocumentFragment

class BusinessInformation2ndScreenActivity :
    BaseActivity<ActivityBusinessInformation2ndScreenBinding, BusinessInformationViewModel>(),
    CardAcceptanceUploadDocumentFragment.OnUploadDocs, AdapterView.OnItemSelectedListener {

    var orderFulfilled = ""
    var orderFulfillment =
        arrayOf(
            "Select",
            "Beyond 3 days",
            "Within 3 days",
            "Not Applicable"
        )
    var lazadaState = 0
    var shopeeState = 0
    var websiteState = 0
    var suysingState = 0
    var physicalStoreState = 0
    var facebookState = 0
    var instagramState = 0
    var othersState = 0
    var branchCounter = 0

    var lazTextfieldInput: String = ""
    var shopeeTextfieldInput: String = ""
    var webTextfieldInput: String = ""
    var suysingTextfieldInput: String = ""
    var physTextfieldInput: String = ""
    var fbTextfieldInput: String = ""
    var igTextfieldInput: String = ""
    var othersTextfieldInput: String = ""
    var fileURI: Uri? = null

    private var uploadDocumentFragment: CardAcceptanceUploadDocumentFragment? = null
    lateinit var imgView: ImageView
    private var uriArrayList = arrayListOf<Uri>()

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

    override fun onViewsBound() {
        super.onViewsBound()

        disableNextButton()
        initClickListener()
        orderFulfillment()
    }

    private fun initClickListener() {
        binding.btnLazada.setOnClickListener { btnLazadaClicked() }
        binding.btnShopee.setOnClickListener { btnShopeeClicked() }
        binding.btnFacebook.setOnClickListener { btnFacebookClicked() }
        binding.btnSuysing.setOnClickListener { toggleSuysingButton() }
        binding.btnPhysicalStore.setOnClickListener { btnPhysicalStoreClicked() }
        binding.btnInstagram.setOnClickListener { btnInstagramClicked() }
        binding.btnWebsite.setOnClickListener { btnWebsiteClicked() }
        binding.btnOthers.setOnClickListener { btnOtherClicked() }
        binding.btnUploadBusinessPolicy.setOnClickListener {
            showUploadDocumentDialog()
        }
        binding.btnNavigateToUploadPhotos.setOnClickListener {
            validation()

        }
    }

    private fun navigate() {
        navigator.navigate(
            this,
            OnboardingUploadPhotosActivity::class.java,
            null,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )

    }

    private fun btnLazadaClicked() {
        lazadaState++
        val stateChecker = lazadaState % 2
        if (stateChecker == 1) {
            binding.btnLazada.background =
                ContextCompat.getDrawable(context, R.drawable.bg_where_do_you_sell_active)
            binding.btnLazada.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreName.visibility = View.VISIBLE
            binding.dividerDashed.visibility = View.VISIBLE
            binding.lazada.root.visibility = View.VISIBLE
            disableNextButton()

            binding.lazada.etLazada.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    if (binding.lazada.root.isShown) {
//                        if (s!!.isNotEmpty()) {
//                            lazTextfieldInput = s.toString()
//                            checker(lazTextfieldInput, shopeeTextfieldInput, fileURI)
//                        }
//                    }

                }

                override fun afterTextChanged(s: Editable?) {
//                    enableNextButton()

                }

            })
        } else if (stateChecker == 0) {
            binding.btnLazada.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnLazada.setTextColor(Color.parseColor("#4A4A4A"))
            binding.dividerDashed.visibility = View.INVISIBLE
            binding.tvInputStoreName.visibility = View.GONE
            binding.view.visibility = View.INVISIBLE
            binding.lazada.root.visibility = View.GONE
            binding.lazada.etLazada.text?.clear()
        }

    }

    private fun btnShopeeClicked() {
        shopeeState++
        val stateChecker = shopeeState % 2
        if (stateChecker == 1) {
            binding.btnShopee.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnShopee.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreNameShopee.visibility = View.VISIBLE
            binding.dividerDashed2.visibility = View.VISIBLE
            binding.shopee.root.visibility = View.VISIBLE
            disableNextButton()

            binding.shopee.etShopee.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    if (binding.shopee.root.isShown) {
//                        if (s!!.isNotEmpty()) {
//                            shopeeTextfieldInput = s.toString()
//                            checker(lazTextfieldInput, shopeeTextfieldInput, fileURI)
//                        }
//                    }

                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

        } else if (stateChecker == 0) {
            binding.btnShopee.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnShopee.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameShopee.visibility = View.GONE
            binding.dividerDashed2.visibility = View.GONE
            binding.view.visibility = View.INVISIBLE
            binding.shopee.root.visibility = View.GONE
            binding.shopee.etShopee.text?.clear()
        }
    }

    private fun btnWebsiteClicked() {
        websiteState++
        val stateChecker = websiteState % 2
        if (stateChecker == 1) {
            binding.btnWebsite.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnWebsite.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreNameWebsite.visibility = View.VISIBLE
            binding.dividerDashed3.visibility = View.VISIBLE
            binding.website.root.visibility = View.VISIBLE
            disableNextButton()

            binding.website.etWebsite.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

        } else if (stateChecker == 0) {
            binding.btnWebsite.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnWebsite.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameWebsite.visibility = View.GONE
            binding.dividerDashed3.visibility = View.GONE
            binding.website.root.visibility = View.GONE
        }
    }

    private fun toggleSuysingButton() {
        suysingState++
        val stateChecker = suysingState % 2
        if (stateChecker == 1) {
            binding.btnSuysing.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnSuysing.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreNameSuysing.visibility = View.VISIBLE
            binding.dividerDashed4.visibility = View.VISIBLE
            binding.suysing.root.visibility = View.VISIBLE

            binding.btnPhysicalStore.background =
                getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnPhysicalStore.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreNamePhysicalStore.visibility = View.VISIBLE
            binding.physicalStore.root.visibility = View.VISIBLE

            disableNextButton()

            binding.suysing.etSuysing.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

        } else if (stateChecker == 0) {
            binding.btnSuysing.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnSuysing.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameSuysing.visibility = View.GONE
            binding.dividerDashed4.visibility = View.GONE
            binding.suysing.root.visibility = View.GONE

            binding.btnPhysicalStore.background =
                getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnPhysicalStore.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNamePhysicalStore.visibility = View.GONE
            binding.physicalStore.root.visibility = View.GONE
        }
    }

    private fun btnPhysicalStoreClicked() {
        physicalStoreState++
        val stateChecker = physicalStoreState % 2
        var addBranchFields: View
        val container: LinearLayout = findViewById(R.id.branchContainer)
        if (stateChecker == 1) {
            binding.btnPhysicalStore.background =
                getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnPhysicalStore.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreNamePhysicalStore.visibility = View.VISIBLE
            binding.dividerDashed5.visibility = View.VISIBLE
            binding.physicalStore.root.visibility = View.VISIBLE
            disableNextButton()

            binding.physicalStore.etPhysicalStore.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            binding.physicalStore.btnAddBranchAddress.setOnClickListener {
                val layoutInflater =
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                addBranchFields = layoutInflater.inflate(R.layout.layout_branch_textfields, null)
                container.addView(addBranchFields, container.childCount)

                val tv = addBranchFields.findViewById<TextView>(R.id.tv_branch_number)
                val branchCount = container.childCount
                if (branchCount <= 99) {
                    branchCounter++
                    tv.text = getString(R.string.branch) + " " + branchCounter
                }
                if (branchCount == 99) {
                    binding.physicalStore.btnAddBranchAddress.visibility(false)
                }
            }
        } else if (stateChecker == 0) {
            binding.btnPhysicalStore.background =
                getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnPhysicalStore.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNamePhysicalStore.visibility = View.GONE
            binding.dividerDashed5.visibility = View.GONE
            binding.physicalStore.root.visibility = View.GONE
            container.removeAllViews()
        }
    }

    private fun btnFacebookClicked() {
        facebookState++
        val stateChecker = facebookState % 2
        if (stateChecker == 1) {
            binding.btnFacebook.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnFacebook.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreNameFacebook.visibility = View.VISIBLE
            binding.dividerDashed6.visibility = View.VISIBLE
            binding.facebook.root.visibility = View.VISIBLE
            disableNextButton()

            binding.facebook.etFacebook.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

        } else if (stateChecker == 0) {
            binding.btnFacebook.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnFacebook.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameFacebook.visibility = View.GONE
            binding.dividerDashed6.visibility = View.GONE
            binding.facebook.root.visibility = View.GONE
        }
    }

    private fun btnInstagramClicked() {
        instagramState++
        val stateChecker = instagramState % 2
        if (stateChecker == 1) {
            binding.btnInstagram.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnInstagram.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreNameInstagram.visibility = View.VISIBLE
            binding.dividerDashed7.visibility = View.VISIBLE
            binding.instagram.root.visibility = View.VISIBLE
            disableNextButton()

            binding.instagram.etInstagram.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

        } else if (stateChecker == 0) {
            binding.btnInstagram.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnInstagram.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameInstagram.visibility = View.GONE
            binding.dividerDashed7.visibility = View.GONE
            binding.instagram.root.visibility = View.GONE
        }
    }


    private fun btnOtherClicked() {
        othersState++
        val stateChecker = othersState % 2
        var addOtherStoreFields: View
        val container: LinearLayout = findViewById(R.id.storeContainer)
        if (stateChecker == 1) {
            binding.btnOthers.background = getDrawable(R.drawable.bg_where_do_you_sell_active)
            binding.btnOthers.setTextColor(Color.parseColor("#FF8200"))
            binding.tvInputStoreNameOthers.visibility = View.VISIBLE
            binding.dividerDashed8.visibility = View.VISIBLE
            binding.others.root.visibility = View.VISIBLE
            disableNextButton()

            binding.others.etOthers.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
//                    enableNextButton()

                }

            })

            binding.others.btnAddAnotherStore.setOnClickListener {
                val layoutInflater =
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                addOtherStoreFields = layoutInflater.inflate(R.layout.layout_store_textfields, null)
                container.addView(addOtherStoreFields, container.childCount)

                val storeCount = container.childCount
                if (storeCount == 98) {
                    binding.others.btnAddAnotherStore.visibility(false)
                }
            }
        } else if (stateChecker == 0) {
            binding.btnOthers.background = getDrawable(R.drawable.bg_where_do_you_sell_inactive)
            binding.btnOthers.setTextColor(Color.parseColor("#4A4A4A"))
            binding.tvInputStoreNameOthers.visibility = View.GONE
            binding.dividerDashed8.visibility = View.GONE
            binding.others.root.visibility = View.GONE
//            if (et_others.text!!.isNotEmpty()) {
//                et_others.text!!.clear()
//                enableNextButton()
//            }
            container.removeAllViews()
        }
    }

    private fun validation() {
        val lazadaTextField = binding.lazada.etLazada.text.toString()
        val shopeeTextField = binding.shopee.etShopee.text.toString()
        val websiteTextField = binding.website.etWebsite.text.toString()
        val suysingTextField = binding.suysing.etSuysing.text.toString()
        val physicalStoreTextField = binding.physicalStore.etPhysicalStore.text.toString()
        val fbTextField = binding.facebook.etFacebook.text.toString()
        val igTextField = binding.instagram.etInstagram.text.toString()
        val othersTextField = binding.others.etOthers.text.toString()


        val spinnerSelectedItem = binding.dropdownOrderFulfillment.selectedItemPosition

        val haveURI = binding.tvURIcontainer.text
        val checkbox = binding.cbNotApplicable.isChecked

        if (lazadaTextField.isNotEmpty() ||
            shopeeTextField.isNotEmpty() ||
            websiteTextField.isNotEmpty() ||
            suysingTextField.isNotEmpty() ||
            physicalStoreTextField.isNotEmpty() ||
            fbTextField.isNotEmpty() ||
            igTextField.isNotEmpty() ||
            othersTextField.isNotEmpty()) {
                if (haveURI.isNotEmpty()){
                    if (spinnerSelectedItem != 0){
                        navigate()
                    } else {
                        disableNextButton()
                    }

                } else if (checkbox){
                    if (spinnerSelectedItem != 0){
                        navigate()
                    } else {
                        disableNextButton()
                    }
                }
        } else {
            disableNextButton()
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
        }
    }

    private fun checker(lazadaText: String, shopeeText: String, imgUri: Uri? = null) {
        val spinnerSelectedItem = binding.dropdownOrderFulfillment.selectedItemPosition

        binding.tvURIcontainer.text = imgUri.toString()
        val haveURI = binding.tvURIcontainer.text
        if (lazadaText.isNotEmpty() || shopeeText.isNotEmpty() && spinnerSelectedItem != 0 && haveURI.isNotEmpty()) {
            enableNextButton()
        } else {
            disableNextButton()
        }
    }

    private fun disableNextButton() {
        binding.btnNavigateToUploadPhotos.isEnabled = false
    }

    private fun enableNextButton() {
        binding.btnNavigateToUploadPhotos.isEnabled = true
    }

    /* Order Fulfillment dropdown */
    private fun orderFulfillment() {
        val arrayAdapter: ArrayAdapter<String> = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            orderFulfillment
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view: TextView = super.getView(position, convertView, parent) as TextView
                if (position == 0) {
                    view.setTextColor(Color.GRAY)
                }
                return view
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: TextView =
                    super.getDropDownView(position, convertView, parent) as TextView
                if (position == 0) {
                    view.setTextColor(Color.GRAY)
                }

                return view
            }

            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }
        }
        binding.dropdownOrderFulfillment.adapter = arrayAdapter

        with(binding.dropdownOrderFulfillment){
            onItemSelectedListener = this@BusinessInformation2ndScreenActivity
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position != 0){
            when {
                binding.cbNotApplicable.isChecked -> {
                    enableNextButton()
                }
                binding.tvURIcontainer.text.isNotEmpty() -> {
                    enableNextButton()
                }
                else -> {
                    disableNextButton()
                }
            }
        } else {
            disableNextButton()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    /* Upload Business Policy docs */
    private fun showUploadDocumentDialog() {
        if (uploadDocumentFragment == null) {
            uploadDocumentFragment = CardAcceptanceUploadDocumentFragment.newInstance()
        }

        uploadDocumentFragment!!.show(
            supportFragmentManager,
            CardAcceptanceUploadDocumentFragment.TAG
        )
    }

    override fun openCamera() {
        captureImageFromCamera()
        uploadDocumentFragment?.dismiss()
    }

    override fun openGallery() {
        selectImageFromGallery()
        uploadDocumentFragment?.dismiss()
    }

    override fun openFileManager() {
        selectPDFDocument()
        uploadDocumentFragment?.dismiss()
    }

    private fun captureImageFromCamera() {
        val bundle = Bundle().apply {
            putParcelableArrayList(
                LIST_OF_IMAGES_URI,
                uriArrayList
            )
        }
        navigator.navigate(
            this,
            BusinessPolicyCameraActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun selectPDFDocument() {
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

    private fun selectImageFromGallery() {
        if (Build.VERSION.SDK_INT < 19) {
            var intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Choose pictures"),
                GALLERY_REQUEST_CODE
            )
        } else {
            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(
                intent, GALLERY_REQUEST_CODE
            )
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imgView = findViewById(R.id.ivPreviewBIR)
        when (requestCode) {
            PDF_REQUEST_CODE ->
                if (resultCode == RESULT_OK) {
                    if (data?.data != null) {
                        val fileUri = data.data!!
                        val fileDescriptor: ParcelFileDescriptor =
                            context.contentResolver.openFileDescriptor(fileUri, "r")!!
                        val fileType: String? = applicationContext.contentResolver.getType(fileUri)
                        if (fileType != DOCU_PDF) {
                            DialogFactory().createColoredSMEDialog(
                                this,
                                title = getString(R.string.item_not_uploaded),
                                content = getString(R.string.invalid_filetype_desc),
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
                            val bitmap = Bitmap.createBitmap(
                                rendererPageWidth,
                                rendererPageHeight,
                                Bitmap.Config.ARGB_8888
                            )
                            rendererPage.render(
                                bitmap,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                            )
                            imgView.setImageBitmap(bitmap)
                            rendererPage.close()
                            pdfRenderer.close()
                        }
                        binding.tvURIcontainer.text = fileUri.toString()
                        enableNextButton()
                    }
                }
            GALLERY_REQUEST_CODE ->
                if (resultCode == RESULT_OK) {
                    showImagePreviewFromGallery()

                    val imageUri = data?.data!!
                    val fileDescriptor: AssetFileDescriptor =
                        applicationContext.contentResolver.openAssetFileDescriptor(imageUri, "r")!!
                    val fileType: String? = applicationContext.contentResolver.getType(imageUri)
                    val fileSize: Long = fileDescriptor.length
                    if (fileSize > MAX_FILESIZE_2MB) {
                        DialogFactory().createColoredSMEDialog(
                            this,
                            title = getString(R.string.item_not_uploaded),
                            content = getString(R.string.invalid_filesize_desc),
                            positiveButtonText = getString(R.string.action_try_again),
                            onPositiveButtonClicked = {
                                imgView.setImageBitmap(null)
//                                layoutVisibilityWhenInvalidFiles()
                            }
                        ).show()
                    }
                    if (fileType != IMAGE_JPEG && fileType != IMAGE_PNG) {
                        DialogFactory().createColoredSMEDialog(
                            this,
                            title = getString(R.string.item_not_uploaded),
                            content = getString(R.string.invalid_filetype_desc),
                            positiveButtonText = getString(R.string.action_try_again),
                            onPositiveButtonClicked = {
                                imgView.setImageBitmap(null)
//                                layoutVisibilityWhenInvalidFiles()
                            }
                        ).show()
                    }

                    imgView.setImageURI(imageUri)
                    binding.tvURIcontainer.text = imageUri.toString()
                    enableNextButton()

                    binding.includePreviewGallery.btnNavigateBackToUploadDocs1.setOnClickListener {
                        hideImagePreviewFromGallery()
                    }
                }
        }
    }

    private fun showImagePreviewFromGallery() {
        binding.popupPreviewDocsFromGallery.visibility(true)
        binding.scrollView2.visibility(false)
        binding.viewToolbar.toolbar.visibility(false)
        binding.btnNavigateToUploadPhotos.visibility(false)
    }

    private fun hideImagePreviewFromGallery() {
        binding.popupPreviewDocsFromGallery.visibility(false)
        binding.scrollView2.visibility(true)
        binding.viewToolbar.toolbar.visibility(true)
        binding.btnNavigateToUploadPhotos.visibility(true)
    }

    fun disableUploadDocs(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            when (view.id) {
                R.id.cb_notApplicable -> {
                    if (checked) {
                        binding.btnUploadBusinessPolicy.isEnabled = false
                        binding.btnUploadBusinessPolicy.text = ""
                        binding.tvURIcontainer.text = ""
//                        checker(lazTextfieldInput, shopeeTextfieldInput, null)
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

    companion object {
        const val PDF_REQUEST_CODE = 200
        const val GALLERY_REQUEST_CODE = 201
        const val CAMERA_REQUEST_CODE = 202
        const val IMAGE_JPEG = "image/jpeg"
        const val IMAGE_PNG = "image/png"
        const val DOCU_PDF = "application/pdf"
        const val LIST_OF_IMAGES_URI = "list_of_image_uri"
        const val MAX_FILESIZE_2MB = 2097152
    }

    override val bindingInflater: (LayoutInflater) -> ActivityBusinessInformation2ndScreenBinding
        get() = ActivityBusinessInformation2ndScreenBinding::inflate
    override val viewModelClassType: Class<BusinessInformationViewModel>
        get() = BusinessInformationViewModel::class.java
}