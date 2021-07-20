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
import com.unionbankph.corporate.databinding.ActivityCheckDepositDetailBinding
import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositScreenEnum
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositStatusEnum
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositTypeEnum
import com.unionbankph.corporate.mcd.presentation.log.CheckDepositActivityLogActivity
import com.unionbankph.corporate.mcd.presentation.preview.CheckDepositPreviewActivity
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by herald25santos on 2019-11-05
 */
class CheckDepositDetailActivity :
    BaseActivity<ActivityCheckDepositDetailBinding, CheckDepositDetailViewModel>() {

    private val id by lazyFast { intent.getStringExtra(EXTRA_ID) }

    private lateinit var checkDeposit: CheckDeposit

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setToolbarTitle(
            binding.viewToolbar.tvToolbar,
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
        binding.buttonViewLogs.setOnClickListener {
            navigateActivityLogScreen()
        }
        RxView.clicks(binding.viewShareButton.buttonShare)
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
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowCheckDepositDetailLoading -> {
                    binding.constraintLayout.visibility(false)
                    binding.viewLoadingState.root.visibility(true)
                }
                is ShowCheckDepositDetailDismissLoading -> {
                    binding.viewLoadingState.root.visibility(false)
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
        binding.constraintLayout.visibility(true)
        binding.textViewStatus.setContextCompatTextColor(
            ConstantHelper.Color.getTextColor(checkDeposit.status)
        )
        binding.textViewStatus.text =
            if (checkDeposit.status?.type == CheckDepositStatusEnum.REJECTED.name) {
                formatString(
                    R.string.param_transaction_status,
                    checkDeposit.status?.description,
                    "Reason: \"${checkDeposit.status?.detailedDescription.notEmpty()}\""
                ).toHtmlSpan()
            } else {
                "<b>${checkDeposit.status?.description}</b>".toHtmlSpan()
            }
        binding.textViewRemarks.text = checkDeposit.remarks.notEmpty()
        binding.textViewCreatedBy.text = checkDeposit.createdBy
        binding.textViewCreatedDate.text = viewUtil.getStringOrEmpty(
            viewUtil.getDateFormatByDateString(
                checkDeposit.createdDate,
                DateFormatEnum.DATE_FORMAT_ISO_WITHOUT_T.value,
                DateFormatEnum.DATE_FORMAT_DEFAULT.value
            )
        )
        binding.textViewBankOfCheck.text = checkDeposit.issuer
        binding.textViewCheckAccountNumber.text = checkDeposit.sourceAccount
        binding.textViewCheckAccountName.text = checkDeposit.sourceAccountName
        binding.textViewCheckNumber.text = checkDeposit.checkNumber
        binding.textViewReferenceNumber.text = checkDeposit.referenceNumber
        binding.textViewDateOnCheck.text = viewUtil.getDateFormatByDateString(
            checkDeposit.checkDate,
            DateFormatEnum.DATE_FORMAT_ISO_WITHOUT_T.value,
            DateFormatEnum.DATE_FORMAT_DATE.value
        )
        binding.textViewAccount.text = formatString(
            R.string.params_two_format,
            checkDeposit.targetAccountName,
            viewUtil.getAccountNumberFormat(checkDeposit.targetAccount)
        ).toHtmlSpan()
        binding.textViewAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
            checkDeposit.checkAmount?.value.toString(),
            checkDeposit.checkAmount?.currency
        )
        if (checkDeposit.customServiceFee != null &&
            checkDeposit.serviceFee != null
        ) {
            binding.textViewServiceFee.text = formatString(
                R.string.value_service,
                autoFormatUtil.formatWithTwoDecimalPlaces(
                    checkDeposit.customServiceFee?.value,
                    checkDeposit.customServiceFee?.currency
                )
            )
            binding.textViewServiceDiscountFee.visibility(true)
            binding.viewBorderServiceDiscountFee.visibility(true)
        } else {
            binding.textViewServiceDiscountFee.visibility(false)
            binding.viewBorderServiceDiscountFee.visibility(false)
        }
        if (checkDeposit.serviceFee != null) {
            if (checkDeposit.customServiceFee != null) {
                binding.textViewServiceDiscountFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        checkDeposit.serviceFee?.value,
                        checkDeposit.serviceFee?.currency
                    )
                )
            } else {
                binding.textViewServiceFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        checkDeposit.serviceFee?.value,
                        checkDeposit.serviceFee?.currency
                    )
                )
            }
        } else {
            binding.textViewServiceFee.text = getString(R.string.value_service_fee_free)
        }
        if (checkDeposit.status?.type == CheckDepositStatusEnum.FOR_CLEARING.name) {
            val imageViewFrontOfCheck =
                binding.viewFrontOfCheck.root.findViewById<AppCompatImageView>(R.id.imageView)
            val imageViewBackOfCheck =
                binding.viewBackOfCheck.root.findViewById<AppCompatImageView>(R.id.imageView)
            imageViewFrontOfCheck.loaderImagePreviewByUrl(
                checkDeposit.frontPath.notNullable(),
                binding.viewFrontOfCheck.root
            )
            imageViewBackOfCheck.loaderImagePreviewByUrl(
                checkDeposit.backPath.notNullable(),
                binding.viewBackOfCheck.root
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
            binding.viewBorderServiceFee.visibility(false)
            binding.textViewFrontOfCheckTitle.visibility(false)
            binding.viewFrontOfCheck.root.visibility(false)
            binding.viewBorderFrontOfCheck.visibility(false)
            binding.textViewBackOfCheckTitle.visibility(false)
            binding.viewBackOfCheck.root.visibility(false)
        }
        initShareDetailStatus(checkDeposit)
    }

    private fun initShareDetailStatus(checkDeposit: CheckDeposit) {
        binding.viewShareDetails.textViewShareCreatedBy.text = checkDeposit.createdBy
        binding.viewShareDetails.textViewShareCreatedOn.text = viewUtil.getStringOrEmpty(
            viewUtil.getDateFormatByDateString(
                checkDeposit.createdDate,
                DateFormatEnum.DATE_FORMAT_ISO_WITHOUT_T.value,
                DateFormatEnum.DATE_FORMAT_DEFAULT.value
            )
        )
        binding.viewShareDetails.textViewShareDateDownloaded.text = viewUtil.getCurrentDateString()
        binding.viewShareDetails.textViewShareBankOfCheck.text = checkDeposit.issuer
        binding.viewShareDetails.textViewShareCheckAccountNumber.text = checkDeposit.sourceAccount
        binding.viewShareDetails.textViewShareCheckNumber.text = checkDeposit.checkNumber
        binding.viewShareDetails.textViewShareRemarks.text = checkDeposit.remarks.notEmpty()
        if (checkDeposit.referenceNumber != null) {
            binding.viewShareDetails.textViewShareReferenceNumber.text = checkDeposit.referenceNumber.notEmpty()
        } else {
            binding.viewShareDetails.textViewShareReferenceNumberTitle.isVisible = false
            binding.viewShareDetails.textViewShareReferenceNumber.isVisible = false
            binding.viewShareDetails.viewBorderShareReferenceNumber.isVisible = false
        }
        binding.viewShareDetails.textViewShareDateOnCheck.text = viewUtil.getDateFormatByDateString(
            checkDeposit.checkDate,
            DateFormatEnum.DATE_FORMAT_ISO_WITHOUT_T.value,
            DateFormatEnum.DATE_FORMAT_DATE.value
        )
        binding.viewShareDetails.textViewShareDepositTo.text = formatString(
            R.string.params_two_format,
            checkDeposit.targetAccountName,
            viewUtil.getAccountNumberFormat(checkDeposit.targetAccount)
        ).toHtmlSpan()
        binding.viewShareDetails.textViewShareAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
            checkDeposit.checkAmount?.value.toString(),
            checkDeposit.checkAmount?.currency
        )
        if (checkDeposit.customServiceFee != null &&
            checkDeposit.serviceFee != null
        ) {
            binding.viewShareDetails.textViewShareServiceFee.text = formatString(
                R.string.value_service,
                autoFormatUtil.formatWithTwoDecimalPlaces(
                    checkDeposit.customServiceFee?.value,
                    checkDeposit.customServiceFee?.currency
                )
            )
            binding.viewShareDetails.textViewShareServiceDiscountFee.visibility(true)
            binding.viewShareDetails.viewBorderShareServiceDiscountFee.visibility(true)
        } else {
            binding.viewShareDetails.textViewShareServiceDiscountFee.visibility(false)
            binding.viewShareDetails.viewBorderShareServiceDiscountFee.visibility(false)
        }
        if (checkDeposit.serviceFee != null) {
            if (checkDeposit.customServiceFee != null) {
                binding.viewShareDetails.textViewShareServiceDiscountFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        checkDeposit.serviceFee?.value,
                        checkDeposit.serviceFee?.currency
                    )
                )
            } else {
                binding.viewShareDetails.textViewShareServiceFee.text = formatString(
                    R.string.value_service,
                    autoFormatUtil.formatWithTwoDecimalPlaces(
                        checkDeposit.serviceFee?.value,
                        checkDeposit.serviceFee?.currency
                    )
                )
            }
        } else {
            binding.viewShareDetails.textViewShareServiceFee.text = getString(R.string.value_service_fee_free)
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
        binding.viewShareDetails.linearLayoutHeaderStatus.setContextCompatBackground(background)
        binding.viewShareDetails.imageViewHeader.setImageResource(logo)
        binding.viewShareDetails.textViewHeader.text = headerTitle
        binding.viewShareDetails.textViewHeader.setContextCompatTextColor(headerTextColor)
        binding.viewShareDetails.textViewMsg.text = headerContent.toHtmlSpan()
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
                    binding.viewShareDetails.viewHeaderTransaction.imageViewQRCode
                    binding.scrollViewShare.visibility(true)
                    Handler().postDelayed(
                        {
                            val shareBitmap = viewUtil.getBitmapByView(binding.viewShareDetails.root)
                            binding.scrollViewShare.visibility(false)
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

    override val layoutId: Int
        get() = R.layout.activity_check_deposit_detail

    override val viewModelClassType: Class<CheckDepositDetailViewModel>
        get() = CheckDepositDetailViewModel::class.java
}
