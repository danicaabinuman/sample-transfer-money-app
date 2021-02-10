package com.unionbankph.corporate.ebilling.presentation.generate

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseViewModel
import com.unionbankph.corporate.app.common.widget.qrgenerator.RxQrCode
import com.unionbankph.corporate.ebilling.domain.form.EBillingForm
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Created by herald on 10/28/20
 */
class EBillingGenerateViewModel
@Inject constructor(
    val context: Context
) : BaseViewModel() {

    private val _eBillingForm = MutableLiveData<EBillingForm>()

    val eBillingForm: LiveData<EBillingForm> get() = _eBillingForm

    private val _shareEBilling = MutableLiveData<File>()

    val qrCodeFile: LiveData<File> get() = _shareEBilling

    fun parseEBillingForm(eBillingForm: EBillingForm?) {
        _eBillingForm.value = eBillingForm
    }

    fun generateQRCode(qrCode: String) {
        RxQrCode.generateQrCodeFile(
            context,
            qrCode,
            context.resources.getDimension(R.dimen.image_view_ebilling_qr).toInt(),
            context.resources.getDimension(R.dimen.image_view_ebilling_qr).toInt()
        )
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _shareEBilling.value = it
                }, {
                    Timber.e(it, "showShareableContent")
                }
            ).addTo(disposables)
    }

}