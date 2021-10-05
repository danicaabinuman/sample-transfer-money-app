package com.unionbankph.corporate.settings.presentation.fingerprint

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.base.BaseBottomSheetDialog
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.common.presentation.constant.PromptTypeEnum
import com.unionbankph.corporate.databinding.BottomSheetFaceIdBinding
import com.unionbankph.corporate.settings.presentation.SettingsViewModel
import com.unionbankph.corporate.settings.presentation.ShowSettingsError
import java.nio.charset.Charset

class FaceIDBottomSheet :
    BaseBottomSheetDialog<BottomSheetFaceIdBinding, SettingsViewModel>() {

    private var onFaceIdListener: OnFaceIdListener? = null
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

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
        biometricPrompt(token)

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onFaceIdListener?.onDismissFaceIDialog()
    }

     fun biometricPrompt(token: String){
         setStatusText()
         if (BiometricManager.from(applicationContext).canAuthenticate() != BiometricManager
                 .BIOMETRIC_SUCCESS) { return }
         if (BiometricManager.from(applicationContext).canAuthenticate() == BiometricManager
                 .BIOMETRIC_SUCCESS) {
             val executor = ContextCompat.getMainExecutor(applicationContext)
             biometricPrompt =
                 BiometricPrompt(this, executor,
                     object : BiometricPrompt.AuthenticationCallback() {
                         override fun onAuthenticationError(errorCode: Int,
                                                            errString: CharSequence) {
                             setStatusText(errString.toString().nullable())
                             if(errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                 dismiss()
                             }
                         }
                         override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                             dismiss()
                             onFaceIdListener?.onCompleteFaceID(token)
                         }

                         override fun onAuthenticationFailed() {
                             setStatusText(formatString(R.string.msg_face_id_not_recognized))
                         }
                     })


             promptInfo =  BiometricPrompt.PromptInfo.Builder()
                 .setTitle(applicationContext.getString(R.string.title_login_in_using_faceid))
                 .setSubtitle(applicationContext.getString(R.string.confirm_fingerprint_description))
                 .setNegativeButtonText(applicationContext.getString(R.string.action_cancel))
                 .setConfirmationRequired(false)
                 .build()
             biometricPrompt.authenticate(promptInfo)


         }

    }

    private fun setStatusText(text: String?) {
        text?.let {
            val shake = AnimationUtils.loadAnimation(context, R.anim.anim_shake)
            binding.textViewFaceIdDesc.text = it
            binding.textViewFaceIdDesc.startAnimation(shake)
        }
    }

    private fun setStatusText() {
        if (BiometricManager.from(applicationContext).canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE){
            binding.textViewFaceIdTitle.text = formatString(R.string.title_face_id_not_detected)
            setStatusText(formatString(R.string.msg_face_id_not_detected))
            viewModel.updateRecentUser(PromptTypeEnum.BIOMETRIC, false)
            viewModel.deleteFingerPrint()
            onFaceIdListener?.onErrorFaceID()
            return
        }
    }

    fun setOnFaceIdListener(onFaceIdListener: OnFaceIdListener) {
        this.onFaceIdListener = onFaceIdListener
    }


    interface OnFaceIdListener {
        fun onCompleteFaceID(token: String) = Unit
        fun onErrorFaceID() = Unit
        fun onDismissFaceIDialog() = Unit
    }

    companion object {

        fun newInstance(token: String, type: String): FaceIDBottomSheet {
            val fragment = FaceIDBottomSheet()
            val bundle = Bundle()
            bundle.putString(EXTRA_TOKEN, token)
            bundle.putString(EXTRA_TYPE, type)
            fragment.arguments = bundle
            return fragment
        }

        const val DECRYPT_TYPE = "decrypt"

        const val EXTRA_TOKEN = "token"
        const val EXTRA_TYPE = "type"
    }

    override val bindingBinder: (View) -> BottomSheetFaceIdBinding
        get() = BottomSheetFaceIdBinding::bind

    override val layoutId: Int
        get() = R.layout.bottom_sheet_face_id

    override val viewModelClassType: Class<SettingsViewModel>
        get() = SettingsViewModel::class.java
}