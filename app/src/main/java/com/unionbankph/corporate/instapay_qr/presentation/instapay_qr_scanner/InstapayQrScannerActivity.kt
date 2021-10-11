package com.unionbankph.corporate.instapay_qr.presentation.instapay_qr_scanner

import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.widget.Toast
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.Result
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.subscribeOnIo
import com.unionbankph.corporate.app.common.widget.qrgenerator.RxQrCode
import com.unionbankph.corporate.databinding.ActivityInstapayQrScannerBinding
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers





class InstapayQrScannerActivity :
    BaseActivity<ActivityInstapayQrScannerBinding, InstapayQrScannerViewModel>() {

    private lateinit var codeScanner: CodeScanner

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
        initCamera()
        getQrResult()
    }

    fun initViews(){

        binding.btnUploadQr.setOnClickListener {
            openGallery()
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    private fun initCamera() {

        codeScanner = CodeScanner(this, binding.qrScanner)

        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = false // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        codeScanner.startPreview()
    }

    private fun getQrResult(){

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                Toast.makeText(this, "Scan Result ${it.text}", Toast.LENGTH_LONG).show()
            }
        }

        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun scanFromImage(){

//        RxQrCode.scanFromPicture(realPath)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({ result: Result? -> }
//            ) { e: Throwable? ->
//                Toast.makeText(
//                    context, "code not found",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun openGallery(){
        if (Build.VERSION.SDK_INT < 19) {
            var intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Choose pictures"),
                REQUEST_CODE
            )
        } else {
            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    companion object{
        const val REQUEST_CODE = 2
    }

    override val viewModelClassType: Class<InstapayQrScannerViewModel>
        get() = InstapayQrScannerViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityInstapayQrScannerBinding
        get() = ActivityInstapayQrScannerBinding::inflate
}