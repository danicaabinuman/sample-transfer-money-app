package com.unionbankph.corporate.instapay_qr.presentation.instapay_qr_scanner

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.Result
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.showToast
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.qrgenerator.RxQrCode
import com.unionbankph.corporate.app.util.FileUtil
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.corporate.data.model.Channel
import com.unionbankph.corporate.corporate.presentation.channel.ChannelActivity
import com.unionbankph.corporate.databinding.ActivityInstapayQrScannerBinding
import com.unionbankph.corporate.fund_transfer.presentation.instapay.InstaPayFormActivity
import com.unionbankph.corporate.fund_transfer.presentation.ubp.UBPFormActivity
import com.unionbankph.corporate.instapay_qr.domain.model.SuccessQRReference
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import javax.inject.Inject


class InstapayQrScannerActivity :
    BaseActivity<ActivityInstapayQrScannerBinding, InstapayQrScannerViewModel>() {

    @Inject
    lateinit var fileUtil: FileUtil

    private lateinit var codeScanner: CodeScanner

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.uiState.observe(this, EventObserver { event ->
            when (event) {
                is UiState.Loading -> showProgressAlertDialog(this::class.java.simpleName)
                is UiState.Complete -> dismissProgressAlertDialog()
                is UiState.Error -> handleOnError(event.throwable)
            }
        })

        viewModel.successQrReference.observe(this, {
            Timber.e(it.toString(), "ksksksksksks")
            it.hasValidationError

            if (it.hasValidationError == true && it.channel == null) {
                // Show Generic Print
                showInvalidQRPrompt()
                return@observe
            } else if (it.hasValidationError == true && it.channel != null) {
                showRestrictionDialog(it.channel!!)
                return@observe
            } else {
                navigateToForm(it)
            }
        })
    }

    private fun navigateToForm(it: SuccessQRReference) {
        val bundle = Bundle()

        when(it.channel?.id) {
            ChannelBankEnum.UBP_TO_UBP.getChannelId() -> {
                bundle.putString(
                    UBPFormActivity.EXTRA_RECIPIENT_DATA,
                    JsonHelper.toJson(it.recipientData)
                )
                navigateUBPFormScreen(bundle, it.channel!!.permission?.id.toString(), it.channel!!)
            }
            ChannelBankEnum.INSTAPAY.getChannelId() -> {
                bundle.putString(
                    InstaPayFormActivity.EXTRA_RECIPIENT_DATA,
                    JsonHelper.toJson(it.recipientData)
                )
                navigateInstapayFormScreen(bundle, it.channel!!.permission?.id.toString(), it.channel!!)
            }
            else -> showInvalidQRPrompt()
        }
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initPermission()
        initViews()
        initCamera()
        getQrResult()
    }


    fun initViews(){

        binding.btnUploadQr.setOnClickListener {
            initPermissionUpload(TAG_QR_CODE)

        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    private fun initPermission() {
        RxPermissions(this)
            .request(Manifest.permission.CAMERA)
            .subscribe { granted ->
                if (granted) {

                } else {
                    initPermission()
                }
            }.addTo(disposables)
    }

    private fun initPermissionUpload(tag: String) {
        RxPermissions(this)
            .request(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .subscribe { granted ->
                if (granted) {
                    openGallery()
                } else {
                    MaterialDialog(this@InstapayQrScannerActivity).show {
                        lifecycleOwner(this@InstapayQrScannerActivity)
                        message(R.string.desc_service_permission)
                        positiveButton(
                            res = R.string.action_ok,
                            click = {
                                it.dismiss()
                                initPermissionUpload(tag)
                            }
                        )
                        negativeButton(
                            res = R.string.action_cancel,
                            click = {
                                it.dismiss()
                                initPermissionUpload(tag)
                            }
                        )
                    }
                }
            }.addTo(disposables)
    }

    private fun showInvalidQRPrompt() {
        showMaterialDialogError(
            "Invalide QR Code",
            "Please try again"
        )
    }

    private fun navigateInstapayFormScreen(
        bundle: Bundle,
        id: String?,
        channel: Channel
    ) {
        bundle.putString(InstaPayFormActivity.EXTRA_ID, id)
        bundle.putString(
            InstaPayFormActivity.EXTRA_CHANNEL,
            JsonHelper.toJson(channel)
        )
        if (channel.serviceFee != null)
            bundle.putString(
                InstaPayFormActivity.EXTRA_SERVICE_FEE,
                JsonHelper.toJson(channel.serviceFee)
            )
        if (channel.customServiceFee != null)
            bundle.putString(
                InstaPayFormActivity.EXTRA_QR_RECIPIENT,
                JsonHelper.toJson(channel.customServiceFee)
            )
        navigator.navigate(
            this,
            InstaPayFormActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateUBPFormScreen(
        bundle: Bundle,
        id: String?,
        channel: Channel
    ) {
        bundle.putString(UBPFormActivity.EXTRA_ID, id)
        bundle.putString(
            UBPFormActivity.EXTRA_CHANNEL,
            JsonHelper.toJson(channel)
        )
        if (channel.serviceFee != null)
            bundle.putString(
                UBPFormActivity.EXTRA_SERVICE_FEE,
                JsonHelper.toJson(channel.serviceFee)
            )
        navigator.navigate(
            this,
            UBPFormActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun hasPermissionToProceed(data: Channel): Boolean {
        return data.hasSourceAccount &&
                (data.hasApprovalRule ||
                        (intent.getStringExtra(ChannelActivity.EXTRA_PAGE) != null &&
                                intent.getStringExtra(ChannelActivity.EXTRA_PAGE) == ChannelActivity.PAGE_BENEFICIARY)) &&
                (data.hasRulesAllowTransaction ||
                        (intent.getStringExtra(ChannelActivity.EXTRA_PAGE) != null &&
                                intent.getStringExtra(ChannelActivity.EXTRA_PAGE) == ChannelActivity.PAGE_BENEFICIARY)) &&
                data.hasPermission
    }

    private fun showRestrictionDialog(channel: Channel) {
        if (!channel.hasSourceAccount) {
            val currencyStringBuilder = StringBuilder()
            channel.currencies.forEachIndexed { index, currency ->
                when (channel.currencies.size) {
                    index.plus(1) -> {
                        if (channel.currencies.size == 1) {
                            currencyStringBuilder.append(currency.name)
                        } else {
                            currencyStringBuilder.append("or " + currency.name)
                        }
                    }
                    else -> currencyStringBuilder.append(currency.name + ", ")
                }
            }
            val title = formatString(
                R.string.title_no_currency_account_enrolled,
                currencyStringBuilder.toString()
            )

            val message = if (channel.currencies.size > 1) {
                formatString(
                    R.string.params_no_currency_account_enrolled_more_than_one,
                    ConstantHelper.Text.getChannelByChannelId(channel.id),
                    currencyStringBuilder.toString()
                )
            } else {
                formatString(
                    R.string.params_no_currency_account_enrolled,
                    ConstantHelper.Text.getChannelByChannelId(channel.id),
                    currencyStringBuilder.toString()
                )
            }
            showMaterialDialogError(title, message)
        } else if (!channel.hasApprovalRule) {
            showMaterialDialogError(
                formatString(R.string.params_title_no_approval_hierarchy_dialog),
                formatString(
                    R.string.params_no_approval_hierarchy,
                    ConstantHelper.Text.getChannelByChannelId(channel.id)
                )
            )
        } else if (!channel.hasRulesAllowTransaction) {
            showMaterialDialogError(
                formatString(
                    R.string.params_title_no_transaction_allowed,
                    ConstantHelper.Text.getChannelByChannelId(channel.id)
                ),
                formatString(
                    R.string.params_no_transaction_allowed,
                    ConstantHelper.Text.getChannelByChannelId(channel.id)
                )
            )
        } else if (!channel.hasPermission) {
            showMaterialDialogError(
                message = formatString(
                    R.string.params_no_permission_channel,
                    ConstantHelper.Text.getChannelByChannelId(channel.id)
                )
            )
        }
    }

    private fun initCamera() {

        codeScanner = CodeScanner(this, binding.qrScanner)

        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = false // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        codeScanner.startPreview()
    }

    private fun getQrResult(){

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                viewModel.submitQrScanned(
                    qrString = it.toString()
                )
            }
        }

        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun scanFromImage(){


    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun openGallery(){
        if (Build.VERSION.SDK_INT < 19) {
            var intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Choose pictures"),
                REQUEST_CODE
            )
        } else {
            var intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {

            when (requestCode) {
                REQUEST_CODE -> {
                    data?.data?.let {
                        try {
                            val filePath = fileUtil.getPath(it)
                            RxQrCode.scanFromPicture(filePath)
                                .subscribeOn(schedulerProvider.io())
                                .observeOn(schedulerProvider.ui())
                                .subscribe({ result ->
                                    viewModel.submitQrScanned(
                                        qrString = result.toString()
                                    )
                                }, {
                                    showMaterialDialogError(
                                        message = formatString(R.string.error_code_not_found)
                                    )
                                }).addTo(disposables)
                        } catch (e: Exception) {
                            showMaterialDialogError(
                                message = formatString(R.string.error_file_not_supported)
                            )
                        }
                    }
                    return

                }
            }
        }
    }

    companion object{
        const val TAG_QR_CODE = "qr_code"

        const val REQUEST_CODE = 2
    }

    override val viewModelClassType: Class<InstapayQrScannerViewModel>
        get() = InstapayQrScannerViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityInstapayQrScannerBinding
        get() = ActivityInstapayQrScannerBinding::inflate
}