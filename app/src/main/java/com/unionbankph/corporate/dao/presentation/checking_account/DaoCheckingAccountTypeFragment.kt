package com.unionbankph.corporate.dao.presentation.checking_account

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.auth.presentation.policy.PrivacyPolicyActivity
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.databinding.FragmentDaoCheckingAccountTypeBinding
import io.reactivex.rxkotlin.addTo
import javax.annotation.concurrent.ThreadSafe

class DaoCheckingAccountTypeFragment :
    BaseFragment<FragmentDaoCheckingAccountTypeBinding, DaoCheckingAccountTypeViewModel>() {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Success -> {
                        findNavController().navigate(R.id.action_welcome_fragment)
                    }
                    is UiState.Loading -> {
                        showProgressAlertDialog(this::class.java.simpleName)
                    }
                    is UiState.Complete -> {
                        dismissProgressAlertDialog()
                    }
                    is UiState.Error -> {
                        handleOnError(event.throwable)
                    }
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initListener()
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe {
            if (ActionSyncEvent.ACTION_AGREE_PRIVACY_POLICY == it.eventType) {
                viewModel.generateDaoToken()
            }
        }.addTo(disposables)
    }

    private fun init() {
        daoActivity.showToolBarDetails()
        daoActivity.showButton(false)
        daoActivity.showProgress(true)
        daoActivity.setProgressValue(1)
        daoActivity.setToolBarDesc(formatString(R.string.title_checking_account_type))
        binding.btnEdit.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_fragment_enter)
        }
    }

    private fun initListener() {
        binding.cvBusinessStarter.setOnClickListener {
            viewModel.checkingAccountType.onNext(BUSINESS_STARTER)
            navigatePrivacyPolicyScreen()
        }
        binding.cvBusinessCheck.setOnClickListener {
            viewModel.checkingAccountType.onNext(BUSINESS_CHECK)
            navigatePrivacyPolicyScreen()
        }
    }

    private fun navigatePrivacyPolicyScreen() {
        val action =
            DaoCheckingAccountTypeFragmentDirections.actionPrivacyPolicyActivity(
                PrivacyPolicyActivity.PAGE_DAO
            )
        findNavController().navigate(action)
    }

    @ThreadSafe
    companion object {
        const val BUSINESS_STARTER = "BUSINESS_STARTER"
        const val BUSINESS_CHECK = "BUSINESS_CHECK"
    }

    override val layoutId: Int
        get() = R.layout.fragment_dao_checking_account_type

    override val viewModelClassType: Class<DaoCheckingAccountTypeViewModel>
        get() = DaoCheckingAccountTypeViewModel::class.java
}
