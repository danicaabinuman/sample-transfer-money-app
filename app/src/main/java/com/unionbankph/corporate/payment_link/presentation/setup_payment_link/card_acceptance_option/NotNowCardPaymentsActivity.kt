package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option

import android.text.Editable
import android.text.TextWatcher
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_not_now_accept_card_payments.*

class NotNowCardPaymentsActivity : BaseActivity<YesAcceptCardPaymentsViewModel>(R.layout.activity_not_now_accept_card_payments) {
    override fun onViewsBound() {
        super.onViewsBound()

        enterMonthlyVolume()
    }

    private fun enterMonthlyVolume(){
        etMonthlyVolume.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val monthlyAmount = s.toString()
                if (start < 5){
                    sliderMonthlyVolume.value = 10000F
                } else if (start < 8){
                    sliderMonthlyVolume.value = monthlyAmount.toFloat()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }
}