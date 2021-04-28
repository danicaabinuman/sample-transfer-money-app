package com.unionbankph.corporate.general.presentation.result

import android.os.Bundle
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.clearTheme
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.toHtmlSpan
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.bills_payment.presentation.frequent_biller_detail.ManageFrequentBillerDetailActivity
import com.unionbankph.corporate.bills_payment.presentation.frequent_biller_list.ManageFrequentBillerActivity
import com.unionbankph.corporate.common.data.model.ApiError
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.GeneralViewModel
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_detail.ManageBeneficiaryDetailActivity
import com.unionbankph.corporate.fund_transfer.presentation.beneficiary_list.ManageBeneficiaryActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_result_landing_page.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*
import java.util.concurrent.TimeUnit

class ResultLandingPageActivity :
    BaseActivity<GeneralViewModel>(R.layout.activity_result_landing_page) {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar, false)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        RxView.clicks(buttonClose)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                navigateToLandingPage()
            }.addTo(disposables)
    }

    override fun onBackPressed() {
        navigateToLandingPage()
    }

    private fun navigateToLandingPage() {
        if (intent.getStringExtra(EXTRA_PAGE) == PAGE_BENEFICIARY_CREATE ||
            intent.getStringExtra(EXTRA_PAGE) == PAGE_BENEFICIARY_DETAIL
        ) {
            eventBus.actionSyncEvent.emmit(BaseEvent(ActionSyncEvent.ACTION_UPDATE_BENEFICIARY_LIST))
            navigator.navigateClearUpStack(
                this,
                ManageBeneficiaryActivity::class.java,
                null,
                isClear = true,
                isAnimated = true
            )
        } else if (intent.getStringExtra(EXTRA_PAGE) == PAGE_BENEFICIARY_UPDATE) {
            eventBus.actionSyncEvent.emmit(BaseEvent(ActionSyncEvent.ACTION_UPDATE_BENEFICIARY_LIST))
            navigator.navigateClearUpStack(
                this,
                ManageBeneficiaryDetailActivity::class.java,
                null,
                isClear = true,
                isAnimated = true
            )
        } else if (intent.getStringExtra(EXTRA_PAGE) == PAGE_SCHEDULED_TRANSFER_DETAIL ||
            intent.getStringExtra(EXTRA_PAGE) == PAGE_SCHEDULED_TRANSFER_LIST
        ) {
            eventBus.actionSyncEvent.emmit(
                BaseEvent(ActionSyncEvent.ACTION_UPDATE_SCHEDULED_TRANSFER_LIST)
            )
            onBackPressed(true)
        } else if (intent.getStringExtra(EXTRA_PAGE) == PAGE_FREQUENT_BILLER_CREATE ||
            intent.getStringExtra(EXTRA_PAGE) == PAGE_FREQUENT_BILLER_DETAIL
        ) {
            navigator.navigateClearUpStack(
                this,
                ManageFrequentBillerActivity::class.java,
                null,
                isClear = true,
                isAnimated = true
            )
        } else if (intent.getStringExtra(EXTRA_PAGE) == PAGE_FREQUENT_BILLER_UPDATE) {
            navigator.navigateClearUpStack(
                this,
                ManageFrequentBillerDetailActivity::class.java,
                null,
                isClear = true,
                isAnimated = true
            )
        } else if (intent.getStringExtra(EXTRA_PAGE) == PAGE_CHANGE_EMAIL ||
            intent.getStringExtra(EXTRA_PAGE) == PAGE_CHANGE_MOBILE_NUMBER
        ) {
            backToDashBoard()
        } else {
            logoutUser()
        }
    }

    private fun initViews() {
        when (intent.getStringExtra(EXTRA_PAGE)) {
            PAGE_NOMINATE_PASSWORD -> {
                imageViewLogo.clearTheme()
                imageViewLogo.layoutParams.width =
                    resources.getDimension(R.dimen.image_view_success_large).toInt()
                imageViewLogo.layoutParams.height =
                    resources.getDimension(R.dimen.image_view_success_large).toInt()
                if (intent.getStringExtra(EXTRA_TITLE) != null &&
                    intent.getStringExtra(EXTRA_DESC) != null
                ) {
                    imageViewLogo.setImageResource(R.drawable.logo_migration_successful)
                    textViewTitle.text = intent.getStringExtra(EXTRA_TITLE)
                    textViewDescription.text =
                        intent.getStringExtra(EXTRA_DESC).notNullable().toHtmlSpan()
                } else {
                    try {
                        val apiError =
                            JsonHelper.fromJson<ApiError>(intent.getStringExtra(EXTRA_DATA))
                        val errorMessage = apiError.errors[0].message ?: ""
                        val errorDescription = apiError.errors[0].description ?: ""
                        val errorCode = apiError.errors[0].code ?: ""
                        textViewTitle.text = errorMessage
                        textViewDescription.text = errorDescription
                        when (errorCode) {
                            "ACTVSYN-00" -> {
                                imageViewLogo.setImageResource(R.drawable.logo_migration_expired)
                            }
                            "ACTVSYN-01" -> {
                                imageViewLogo.setImageResource(R.drawable.logo_migration_already_migrated)
                            }
                            "ACTVSYN-02" -> {
                                imageViewLogo.setImageResource(R.drawable.logo_migration_expired)
                            }
                            "ACTVSYN-03" -> {
                                imageViewLogo.setImageResource(R.drawable.logo_migration_expired)
                            }
                        }
                    } catch (e: Exception) {
                        var errorMessage = e?.message
                        if(errorMessage!=null){
                            showMaterialDialogError(message = errorMessage)
                        }else{
                            showMaterialDialogError(message = formatString(R.string.error_something_went_wrong))
                        }
                    }
                }
            }
            else -> {
                textViewTitle.text = intent.getStringExtra(EXTRA_TITLE)
                textViewDescription.text =
                    intent.getStringExtra(EXTRA_DESC).notNullable().toHtmlSpan()
            }
        }
        buttonClose.text = intent.getStringExtra(EXTRA_BUTTON)
    }

    companion object {
        const val EXTRA_PAGE = "page"
        const val EXTRA_TITLE = "title"
        const val EXTRA_DESC = "desc"
        const val EXTRA_BUTTON = "button"
        const val EXTRA_DATA = "data"

        const val PAGE_BENEFICIARY_CREATE = "beneficiary_create"
        const val PAGE_BENEFICIARY_UPDATE = "beneficiary_update"
        const val PAGE_BENEFICIARY_DETAIL = "beneficiary_detail"
        const val PAGE_FREQUENT_BILLER_CREATE = "frequent_biller_create"
        const val PAGE_FREQUENT_BILLER_UPDATE = "frequent_biller_update"
        const val PAGE_FREQUENT_BILLER_DETAIL = "frequent_biller_detail"
        const val PAGE_RESET_PASSWORD = "reset_password"
        const val PAGE_CHANGE_EMAIL = "change_email"
        const val PAGE_CHANGE_MOBILE_NUMBER = "change_mobile_number"
        const val PAGE_SCHEDULED_TRANSFER_DETAIL = "scheduled_transfer_detail"
        const val PAGE_SCHEDULED_TRANSFER_LIST = "scheduled_transfer_list"
        const val PAGE_MIGRATION_RESULT = "migration_result"
        const val PAGE_NOMINATE_PASSWORD = "nominate_password"
    }
}
