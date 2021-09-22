package com.unionbankph.corporate.dao.presentation

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.enableButton
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.setContextCompatBackgroundColor
import com.unionbankph.corporate.app.common.extension.setContextCompatTextColor
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.dao.domain.model.SignatoryDetail
import com.unionbankph.corporate.dao.presentation.jumio_verification.DaoJumioVerificationViewModel
import com.unionbankph.corporate.dao.presentation.personal_info_1.DaoPersonalInformationStepOneViewModel
import com.unionbankph.corporate.dao.presentation.personal_info_2.DaoPersonalInformationStepTwoViewModel
import com.unionbankph.corporate.dao.presentation.personal_info_3.DaoPersonalInformationStepThreeViewModel
import com.unionbankph.corporate.dao.presentation.personal_info_4.DaoPersonalInformationStepFourViewModel
import com.unionbankph.corporate.dao.presentation.signature.DaoSignatureViewModel
import com.unionbankph.corporate.databinding.ActivityDaoBinding
import io.reactivex.rxkotlin.addTo
import javax.annotation.concurrent.ThreadSafe

class DaoActivity :
    BaseActivity<ActivityDaoBinding, DaoViewModel>() {

    private lateinit var buttonAction: Button

    private lateinit var actionEvent: ActionEvent
    private lateinit var navHostFragment: NavHostFragment

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        binding.viewToolbar.textViewCorporationName.setContextCompatTextColor(R.color.colorInfo)
        binding.viewToolbar.textViewTitle.setContextCompatTextColor(R.color.colorInfo)
        binding.viewToolbar.textViewCorporationName.text = formatString(R.string.title_digital_account_opening)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.navigatePages.observe(this, EventObserver {
            if (intent.getBooleanExtra(EXTRA_PRIVACY_POLICY, false)) {
                eventBus.actionSyncEvent.emmit(BaseEvent(ActionSyncEvent.ACTION_PLOT_SIGNATORY_DETAILS))
            }
            if (it.hasEditPersonalInformationOneInput
                && !intent.getBooleanExtra(EXTRA_PRIVACY_POLICY, false)
            ) {
                binding.navHostFragment.findNavController()
                    .navigate(R.id.action_personal_information_step_one)
            }
            if (it.hasEditPersonalInformationTwoInput) {
                binding.navHostFragment.findNavController()
                    .navigate(R.id.action_personal_information_step_two)
            }
            if (it.hasEditPersonalInformationThreeInput) {
                binding.navHostFragment.findNavController()
                    .navigate(R.id.action_personal_information_step_three)
            }
            if (it.hasEditPersonalInformationFourInput) {
                binding.navHostFragment.findNavController()
                    .navigate(R.id.action_personal_information_step_four)
            }
            if (it.hasEditJumioVerificationInput) {
                binding.navHostFragment.findNavController()
                    .navigate(R.id.action_jumio_verification_fragment)
            }
            if (it.hasEditSignatureInput) {
                binding.navHostFragment.findNavController()
                    .navigate(R.id.action_dao_signature_fragment)
            }
            if (it.hasEditConfirmationInput) {
                binding.navHostFragment.findNavController()
                    .navigate(R.id.action_dao_confirmation_fragment)
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initBinding()
    }

    override fun onSupportNavigateUp(): Boolean {
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_action_button -> {
                actionEvent.onClickNext()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val menuActionButton = menu.findItem(R.id.menu_action_button)
        val menuView = menuActionButton.actionView
        buttonAction = menuView.findViewById(R.id.buttonAction)
        buttonAction.text = viewModel.buttonName.value ?: formatString(R.string.action_next)
        buttonAction.visibility(viewModel.isShowButton.value ?: false)
        buttonAction.enableButton(viewModel.isEnableButton.value ?: true)
        menuActionButton.isVisible = true
        buttonAction.setOnClickListener { onOptionsItemSelected(menuActionButton) }
        return super.onCreateOptionsMenu(menu)
    }

    private fun initBinding() {
        viewModel.isShowButton
            .subscribe {
                invalidateOptionsMenu()
            }.addTo(disposables)
        viewModel.isEnableButton
            .subscribe {
                invalidateOptionsMenu()
            }.addTo(disposables)
        viewModel.buttonName
            .subscribe {
                invalidateOptionsMenu()
            }.addTo(disposables)
    }

    private fun init() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.dao_navigation)
        if (intent.getBooleanExtra(EXTRA_PRIVACY_POLICY, false)) {
            graph.setStartDestination(R.id.dao_personal_information_step_one_fragment)
            navHostFragment.navController.graph = graph
            viewModel.getSignatoryDetailsFromCache()
        }
        val token = intent.getStringExtra(EXTRA_TOKEN_DEEP_LINK)
        token?.let {
            viewModel.tokenDeepLink.onNext(it)
        }
    }

    fun showGoBackBottomSheet() {
        val cancelBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_go_back),
            formatString(R.string.msg_go_back),
            formatString(R.string.action_confirm),
            formatString(R.string.action_no)
        )
        cancelBottomSheet.setOnConfirmationPageCallBack(object : OnConfirmationPageCallBack {
            override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
                cancelBottomSheet.dismiss()
                popBackStack()
            }

            override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                cancelBottomSheet.dismiss()
            }
        })
        cancelBottomSheet.show(
            supportFragmentManager,
            TAG_GO_BACK_DAO_DIALOG
        )
    }

    fun popBackStack() {
        binding.navHostFragment.findNavController().popBackStack()
    }

    fun setToolBarDesc(desc: String) {
        binding.viewToolbar.textViewTitle.text = desc
    }

    fun setToolBarTitle(title: String) {
        binding.viewToolbar.textViewCorporationName.text = title
    }

    fun hideToolBarDetails() {
        binding.viewToolbar.textViewCorporationName.visibility(false)
        binding.viewToolbar.textViewTitle.visibility(false)
    }

    fun showToolBarDetails() {
        binding.viewToolbar.textViewCorporationName.visibility(true)
        binding.viewToolbar.textViewTitle.visibility(true)
    }

    fun clearToolbarDetails() {
        hideToolBarDetails()
        showProgress(false)
        showButton(false)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
        removeElevation(binding.viewToolbar.appBarLayout)
        binding.viewToolbar.appBarLayout.setContextCompatBackgroundColor(R.color.colorTransparent)
    }

    fun setActionEvent(actionEvent: ActionEvent) {
        this.actionEvent = actionEvent
    }

    fun showButton(isShownButton: Boolean) {
        viewModel.isShowButton.onNext(isShownButton)
    }

    fun setEnableButton(isEnable: Boolean) {
        viewModel.isEnableButton.onNext(isEnable)
    }

    fun setButtonName(actionName: String) {
        viewModel.buttonName.onNext(actionName)
    }

    fun showProgress(isShown: Boolean) {
        binding.progressAction.visibility(isShown)
    }

    fun setProgressValue(step: Int) {
        val progressValue = step * 100 / TOTAL_STEPS
        //progress_action.progress = progressValue
        ObjectAnimator.ofInt(binding.progressAction, "progress", progressValue)
            .setDuration(150)
            .start()
        binding.viewToolbar.textViewCorporationName.text = formatString(R.string.param_dao_part, step, TOTAL_STEPS)
    }

    fun setPersonalInformationStepOneInput(input: DaoPersonalInformationStepOneViewModel.Input) {
        viewModel.setPersonalInformationStepOneInput(input)
    }

    fun setPersonalInformationStepTwoInput(input: DaoPersonalInformationStepTwoViewModel.Input) {
        viewModel.setPersonalInformationStepTwoInput(input)
    }

    fun setPersonalInformationStepThreeInput(input: DaoPersonalInformationStepThreeViewModel.Input) {
        viewModel.setPersonalInformationStepThreeInput(input)
    }

    fun setPersonalInformationStepFourInput(input: DaoPersonalInformationStepFourViewModel.Input) {
        viewModel.setPersonalInformationStepFourInput(input)
    }

    fun setJumioVerificationInput(input: DaoJumioVerificationViewModel.Input) {
        viewModel.setJumioVerificationInput(input)
    }

    fun setSignatureInput(input: DaoSignatureViewModel.Input) {
        viewModel.setSignatureInput(input)
    }

    fun setSignatoriesDetail(signatoryDetail: SignatoryDetail) {
        viewModel.setSignatoriesDetail(signatoryDetail)
    }

    interface ActionEvent {
        fun onClickNext()
    }

    @ThreadSafe
    companion object {
        const val TOTAL_STEPS = 7
        const val EXTRA_TOKEN_DEEP_LINK = "token"
        const val EXTRA_PRIVACY_POLICY = "privacy_policy"

        const val TAG_GO_BACK_DAO_DIALOG = "go_back_dialog"
    }

    override val viewModelClassType: Class<DaoViewModel>
        get() = DaoViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityDaoBinding
        get() = ActivityDaoBinding::inflate
}
