package com.unionbankph.corporate.instapay_qr.presentation.instapay_qr_scanner

import android.view.LayoutInflater
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.databinding.ActivityInstapayQrScannerBinding

class InstapayQrScannerActivity :
    BaseActivity<ActivityInstapayQrScannerBinding, InstapayQrScannerViewModel>() {

    override fun onViewsBound() {
        super.onViewsBound()

        initCamera()
    }

    private fun initCamera() {
        binding.cameraView.open()
    }

    override val viewModelClassType: Class<InstapayQrScannerViewModel>
        get() = InstapayQrScannerViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityInstapayQrScannerBinding
        get() = ActivityInstapayQrScannerBinding::inflate
}