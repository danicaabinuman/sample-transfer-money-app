package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.databinding.ActivityUploadDocumentsBinding
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.NotNowCardPaymentsActivity
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask

class CardAcceptanceUploadDocumentsActivity :
    BaseActivity<ActivityUploadDocumentsBinding, CardAcceptanceUploadDocumentsViewModel>(),
    CardAcceptanceUploadDocumentFragment.OnUploadBIRDocs {

    private var uploadBIRFragment: CardAcceptanceUploadDocumentFragment? = null
    private var fileUri: Uri? = null
    lateinit var imgView: ImageView
    val REQUEST_CODE = 200

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
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

        binding.viewToolbar.btnSaveAndExit.visibility = View.GONE
        binding.btnUploadBIRDocs.setOnClickListener {
            showbottomSheetDialog()
        }
        binding.btnNext.setOnClickListener {
            if (binding.clPreviewBIR.isShown){
                binding.clPreviewBIR.visibility = View.GONE
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
//                        clUploadBIRDocs.visibility = View.GONE
//                        clPreviewBIR.visibility = View.VISIBLE

                        val fileUri = data.data!!
//                        val file = File(fileUri)
//                        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
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
                        binding.clPreviewBIR.visibility = View.VISIBLE
                        binding.btnNext.visibility = View.VISIBLE

                    }
//                        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
//                        val pdfRenderer = PdfRenderer(fileDescriptor)
//                        val rendererPage = pdfRenderer.openPage(0)
//                        val rendererPageWidth = rendererPage.width
//                        val rendererPageHeight = rendererPage.height
//                        val bitmap = Bitmap.createBitmap(rendererPageWidth, rendererPageHeight, Bitmap.Config.ARGB_8888)
//                        rendererPage.render(bitmap,null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
//                        imgView.setImageBitmap(bitmap)
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
                REQUEST_CODE
            )
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/pdf"
            startActivityForResult(intent, REQUEST_CODE)
        }

    }

    override fun openFileManager() {
        openFiles()
        uploadBIRFragment?.dismiss()
    }

    override val bindingInflater: (LayoutInflater) -> ActivityUploadDocumentsBinding
        get() = ActivityUploadDocumentsBinding::inflate

    override val viewModelClassType: Class<CardAcceptanceUploadDocumentsViewModel>
        get() = CardAcceptanceUploadDocumentsViewModel::class.java
}



