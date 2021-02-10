package com.unionbankph.corporate.dao.presentation.welcome_enter

import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.refresh
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.auth.data.model.Details
import com.unionbankph.corporate.auth.presentation.policy.PrivacyPolicyActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.domain.exception.ReachOutPageException
import com.unionbankph.corporate.dao.domain.exception.VerificationCompletedException
import com.unionbankph.corporate.dao.domain.model.DaoHit
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.dao.presentation.result.DaoResultFragment
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_dao_welcome_enter.*

class DaoWelcomeEnterFragment :
    BaseFragment<DaoWelcomeEnterViewModel>(R.layout.fragment_dao_welcome_enter),
    DaoActivity.ActionEvent {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[DaoWelcomeEnterViewModel::class.java]
        viewModel.navigateNextStep.observe(viewLifecycleOwner, EventObserver {
            if (it) {
                navigateDaoScreen()
            } else {
                navigatePrivacyPolicy()
            }
        })
        viewModel.uiState.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is UiState.Complete -> {
                    dismissProgressAlertDialog()
                }
                is UiState.Error -> {
                    handleDaoOnError(it.throwable)
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initBinding()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
    }

    private fun init() {
        daoActivity.setToolBarTitle(formatString(R.string.title_digital_account_opening))
        daoActivity.setToolBarDesc(formatString(R.string.title_personal_information))
        daoActivity.showToolBarDetails()
        daoActivity.showButton(true)
        daoActivity.setEnableButton(false)
        daoActivity.setActionEvent(this)
        daoActivity.showProgress(false)
        validateForm()
    }

    private fun initBinding() {
        daoActivity.viewModel.tokenDeepLink.value?.let {
            viewModel.loadDeepLink(it)
        }
        viewModel.input.isValidFormInput
            .subscribe {
                daoActivity.setEnableButton(it)
            }.addTo(disposables)
    }

    private fun handleDaoOnError(throwable: Throwable) {
        when (throwable) {
            is VerificationCompletedException -> {
                val details = JsonHelper.fromJson<Details>(throwable.message)
                val daoHit = DaoHit(
                    false,
                    businessName = details.businessName,
                    referenceNumber = tie_reference_number.text?.toString().notNullable()
                )
                navigationDaoResult(daoHit)
            }
            else -> {
                handleOnError(throwable)
            }
        }
    }

    private fun validateForm() {
        val referenceNumberObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = tie_reference_number
        )
        initSetError(referenceNumberObservable)
        RxCombineValidator(referenceNumberObservable)
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                viewModel.input.isValidFormInput.onNext(it)
            }
            .subscribe()
            .addTo(disposables)
    }

    private fun clearFormFocus() {
        constraint_layout.requestFocus()
        constraint_layout.isFocusableInTouchMode = true
        viewUtil.dismissKeyboard(getAppCompatActivity())
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            if (ActionSyncEvent.ACTION_AGREE_PRIVACY_POLICY == it.eventType) {
                // PDAOO70V1AJ9-001
                navigateDaoScreen()
            }
        }.addTo(disposables)
    }

    private fun navigateDaoScreen() {
        viewModel.output.signatoryDetail.value?.let { signatoryDetail ->
            daoActivity.setSignatoriesDetail(signatoryDetail)
        }
    }

    private fun navigatePrivacyPolicy() {
        val action =
            DaoWelcomeEnterFragmentDirections.actionPrivacyPolicyActivity(PrivacyPolicyActivity.PAGE_DAO)
        findNavController().navigate(action)
    }

    private fun navigationDaoResult(daoHit: DaoHit) {
        val type = if (daoHit.isHit) {
            DaoResultFragment.TYPE_REACH_OUT_HIT
        } else {
            DaoResultFragment.TYPE_REACH_OUT_COMPLETED
        }
        val action =
            DaoWelcomeEnterFragmentDirections.actionDaoResultFragment(
                daoHit.referenceNumber,
                type,
                daoHit.businessName,
                daoHit.preferredBranch,
                daoHit.preferredBranchEmail
            )
        findNavController().navigate(action)
    }

    private fun showMissingFieldDialog() {
        showMaterialDialogError(
            message = formatString(R.string.msg_error_missing_fields)
        )
    }

    override fun onClickNext() {
        constraint_layout.post {
            clearFormFocus()
            if (viewModel.hasValidForm()) {
                viewModel.onClickedSubmit(tie_reference_number.text.toString())
            } else {
                showMissingFieldDialog()
                tie_reference_number.refresh()
            }
        }
    }

}
