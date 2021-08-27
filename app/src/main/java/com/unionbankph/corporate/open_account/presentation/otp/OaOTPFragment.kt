package com.unionbankph.corporate.open_account.presentation.otp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.widget.edittext.PincodeEditText
import com.unionbankph.corporate.databinding.FragmentOaOtpBinding
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit


class OaOTPFragment :
    BaseFragment<FragmentOaOtpBinding, OaOTPViewModel>(),
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
                binding.parentLayout,
                binding.btnSubmit,
                binding.editTextHidden,
                binding.viewPinCode.etPin1,
                binding.viewPinCode.etPin2,
                binding.viewPinCode.etPin3,
                binding.viewPinCode.etPin4,
                binding.viewPinCode.etPin5,
                binding.viewPinCode.etPin6
            )
        pinCodeEditText.setOnOTPCallback(this)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()

        RxView.clicks(binding.buttonGenerateOTP)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            ).subscribe {
//                viewModel.resendOTP()
                findNavController().navigate(R.id.action_otp_to_nominate_password)
            }.addTo(disposables)
        RxView.clicks(binding.btnSubmit)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                //                viewMode.submitOTP()
            }.addTo(disposables)
        RxView.clicks(binding.btnResend)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
//                viewModel.resendOTP()
            }.addTo(disposables)
    }

    override fun onSubmitOTP(otpCode: String) {
        findNavController().navigate(R.id.action_otp_to_nominate_password)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentOaOtpBinding
        get() = FragmentOaOtpBinding::inflate

    override val viewModelClassType: Class<OaOTPViewModel>
        get() = OaOTPViewModel::class.java
}