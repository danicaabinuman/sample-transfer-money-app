package com.unionbankph.corporate.ebilling.presentation.confirmation

import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatAccountNumber
import com.unionbankph.corporate.app.common.extension.formatAmount
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.toHtmlSpan
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.ebilling.domain.form.EBillingForm
import com.unionbankph.corporate.ebilling.presentation.generate.EBillingGenerateActivity
import kotlinx.android.synthetic.main.activity_ebilling_confirmation.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*

/**
 * Created by herald on 10/28/20
 */
class EBillingConfirmationActivity :
    BaseActivity<EBillingConfirmationViewModel>(R.layout.activity_ebilling_confirmation) {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
        initGeneralViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
        setupBindings()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        btn_generate.setOnClickListener {
            viewModel.generateQRCode()
        }
        btn_edit.setOnClickListener {
            onBackPressed()
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

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[EBillingConfirmationViewModel::class.java]
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.parseEBillingForm(intent.getStringExtra(EXTRA_FORM))
        viewModel.eBillingForm.observe(this, Observer {
            setupViews(it)
        })
        viewModel.navigateGenerate.observe(this, EventObserver {
            navigateGenerate(it)
        })
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        textViewTitle,
                        textViewCorporationName,
                        formatString(R.string.title_generate_qr),
                        it.orgName
                    )
                }
            }
        })
        generalViewModel.getOrgName()
    }

    private fun setupViews(eBillingForm: EBillingForm) {
        tv_deposit_to.text = formatString(
            R.string.params_account_detail,
            eBillingForm.depositTo?.name,
            eBillingForm.depositTo?.accountNumber.formatAccountNumber(),
            eBillingForm.depositTo?.productCodeDesc.notNullable()
        ).toHtmlSpan()
        tv_amount.text = eBillingForm.amount.toString().formatAmount(eBillingForm.currency)
    }

    private fun setupBindings() {}

    private fun initViews() {
        tv_reminders_title.isVisible = false
        tv_reminders.isVisible = false
//        val reminders = JsonHelper.fromListJson<String>(
//            intent.getStringExtra(
//                EXTRA_REMINDERS
//            )
//        )
//        if (reminders.isNotEmpty()) {
//            val remindersContent = StringBuilder()
//            reminders.forEach {
//                remindersContent.append("$it\n\n")
//            }
//            tv_reminders.text = remindersContent
//        } else {
//            border_reminders.isInvisible = true
//            tv_reminders_title.isInvisible = true
//            tv_reminders.isVisible = false
//        }
    }

    private fun navigateGenerate(eBillingForm: EBillingForm) {
        navigator.navigate(
            this,
            EBillingGenerateActivity::class.java,
            Bundle().apply {
                putParcelable(EBillingGenerateActivity.EXTRA_FORM, eBillingForm)
            },
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    companion object {

        const val EXTRA_FORM = "form"
        const val EXTRA_REMINDERS = "reminders"

    }

}