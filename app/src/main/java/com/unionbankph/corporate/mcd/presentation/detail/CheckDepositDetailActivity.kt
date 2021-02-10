package com.unionbankph.corporate.mcd.presentation.detail

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.jakewharton.rxbinding2.view.RxView
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.qrgenerator.RxQrCode
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositScreenEnum
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositStatusEnum
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositTypeEnum
import com.unionbankph.corporate.mcd.presentation.log.CheckDepositActivityLogActivity
import com.unionbankph.corporate.mcd.presentation.preview.CheckDepositPreviewActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_check_deposit_detail.*
import kotlinx.android.synthetic.main.view_check_details_share.*
import kotlinx.android.synthetic.main.widget_button_share_outline.*
import kotlinx.android.synthetic.main.widget_header_transaction_summary.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by herald25santos on 2019-11-05
 */
class CheckDepositDetailActivity :
    BaseActivity<CheckDepositDetailViewModel>(R.layout.activity_check_deposit_detail) {

    private val id by lazyFast { intent.getStringExtra(EXTRA_ID) }

    private lateinit var checkDeposit: CheckDeposit

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setToolbarTitle(
            tvToolbar,
            formatString(R.string.title_checks_deposit_details)
        )
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initCheckDepositViewModel()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        buttonViewLogs.setOnClickListener {
            navigateActivityLogScreen()
        }
        RxView.clicks(buttonShare)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                initPermission()
            }.addTo(disposables)
    }

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
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

    private fun initCheckDepositViewModel() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[CheckDepositDetailViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowCheckDepositDetailLoading -> {
                    constraintLayout.visibility(false)
                    viewLoadingState.visibility(true)
                }
                is ShowCheckDepositDetailDismissLoading -> {
                    viewLoadingState.visibility(false)
                }
                is ShowCheckDepositDetailGetCheckDeposit -> {
                    checkDeposit = it.data
                    initViews()
                }
                is ShowCheckDepositDetailError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.getCheckDeposit(id.notNullable())
    }

    private fun initViews() {
        constraintLayout.visibility(true)
        textViewStatus.setContextCompatTextColor(
            ConstantHelper.Color.getTextColor(checkDeposit.status)
        )
        textViewStatus.text =
            if (checkDeposit.status?.type == CheckDepositStatusEnum.REJECTED.name) {
                formatString(
                    R.string.param_transaction_status,
                    checkDeposit.status?.description,
                    "Reason: \"${checkDeposit.status?.detailedDescription.notEmpty()}\""
                ).toHtmlSpan()
            } else {
                "<b>${checkDeposit.status?.description}</b>".toHtmlSpan()
            }
        textViewRemarks.text = checkDeposit.remarks.notEmpty()
        textViewCreatedBy.text = checkDeposit.createdBy
        textViewCreatedDate.text = viewUtil.getStringOrEmpty(
            viewUtil.getDateFormatByDateString(
                checkDeposit.createdDate,
                DateFormatEnum.DATE_FORMAT_ISO_WITHOUT_T.value,
                DateFormatEnum.DATE_FORMAT_DEFAULT.value
            )
        )
        textViewBankOfCheck.text = checkDeposit.issuer
        textViewCheckAccountNumber.text = checkDeposit.sourceAccount
        textViewCheckAccountName.text = checkDeposit.sourceAccountName
        textViewCheckNumber.text = checkDeposit.checkNumber
        textViewReferenceNumber.text = checkDeposit.referenceNumber
        textViewDateOnCheck.text = viewUtil.getDateFormatByDateString(
            checkDeposit.checkDate,
            DateFormatEnum.DATE_FORMAT_ISO_WITHOUT_T.value,
            DateFormatEnum.DATE_FORMAT_DATE.value
        )
        textViewAccount.text = formatString(
            R.string.params_two_format,
            checkDeposit.targetAccountName,
            viewUtil.getAccountNumberFormat(checkDeposit.targetAccount)
        ).toHtmlSpan()
        textViewAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
            checkDeposit.checkAmount?.value.toString(),
            checkDeposit.checkAmount?.currency
        )
        if (checkDeposit.customServiceFee != null &&
            checkDeposit.serviceFee != null
        ) {
            textViewServiceFee.text = formatString(
                R.string.value_service,
                autoFormatUtil.formatWithTwoDecimalPlaces(
                    checkDeposit.customServiceFee?.value,
                    checkDeposit.customServiceFee?.currency
                )
            )
            textViewServiceDiscountFee.visibility(true)
            viewBorderServiceDiscountFee.visibility(true)
        } else {
            textViewServiceDiscountFee.visibility(false)
            viewBorderServiceDiscountFee.visibility(false)
        }
        if (checkDeposit.serviceFee != null) {
            if (checkDeposit.customServiceFee != null) {
                textViewServiceDiscountFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        checkDeposit.serviceFee?.value,
                        checkDeposit.serviceFee?.currency
                    )
                )
            } else {
                textViewServiceFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        checkDeposit.serviceFee?.value,
                        checkDeposit.serviceFee?.currency
                    )
                )
            }
        } else {
            textViewServiceFee.text = getString(R.string.value_service_fee_free)
        }
        if (checkDeposit.status?.type == CheckDepositStatusEnum.FOR_CLEARING.name) {
            val imageViewFrontOfCheck =
                viewFrontOfCheck.findViewById<AppCompatImageView>(R.id.imageView)
            val imageViewBackOfCheck =
                viewBackOfCheck.findViewById<AppCompatImageView>(R.id.imageView)
            imageViewFrontOfCheck.loaderImagePreviewByUrl(
                checkDeposit.frontPath.notNullable(),
                viewFrontOfCheck
            )
            imageViewBackOfCheck.loaderImagePreviewByUrl(
                checkDeposit.backPath.notNullable(),
                viewBackOfCheck
            )
            imageViewFrontOfCheck.setOnClickListener {
                navigateCheckDepositPreviewScreen(
                    CheckDepositTypeEnum.FRONT_OF_CHECK,
                    checkDeposit.frontPath.notNullable()
                )
            }
            imageViewBackOfCheck.setOnClickListener {
                navigateCheckDepositPreviewScreen(
                    CheckDepositTypeEnum.BACK_OF_CHECK,
                    checkDeposit.backPath.notNullable()
                )
            }
        } else {
            viewBorderServiceFee.visibility(false)
            textViewFrontOfCheckTitle.visibility(false)
            viewFrontOfCheck.visibility(false)
            viewBorderFrontOfCheck.visibility(false)
            textViewBackOfCheckTitle.visibility(false)
            viewBackOfCheck.visibility(false)
        }
        initShareDetailStatus(checkDeposit)
    }

    private fun initShareDetailStatus(checkDeposit: CheckDeposit) {
        textViewShareCreatedBy.text = checkDeposit.createdBy
        textViewShareCreatedOn.text = viewUtil.getStringOrEmpty(
            viewUtil.getDateFormatByDateString(
                checkDeposit.createdDate,
                DateFormatEnum.DATE_FORMAT_ISO_WITHOUT_T.value,
                DateFormatEnum.DATE_FORMAT_DEFAULT.value
            )
        )
        textViewShareDateDownloaded.text = viewUtil.getCurrentDateString()
        textViewShareBankOfCheck.text = checkDeposit.issuer
        textViewShareCheckAccountNumber.text = checkDeposit.sourceAccount
        textViewShareCheckNumber.text = checkDeposit.checkNumber
        textViewShareRemarks.text = checkDeposit.remarks.notEmpty()
        if (checkDeposit.referenceNumber != null) {
            textViewShareReferenceNumber.text = checkDeposit.referenceNumber.notEmpty()
        } else {
            textViewShareReferenceNumberTitle.isVisible = false
            textViewShareReferenceNumber.isVisible = false
            viewBorderShareReferenceNumber.isVisible = false
        }
        textViewShareDateOnCheck.text = viewUtil.getDateFormatByDateString(
            checkDeposit.checkDate,
            DateFormatEnum.DATE_FORMAT_ISO_WITHOUT_T.value,
            DateFormatEnum.DATE_FORMAT_DATE.value
        )
        textViewShareDepositTo.text = formatString(
            R.string.params_two_format,
            checkDeposit.targetAccountName,
            viewUtil.getAccountNumberFormat(checkDeposit.targetAccount)
        ).toHtmlSpan()
        textViewShareAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
            checkDeposit.checkAmount?.value.toString(),
            checkDeposit.checkAmount?.currency
        )
        if (checkDeposit.customServiceFee != null &&
            checkDeposit.serviceFee != null
        ) {
            textViewShareServiceFee.text = formatString(
                R.string.value_service,
                autoFormatUtil.formatWithTwoDecimalPlaces(
                    checkDeposit.customServiceFee?.value,
                    checkDeposit.customServiceFee?.currency
                )
            )
            textViewShareServiceDiscountFee.visibility(true)
            viewBorderShareServiceDiscountFee.visibility(true)
        } else {
            textViewShareServiceDiscountFee.visibility(false)
            viewBorderShareServiceDiscountFee.visibility(false)
        }
        if (checkDeposit.serviceFee != null) {
            if (checkDeposit.customServiceFee != null) {
                textViewShareServiceDiscountFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        checkDeposit.serviceFee?.value,
                        checkDeposit.serviceFee?.currency
                    )
                )
            } else {
                textViewShareServiceFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        checkDeposit.serviceFee?.value,
                        checkDeposit.serviceFee?.currency
                    )
                )
            }
        } else {
            textViewShareServiceFee.text = getString(R.string.value_service_fee_free)
        }
        initShareStatus(checkDeposit)
    }

    private fun initShareStatus(checkDeposit: CheckDeposit) {
        when (checkDeposit.status?.type) {
            CheckDepositStatusEnum.FOR_CLEARING.name -> {
                setTransactionHeaderDetails(
                    background = R.drawable.bg_rectangle_yellow,
                    logo = R.drawable.ic_fund_transfer_clock_yellow,
                    headerTextColor = R.color.colorPendingStatus,
                    headerTitle = formatString(R.string.title_check_deposit_for_clearing),
                    headerContent = formatString(R.string.msg_check_deposit_for_clearing)
                )
            }
            CheckDepositStatusEnum.POSTED.name -> {
                setTransactionHeaderDetails(
                    background = R.drawable.bg_rectangle_green,
                    logo = R.drawable.ic_fund_transfer_check_green,
                    headerTextColor = R.color.colorSuccess,
                    headerTitle = formatString(R.string.title_check_deposit_approved),
                    headerContent = formatString(R.string.msg_check_deposit_approved)
                )
            }
            CheckDepositStatusEnum.REJECTED.name -> {
                setTransactionHeaderDetails(
                    background = R.drawable.bg_rectangle_red,
                    logo = R.drawable.ic_fund_transfer_warning_red,
                    headerTextColor = R.color.colorRejectedStatus,
                    headerTitle = formatString(R.string.title_check_deposit_failed),
                    headerContent = formatString(
                        R.string.msg_check_deposit_failed,
                        checkDeposit.checkNumber,
                        checkDeposit.status?.detailedDescription
                    )
                )
            }
            else -> {
                setTransactionHeaderDetails(
                    background = R.drawable.bg_rectangle_gray,
                    logo = R.drawable.ic_fund_transfer_calendar_gray,
                    headerTextColor = R.color.colorInfo,
                    headerTitle = formatString(R.string.title_fund_transfer_for_processing),
                    headerContent = formatString(R.string.msg_fund_transfer_default_status)
                )
            }
        }
    }

    private fun setTransactionHeaderDetails(
        background: Int,
        logo: Int,
        headerTextColor: Int,
        headerTitle: String,
        headerContent: String
    ) {
        linearLayoutHeaderStatus.setContextCompatBackground(background)
        imageViewHeader.setImageResource(logo)
        textViewHeader.text = headerTitle
        textViewHeader.setContextCompatTextColor(headerTextColor)
        textViewMsg.text = headerContent.toHtmlSpan()
    }

    private fun navigateCheckDepositPreviewScreen(
        checkDepositTypeEnum: CheckDepositTypeEnum,
        filePath: String
    ) {
        val bundle = Bundle().apply {
            putString(
                CheckDepositPreviewActivity.EXTRA_SCREEN,
                CheckDepositScreenEnum.DETAIL.name
            )
            putString(
                CheckDepositPreviewActivity.EXTRA_CHECK_DEPOSIT_TYPE,
                checkDepositTypeEnum.name
            )
            putString(
                CheckDepositPreviewActivity.EXTRA_FILE_PATH,
                filePath
            )
        }
        navigator.navigate(
            this,
            CheckDepositPreviewActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun navigateActivityLogScreen() {
        val bundle = Bundle().apply {
            putString(
                CheckDepositActivityLogActivity.EXTRA_ID,
                checkDeposit.id.toString()
            )
        }
        navigator.navigate(
            this,
            CheckDepositActivityLogActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    private fun initPermission() {
        RxPermissions(this)
            .request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .subscribe { granted ->
                if (granted) {
                    Handler().postDelayed(
                        {
                            showShareableContent()
                        }, resources.getInteger(R.integer.time_delay_thread).toLong()
                    )
                } else {
                    MaterialDialog(this).show {
                        lifecycleOwner(this@CheckDepositDetailActivity)
                        cancelOnTouchOutside(false)
                        message(R.string.desc_service_permission)
                        positiveButton(
                            res = R.string.action_ok,
                            click = {
                                it.dismiss()
                                initPermission()
                            }
                        )
                        negativeButton(
                            res = R.string.action_cancel,
                            click = {
                                it.dismiss()
                                initPermission()
                            }
                        )
                    }
                }
            }.addTo(disposables)
    }

    private fun showShareableContent() {
        RxQrCode.generateQrCodeFile(
            this,
            checkDeposit.checkNumber,
            resources.getDimension(R.dimen.image_view_qr_code).toInt(),
            resources.getDimension(R.dimen.image_view_qr_code).toInt()
        )
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe {
                if (getProgressAlertDialog() == null || getProgressAlertDialog()?.isVisible == false)
                    showProgressAlertDialog(CheckDepositDetailActivity::class.java.simpleName)
            }
            .subscribe(
                {
                    imageViewQRCode.setImageBitmap(it.toBitmap())
                    scrollViewShare.visibility(true)
                    Handler().postDelayed(
                        {
                            val shareBitmap = viewUtil.getBitmapByView(viewShareDetails)
                            scrollViewShare.visibility(false)
                            dismissProgressAlertDialog()
                            startShareMediaActivity(shareBitmap)
                        }, resources.getInteger(R.integer.time_delay_share_media).toLong()
                    )
                }, {
                    Timber.e(it, "showShareableContent")
                    dismissProgressAlertDialog()
                    handleOnError(it)
                }
            ).addTo(disposables)
    }

    companion object {
        const val EXTRA_ID = "id"
    }
}
