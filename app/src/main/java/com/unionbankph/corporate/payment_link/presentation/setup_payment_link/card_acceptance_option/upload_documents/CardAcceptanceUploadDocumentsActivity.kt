package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents

import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.DialogFactory
import com.unionbankph.corporate.app.util.FileUtil
import com.unionbankph.corporate.payment_link.presentation.onboarding.camera.DocumentCameraActivity
import com.unionbankph.corporate.payment_link.presentation.onboarding.upload_photos.OnboardingUploadPhotosActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.NotNowCardPaymentsActivity
import java.util.ArrayList
import javax.annotation.concurrent.ThreadSafe
import javax.inject.Inject
import com.unionbankph.corporate.databinding.ActivityUploadDocumentsBinding
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask

class CardAcceptanceUploadDocumentsActivity :
    BaseActivity<ActivityUploadDocumentsBinding,CardAcceptanceUploadDocumentsViewModel>(),
    CardAcceptanceUploadDocumentFragment.OnUploadDocs {

    private var uploadBIRFragment: CardAcceptanceUploadDocumentFragment? = null
    lateinit var imgView: ImageView
    private var uriArrayList = arrayListOf<Uri>()
    var adapter: BaseAdapter? = null
    val PDF_REQUEST_CODE = 200

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

    }
    override fun onViewsBound() {
        super.onViewsBound()
        init()

        binding.viewToolbar.btnSaveAndExit.visibility = View.GONE
        binding.btnUploadBIRDocs.setOnClickListener {
            showbottomSheetDialog()
        }
        binding.btnNext.setOnClickListener {
            if (binding.includePreviewBIRDocs.clPreviewBIR.isShown){
                binding.includePreviewBIRDocs.clPreviewBIR.visibility = View.GONE
                binding.clUploadBIRDocs.visibility = View.VISIBLE
            } else {
                val snackbarView = findViewById<TextView>(R.id.snackbar)
                val snackUploading = Snackbar.make(snackbarView, "Uploading document...", Snackbar.LENGTH_LONG).setAnchorView(R.id.btnNext)
                snackUploading.show()
                val intent = Intent(this, NotNowCardPaymentsActivity::class.java)
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    startActivity(intent)
                }, 3000)

            }

        }
    }

    private fun init(){
        uriArrayList = intent.getParcelableArrayListExtra<Uri>(OnboardingUploadPhotosActivity.LIST_OF_IMAGES_URI)
            .notNullable() as ArrayList<Uri>
        if (uriArrayList.size != 0){
            layoutVisibility()
            initAdapterListener()
        }
    }
    private fun showbottomSheetDialog() {

        if (uploadBIRFragment == null) {
            uploadBIRFragment = CardAcceptanceUploadDocumentFragment.newInstance()
        }

        uploadBIRFragment!!.show(supportFragmentManager, CardAcceptanceUploadDocumentFragment.TAG)
    }


    private fun layoutVisibility(){
        binding.viewToolbar.root.visibility = View.GONE
        binding.popupPreviewDocsFromCamera.visibility = View.VISIBLE

        binding.includePreviewBIRDocs.ivBackButton.setOnClickListener {
            binding.popupPreviewDocsFromCamera.visibility = View.GONE
            binding.viewToolbar.root.visibility = View.VISIBLE
            binding.clUploadBIRDocs.visibility = View.VISIBLE
        }

        binding.includePreviewBIRDocs.btnEdit.setOnClickListener {
            showbottomSheetDialog()
        }

        binding.includePreviewBIRDocs.btnNavigateBackToUploadDocs.setOnClickListener {
            binding.popupPreviewDocsFromCamera.visibility = View.GONE
            binding.viewToolbar.root.visibility = View.VISIBLE
            binding.clUploadBIRDocs.visibility = View.VISIBLE
            binding.btnNext.visibility = View.VISIBLE
        }
    }

    private fun layoutVisibilityFromGallery(){
        binding.viewToolbar.root.visibility = View.GONE
        binding.popupPreviewDocsFromGallery.visibility = View.VISIBLE

        binding.includePreviewBIRPhoto.ivBackButton1.setOnClickListener {
            binding.popupPreviewDocsFromGallery.visibility = View.GONE
            binding.viewToolbar.root.visibility = View.VISIBLE
            binding.clUploadBIRDocs.visibility = View.VISIBLE
        }

        binding.includePreviewBIRPhoto.btnEdit1.setOnClickListener {
            showbottomSheetDialog()
        }

        binding.includePreviewBIRPhoto.btnNavigateBackToUploadDocs1.setOnClickListener {
            binding.popupPreviewDocsFromGallery.visibility = View.GONE
            binding.viewToolbar.root.visibility = View.VISIBLE
            binding.clUploadBIRDocs.visibility = View.VISIBLE
            binding.btnNext.visibility = View.VISIBLE
        }
    }

    private fun initAdapterListener(){
        adapter = UploadDocumentsAdapter(this, uriArrayList)
        binding.includePreviewBIRDocs.gvDocs.adapter = adapter

    }

    private fun layoutVisibilityWhenInvalidFiles(){
        binding.popupPreviewDocsFromGallery.visibility = View.GONE
        binding.viewToolbar.root.visibility = View.VISIBLE
        binding.clUploadBIRDocs.visibility = View.VISIBLE
        binding.btnNext.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imgView = findViewById(R.id.ivPreviewBIR)
        when (requestCode) {
            PDF_REQUEST_CODE ->
                if (resultCode == RESULT_OK) {
                    if (data?.data != null) {

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
                                    layoutVisibilityWhenInvalidFiles()
                                }
                            ).show()

                        }
                        val pdfRenderer = PdfRenderer(fileDescriptor)
                        val rendererPage = pdfRenderer.openPage(0)
                        val rendererPageWidth = rendererPage.width
                        val rendererPageHeight = rendererPage.height
                        val bitmap = Bitmap.createBitmap(rendererPageWidth, rendererPageHeight, Bitmap.Config.ARGB_8888)
                        rendererPage.render(bitmap,null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        imgView.setImageBitmap(bitmap)
                        rendererPage.close()
                        pdfRenderer.close()
                        layoutVisibilityFromGallery()
                    }
                }
            GALLERY_REQUEST_CODE ->
                if (resultCode == RESULT_OK){
                    if (data?.data != null){
                        binding.clUploadBIRDocs.visibility = View.GONE
                        layoutVisibilityFromGallery()

                        val imageUri = data.data!!
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
                                    layoutVisibilityWhenInvalidFiles()
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
                                    layoutVisibilityWhenInvalidFiles()
                                }
                            ).show()
                        }
                        imgView.setImageURI(imageUri)
                    }


                }
