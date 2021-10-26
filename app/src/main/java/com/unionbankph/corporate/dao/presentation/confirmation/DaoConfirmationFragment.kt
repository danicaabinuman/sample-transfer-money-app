package com.unionbankph.corporate.dao.presentation.confirmation

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
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
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.constant.EnrollingSourceEnum
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
import com.unionbankph.corporate.databinding.FragmentDaoConfirmationBinding
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.annotation.concurrent.ThreadSafe

class DaoConfirmationFragment :
    BaseFragment<FragmentDaoConfirmationBinding, DaoConfirmationViewModel>(),
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
        viewModel.apply {
            uiState.observe(viewLifecycleOwner, EventObserver {
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
            navigateNextStep.observe(viewLifecycleOwner, EventObserver {
                navigationDaoResult(it)
            })
        }

        /*daoActivity.viewModel.apply {
            navigatePages.observe(viewLifecycleOwner, Observer {
                when(daoActivity.viewModel.signatoriesDetail.value?.enrollingSource){
                    EnrollingSourceEnum.SALES_FORCE.value -> {
                        isEdittableDetails(false)
                    }
                }
            })
        }*/ /*This is currently on hold for salesforce*/
    }

    private fun handleDaoOnError(throwable: Throwable) {
        when (throwable) {
            is VerificationProcessingException -> {
                showDaoErrorDetailsDialog(DaoErrorCodeEnum.DAO_JUMIO_NEED_MORE_TIME)
            }
            is MismatchIDException -> {
                Timber.e("throwable " + throwable.message)
                showDaoErrorDetailsDialog(DaoErrorCodeEnum.DAO_JUMIO_SCAN_RETRY, throwable.message)
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
        daoActivity.viewModel.apply {
            if(hasPersonalInformationOneInput.hasValue() &&
                !viewModel.isLoadedScreen.hasValue()
            ) {
                input1.let {
                    viewModel.setExistingPersonalInformationStepOne(it)
                }
            }
            if (hasPersonalInformationTwoInput.hasValue() &&
                !viewModel.isLoadedScreen.hasValue()
            ) {
                input2.let {
                    viewModel.setExistingPersonalInformationStepTwo(it)
                }
            }
            if (hasPersonalInformationThreeInput.hasValue() &&
                !viewModel.isLoadedScreen.hasValue()
            ) {
                input3.let {
                    viewModel.setExistingPersonalInformationStepThree(it)
                }
            }
            if (hasPersonalInformationFourInput.hasValue() &&
                !viewModel.isLoadedScreen.hasValue()
            ) {
                input4.let {
                    viewModel.setExistingPersonalInformationStepFour(it)
                }
            }
            if (hasJumioVerificationInput.hasValue() &&
                !viewModel.isLoadedScreen.hasValue()
            ) {
                input5.let {
                    viewModel.setExistingJumioVerification(it)
                }
            }
            if (hasSignatureInput.hasValue() &&
                !viewModel.isLoadedScreen.hasValue()
            ) {
                input6.let {
                    viewModel.setExistingSignature(it)
                }
            }
            signatoriesDetail.value?.unionBankOfficer?.let {
                binding.cbUnionbankEmployee.isChecked = it
                viewModel.isCheckedIsUbEmployee.onNext(it)
            }
        }
    }

    private fun initDaoActivity() {
        daoActivity.apply {
            setToolBarDesc(formatString(R.string.title_confirm_your_details))
            setProgressValue(7)
            showToolBarDetails()
            setButtonName(formatString(R.string.action_submit))
            setEnableButton(true)
            showProgress(false)
            showButton(true)
            setActionEvent(this@DaoConfirmationFragment)
            setDrawableBackButton(R.drawable.ic_close_white_24dp)
            onBackPressedDispatcher.addCallback(viewLifecycleOwner){
                showExitDaoBottomSheet()
            }
        }
    }

    private fun initClickListener() {
        binding.viewPersonalInfo1.btnPersonalInformation1.setOnClickListener {
            val action = DaoConfirmationFragmentDirections.actionPersonalInformationStepOne(true)
            findNavController().navigate(action)
            setBackIconToDefault()
        }
        binding.viewPersonalInfo2.btnPersonalInformation2.setOnClickListener {
            val action = DaoConfirmationFragmentDirections.actionPersonalInformationStepTwo(true)
            findNavController().navigate(action)
            setBackIconToDefault()
        }
        binding.viewPersonalInfo3.btnPersonalInformation3.setOnClickListener {
            val action = DaoConfirmationFragmentDirections.actionPersonalInformationStepThree(true)
            findNavController().navigate(action)
            setBackIconToDefault()
        }
        binding.viewFinancialInformation.btnFinancialInformation.setOnClickListener {
            val action = DaoConfirmationFragmentDirections.actionPersonalInformationStepFour(true)
            findNavController().navigate(action)
            setBackIconToDefault()
        }
        binding.btnIdVerification.setOnClickListener {
            val action = DaoConfirmationFragmentDirections.actionJumioVerificationFragment(true)
            findNavController().navigate(action)
            setBackIconToDefault()
        }
        binding.btnSignatureSpecimen.setOnClickListener {
            val action = DaoConfirmationFragmentDirections.actionDaoSignatureFragment(true)
            findNavController().navigate(action)
            setBackIconToDefault()
        }
        binding.ivUnionbankEmployeeTooltip.setOnClickListener {
            displayIsUBEmployeeTooltip()
        }
    }

    private fun displayIsUBEmployeeTooltip() {
        val isUbEmployeeToolTip = MaterialDialog(getAppCompatActivity()).apply {
            lifecycleOwner(this@DaoConfirmationFragment)
            customView(R.layout.dialog_tool_tip)
        }
        val buttonClose =
            isUbEmployeeToolTip.view.findViewById<AppCompatButton>(R.id.buttonClose)
        val textViewTitle =
            isUbEmployeeToolTip.view.findViewById<TextView>(R.id.textViewTitle)
        val textViewContent =
            isUbEmployeeToolTip.view.findViewById<TextView>(R.id.textViewContent)
        textViewTitle.text = formatString(R.string.title_not_a_unionbank_employee)
        textViewContent.text = formatString(R.string.msg_not_a_unionbank_employee)
        buttonClose.setOnClickListener { isUbEmployeeToolTip.dismiss() }
        isUbEmployeeToolTip.apply {
            window?.attributes?.windowAnimations =
                R.style.SlideUpAnimation
                window?.setGravity(Gravity.CENTER)
                show()
        }
    }

    private fun isEdittableDetails(isShow: Boolean){
        binding.viewPersonalInfo1.btnPersonalInformation1.setVisible(isShow)
        binding.viewPersonalInfo2.btnPersonalInformation2.setVisible(isShow)
        binding.viewPersonalInfo3.btnPersonalInformation3.setVisible(isShow)
        binding.viewFinancialInformation.btnFinancialInformation.setVisible(isShow)
        binding.btnIdVerification.setVisible(isShow)
    }

    private fun setBackIconToDefault() {
        daoActivity.setDrawableBackButton(R.drawable.ic_arrow_back_white_24dp)
    }

    private fun initCheckListener() {
        binding.cbAgreement.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isCheckedTNC.onNext(isChecked)
        }
        binding.cbUnionbankEmployee.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isCheckedIsUbEmployee.onNext(isChecked)
        }
    }

    private fun init() {
        if (App.isSupportedInProduction) {
            binding.btnIdVerification.isInvisible = true
        }
        viewModel.loadDaoForm(daoActivity.viewModel.defaultDaoForm())
        ivSignature = binding.viewSignature.imageView
        viewOverlay = binding.viewSignature.viewOverLay.root
        pbSignature = binding.viewSignature.imageViewProgressBar
        viewOverlay.isVisible = false
        pbSignature.isVisible = false
    }

    private fun initBinding() {
        Handler().post {
            viewModel.input.apply {
                salutationInput
                    .subscribe {
                        binding.viewPersonalInfo1.tvSalutation.text = it.value
                    }.addTo(disposables)
                firstNameInput
                    .subscribe {
                        binding.viewPersonalInfo1.tvFirstName.text = it
                    }.addTo(disposables)
                middleNameInput
                    .subscribe {
                        binding.viewPersonalInfo1.tvMiddleName.text = it
                    }.addTo(disposables)
                lastNameInput
                    .subscribe {
                        binding.viewPersonalInfo1.tvLastName.text = it
                    }.addTo(disposables)
                emailAddressInput
                    .subscribe {
                        binding.viewPersonalInfo1.tvEmailAddress.text = it
                    }.addTo(disposables)
                countryCodeInput
                    .subscribe {
                        binding.viewPersonalInfo1.tvMobileNumber.text = it.name
                    }.addTo(disposables)
                businessMobileNumberInput
                    .subscribe {
                        binding.viewPersonalInfo1.tvMobileNumber.text = it
                    }.addTo(disposables)
                genderInput
                    .subscribe {
                        binding.viewPersonalInfo1.tvGender.text = it.value
                    }.addTo(disposables)
                civilStatusInput
                    .subscribe {
                        binding.viewPersonalInfo1.tvCivilStatus.text = it.value
                    }.addTo(disposables)
                governmentIdNumberInput
                    .subscribe {
                        binding.viewPersonalInfo2.tvGovernmentId.text = it
                    }.addTo(disposables)
                dateOfBirthInput
                    .subscribe {
                        binding.viewPersonalInfo2.tvDateOfBirth.text =
                            it.convertDateToDesireFormat(DateFormatEnum.DATE_FORMAT_DATE)
                    }.addTo(disposables)
                placeOfBirthInput
                    .subscribe {
                        binding.viewPersonalInfo2.tvPlaceOfBirth.text = it
                    }.addTo(disposables)
                nationalityInput
                    .subscribe {
                        binding.viewPersonalInfo2.tvNationality.text = it.value
                    }.addTo(disposables)
                permanentAddressInput
                    .subscribe {
                        if (!it) {
                            binding.viewPersonalInfo3.tvAddressTitle.text = formatString(R.string.title_permanent_address)
                        } else {
                            binding.viewPersonalInfo3.tvAddressTitle.text = formatString(R.string.title_present_address)
                        }
                    }.addTo(disposables)
                viewModel.homeAddress
                    .subscribe {
                        binding.viewPersonalInfo3.tvAddress.text = it
                    }.addTo(disposables)

                occupationInput
                    .subscribe {
                        if (it.id != OTHERS_CODE) {
                            binding.viewFinancialInformation.tvOccupation.text = it.value
                        }
                    }.addTo(disposables)
                otherOccupationInput
                    .subscribe {
                        if (this.occupationInput.value?.id == OTHERS_CODE) {
                            binding.viewFinancialInformation.tvOccupation.text =
                                ("${this.occupationInput.value?.value} - $it")
                        }
                    }.addTo(disposables)
                sourceOfFundsInput
                    .subscribe {
                        binding.viewFinancialInformation.tvSourceOfFunds.text = it.value
                    }.addTo(disposables)
                percentOwnershipInput
                    .subscribe {
                        binding.viewFinancialInformation.tvPercentOwnership.text = ("$it%")
                    }.addTo(disposables)

                idTypeInput
                    .subscribe {
                        binding.tvIdTypeTitle.text = it
                    }.addTo(disposables)

                fileInput
                    .subscribe {
                        if (it != null) {
                            ivSignature.loadImage(it, "1${System.currentTimeMillis()}")
                        }
                    }.addTo(disposables)
                imagePath
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
        loadingDialog?.apply {
            MaterialDialog(getAppCompatActivity()).apply {
                lifecycleOwner(getAppCompatActivity())
                customView(R.layout.dialog_dao_progress_bar)
            }
            cancelable(false)
            cancelOnTouchOutside(false)
            show()
        }
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }

    private fun showDaoErrorDetailsDialog(daoErrorCodeEnum: DaoErrorCodeEnum, mismatchIdError: String? = null) {
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
            dialog.view.findViewById<AppCompatButton>(R.id.buttonClose)
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
                DaoErrorCodeEnum.DAO_JUMIO_SCAN_RETRY -> {
                    mismatchIdError?.let {
                        navigateToMismatchedForms(mismatchIdError)
                    }
                }
            }
        }
        ivClose.setOnClickListener { view ->
            Timber.e("ivClose.setOnClickListener throwable " + mismatchIdError)

            mismatchIdError?.let {
                navigateToMismatchedForms(mismatchIdError)
            }

            view.setOnClickListener(null)
            dialog.cancel()
        }
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.show()
    }

    private fun navigateToMismatchedForms(mismatchedError: String) {
        val mismatchedErrorList : MutableList<String> = JsonHelper.fromListJson(mismatchedError)
        val hasFirstNameMismatch = mismatchedErrorList.contains(Constant.MismatchIDDetails.FIRST_NAME)
        val hasLastNameMismatch = mismatchedErrorList.contains(Constant.MismatchIDDetails.LAST_NAME)
        val hasBirthDateMismatch = mismatchedErrorList.contains(Constant.MismatchIDDetails.BIRTH_DATE)

        if (hasFirstNameMismatch || hasLastNameMismatch) {
            val action = DaoConfirmationFragmentDirections.actionPersonalInformationStepOne(
                isEdit = true,
                isFirstNameMismatched = hasFirstNameMismatch,
                isLastNameMismatched= hasLastNameMismatch,
                isBirthDateMismatched = hasBirthDateMismatch
            )
            findNavController().navigate(action)
            setBackIconToDefault()
        } else if (hasBirthDateMismatch) {
            val action = DaoConfirmationFragmentDirections.actionPersonalInformationStepTwo(
                isEdit = true,
                isBirthDateMismatched = true
            )
            findNavController().navigate(action)
            setBackIconToDefault()
        }
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

    override val viewModelClassType: Class<DaoConfirmationViewModel>
        get() = DaoConfirmationViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDaoConfirmationBinding
        get() = FragmentDaoConfirmationBinding::inflate
}
