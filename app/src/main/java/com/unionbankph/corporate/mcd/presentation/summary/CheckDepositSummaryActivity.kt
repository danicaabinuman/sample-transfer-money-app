package com.unionbankph.corporate.mcd.presentation.summary

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.MenuItem
import android.widget.ImageView
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
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.qrgenerator.RxQrCode
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.mcd.data.form.CheckDepositForm
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositScreenEnum
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositStatusEnum
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositTypeEnum
import com.unionbankph.corporate.mcd.presentation.list.CheckDepositActivity
import com.unionbankph.corporate.mcd.presentation.onboarding.CheckDepositOnBoardingActivity
import com.unionbankph.corporate.mcd.presentation.preview.CheckDepositPreviewActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_check_deposit_summary.*
import kotlinx.android.synthetic.main.widget_button_share_outline.*
import kotlinx.android.synthetic.main.widget_header_transaction_summary.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.toolbar
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by herald25santos on 2019-10-23
 */
class CheckDepositSummaryActivity :
    BaseActivity<CheckDepositSummaryViewModel>(R.layout.activity_check_deposit_summary) {

    private val checkDepositForm by lazyFast {
        JsonHelper.fromJson<CheckDepositForm>(intent.getStringExtra(EXTRA_FORM))
    }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[CheckDepositSummaryViewModel::class.java]
        initGeneralViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initCheckDepositSummaryDetails()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        imageViewFrontOfCheck.setOnClickListener {
            navigateCheckDepositPreviewScreen(
                CheckDepositTypeEnum.FRONT_OF_CHECK,
                checkDepositForm.frontOfCheckFilePath.notNullable(),
                imageViewFrontOfCheck
            )
        }
        imageViewBackOfCheck.setOnClickListener {
            navigateCheckDepositPreviewScreen(
                CheckDepositTypeEnum.BACK_OF_CHECK,
                checkDepositForm.backOfCheckFilePath.notNullable(),
                imageViewBackOfCheck
            )
        }
        buttonMakeAnotherDeposit.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            viewModel.deleteFiles()
            val bundle = Bundle().apply {
                putString(
                    CheckDepositOnBoardingActivity.EXTRA_SCREEN,
                    CheckDepositScreenEnum.SUMMARY.name
                )
            }
            navigator.navigateClearUpStack(
                this,
                CheckDepositOnBoardingActivity::class.java,
                bundle,
                isClear = true,
                isAnimated = true
            )
            eventBus.actionSyncEvent.emmit(
                BaseEvent(ActionSyncEvent.ACTION_UPDATE_CHECK_DEPOSIT_LIST)
            )
        }
        buttonViewViewChecksInClearing.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            viewModel.deleteFiles()
            navigator.navigateClearUpStack(
                this,
                CheckDepositActivity::class.java,
                null,
                isClear = true,
                isAnimated = true
            )
            eventBus.actionSyncEvent.emmit(
                BaseEvent(ActionSyncEvent.ACTION_UPDATE_CHECK_DEPOSIT_LIST)
            )
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
        viewModel.deleteFiles()
        navigator.navigateClearUpStack(
            this,
            CheckDepositActivity::class.java,
            null,
            isClear = true,
            isAnimated = true
        )
        eventBus.actionSyncEvent.emmit(
            BaseEvent(ActionSyncEvent.ACTION_UPDATE_CHECK_DEPOSIT_LIST)
        )
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        textViewTitle,
                        textViewCorporationName,
                        formatString(R.string.title_check_deposit_summary),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun initCheckDepositSummaryDetails() {
        setTransactionHeaderDetails(
            background = R.drawable.bg_rectangle_yellow,
            logo = R.drawable.ic_fund_transfer_clock_yellow,
            headerTextColor = R.color.colorPendingStatus,
            headerTitle = formatString(R.string.title_check_deposit_for_clearing),
            headerContent = formatString(
                R.string.msg_check_deposit_for_clearing,
                intent.getStringExtra(EXTRA_REFERENCE_NUMBER)
            )
        )
        textViewBankOfCheck.text = checkDepositForm.issuerName
        textViewCheckAccountNumber.text = checkDepositForm.sourceAccount
        textViewCheckNumber.text = checkDepositForm.checkNumber
        textViewDateOnCheck.text = viewUtil.getDateFormatByDateString(
            checkDepositForm.checkDate,
            DateFormatEnum.DATE_FORMAT_ISO_Z.value,
            DateFormatEnum.DATE_FORMAT_DATE.value
        )
        textViewShareDateDownloaded.text = viewUtil.getCurrentDateString()
        textViewShareCreatedBy.text = intent.getStringExtra(EXTRA_CREATED_BY)
        textViewShareCreatedOn.text = viewUtil.getStringOrEmpty(
            viewUtil.getDateFormatByDateString(
                intent.getStringExtra(EXTRA_CREATED_DATE),
                DateFormatEnum.DATE_FORMAT_ISO_WITHOUT_T.value,
                DateFormatEnum.DATE_FORMAT_DEFAULT.value
            )
        )
        textViewAmount.text = AutoFormatUtil().formatWithTwoDecimalPlaces(
            checkDepositForm.checkAmount, checkDepositForm.currency
        )
        textViewServiceFee.text = if (checkDepositForm.serviceFee != null) {
            formatString(
                R.string.value_service,
                autoFormatUtil.formatWithTwoDecimalPlaces(
                    checkDepositForm.serviceFee?.value,
                    checkDepositForm.serviceFee?.currency
                )
            )
        } else {
            formatString(R.string.value_service_fee_free)
        }
        textViewDepositTo.text = formatString(
            R.string.params_account_detail,
            checkDepositForm.targetAccountName,
            viewUtil.getAccountNumberFormat(checkDepositForm.targetAccount),
            checkDepositForm.accountType.notEmpty()
        ).toHtmlSpan()
        imageViewFrontOfCheck.loadImage(
            checkDepositForm.frontOfCheckFilePath.notNullable(),
            "front_${checkDepositForm.id}"
        )
        imageViewBackOfCheck.loadImage(
            checkDepositForm.backOfCheckFilePath.notNullable(),
            "back_${checkDepositForm.id}"
        )
        textViewReferenceNumber.text = intent.getStringExtra(EXTRA_REFERENCE_NUMBER).notEmpty()
        textViewRemarks.text = checkDepositForm.remarks.notEmpty()
        initShareStatus(intent.getStringExtra(EXTRA_REMARKS).notNullable())
    }

    private fun initShareStatus(remarks: String) {
        when (intent.getStringExtra(EXTRA_STATUS)) {
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
                        checkDepositForm.checkNumber,
                        remarks
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
        viewHeader.setContextCompatBackground(background)
        imageViewHeader.setImageResource(logo)
        textViewHeader.text = headerTitle
        textViewHeader.setContextCompatTextColor(headerTextColor)
        textViewMsg.text = headerContent.toHtmlSpan()
    }

    private fun navigateCheckDepositPreviewScreen(
        checkDepositTypeEnum: CheckDepositTypeEnum,
        filePath: String,
        imageView: ImageView
    ) {
        val bundle = Bundle().apply {
            putString(
                CheckDepositPreviewActivity.EXTRA_SCREEN,
                CheckDepositScreenEnum.SUMMARY.name
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            navigator.navigateImageTransition(
                this,
                CheckDepositPreviewActivity::class.java,
                bundle,
                imageView
            )
        } else {
            navigator.navigate(
                this,
                CheckDepositPreviewActivity::class.java,
                bundle,
                isClear = false,
                isAnimated = true,
                transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
            )
        }
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
                        lifecycleOwner(this@CheckDepositSummaryActivity)
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
            intent.getStringExtra(EXTRA_QR_CONTENT),
            resources.getDimension(R.dimen.image_view_qr_code).toInt(),
            resources.getDimension(R.dimen.image_view_qr_code).toInt()
        )
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnSubscribe { showProgressAlertDialog(CheckDepositSummaryActivity::class.java.simpleName) }
            .subscribe(
                {
                    imageViewQRCode.setImageBitmap(it.toBitmap())
                    viewHeaderTransaction.visibility(true)
                    viewBorderCreatedBy.visibility(true)
                    textViewShareCreatedByTitle.visibility(true)
                    textViewShareCreatedBy.visibility(true)
                    textViewShareCreatedOnTitle.visibility(true)
                    textViewShareCreatedOn.visibility(true)
                    textViewShareDateDownloadedTitle.visibility(true)
                    textViewShareDateDownloaded.visibility(true)
                    viewShareButton.visibility(false)
                    viewBorderReferenceNumber.visibility(false)
                    textViewFrontOfCheckTitle.visibility(false)
                    imageViewFrontOfCheck.visibility(false)
                    viewBorderFrontOfCheck.visibility(false)
                    textViewBackOfCheckTitle.visibility(false)
                    imageViewBackOfCheck.visibility(false)
                    buttonMakeAnotherDeposit.visibility(false)
                    buttonViewViewChecksInClearing.visibility(false)
                    Handler().postDelayed(
                        {
                            val shareBitmap = viewUtil.getBitmapByView(constraintLayoutShare)
                            viewHeaderTransaction.visibility(false)
                            viewBorderCreatedBy.visibility(false)
                            textViewShareCreatedByTitle.visibility(false)
                            textViewShareCreatedBy.visibility(false)
                            textViewShareCreatedOnTitle.visibility(false)
                            textViewShareCreatedOn.visibility(false)
                            textViewShareDateDownloadedTitle.visibility(false)
                            textViewShareDateDownloaded.visibility(false)
                            viewShareButton.visibility(true)
                            viewBorderReferenceNumber.visibility(true)
                            textViewFrontOfCheckTitle.visibility(true)
                            imageViewFrontOfCheck.visibility(true)
                            viewBorderFrontOfCheck.visibility(true)
                            textViewBackOfCheckTitle.visibility(true)
                            imageViewBackOfCheck.visibility(true)
                            buttonMakeAnotherDeposit.visibility(true)
                            buttonViewViewChecksInClearing.visibility(true)
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
        const val EXTRA_FORM = "form"
        const val EXTRA_QR_CONTENT = "qr_content"
        const val EXTRA_REFERENCE_NUMBER = "reference_number"
        const val EXTRA_STATUS = "status"
        const val EXTRA_REMARKS = "remarks"
        const val EXTRA_CREATED_BY = "created_by"
        const val EXTRA_CREATED_DATE = "created_date"
    }
}