//            CAPTURE_PHOTO -> {
//                if (resultCode == RESULT_OK){
//                    if (data != null){
//                        layoutVisibility()
//                        imgView.setImageBitmap(data.extras?.get("data") as Bitmap)
//                    }
//
//                }
////                viewModel.currentFile.value?.let {
////                    viewModel.originalApplicationFileInput.onNext(it)
////                }
//            }

        }
    }

    private fun openFiles() {
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

    override fun openFileManager() {
        openFiles()
        uploadBIRFragment?.dismiss()
    }

    override fun openCamera() {
        val bundle = Bundle().apply {
            putParcelableArrayList(
                LIST_OF_IMAGES_URI,
                uriArrayList
            )
        }
        navigator.navigate(
            this,
            DocumentCameraActivity::class.java,
            bundle,
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun addPhotoOfBIRDocs(){
        if (Build.VERSION.SDK_INT < 19) {
            var intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Choose pictures"), GALLERY_REQUEST_CODE)
        } else {
            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

    }

    override fun openGallery() {
        addPhotoOfBIRDocs()
        uploadBIRFragment?.dismiss()
    }

    @ThreadSafe
    companion object{
        const val GALLERY_REQUEST_CODE = 1
        const val CAPTURE_PHOTO = 2
        const val LIST_OF_IMAGES_URI = "list_of_image_uri"
        const val MAX_FILESIZE_2MB = 2097152
        const val IMAGE_JPEG = "image/jpeg"
        const val IMAGE_PNG = "image/png"
        const val DOCU_PDF = "application/pdf"
    }

    override val bindingInflater: (LayoutInflater) -> ActivityUploadDocumentsBinding
        get() = ActivityUploadDocumentsBinding::inflate
    override val viewModelClassType: Class<CardAcceptanceUploadDocumentsViewModel>
        get() = CardAcceptanceUploadDocumentsViewModel::class.java
}



