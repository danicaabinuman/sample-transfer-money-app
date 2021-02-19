package com.unionbankph.corporate.request_payment_link.presentation

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.trimmedLength
import androidx.core.widget.addTextChangedListener
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.asDriver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_check_deposit_form.*
import kotlinx.android.synthetic.main.activity_check_deposit_form.et_amount
import kotlinx.android.synthetic.main.activity_request_payment.*
import kotlinx.android.synthetic.main.fragment_send_request.*

class RequestPaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_payment)

        initListener()
        et_amount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateForm()

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().trim().trimmedLength()==0) {
                    et_amount.isEnabled
                }
            }
        })

        et_paymentFor.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateForm()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        et_notes.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateForm()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        dropdownPaymentLinkExpory.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateForm()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun validateForm(){
        var amountString = et_amount.text.toString()
        var paymentForString = et_paymentFor.text.toString()
        var notesString = et_notes.text.toString()
        var paymentLinkExpiry = dropdownPaymentLinkExpory.text.toString()


    }
//    override fun onViewsBound() {
//        super.onViewsBound()
//        textCounter()
//        initBinding()
//    }


    private fun initBinding(){
//        et_amount.asDriver()
//                .subscribe {
//                    viewModel.requestPaymentAmount.value?.checkAmount = et_amount.getNumericValue().toString()
//                }.addTo(disposables)
    }

    private fun initListener(){
        btnRequestPaymentCancel.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun textCounter(){

    }
}