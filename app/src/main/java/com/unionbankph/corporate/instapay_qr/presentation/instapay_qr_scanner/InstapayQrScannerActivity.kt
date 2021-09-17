package com.unionbankph.corporate.instapay_qr.presentation.instapay_qr_scanner

import android.Manifest
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.databinding.ActivityInstapayQrScannerBinding
import io.reactivex.rxkotlin.addTo


class InstapayQrScannerActivity :
    BaseActivity<ActivityInstapayQrScannerBinding, InstapayQrScannerViewModel>() {

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
    }

    override fun onStart() {
        super.onStart()
        initCamera()
    }

    fun initViews(){

        binding.btnUploadQr.setOnClickListener {
            openGallery()
        }
    }

    private fun initCamera() {
        binding.cameraView.open()
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