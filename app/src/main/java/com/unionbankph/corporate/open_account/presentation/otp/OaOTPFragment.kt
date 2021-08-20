package com.unionbankph.corporate.open_account.presentation.otp

import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.widget.edittext.PincodeEditText
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit
import kotlinx.android.synthetic.main.fragment_oa_otp.*
import kotlinx.android.synthetic.main.fragment_oa_otp.btnResend
import kotlinx.android.synthetic.main.fragment_oa_otp.btnSubmit
import kotlinx.android.synthetic.main.fragment_oa_otp.buttonGenerateOTP
import kotlinx.android.synthetic.main.fragment_oa_otp.editTextHidden
import kotlinx.android.synthetic.main.fragment_oa_otp.parentLayout
import kotlinx.android.synthetic.main.widget_pin_code.*


class OaOTPFragment : BaseFragment<OaOTPViewModel>(R.layout.fragment_oa_otp),
    PincodeEditText.OnOTPCallback {

    private val openAccountActivity by lazyFast { getAppCompatActivity() as OpenAccountActivity }

    private lateinit var pinCodeEditText: PincodeEditText

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    private fun init() {
        pinCodeEditText =
            PincodeEditText(
                openAccountActivity,
                parentLayout,
                btnSubmit,
                editTextHidden,
                etPin1,
                etPin2,
                etPin3,
                etPin4,
                etPin5,
                etPin6
            )
        pinCodeEditText.setOnOTPCallback(this)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()

        RxView.clicks(buttonGenerateOTP)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            ).subscribe {
//                viewModel.resendOTP()
                findNavController().navigate(R.id.action_otp_to_nominate_password)
            }.addTo(disposables)
        RxView.clicks(btnSubmit)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                //                viewMode.submitOTP()
            }.addTo(disposables)
        RxView.clicks(btnResend)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
//                viewModel.resendOTP()
            }.addTo(disposables)
    }

    override fun onSubmitOTP(otpCode: String) {

    }
}