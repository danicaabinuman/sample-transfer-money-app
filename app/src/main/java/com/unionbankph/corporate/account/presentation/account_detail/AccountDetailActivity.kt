package com.unionbankph.corporate.account.presentation.account_detail

import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.data.model.Record
import com.unionbankph.corporate.account.presentation.account_history.AccountTransactionHistoryActivity
import com.unionbankph.corporate.account.presentation.account_history_detail.AccountTransactionHistoryDetailsActivity
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivityAccountDetailsBinding
import io.reactivex.rxkotlin.addTo

class AccountDetailActivity :
    BaseActivity<ActivityAccountDetailsBinding, AccountDetailViewModel>(),
    AccountDetailController.AdapterCallbacks,
    OnTutorialListener {

    private var account: Account? = null

    private var isLoading: Boolean = false

    private val controller by lazyFast {
        AccountDetailController(applicationContext, viewUtil, autoFormatUtil)
    }

    override fun beforeLayout(savedInstanceState: Bundle?) {
        super.beforeLayout(savedInstanceState)
        if (account == null)
            account = JsonHelper.fromJson(intent.getStringExtra(EXTRA_ACCOUNT))
    }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_account_details))
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        tutorialEngineUtil.setOnTutorialListener(this)
        initRecyclerView()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initTutorialViewModel()
        initViewModel()
    }

    private fun initTutorialViewModel() {
        tutorialViewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[TutorialViewModel::class.java]
        tutorialViewModel.state.observe(this, Observer {
            when (it) {
                is ShowTutorialHasTutorial -> {
                    if (it.hasTutorial) {
                        startViewTutorial()
                    }
                }
                is ShowTutorialError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        // tutorialViewModel.hasTutorial(TutorialScreenEnum.ACCOUNT_DETAILS)
    }

    private fun initViewModel() {
    viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    isLoading = true
                    updateController()
                }
                is UiState.Complete -> {
                    isLoading = false
                    updateController()
                }
                is UiState.Error -> {
                    isLoading = false
                    updateController()
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.records.observe(this, Observer {
            updateController(it)
        })
        viewModel.getRecentTransactions(account?.id.toString(), 3)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        dataBus.accountDataBus.flowable.subscribe {
            if (it?.id == account?.id) {
                account = it
                updateController()
            }
        }.addTo(disposables)
    }

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val helpMenu = menu.findItem(R.id.menu_help)
        helpMenu.isVisible = true
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_help -> {
                isClickedHelpTutorial = true
                binding.recyclerViewAccountDetails.post {
                    binding.recyclerViewAccountDetails.smoothScrollToPosition(0)
                }
                Handler().postDelayed(
                    {
                        startViewTutorial()
                    }, resources.getInteger(R.integer.time_enter_tutorial_immediate).toLong()
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClickSkipButtonTutorial(spotlight: Spotlight) {
        isSkipTutorial = true
        tutorialViewModel.skipTutorial()
        spotlight.closeSpotlight()
    }

    override fun onClickOkButtonTutorial(spotlight: Spotlight) {
        spotlight.closeCurrentTarget()
    }

    override fun onStartedTutorial(view: View?, viewTarget: View) {
        // onStartedTutorial
        isSkipTutorial = false
    }

    override fun onEndedTutorial(view: View?, viewTarget: View) {
        if (isSkipTutorial) {
            binding.recyclerViewAccountDetails.post { binding.recyclerViewAccountDetails.scrollToPosition(0) }
        } else {
            when (view) {
                binding.recyclerViewAccountDetails.findViewHolderForAdapterPosition(0)
                    ?.itemView?.findViewById<androidx.cardview.widget.CardView>(R.id.cardViewAccountDetails) -> {
                    binding.recyclerViewAccountDetails.post {
                        binding.recyclerViewAccountDetails.scrollToPosition(
                            controller.adapter.itemCount - 1
                        )
                    }
                    Handler().postDelayed(
                        {
                            val cardViewAll =
                                binding.recyclerViewAccountDetails.findViewHolderForAdapterPosition(0)
                                    ?.itemView?.findViewById<androidx.cardview.widget.CardView>(R.id.cardViewAll)
                            tutorialEngineUtil.startTutorial(
                                this,
                                cardViewAll!!,
                                R.layout.frame_tutorial_lower_right,
                                resources.getDimension(R.dimen.grid_2),
                                false,
                                getString(R.string.msg_tutorial_account_detail_view_all),
                                GravityEnum.TOP,
                                OverlayAnimationEnum.ANIM_EXPLODE
                            )
                        }, resources.getInteger(R.integer.time_enter_tutorial_immediate).toLong()
                    )
                }
                else -> {
                    binding.recyclerViewAccountDetails.post { binding.recyclerViewAccountDetails.scrollToPosition(0) }
                    // tutorialViewModel.setTutorial(TutorialScreenEnum.ACCOUNT_DETAILS, false)
                }
            }
        }
    }

    private fun updateController(data: MutableList<Record> = viewModel.records.value.notNullable()) {
        controller.setData(data, account, isLoading)
    }

    override fun onClickItem(record: String?) {
        val bundle = Bundle()
        bundle.putString(AccountTransactionHistoryDetailsActivity.EXTRA_RECORD, record)
        bundle.putString(
            AccountTransactionHistoryDetailsActivity.EXTRA_ID,
            account?.id.toString()
        )
        navigator.navigate(
            this,
            AccountTransactionHistoryDetailsActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    override fun onClickViewAll(id: String?) {
        val bundle = Bundle()
        bundle.putString(AccountTransactionHistoryActivity.EXTRA_ID, id)
        navigator.navigate(
            this,
            AccountTransactionHistoryActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    private fun initRecyclerView() {
        controller.setAdapterCallbacks(this)
        binding.recyclerViewAccountDetails.setController(controller)
        updateController()
    }

    private fun startViewTutorial() {
        val cardViewDetails =
            binding.recyclerViewAccountDetails.findViewHolderForAdapterPosition(0)
                ?.itemView?.findViewById<androidx.cardview.widget.CardView>(R.id.cardViewAccountDetails)!!
        binding.recyclerViewAccountDetails.post {
            binding.recyclerViewAccountDetails.scrollTo(0, cardViewDetails.bottom)
        }
        tutorialEngineUtil.startTutorial(
            this,
            cardViewDetails,
            R.layout.frame_tutorial_lower_left,
            resources.getDimension(R.dimen.grid_2),
            false,
            getString(R.string.msg_tutorial_account_detail),
            GravityEnum.TOP,
            OverlayAnimationEnum.ANIM_EXPLODE
        )
    }

    companion object {
        const val EXTRA_ACCOUNT = "account"
    }

    override val viewModelClassType: Class<AccountDetailViewModel>
        get() = AccountDetailViewModel::class.java

    override val layoutId: Int
        get() = R.layout.activity_account_details
}
