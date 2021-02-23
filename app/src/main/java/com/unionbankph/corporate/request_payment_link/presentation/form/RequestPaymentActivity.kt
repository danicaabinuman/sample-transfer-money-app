package com.unionbankph.corporate.request_payment_link.presentation.form

import android.os.Bundle
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.asDriver
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_check_deposit_form.et_amount
import kotlinx.android.synthetic.main.activity_ebilling_form.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*

class RequestPaymentActivity : BaseActivity<RequestPaymentViewModel>(R.layout.activity_request_payment) {

//    override fun afterLayout(savedInstanceState: Bundle?) {
//        super.afterLayout(savedInstanceState)
//        initToolbar(toolbar, viewToolbar)
//    }
//
//    override fun onViewsBound() {
//        super.onViewsBound()
//        textCounter()
//        initBinding()
//    }
//
//
//    private fun initBinding(){
//        et_amount.asDriver()
//                .subscribe {
//                    viewModel.requestPaymentAmount.value?.checkAmount = et_amount.getNumericValue().toString()
//                }.addTo(disposables)
//    }
//
//    private fun textCounter(){
//
//    }
}