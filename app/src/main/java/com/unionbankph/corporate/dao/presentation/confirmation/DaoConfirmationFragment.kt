package com.unionbankph.corporate.dao.presentation.confirmation

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.button.MaterialButton
import com.mpt.android.stv.Slice
import com.mpt.android.stv.SpannableTextView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.auth.data.model.Details
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.domain.constant.DaoErrorCodeEnum
import com.unionbankph.corporate.dao.domain.exception.ExpiredIDException
import com.unionbankph.corporate.dao.domain.exception.MismatchIDException
import com.unionbankph.corporate.dao.domain.exception.ReachOutPageException
import com.unionbankph.corporate.dao.domain.exception.RetakeIDException
import com.unionbankph.corporate.dao.domain.exception.VerificationProcessingException
import com.unionbankph.corporate.dao.domain.model.DaoHit
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.dao.presentation.result.DaoResultFragment
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_dao_confirmation.*
import kotlinx.android.synthetic.main.view_financial_information_details.*
import kotlinx.android.synthetic.main.view_personal_info_1_details.*
import kotlinx.android.synthetic.main.view_personal_info_2_details.*
import kotlinx.android.synthetic.main.view_personal_info_3_details.*
import javax.annotation.concurrent.ThreadSafe

class DaoConfirmationFragment :
    BaseFragment<DaoConfirmationViewModel>(R.layout.fragment_dao_confirmation),
    DaoActivity.ActionEvent {

    private lateinit var ivSignature: AppCompatImageView
    private lateinit var pbSignature: ProgressBar
    private lateinit var viewOverlay: View

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    private var loadingDialog: MaterialDialog? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initDaoActivity()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initClickListener()
        initCheckListener()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initReviewDetails()
        initBinding()
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[DaoConfirmationViewModel::class.java]
        viewModel.uiState.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    showLoadingDialog()
                }
                is UiState.Complete -> {
                    dismissLoadingDialog()
                }
                is UiState.Exit -> {
                    navigator.navigateClearStacks(
                        getAppCompatActivity(),
                        LoginActivity::class.java,
                        Bundle().apply { putBoolean(LoginActivity.EXTRA_SPLASH_SCREEN, false) },
                        true
                    )
                }
                is UiState.Error -> {
                    handleDaoOnError(it.throwable)
                }
            }
        })
        viewModel.navigateNextStep.observe(viewLifecycleOwner, EventObserver {
            navigationDaoResult(it)
        })
    }

    private fun handleDaoOnError(throwable: Throwable) {
        when (throwable) {
            is VerificationProcessingException -> {
                showDaoErrorDetailsDialog(DaoErrorCodeEnum.DAO_JUMIO_NEED_MORE_TIME)
            }
            is MismatchIDException -> {
                showDaoErrorDetailsDialog(DaoErrorCodeEnum.DAO_JUMIO_SCAN_RETRY)
            }
            is ExpiredIDException -> {
                showDaoErrorDetailsDialog(DaoErrorCodeEnum.DAO_JUMIO_EXPIRED_DOCUMENT_SCAN_RETRY)
            }
            is RetakeIDException -> {
                showDaoErrorDetailsDialog(DaoErrorCodeEnum.DAO_JUMIO_UNREADABLE_ID_SCAN_RETRY)
            }
            is ReachOutPageException -> {
                val details = JsonHelper.fromJson<Details>(throwable.message)
                val daoHit = DaoHit(
                    isHit = true,
                    businessName = details.businessName,
                    preferredBranchEmail = details.preferredBranchEmail,
                    preferredBranch = details.preferredBranch,
                    referenceNumber = daoActivity.viewModel.referenceNumber.value.notNullable()
                )
                navigationDaoResult(daoHit)
            }
            else -> {
                handleOnError(throwable)
            }
        }
    }

    private fun initReviewDetails() {
        if (daoActivity.viewModel.hasPersonalInformationOneInput.hasValue() &&
            !viewModel.isLoadedScreen.hasValue()
        ) {
            daoActivity.viewModel.input1.let {
                viewModel.setExistingPersonalInformationStepOne(it)
            }
        }
        if (daoActivity.viewModel.hasPersonalInformationTwoInput.hasValue() &&
            !viewModel.isLoadedScreen.hasValue()
        ) {
            daoActivity.viewModel.input2.let {
                viewModel.setExistingPersonalInformationStepTwo(it)
            }
        }
        if (daoActivity.viewModel.hasPersonalInformationThreeInput.hasValue() &&
            !viewModel.isLoadedScreen.hasValue()
        ) {
            daoActivity.viewModel.input3.let {
                viewModel.setExistingPersonalInformationStepThree(it)
            }
        }
        if (daoActivity.viewModel.hasPersonalInformationFourInput.hasValue() &&
            !viewModel.isLoadedScreen.hasValue()
        ) {
            daoActivity.viewModel.input4.let {
                viewModel.setExistingPersonalInformationStepFour(it)
            }
        }
        if (daoActivity.viewModel.hasJumioVerificationInput.hasValue() &&
            !viewModel.isLoadedScreen.hasValue()
        ) {
            daoActivity.viewModel.input5.let {
                viewModel.setExistingJumioVerification(it)
            }
        }
        if (daoActivity.viewModel.hasSignatureInput.hasValue() &&
            !viewModel.isLoadedScreen.hasValue()
        ) {
            daoActivity.viewModel.input6.let {
                viewModel.setExistingSignature(it)
            }
        }
    }

    private fun initDaoActivity() {
        daoActivity.setToolBarDesc(formatString(R.string.title_confirm_your_details))
        daoActivity.setProgressValue(7)
        daoActivity.showToolBarDetails()
        daoActivity.setButtonName(formatString(R.string.action_submit))
        daoActivity.setEnableButton(true)
        daoActivity.showProgress(false)
        daoActivity.showButton(true)
        daoActivity.setActionEvent(this)
        daoActivity.setDrawableBackButton(R.drawable.ic_close_white_24dp)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            showExitDaoBottomSheet()
        }
    }

    private fun initClickListener() {
        btn_personal_information_1.setOnClickListener {
            val action = DaoConfirmationFragmentDirections.actionPersonalInformationStepOne(true)
            findNavController().navigate(action)
            setBackIconToDefault()
        }
        btn_personal_information_2.setOnClickListener {
            val action = DaoConfirmationFragmentDirections.actionPersonalInformationStepTwo(true)
            findNavController().navigate(action)
            setBackIconToDefault()
        }
        btn_personal_information_3.setOnClickListener {
            val action = DaoConfirmationFragmentDirections.actionPersonalInformationStepThree(true)
            findNavController().navigate(action)
            setBackIconToDefault()
        }
        btn_financial_information.setOnClickListener {
            val action = DaoConfirmationFragmentDirections.actionPersonalInformationStepFour(true)
            findNavController().navigate(action)
            setBackIconToDefault()
        }
        btn_id_verification.setOnClickListener {
            val action = DaoConfirmationFragmentDirections.actionJumioVerificationFragment(true)
            findNavController().navigate(action)
            setBackIconToDefault()
        }
        btn_signature_specimen.setOnClickListener {
            val action = DaoConfirmationFragmentDirections.actionDaoSignatureFragment(true)
            findNavController().navigate(action)
            setBackIconToDefault()
        }
    }

    private fun setBackIconToDefault() {
        daoActivity.setDrawableBackButton(R.drawable.ic_arrow_back_white_24dp)
    }

    private fun initCheckListener() {
        cb_agreement.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isCheckedTNC.onNext(isChecked)
        }
    }

    private fun init() {
        if (App.isSupportedInProduction) {
            btn_id_verification.isInvisible = true
        }
        viewModel.loadDaoForm(daoActivity.viewModel.defaultDaoForm())
        ivSignature = view_signature.findViewById(R.id.imageView)
        viewOverlay = view_signature.findViewById(R.id.viewOverLay)
        pbSignature = view_signature.findViewById(R.id.imageViewProgressBar)
        viewOverlay.isVisible = false
        pbSignature.isVisible = false
    }

    private fun initBinding() {
        Handler().post {
            viewModel.input.salutationInput
                .subscribe {
                    tv_salutation.text = it.value
                }.addTo(disposables)
            viewModel.input.firstNameInput
                .subscribe {
                    tv_first_name.text = it
                }.addTo(disposables)
            viewModel.input.middleNameInput
                .subscribe {
                    tv_middle_name.text = it
                }.addTo(disposables)
            viewModel.input.lastNameInput
                .subscribe {
                    tv_last_name.text = it
                }.addTo(disposables)
            viewModel.input.emailAddressInput
                .subscribe {
                    tv_email_address.text = it
                }.addTo(disposables)
            viewModel.input.countryCodeInput
                .subscribe {
                    tv_mobile_number.text = it.name
                }.addTo(disposables)
            viewModel.input.businessMobileNumberInput
                .subscribe {
                    tv_mobile_number.text = it
                }.addTo(disposables)
            viewModel.input.genderInput
                .subscribe {
                    tv_gender.text = it.value
                }.addTo(disposables)
            viewModel.input.civilStatusInput
                .subscribe {
                    tv_civil_status.text = it.value
                }.addTo(disposables)

            viewModel.input.governmentIdNumberInput
                .subscribe {
                    tv_government_id.text = it
                }.addTo(disposables)
            viewModel.input.dateOfBirthInput
                .subscribe {
                    tv_date_of_birth.text =
                        it.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)
                }.addTo(disposables)
            viewModel.input.placeOfBirthInput
                .subscribe {
                    tv_place_of_birth.text = it
                }.addTo(disposables)
            viewModel.input.nationalityInput
                .subscribe {
                    tv_nationality.text = it.value
                }.addTo(disposables)
            viewModel.input.permanentAddressInput
                .subscribe {
                    if (!it) {
                        tv_address_title.text = formatString(R.string.title_permanent_address)
                    } else {
                        tv_address_title.text = formatString(R.string.title_present_address)
                    }
                }.addTo(disposables)
            viewModel.homeAddress
                .subscribe {
                    tv_address.text = it
                }.addTo(disposables)

            viewModel.input.occupationInput
                .subscribe {
                    if (it.id != OTHERS_CODE) {
                        tv_occupation.text = it.value
                    }
                }.addTo(disposables)
            viewModel.input.otherOccupationInput
                .subscribe {
                    if (viewModel.input.occupationInput.value?.id == OTHERS_CODE) {
                        tv_occupation.text =
                            ("${viewModel.input.occupationInput.value?.value} - $it")
                    }
                }.addTo(disposables)
            viewModel.input.sourceOfFundsInput
                .subscribe {
                    tv_source_of_funds.text = it.value
                }.addTo(disposables)
            viewModel.input.percentOwnershipInput
                .subscribe {
                    tv_percent_ownership.text = ("$it%")
                }.addTo(disposables)

            viewModel.input.idTypeInput
                .subscribe {
                    tv_id_type_title.text = it
                }.addTo(disposables)

            viewModel.input.fileInput
                .subscribe {
                    if (it != null) {
                        ivSignature.loadImage(it, "1${System.currentTimeMillis()}")
                    }
                }.addTo(disposables)
            viewModel.input.imagePath
                .filter { it != "" }
                .subscribe {
                    if (it != null) {
                        ivSignature.loaderImageByUrl(
                            it,
                            pbSignature,
                            "2"
                        )
                    }
                }.addTo(disposables)
        }
    }

    override fun onClickNext() {
        if (viewModel.isCheckedTNC.value == false) {
            showBottomSheetError()
        } else {
            viewModel.onClickedNext()
        }
    }

    private fun showBottomSheetError() {
        val confirmationBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_confirm_your_details),
            formatString(R.string.msg_confirm_your_details),
            actionNegative = formatString(R.string.action_close)
        )
        confirmationBottomSheet.setOnConfirmationPageCallBack(object : OnConfirmationPageCallBack {
            override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                confirmationBottomSheet.dismiss()
            }
        })
        confirmationBottomSheet.show(
            childFragmentManager,
            this::class.java.simpleName
        )
    }

    private fun showExitDaoBottomSheet() {
        val cancelBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_exit_dao),
            formatString(R.string.msg_exit_dao),
            formatString(R.string.action_yes),
            formatString(R.string.action_no)
        )
        cancelBottomSheet.setOnConfirmationPageCallBack(object : OnConfirmationPageCallBack {
            override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
                cancelBottomSheet.dismiss()
                viewModel.clearDaoCache()
            }

            override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                cancelBottomSheet.dismiss()
            }
        })
        cancelBottomSheet.show(
            childFragmentManager,
            TAG_CANCEL_DAO_DIALOG
        )
    }

    private fun showLoadingDialog() {
        loadingDialog = MaterialDialog(getAppCompatActivity()).apply {
            lifecycleOwner(getAppCompatActivity())
            customView(R.layout.dialog_dao_progress_bar)
        }
        loadingDialog?.cancelable(false)
        loadingDialog?.cancelOnTouchOutside(false)
        loadingDialog?.show()
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }

    private fun showDaoErrorDetailsDialog(daoErrorCodeEnum: DaoErrorCodeEnum) {
        val dialog = MaterialDialog(getAppCompatActivity()).apply {
            lifecycleOwner(getAppCompatActivity())
            customView(R.layout.dialog_tool_tip)
        }
        dialog.cancelable(false)
        dialog.cancelOnTouchOutside(false)
        val ivTip =
            dialog.view.findViewById<ImageView>(R.id.iv_tip)
        val ivClose =
            dialog.view.findViewById<ImageView>(R.id.iv_close)
        val buttonAction =
            dialog.view.findViewById<MaterialButton>(R.id.buttonClose)
        val textViewTitle =
            dialog.view.findViewById<TextView>(R.id.textViewTitle)
        val textViewContent =
            dialog.view.findViewById<SpannableTextView>(R.id.textViewContent)
        ivTip.isVisible = true
        textViewTitle.isVisible = false
        buttonAction.backgroundTintList = null
        buttonAction.setContextCompatBackground(R.drawable.bg_gradient_orange)
        buttonAction.setContextCompatTextColor(R.color.colorWhite)
        val logo = when (daoErrorCodeEnum) {
            DaoErrorCodeEnum.DAO_JUMIO_NEED_MORE_TIME -> {
                R.drawable.logo_dao_verification_processing
            }
            DaoErrorCodeEnum.DAO_JUMIO_UNREADABLE_ID_SCAN_RETRY -> {
                R.drawable.logo_dao_retake_id
            }
            DaoErrorCodeEnum.DAO_JUMIO_EXPIRED_DOCUMENT_SCAN_RETRY -> {
                R.drawable.logo_dao_expired_id
            }
            else -> {
                R.drawable.logo_dao_id_mismatch
            }
        }
        ivTip.setImageResource(logo)
        buttonAction.text = when (daoErrorCodeEnum) {
            DaoErrorCodeEnum.DAO_JUMIO_NEED_MORE_TIME -> {
                formatString(R.string.action_try_again)
            }
            DaoErrorCodeEnum.DAO_JUMIO_UNREADABLE_ID_SCAN_RETRY,
            DaoErrorCodeEnum.DAO_JUMIO_EXPIRED_DOCUMENT_SCAN_RETRY -> {
                formatString(R.string.action_retake_id)
            }
            else -> {
                formatString(R.string.action_go_back)
            }
        }
        ivClose.isVisible = daoErrorCodeEnum != DaoErrorCodeEnum.DAO_JUMIO_SCAN_RETRY
        val title = when (daoErrorCodeEnum) {
            DaoErrorCodeEnum.DAO_JUMIO_NEED_MORE_TIME -> {
                R.string.title_verification_still_processing
            }
            DaoErrorCodeEnum.DAO_JUMIO_UNREADABLE_ID_SCAN_RETRY -> {
                R.string.title_retake_your_id
            }
            DaoErrorCodeEnum.DAO_JUMIO_EXPIRED_DOCUMENT_SCAN_RETRY -> {
                R.string.title_expired_id
            }
            else -> {
                R.string.dao_title_mismatch_info
            }
        }
        val message = when (daoErrorCodeEnum) {
            DaoErrorCodeEnum.DAO_JUMIO_NEED_MORE_TIME -> {
                R.string.dao_desc_verification_processing
            }
            DaoErrorCodeEnum.DAO_JUMIO_UNREADABLE_ID_SCAN_RETRY -> {
                R.string.dao_desc_retake_id
            }
            DaoErrorCodeEnum.DAO_JUMIO_EXPIRED_DOCUMENT_SCAN_RETRY -> {
                R.string.dao_desc_expired_id
            }
            else -> {
                R.string.dao_desc_mismatch_info
            }
        }
        textViewContent.addSlice(
            Slice.Builder(formatString(title))
                .textSize(resources.getDimensionPixelSize(R.dimen.text_14))
                .textColor(ContextCompat.getColor(getAppCompatActivity(), R.color.colorInfo))
                .style(Typeface.BOLD)
                .build()
        )
        textViewContent.addSlice(
            Slice.Builder("\n\n")
                .textColor(ContextCompat.getColor(getAppCompatActivity(), R.color.colorInfo))
                .build()
        )
        textViewContent.addSlice(
            Slice.Builder(formatString(message))
                .textColor(ContextCompat.getColor(getAppCompatActivity(), R.color.colorInfo))
                .build()
        )
        textViewContent.display()
        buttonAction.setOnClickListener {
            it.setOnClickListener(null)
            dialog.cancel()
            when (daoErrorCodeEnum) {
                DaoErrorCodeEnum.DAO_JUMIO_NEED_MORE_TIME -> {
                    onClickNext()
                }
                DaoErrorCodeEnum.DAO_JUMIO_UNREADABLE_ID_SCAN_RETRY -> {
                    navigateJumioVerification()
                }
                DaoErrorCodeEnum.DAO_JUMIO_EXPIRED_DOCUMENT_SCAN_RETRY -> {
                    navigateJumioVerification()
                }
            }
        }
        ivClose.setOnClickListener {
            it.setOnClickListener(null)
            dialog.cancel()
        }
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.show()
    }

    private fun navigationDaoResult(daoHit: DaoHit) {
        val type = if (daoHit.isHit) {
            DaoResultFragment.TYPE_REACH_OUT_HIT
        } else {
            DaoResultFragment.TYPE_REACH_OUT_SUCCESS
        }
        val action = DaoConfirmationFragmentDirections.actionDaoResultFragment(
            daoHit.referenceNumber,
            type,
            daoHit.businessName,
            daoHit.preferredBranch,
            daoHit.preferredBranchEmail
        )
        findNavController().navigate(action)
    }

    private fun navigateJumioVerification() {
        val action = DaoConfirmationFragmentDirections.actionJumioVerificationFragment(true)
        findNavController().navigate(action)
        setBackIconToDefault()
    }

    @ThreadSafe
    companion object {
        const val TAG_CANCEL_DAO_DIALOG = "cancel_dao"
        const val OTHERS_CODE = "NV"
    }
}
