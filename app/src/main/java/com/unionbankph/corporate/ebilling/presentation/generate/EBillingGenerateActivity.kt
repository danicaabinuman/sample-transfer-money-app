package com.unionbankph.corporate.ebilling.presentation.generate

import android.Manifest
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.common.presentation.viewmodel.ShowGeneralGetOrganizationName
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.ebilling.domain.form.EBillingForm
import com.unionbankph.corporate.ebilling.presentation.form.EBillingFormActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_ebilling_generate.*
import kotlinx.android.synthetic.main.view_e_billing_share.*
import kotlinx.android.synthetic.main.widget_button_share_outline.*
import kotlinx.android.synthetic.main.widget_header_transaction_summary.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*

/**
 * Created by herald on 10/28/20
 */
class EBillingGenerateActivity :
    BaseActivity<EBillingGenerateViewModel>(R.layout.activity_ebilling_generate) {

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
        textViewQRCodeDesc.isVisible = false
        imageViewQRCode.isVisible = false
        setupBindings()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        btn_make_another.setOnClickListener {
            navigator.navigateClearUpStack(
                this,
                EBillingFormActivity::class.java,
                null,
                isClear = true,
                isAnimated = true
            )
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
            ViewModelProviders.of(this, viewModelFactory)[EBillingGenerateViewModel::class.java]
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.parseEBillingForm(intent.getParcelableExtra(EXTRA_FORM))
        viewModel.eBillingForm.observe(this, Observer {
            setupViews(it)
        })
        viewModel.qrCodeFile.observe(this, Observer {
            iv_qr_code.setImageBitmap(it.toBitmap())
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
        viewModel.generateQRCode(eBillingForm.qrCodePath.notNullable())
        tv_account_name.text = eBillingForm.depositTo?.name
        tv_account_number.text = eBillingForm.depositTo?.accountNumber.formatAccountNumber()
        buttonShare.setOnClickListener {
            setShareTransactionValues(eBillingForm)
        }
    }

    private fun setupBindings() {}

    private fun setShareTransactionValues(eBillingForm: EBillingForm) {
        iv_share_qr_code.setImageBitmap(viewModel.qrCodeFile.value?.toBitmap())
        tv_share_deposit_to.text = formatString(
            R.string.params_account_detail,
            eBillingForm.depositTo?.name,
            eBillingForm.depositTo?.accountNumber.formatAccountNumber(),
            eBillingForm.depositTo?.productCodeDesc.notNullable()
        ).toHtmlSpan()
        tv_share_amount.text = eBillingForm.amount.toString().formatAmount(eBillingForm.currency)
        tv_share_date_downloaded.text = viewUtil.getCurrentDateString()
        initPermission()
    }

    private fun initPermission() {
        RxPermissions(this)
            .request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .subscribe { granted ->
                if (granted) {
                    runPostDelayed(
                        {
                            showShareableContent()
                        }, resources.getInteger(R.integer.time_delay_thread).toLong()
                    )
                } else {
                    MaterialDialog(this).show {
                        lifecycleOwner(this@EBillingGenerateActivity)
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
        showShareContent(true)
        runPostDelayed(
            {
                val shareBitmap = viewUtil.getBitmapByView(view_share_content)
                dismissProgressAlertDialog()
                showShareContent(false)
                startShareMediaActivity(shareBitmap)
            }, resources.getInteger(R.integer.time_delay_share_media).toLong()
        )
    }

    private fun showShareContent(isShown: Boolean) {
        view_share_content.isVisible = isShown
    }

    companion object {

        const val EXTRA_FORM = "form"

    }

}