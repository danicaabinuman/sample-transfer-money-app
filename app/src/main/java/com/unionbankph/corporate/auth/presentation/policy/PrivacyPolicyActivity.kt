package com.unionbankph.corporate.auth.presentation.policy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.service.fcm.AutobahnFirebaseMessagingService
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.databinding.ActivityPrivacyPolicyBinding
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class PrivacyPolicyActivity :
    BaseActivity<ActivityPrivacyPolicyBinding, PrivacyPolicyViewModel>() {

    private var adapter: ViewPagerAdapter? = null

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        if (isSME) {
            removeElevation(binding.viewToolbar.appBarLayout)
            binding.shadowToolbar.isVisible = true
        }
        setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_terms_and_conditions))
        if (intent.getStringExtra(EXTRA_REQUEST_PAGE) != PAGE_LEARN_MORE)
            setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initBinding()
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

    override fun onBackPressed() {
        when (intent.getStringExtra(EXTRA_REQUEST_PAGE)) {
            PAGE_LOGIN -> {
                viewModel.clearLoginCredential()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initClickListener()
        initCheckListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (intent.getStringExtra(EXTRA_REQUEST_PAGE) == PAGE_MCD_TERMS) {
            invokePrivacyPolicyAgreed()
        }
    }

    private fun initViewModel() {
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    showProgressAlertDialog(this::class.java.simpleName)
                }
                is UiState.Complete -> {
                    dismissProgressAlertDialog()
                }
                is UiState.Success -> {
                    when (intent.getStringExtra(EXTRA_REQUEST_PAGE)) {
                        PAGE_LOGIN -> {
                            navigateDashboardScreen()
                        }
                        PAGE_MCD_TERMS -> {
                            onBackPressed()
                        }
                    }
                }
                is UiState.Exit -> {
                    navigateLoginScreen()
                }
                is UiState.Error -> {
                    showMaterialDialogError(message = it.throwable.message)
                }
            }
        })
    }

    private fun invokePrivacyPolicyAgreed() {
        eventBus.actionSyncEvent.emmit(
            BaseEvent(ActionSyncEvent.ACTION_AGREE_PRIVACY_POLICY)
        )
    }

    private fun init() {
        setupViewPager()
        if (intent.getStringExtra(EXTRA_REQUEST_PAGE) == PAGE_LEARN_MORE) {
            binding.buttonAccept.visibility = View.GONE
            val viewPagerParams = binding.viewPager.layoutParams as ViewGroup.MarginLayoutParams
            viewPagerParams.setMargins(
                viewPagerParams.leftMargin,
                viewPagerParams.topMargin,
                viewPagerParams.rightMargin,
                0
            )
            binding.cbTnc.visibility(false)
            binding.cbPrivacy.visibility(false)
        } else if (intent.getStringExtra(EXTRA_REQUEST_PAGE) == PAGE_DAO) {
            binding.cbUbAccOpening.visibility(true)
        }
    }

    private fun initBinding() {
//        Observables.combineLatest(
//            viewModel.isCheckedPrivacyAgreement,
//            viewModel.isCheckedTNCAgreement
//        ) { isCheckedPrivacyAgreement, isCheckedTNCAgreement ->
//            isCheckedPrivacyAgreement && isCheckedTNCAgreement
//        }
//            .subscribe {
//                buttonAccept.enableButton(it)
//            }.addTo(disposables)
    }

    private fun navigateDashboardScreen() {
        val bundle = Bundle()
        bundle.putString(
            AutobahnFirebaseMessagingService.EXTRA_DATA, intent.getStringExtra(
                AutobahnFirebaseMessagingService.EXTRA_DATA
            )
        )
        navigator.navigateClearStacks(
            this,
            DashboardActivity::class.java,
            bundle,
            true,
            Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun initClickListener() {
        RxView.clicks(binding.buttonAccept)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                if (viewModel.isCheckedPrivacyAgreement.value == false ||
                    viewModel.isCheckedTNCAgreement.value == false ||
                    isNotCheckedUBAccountOpeningAgreement()
                ) {
                    showBottomSheetError()
                } else {
                    when {
                        intent.getStringExtra(EXTRA_REQUEST_PAGE) == PAGE_DAO -> {
                            navigateDaoScreen()
                        }
                        intent.getStringExtra(EXTRA_REQUEST_PAGE) == PAGE_MCD_TERMS -> {
                            viewModel.agreeMCDTerms()
                        }
                        intent.getStringExtra(EXTRA_REQUEST_PAGE) == PAGE_LOGIN -> {
                            viewModel.privacyPolicy()
                        }
                    }
                }
            }.addTo(disposables)
    }

    private fun isNotCheckedUBAccountOpeningAgreement() : Boolean {
        return (intent.getStringExtra(EXTRA_REQUEST_PAGE) == PAGE_DAO &&
                viewModel.isCheckedUBAccountOpeningAgreement.value == false)
    }

    private fun initCheckListener() {
        binding.cbTnc.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isCheckedTNCAgreement.onNext(isChecked)
        }
        binding.cbPrivacy.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isCheckedPrivacyAgreement.onNext(isChecked)
        }
        binding.cbUbAccOpening.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isCheckedUBAccountOpeningAgreement.onNext(isChecked)
        }
    }

    private fun setupViewPager() {
        adapter = ViewPagerAdapter(
            supportFragmentManager
        )
        adapter?.addFragment(
            PrivacyPolicyFragment.newInstance(),
            getString(R.string.title_privacy_policy)
        )
        adapter?.addFragment(
            TermsAndConditionsFragment.newInstance(),
            getString(R.string.title_terms_and_conditions)
        )
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 0
        binding.tabLayoutPrivacyPolicy.setupWithViewPager(binding.viewPager, false)
    }

    private fun navigateLoginScreen() {
        navigator.navigateClearUpStack(
            this,
            LoginActivity::class.java,
            null,
            isClear = true,
            isAnimated = true
        )
    }

    private fun navigateDaoScreen() {
        navigator.navigateClearStacks(
            this,
            DaoActivity::class.java,
            Bundle().apply {
                putBoolean(DaoActivity.EXTRA_PRIVACY_POLICY, true)
            },
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
        )
    }

    private fun showBottomSheetError() {
        val confirmationBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_warning_white,
            formatString(R.string.title_acceptance_required),
            formatString(R.string.msg_acceptance_required),
            actionNegative = formatString(R.string.action_close)
        )
        confirmationBottomSheet.setOnConfirmationPageCallBack(object : OnConfirmationPageCallBack {
            override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
                confirmationBottomSheet.dismiss()
            }
        })
        confirmationBottomSheet.show(
            supportFragmentManager,
            this::class.java.simpleName
        )
    }

    companion object {

        const val EXTRA_REQUEST_PAGE = "page"
        const val EXTRA_EMAIL = "email"

        const val PAGE_LEARN_MORE = "learn_more"
        const val PAGE_LOGIN = "login"
        const val PAGE_DAO = "dao"
        const val PAGE_MCD_TERMS = "mcd_terms"
    }

    override val viewModelClassType: Class<PrivacyPolicyViewModel>
        get() = PrivacyPolicyViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityPrivacyPolicyBinding
        get() = ActivityPrivacyPolicyBinding::inflate
}
