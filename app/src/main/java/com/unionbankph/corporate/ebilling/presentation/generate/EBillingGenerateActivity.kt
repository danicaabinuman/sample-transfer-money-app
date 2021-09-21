package com.unionbankph.corporate.ebilling.presentation.generate

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
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
import com.unionbankph.corporate.databinding.ActivityEbillingGenerateBinding
import com.unionbankph.corporate.ebilling.domain.form.EBillingForm
import com.unionbankph.corporate.ebilling.presentation.form.EBillingFormActivity
import io.reactivex.rxkotlin.addTo

/**
 * Created by herald on 10/28/20
 */
class EBillingGenerateActivity :
    BaseActivity<ActivityEbillingGenerateBinding, EBillingGenerateViewModel>() {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
        initGeneralViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        binding.viewShareContent.viewHeaderTransaction.textViewQRCodeDesc.isVisible = false
        binding.viewShareContent.viewHeaderTransaction.imageViewQRCode.isVisible = false
        setupBindings()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.btnMakeAnother.setOnClickListener {
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
            binding.ivQrCode.setImageBitmap(it.toBitmap())
        })
    }

    private fun initGeneralViewModel() {
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralGetOrganizationName -> {
                    setToolbarTitle(
                        binding.viewToolbar.textViewTitle,
                        binding.viewToolbar.textViewCorporationName,
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
        binding.tvAccountName.text = eBillingForm.depositTo?.name
        binding.tvAccountNumber.text = eBillingForm.depositTo?.accountNumber.formatAccountNumber()
        binding.viewShareButton.buttonShare.setOnClickListener {
            setShareTransactionValues(eBillingForm)
        }
    }

    private fun setupBindings() {}

    private fun setShareTransactionValues(eBillingForm: EBillingForm) {
        binding.viewShareContent.ivShareQrCode.setImageBitmap(viewModel.qrCodeFile.value?.toBitmap())
        binding.viewShareContent.tvShareDepositTo.text = formatString(
            R.string.params_account_detail,
            eBillingForm.depositTo?.name,
            eBillingForm.depositTo?.accountNumber.formatAccountNumber(),
            eBillingForm.depositTo?.productCodeDesc.notNullable()
        ).toHtmlSpan()
        binding.viewShareContent.tvShareAmount.text = eBillingForm.amount.toString().formatAmount(eBillingForm.currency)
        binding.viewShareContent.tvShareDateDownloaded.text = viewUtil.getCurrentDateString()
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
                val shareBitmap = viewUtil.getBitmapByView(binding.viewShareContent.root)
                dismissProgressAlertDialog()
                showShareContent(false)
                startShareMediaActivity(shareBitmap)
            }, resources.getInteger(R.integer.time_delay_share_media).toLong()
        )
    }

    private fun showShareContent(isShown: Boolean) {
        binding.viewShareContent.root.isVisible = isShown
    }

    companion object {

        const val EXTRA_FORM = "form"

    }

    override val viewModelClassType: Class<EBillingGenerateViewModel>
        get() = EBillingGenerateViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityEbillingGenerateBinding
        get() = ActivityEbillingGenerateBinding::inflate
}