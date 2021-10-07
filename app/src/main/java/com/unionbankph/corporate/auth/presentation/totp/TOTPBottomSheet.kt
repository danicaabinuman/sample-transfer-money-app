package com.unionbankph.corporate.auth.presentation.totp

import android.content.DialogInterface
import android.os.CountDownTimer
import android.view.View
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.databinding.BottomSheetTotpBinding
import io.reactivex.rxkotlin.addTo
import org.jboss.aerogear.security.otp.Totp
import org.jboss.aerogear.security.otp.api.Clock
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class TOTPBottomSheet :
    BaseBottomSheetDialog<BottomSheetTotpBinding, GeneralViewModel>() {

    private lateinit var totp: Totp

    private lateinit var clock: Clock

    private var countDownTimer: CountDownTimer? = null

    private lateinit var callbackListener: OnCallbackListener

    override fun onViewsBound() {
        super.onViewsBound()

        initClickEvents()
        initTOTP()
    }

    private fun initClickEvents() {
        RxView.clicks(binding.buttonContinue)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                callbackListener.onContinueClicked(totp.now())
                this.dismiss()
            }
            .addTo(disposables)

        RxView.clicks(binding.buttonCancel)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                callbackListener.onCancelClicked()
                this.dismiss()
            }
            .addTo(disposables)
    }

    private fun initTOTP() {
        clock = Clock()
        totp = Totp(sharedPreferenceUtil.totpTokenPref().get(), clock)
        updateOTP()
        val calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"))
        var currentSeconds = ((calendar.timeInMillis / 1000) % 60).toInt()
        currentSeconds.apply {
            if (currentSeconds >= resources.getInteger(R.integer.time_interval_totp_code)) {
                currentSeconds -= resources.getInteger(R.integer.time_interval_totp_code)
                currentSeconds =
                    resources.getInteger(R.integer.time_interval_totp_code) - currentSeconds
            } else {
                currentSeconds =
                    resources.getInteger(R.integer.time_interval_totp_code) - currentSeconds
            }
        }
        binding.circularProgressBar.setProgress(
            TimeUnit.SECONDS.toMillis(currentSeconds.toLong()).toDouble(),
            COUNTDOWN_DURATION.toDouble()
        )
        binding.circularProgressBar.setProgressTextAdapter {
            TimeUnit.MILLISECONDS.toSeconds(it.toLong()).toString()
        }
        startTimer(currentSeconds)
    }

    private fun startTimer(seconds: Int) {
        binding.circularProgressBar.isAnimationEnabled = true
        countDownTimer = object : CountDownTimer(
            TimeUnit.SECONDS.toMillis(seconds.toLong()),
            COUNTDOWN_STEP.toLong()
        ) {
            override fun onTick(millisUntilFinished: Long) {
                binding.circularProgressBar.setProgress(
                    millisUntilFinished.toDouble(),
                    COUNTDOWN_DURATION.toDouble()
                )
                binding.circularProgressBar.setProgressTextAdapter {
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toString()
                }
            }

            override fun onFinish() {
                this.cancel()
                updateOTP()
                startTimer(resources.getInteger(R.integer.time_interval_totp_code))
            }
        }
        countDownTimer?.start()
    }

    private fun updateOTP() {
        val otpCode = totp.now()
        Timber.d("otpCode: %s", otpCode)
        binding.textViewOTP.text = String.format(
            getString(R.string.param_totp_code),
            otpCode[0],
            otpCode[1],
            otpCode[2],
            otpCode[3],
            otpCode[4],
            otpCode[5]
        )
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        countDownTimer?.cancel()
    }

    fun setCallbackListener(onPositiveClick: (totp: String) -> Unit, onCancelClicked: (() -> Unit)? = null) {
        callbackListener = object : OnCallbackListener {
            override fun onCancelClicked() { onCancelClicked?.invoke() }
            override fun onContinueClicked(totp: String) = onPositiveClick(totp)
        }
    }

    interface OnCallbackListener {
        fun onCancelClicked ()
        fun onContinueClicked (totp: String)
    }

    companion object {
        const val COUNTDOWN_DURATION = 30000
        const val COUNTDOWN_STEP = 100 // only 100 ms for smoother action
        const val REQUEST_CODE_SETTINGS = 1
    }

    override val bindingBinder: (View) -> BottomSheetTotpBinding
        get() = BottomSheetTotpBinding::bind

    override val layoutId: Int
        get() = R.layout.bottom_sheet_totp

    override val viewModelClassType: Class<GeneralViewModel>
        get() = GeneralViewModel::class.java
}