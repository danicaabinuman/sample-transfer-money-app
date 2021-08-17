package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.util.FileUtil
import com.unionbankph.corporate.payment_link.presentation.onboarding.OnboardingUploadPhotosActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.NotNowCardPaymentsActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_onboarding_upload_photos.viewToolbar
import kotlinx.android.synthetic.main.activity_upload_documents.*
import kotlinx.android.synthetic.main.widget_transparent_rmo_appbar.*
import java.io.File
import java.io.IOException
import javax.annotation.concurrent.ThreadSafe

class CardAcceptanceUploadDocumentsActivity :
    BaseActivity<CardAcceptanceUploadDocumentsViewModel>(R.layout.activity_upload_documents),
    CardAcceptanceUploadDocumentFragment.OnUploadBIRDocs {

    private var uploadBIRFragment: CardAcceptanceUploadDocumentFragment? = null
    lateinit var fileUtil: FileUtil
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
        viewModel =
            ViewModelProviders.of(
                this,
                viewModelFactory
            )[CardAcceptanceUploadDocumentsViewModel::class.java]

    }
    override fun onViewsBound() {
        super.onViewsBound()

        btnSaveAndExit.visibility = View.GONE
        btnUploadBIRDocs.setOnClickListener {
            showbottomSheetDialog()
        }
        btnNext.setOnClickListener {
            if (clPreviewBIR.isShown){
                clPreviewBIR.visibility = View.GONE
                clUploadBIRDocs.visibility = View.VISIBLE
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

    private fun showbottomSheetDialog() {

        if (uploadBIRFragment == null) {
            uploadBIRFragment = CardAcceptanceUploadDocumentFragment.newInstance()
        }

        uploadBIRFragment!!.show(supportFragmentManager, CardAcceptanceUploadDocumentFragment.TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imgView = findViewById(R.id.ivPreviewBIR)
        when (requestCode) {
            REQUEST_CODE ->
                if (resultCode == RESULT_OK) {
                    if (data?.data != null) {

                        val fileUri = data.data!!
                        val fileDescriptor : ParcelFileDescriptor = context.contentResolver.openFileDescriptor(fileUri,"r")!!
                        val pdfRenderer = PdfRenderer(fileDescriptor)
                        val rendererPage = pdfRenderer.openPage(0)
                        val rendererPageWidth = rendererPage.width
                        val rendererPageHeight = rendererPage.height
                        val bitmap = Bitmap.createBitmap(rendererPageWidth, rendererPageHeight, Bitmap.Config.ARGB_8888)
                        rendererPage.render(bitmap,null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        imgView.setImageBitmap(bitmap)
                        rendererPage.close()
                        pdfRenderer.close()
                        clPreviewBIR.visibility = View.VISIBLE
                        btnNext.visibility = View.VISIBLE

                    }
                }
            GALLERY_REQUEST_CODE ->
                if (resultCode == RESULT_OK){
                    if (data?.data != null){
                        clUploadBIRDocs.visibility = View.GONE
                        clPreviewBIR.visibility = View.VISIBLE
                        btnNext.visibility = View.VISIBLE

                        val imageUri = data.data!!
                        imgView.setImageURI(imageUri)
                    }


                }
            CAPTURE_PHOTO -> {
                viewModel.currentFile.value?.let {
                    viewModel.originalApplicationFileInput.onNext(it)
                }
            }

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
                GALLERY_REQUEST_CODE
            )
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/pdf"
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

    }

    override fun openFileManager() {
        openFiles()
        uploadBIRFragment?.dismiss()
    }

    private fun takePhotoOfBIRDocs(){
        RxPermissions(this)
            .request(Manifest.permission.CAMERA)
            .subscribe { granted ->
                if (granted) {
//                    cameraView.open()
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                        takePictureIntent.resolveActivity(packageManager)?.also {
                            val photoFile: File? = try {
                                fileUtil.createCaptureImageFile()
                            } catch (ex: IOException) {
                                showMaterialDialogError(message = ex.message.notNullable())
                                null
                            }
                            photoFile?.let {
                                viewModel.currentFile.onNext(it)
                                it.also {
                                    val photoURI: Uri = FileProvider.getUriForFile(
                                        this,
                                        "${BuildConfig.APPLICATION_ID}.fileprovider",
                                        it
                                    )
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                                    startActivityForResult(takePictureIntent, CAPTURE_PHOTO
                                    )
                                }
                            }
                        }
                    }
                } else {
                    takePhotoOfBIRDocs()
                }
            }.addTo(disposables)

    }

    override fun openCamera() {
        takePhotoOfBIRDocs()
        uploadBIRFragment?.dismiss()
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
        const val REQUEST_CODE = 200
        const val GALLERY_REQUEST_CODE = 1
        const val CAPTURE_PHOTO = 2

    }
}



