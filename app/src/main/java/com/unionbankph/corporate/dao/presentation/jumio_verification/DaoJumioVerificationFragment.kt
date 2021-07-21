package com.unionbankph.corporate.dao.presentation.jumio_verification

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.button.MaterialButton
import com.jumio.core.enums.JumioDataCenter
import com.jumio.nv.NetverifySDK
import com.jumio.nv.data.document.NVDocumentType
import com.jumio.nv.data.document.NVDocumentVariant
import com.tbruyelle.rxpermissions2.RxPermissions
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.domain.model.DaoHit
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.dao.presentation.result.DaoResultFragment
import com.unionbankph.corporate.databinding.FragmentDaoJumioVerificationBinding
import io.reactivex.rxkotlin.addTo
import javax.annotation.concurrent.ThreadSafe

class DaoJumioVerificationFragment :
    BaseFragment<FragmentDaoJumioVerificationBinding, DaoJumioVerificationViewModel>(),
    DaoActivity.ActionEvent {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    private lateinit var netVerifySDK: NetverifySDK

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.uiState.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is UiState.Complete -> {
                    dismissProgressAlertDialog()
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.navigateNextStep.observe(viewLifecycleOwner, EventObserver {
            daoActivity.setJumioVerificationInput(it)
            navigateNextScreen()
        })
        viewModel.navigateResult.observe(viewLifecycleOwner, EventObserver {
            navigationDaoResult(it)
        })
        if (daoActivity.viewModel.hasJumioVerificationInput.hasValue() &&
            !viewModel.isLoadedScreen.hasValue()
        ) {
            daoActivity.viewModel.input5.let {
                viewModel.setExistingJumioVerification(it)
            }
        }
        arguments?.getBoolean(EXTRA_IS_EDIT, false)?.let {
            viewModel.isEditMode.onNext(it)
        }
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initJumio()
        initBinding()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initClickListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == NetverifySDK.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val scanReference = data?.getStringExtra(NetverifySDK.EXTRA_SCAN_REFERENCE)
                scanReference?.let {
                    viewModel.input.scanReferenceInput.onNext(it)
                    viewModel.onClickedNext()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // String scanReference = data.getStringExtra(NetverifySDK.EXTRA_SCAN_REFERENCE);
                // String errorMessage = data.getStringExtra(NetverifySDK.EXTRA_ERROR_MESSAGE);
                // String errorCode = data.getStringExtra(NetverifySDK.EXTRA_ERROR_CODE);
                // YOURCODE
            }
        }
    }

    private fun initClickListener() {
        binding.cvPassport.setOnClickListener {
            viewModel.input.tempIdTypeInput.onNext(formatString(R.string.title_passport))
            netVerifySDK.setPreselectedDocumentTypes(
                arrayListOf<NVDocumentType>(NVDocumentType.PASSPORT)
            )
            showJumioToolTip()
        }
        binding.cvDriversLicense.setOnClickListener {
            viewModel.input.tempIdTypeInput.onNext(formatString(R.string.title_drivers_license))
            netVerifySDK.setPreselectedDocumentTypes(
                arrayListOf<NVDocumentType>(NVDocumentType.DRIVER_LICENSE)
            )
            showJumioToolTip()
        }
        binding.cvSssId.setOnClickListener {
            viewModel.input.tempIdTypeInput.onNext(formatString(R.string.title_sss_id))
            setVerificationIdTypes()
        }
        binding.cvPrcId.setOnClickListener {
            viewModel.input.tempIdTypeInput.onNext(formatString(R.string.title_prc_id))
            setVerificationIdTypes()
        }
        binding.cvPostalId.setOnClickListener {
            viewModel.input.tempIdTypeInput.onNext(formatString(R.string.title_postal_id))
            setVerificationIdTypes()
        }
        binding.cvUmid.setOnClickListener {
            viewModel.input.tempIdTypeInput.onNext(formatString(R.string.title_umid_id))
            setVerificationIdTypes()
        }
    }

    private fun setVerificationIdTypes() {
        netVerifySDK.setPreselectedDocumentTypes(
            arrayListOf<NVDocumentType>(NVDocumentType.IDENTITY_CARD)
        )
        showJumioToolTip()
    }

    private fun init() {
        daoActivity.setToolBarDesc(formatString(R.string.title_upload_document))
        daoActivity.showToolBarDetails()
        daoActivity.showButton(true)
        daoActivity.setEnableButton(false)
        daoActivity.showProgress(true)
        daoActivity.setActionEvent(this)
        daoActivity.setProgressValue(5)
    }

    private fun initBinding() {
        viewModel.loadDaoForm(daoActivity.viewModel.defaultDaoForm())
        viewModel.isEditMode
            .subscribe {
                if (it) {
                    daoActivity.setButtonName(formatString(R.string.action_save))
                } else {
                    daoActivity.setButtonName(formatString(R.string.action_next))
                }
            }.addTo(disposables)
        viewModel.input.idTypeInput
            .subscribe {
                if (it != null && it != "" && !viewModel.isEditMode.value.notNullable()) {
                    enableItems(false)
                } else {
                    enableItems(true)
                }
                if (!viewModel.isEditMode.value.notNullable()) {
                    setCheckedState(it)
                }
            }.addTo(disposables)
    }

    private fun enableItems(isEnabled: Boolean) {
        daoActivity.setEnableButton(!isEnabled)
        binding.cvDriversLicense.setEnableView(isEnabled)
        binding.cvPassport.setEnableView(isEnabled)
        binding.cvSssId.setEnableView(isEnabled)
        binding.cvPrcId.setEnableView(isEnabled)
        binding.cvPostalId.setEnableView(isEnabled)
        binding.cvUmid.setEnableView(isEnabled)
        binding.tvJumioDesc.visibility(!isEnabled)
    }

    private fun setCheckedState(idType: String) {
        binding.ivDriversLicense.setImageResource(R.drawable.ic_arrow_orange_right)
        binding.ivPassport.setImageResource(R.drawable.ic_arrow_orange_right)
        binding.ivSssId.setImageResource(R.drawable.ic_arrow_orange_right)
        binding.ivPrcId.setImageResource(R.drawable.ic_arrow_orange_right)
        binding.ivPostalId.setImageResource(R.drawable.ic_arrow_orange_right)
        binding.ivUmid.setImageResource(R.drawable.ic_arrow_orange_right)
        when (idType) {
            formatString(R.string.title_drivers_license) -> {
                binding.ivDriversLicense.setImageResource(R.drawable.ic_solid_check_orange)
            }
            formatString(R.string.title_passport) -> {
                binding.ivPassport.setImageResource(R.drawable.ic_solid_check_orange)
            }
            formatString(R.string.title_sss_id) -> {
                binding.ivSssId.setImageResource(R.drawable.ic_solid_check_orange)
            }
            formatString(R.string.title_prc_id) -> {
                binding.ivPrcId.setImageResource(R.drawable.ic_solid_check_orange)
            }
            formatString(R.string.title_postal_id) -> {
                binding.ivPostalId.setImageResource(R.drawable.ic_solid_check_orange)
            }
            formatString(R.string.title_umid_id) -> {
                binding.ivUmid.setImageResource(R.drawable.ic_solid_check_orange)
            }
        }
    }

    fun initPermission() {
        RxPermissions(this)
            .request(Manifest.permission.CAMERA)
            .subscribe { granted ->
                if (granted) {
                    if (!NetverifySDK.isRooted(getAppCompatActivity()) &&
                        NetverifySDK.isSupportedPlatform(getAppCompatActivity())
                    ) {
                        startActivityForResult(netVerifySDK.intent, NetverifySDK.REQUEST_CODE)
                    }
                } else {
                    MaterialDialog(getAppCompatActivity()).show {
                        lifecycleOwner(getAppCompatActivity())
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

    private fun initJumio() {
        netVerifySDK =
            NetverifySDK.create(
                getAppCompatActivity(),
                daoActivity.viewModel.apiTokenJumio.value,
                daoActivity.viewModel.apiSecretJumio.value,
                JumioDataCenter.US
            )
        netVerifySDK.setPreselectedCountry("PHL")
        netVerifySDK.setPreselectedDocumentVariant(NVDocumentVariant.PLASTIC)
        netVerifySDK.setEnableVerification(true)
        netVerifySDK.setEnableIdentityVerification(true)
//        netVerifySDK.setEnableEMRTD(true)
        netVerifySDK.sendDebugInfoToJumio(true)
    }

    private fun showJumioToolTip() {
        val jumioToolTip = MaterialDialog(getAppCompatActivity()).apply {
            lifecycleOwner(getAppCompatActivity())
            customView(R.layout.dialog_tool_tip)
        }
        val ivTip =
            jumioToolTip.view.findViewById<ImageView>(R.id.iv_tip)
        val buttonClose =
            jumioToolTip.view.findViewById<MaterialButton>(R.id.buttonClose)
        val textViewTitle =
            jumioToolTip.view.findViewById<TextView>(R.id.textViewTitle)
        val textViewContent =
            jumioToolTip.view.findViewById<TextView>(R.id.textViewContent)
        ivTip.visibility(true)
        ivTip.setImageResource(R.drawable.logo_jumio_tips)
        buttonClose.backgroundTintList = null
        buttonClose.setContextCompatBackground(R.drawable.bg_gradient_orange)
        buttonClose.setContextCompatTextColor(R.color.colorWhite)
        buttonClose.text = formatString(R.string.action_got_it)
        textViewTitle.text = formatString(R.string.title_tooltip_jumio)
        textViewContent.text = formatString(R.string.msg_tooltip_jumio).toHtmlSpan()
        textViewContent.gravity = Gravity.START
        textViewContent.setPadding(
            resources.getDimensionPixelSize(R.dimen.content_spacing),
            0,
            resources.getDimensionPixelSize(R.dimen.content_spacing),
            0
        )
        buttonClose.setOnClickListener {
            it.setOnClickListener(null)
            jumioToolTip.cancel()
            initPermission()
        }
        jumioToolTip.window?.attributes?.windowAnimations =
            R.style.SlideUpAnimation
        jumioToolTip.window?.setGravity(Gravity.CENTER)
        jumioToolTip.show()
    }

    private fun navigateNextScreen() {
        if (viewModel.isEditMode.value == true) {
            enableItems(false)
            setCheckedState(viewModel.input.idTypeInput.value.notNullable())
            requireActivity().onBackPressed()
        } else {
            findNavController().navigate(R.id.action_dao_signature_fragment)
        }
    }

    private fun navigationDaoResult(daoHit: DaoHit) {
        val action =
            DaoJumioVerificationFragmentDirections.actionDaoResultFragment(
                daoHit.referenceNumber,
                DaoResultFragment.TYPE_REACH_OUT_HIT,
                daoHit.businessName,
                daoHit.preferredBranch,
                daoHit.preferredBranchEmail
            )
        findNavController().navigate(action)
    }

    override fun onClickNext() {
        navigateNextScreen()
    }

    @ThreadSafe
    companion object {
        const val EXTRA_IS_EDIT = "isEdit"
    }

    override val layoutId: Int
        get() = R.layout.fragment_dao_jumio_verification

    override val viewModelClassType: Class<DaoJumioVerificationViewModel>
        get() = DaoJumioVerificationViewModel::class.java
}
