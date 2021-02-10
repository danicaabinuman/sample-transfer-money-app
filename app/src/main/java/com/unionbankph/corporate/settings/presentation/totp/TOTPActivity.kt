package com.unionbankph.corporate.settings.presentation.totp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import kotlinx.android.synthetic.main.activity_generate_totp.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*
import org.jboss.aerogear.security.otp.Totp
import org.jboss.aerogear.security.otp.api.Clock
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class TOTPActivity : BaseActivity<GeneralViewModel>(R.layout.activity_generate_totp) {

    private lateinit var totp: Totp

    private lateinit var clock: Clock

    private var countDownTimer: CountDownTimer? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        initStyle()
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    private fun initStyle() {
        if (isSME) {
            circularProgressBar.progressBackgroundColor =
                ContextCompat.getColor(this, R.color.colorGrey)
        }
    }

    override fun onViewsBound() {
        super.onViewsBound()

        textViewGenerateOTPDesc.text = formatString(
            if (isSME) {
                R.string.msg_generate_totp_sme
            } else {
                R.string.msg_generate_totp
            }
        )
        initDateAndTime()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        buttonDeviceSettings.setOnClickListener {
            startActivityForResult(Intent(Settings.ACTION_DATE_SETTINGS), REQUEST_CODE_SETTINGS)
        }
        buttonReceiveViaSms.setOnClickListener {
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimer()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SETTINGS) {
            initDateAndTime()
        }
    }

    private fun initDateAndTime() {
        if (settingsUtil.isTimeAutomatic()) {
            constraintLayoutGenerateTOTP.visibility = View.VISIBLE
            constraintLayoutFailedGenerateTOTP.visibility = View.GONE
            initTOTP()
        } else {
            constraintLayoutFailedGenerateTOTP.visibility = View.VISIBLE
            constraintLayoutGenerateTOTP.visibility = View.GONE
        }
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
        circularProgressBar.setProgress(
            TimeUnit.SECONDS.toMillis(currentSeconds.toLong()).toDouble(),
            COUNTDOWN_DURATION.toDouble()
        )
        circularProgressBar.setProgressTextAdapter {
            TimeUnit.MILLISECONDS.toSeconds(it.toLong()).toString()
        }
        startTimer(currentSeconds)
    }

    private fun startTimer(seconds: Int) {
        circularProgressBar.isAnimationEnabled = true
        countDownTimer = object : CountDownTimer(
            TimeUnit.SECONDS.toMillis(seconds.toLong()),
            COUNTDOWN_STEP.toLong()
        ) {
            override fun onTick(millisUntilFinished: Long) {
                circularProgressBar.setProgress(
                    millisUntilFinished.toDouble(),
                    COUNTDOWN_DURATION.toDouble()
                )
                circularProgressBar.setProgressTextAdapter {
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

    private fun cancelTimer() {
        countDownTimer?.cancel()
    }

    private fun updateOTP() {
        val otpCode = totp.now()
        Timber.d("otpCode: %s", otpCode)
        textViewOTPCode.text = String.format(
            getString(R.string.param_totp_code),
            otpCode[0],
            otpCode[1],
            otpCode[2],
            otpCode[3],
            otpCode[4],
            otpCode[5]
        )
    }

    companion object {
        const val COUNTDOWN_DURATION = 30000
        const val COUNTDOWN_STEP = 100 // only 100 ms for smoother action
        const val REQUEST_CODE_SETTINGS = 1
    }
}
