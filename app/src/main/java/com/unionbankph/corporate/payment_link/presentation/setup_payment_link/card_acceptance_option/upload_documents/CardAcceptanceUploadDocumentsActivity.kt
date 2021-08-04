package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.*
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_onboarding_upload_photos.viewToolbar
import kotlinx.android.synthetic.main.activity_upload_documents.*
import kotlinx.android.synthetic.main.widget_transparent_rmo_appbar.*
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask

class CardAcceptanceUploadDocumentsActivity :
    BaseActivity<CardAcceptanceUploadDocumentsViewModel>(R.layout.activity_upload_documents),
    CardAcceptanceUploadDocumentFragment.OnUploadBIRDocs {

    private var uploadBIRFragment: CardAcceptanceUploadDocumentFragment? = null
    private var fileUri: Uri? = null
    lateinit var imgView: ImageView
    val REQUEST_CODE = 200

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

    override fun onViewsBound() {
        super.onViewsBound()

        btnSaveAndExit.visibility = View.GONE
        btnUploadBIRDocs.setOnClickListener {
            showbottomSheetDialog()
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

                        val fileUri = data.data!!.toString()
                        val file = File(fileUri)
                        clUploadBIRDocs.visibility = View.VISIBLE
                        val snackbarView = findViewById<TextView>(R.id.snackbar)
                        val snackUploading = Snackbar.make(snackbarView, "Uploading photo...", Snackbar.LENGTH_LONG).setAnchorView(R.id.btnNext)
                        snackUploading.show()

                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed({
                            btnNext.visibility = View.VISIBLE
                        }, 3000)
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
}



