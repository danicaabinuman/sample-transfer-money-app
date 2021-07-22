package com.unionbankph.corporate.settings.presentation.fingerprint

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mtramin.rxfingerprint.EncryptionMethod
import com.mtramin.rxfingerprint.RxFingerprint
import com.mtramin.rxfingerprint.data.FingerprintAuthenticationException
import com.mtramin.rxfingerprint.data.FingerprintResult
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum
import com.unionbankph.corporate.databinding.BottomSheetFingerprintBinding
import com.unionbankph.corporate.settings.presentation.SettingsViewModel
import com.unionbankph.corporate.settings.presentation.ShowSettingsError
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class FingerprintBottomSheet :
    BaseBottomSheetDialog<BottomSheetFingerprintBinding, SettingsViewModel>() {

    private var onFingerPrintListener: OnFingerPrintListener? = null

    private var encrypted: String = ""

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(this, viewModelFactory)[SettingsViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowSettingsError -> {
                    (activity as BaseActivity<*,*>).handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        binding.buttonCancel.setOnClickListener { this.dismiss() }
        val bundle = arguments
        val token = bundle?.getString(EXTRA_TOKEN).notNullable()
        decrypt(token)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onFingerPrintListener?.onDismissFingerprintDialog()
    }

    private fun setStatusText(text: String?) {
        text?.let {
            val shake = AnimationUtils.loadAnimation(context, R.anim.anim_shake)
            binding.textViewFingerprintDesc.text = it
            binding.textViewFingerprintDesc.startAnimation(shake)
        }
    }

    private fun setStatusText() {
        if (!RxFingerprint.isAvailable(applicationContext)) {
            binding.textViewFingerprintTitle.text = formatString(R.string.title_fingerprint_not_detected)
            setStatusText(formatString(R.string.msg_fingerprint_not_detected))
            viewModel.updateRecentUser(PromptTypeEnum.BIOMETRIC, false)
            viewModel.deleteFingerPrint()
            onFingerPrintListener?.onErrorFingerprint()
            return
        }
    }

    fun encrypt(context: Context, key: String, toEncrypt: String) {

        if (RxFingerprint.isUnavailable(context)) return

        if (TextUtils.isEmpty(toEncrypt)) return

        RxFingerprint.encrypt(EncryptionMethod.RSA, context, key, toEncrypt)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fingerprintEncryptionResult ->
                    when (fingerprintEncryptionResult?.result) {
                        FingerprintResult.FAILED ->
                            setStatusText(getString(R.string.msg_fingerprint_not_recognized))
                        FingerprintResult.HELP ->
                            setStatusText(fingerprintEncryptionResult.message.nullable())
                        FingerprintResult.AUTHENTICATED -> {
                            encrypted = fingerprintEncryptionResult.encrypted
                            onFingerPrintListener?.setOnEncryptedToken(encrypted)
                        }
                    }
                }, { throwable ->

                    if (RxFingerprint.keyInvalidated(throwable)) {
                        // The keys you wanted to use are invalidated because the user has turned off his
                        // secure lock screen or changed the fingerprints stored on the device
                        // You have to re-encrypt the data to access it
                    }
                    Timber.e(throwable, "encrypt")
                })
            .addTo(disposables)
    }

    private fun decrypt(encrypted: String) {
        setStatusText()
        if (!RxFingerprint.isAvailable(applicationContext)) {
            return
        }
        RxFingerprint.decrypt(EncryptionMethod.RSA, applicationContext, EXTRA_TOKEN, encrypted)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { fingerprintDecryptionResult ->
                    when (fingerprintDecryptionResult?.result) {
                        FingerprintResult.FAILED ->
                            setStatusText(formatString(R.string.msg_fingerprint_not_recognized))
                        FingerprintResult.HELP ->
                            setStatusText(fingerprintDecryptionResult.message.nullable())
                        FingerprintResult.AUTHENTICATED -> {
                            this.dismiss()
                            onFingerPrintListener?.onCompleteFingerprint(
                                fingerprintDecryptionResult.decrypted
                            )
                        }
                    }
                }, { throwable ->
                    Timber.d("throwable.message: ${throwable.message}")
                    if (RxFingerprint.keyInvalidated(throwable)) {
                        resetRecentUserBiometric()
                        // The keys you wanted to use are invalidated because the user has turned off his
                        // secure lock screen or changed the fingerprints stored on the device
                        // You have to re-encrypt the data to access it
                    } else {
                        setStatusText(throwable.message)
                    }
                    if (throwable is FingerprintAuthenticationException &&
                        throwable.message.equals("Fingerprint operation cancelled by user.")) {
                        dismiss()
                    }
                    Timber.e(throwable, "decrypt")
                    // this.dismiss()
                })
            .addTo(disposables)
    }

    private fun resetRecentUserBiometric() {
        val message = formatString(R.string.msg_new_fingerprint_detected)
        binding.textViewFingerprintTitle.text = formatString(R.string.title_new_fingerprint_detected)
        setStatusText(message)
        onFingerPrintListener?.onErrorFingerprint()
        viewModel.updateRecentUser(PromptTypeEnum.BIOMETRIC, false)
        viewModel.deleteFingerPrint()
    }

    fun setOnFingerPrintListener(onFingerPrintListener: OnFingerPrintListener) {
        this.onFingerPrintListener = onFingerPrintListener
    }

    interface OnFingerPrintListener {
        fun setOnEncryptedToken(token: String) = Unit
        fun onCompleteFingerprint(token: String) = Unit
        fun onErrorFingerprint() = Unit
        fun onDismissFingerprintDialog() = Unit
    }

    companion object {

        fun newInstance(token: String, type: String): FingerprintBottomSheet {
            val fragment = FingerprintBottomSheet()
            val bundle = Bundle()
            bundle.putString(EXTRA_TOKEN, token)
            bundle.putString(EXTRA_TYPE, type)
            fragment.arguments = bundle
            return fragment
        }

        const val ENCRYPT_TYPE = "encrypt"
        const val DECRYPT_TYPE = "decrypt"

        const val EXTRA_TOKEN = "token"
        const val EXTRA_TYPE = "type"
    }

    override val layoutId: Int
        get() = R.layout.bottom_sheet_fingerprint

    override val viewModelClassType: Class<SettingsViewModel>
        get() = SettingsViewModel::class.java

    override val bindingBinder: (View) -> BottomSheetFingerprintBinding
        get() = BottomSheetFingerprintBinding::bind
}
