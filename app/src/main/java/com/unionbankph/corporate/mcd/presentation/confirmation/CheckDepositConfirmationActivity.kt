package com.unionbankph.corporate.mcd.presentation.confirmation

import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.MenuItem
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.mcd.data.form.CheckDepositForm
import com.unionbankph.corporate.mcd.data.model.CheckDeposit
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositScreenEnum
import com.unionbankph.corporate.mcd.presentation.constant.CheckDepositTypeEnum
import com.unionbankph.corporate.mcd.presentation.preview.CheckDepositPreviewActivity
import com.unionbankph.corporate.mcd.presentation.summary.CheckDepositSummaryActivity
import kotlinx.android.synthetic.main.activity_check_deposit_confirmation.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.toolbar
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*

/**
 * Created by herald25santos on 2019-10-23
 */
class CheckDepositConfirmationActivity :
    BaseActivity<CheckDepositConfirmationViewModel>(R.layout.activity_check_deposit_confirmation) {

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
        initCheckDepositViewModel()
        initGeneralViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initCheckDepositConfirmationDetails()
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
        buttonEdit.setOnClickListener {
            onBackPressed()
        }
        buttonSubmit.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            viewModel.checkDeposit(checkDepositForm)
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

    private fun initCheckDepositViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[CheckDepositConfirmationViewModel::class.java]
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowCheckDepositConfirmationLoading -> {
                    showProgressAlertDialog(CheckDepositConfirmationActivity::class.java.simpleName)
                }
                is ShowCheckDepositConfirmationDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowCheckDepositConfirmationError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.checkDeposit.observe(this, Observer {
            navigateCheckDepositSummaryScreen(it)
        })
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        textViewTitle,
                        textViewCorporationName,
                        formatString(R.string.title_check_deposit_confirmation),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun navigateCheckDepositPreviewScreen(
        checkDepositTypeEnum: CheckDepositTypeEnum,
        filePath: String,
        imageView: ImageView
    ) {
        val bundle = Bundle().apply {
            putString(
                CheckDepositPreviewActivity.EXTRA_SCREEN,
                CheckDepositScreenEnum.CONFIRMATION.name
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

    private fun initCheckDepositConfirmationDetails() {
        textViewBankOfCheck.text = checkDepositForm.issuerName
        textViewCheckAccountNumber.text = checkDepositForm.sourceAccount
        textViewCheckNumber.text = checkDepositForm.checkNumber
        textViewRemarks.text = checkDepositForm.remarks.notEmpty()
        textViewDateOnCheck.text = viewUtil.getDateFormatByDateString(
            checkDepositForm.checkDate,
            DateFormatEnum.DATE_FORMAT_ISO_Z.value,
            DateFormatEnum.DATE_FORMAT_DATE.value
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
    }

    private fun navigateCheckDepositSummaryScreen(checkDeposit: CheckDeposit) {
        navigator.navigate(
            this,
            CheckDepositSummaryActivity::class.java,
            Bundle().apply {
                putString(
                    CheckDepositSummaryActivity.EXTRA_FORM,
                    JsonHelper.toJson(checkDepositForm)
                )
                putString(
                    CheckDepositSummaryActivity.EXTRA_QR_CONTENT,
                    checkDeposit.qrContent.notEmpty()
                )
                putString(
                    CheckDepositSummaryActivity.EXTRA_REMARKS,
                    checkDeposit.status?.detailedDescription
                )
                putString(
                    CheckDepositSummaryActivity.EXTRA_REFERENCE_NUMBER,
                    checkDeposit.referenceNumber
                )
                putString(
                    CheckDepositSummaryActivity.EXTRA_STATUS,
                    checkDeposit.status?.type
                )
                putString(
                    CheckDepositSummaryActivity.EXTRA_CREATED_BY,
                    checkDeposit.createdBy
                )
                putString(
                    CheckDepositSummaryActivity.EXTRA_CREATED_DATE,
                    checkDeposit.createdDate
                )
            },
            isClear = true,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    companion object {
        const val EXTRA_FORM = "form"
    }
}
