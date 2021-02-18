package com.unionbankph.corporate.request_payment_link.presentation

import android.os.Bundle
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.asDriver
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_check_deposit_form.*
import kotlinx.android.synthetic.main.activity_check_deposit_form.et_amount
import kotlinx.android.synthetic.main.activity_request_payment.*

class RequestPaymentActivity : BaseActivity<RequestPaymentViewModel>(R.layout.activity_request_payment) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_payment)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        textCounter()
        initBinding()
    }


    private fun initBinding(){
        et_amount.asDriver()
                .subscribe {
                    viewModel.requestPaymentAmount.value?.checkAmount = et_amount.getNumericValue().toString()
                }.addTo(disposables)
    }

    private fun textCounter(){

    }
}