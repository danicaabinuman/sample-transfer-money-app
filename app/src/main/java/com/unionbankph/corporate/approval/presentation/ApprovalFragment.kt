package com.unionbankph.corporate.approval.presentation

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.NotificationSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.widget.recyclerview.viewpager.ViewPagerAdapter
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.approval.presentation.approval_done.ApprovalDoneFragment
import com.unionbankph.corporate.approval.presentation.approval_ongoing.ApprovalOngoingFragment
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.databinding.FragmentApprovalsBinding
import io.reactivex.rxkotlin.addTo


class ApprovalFragment :
    BaseFragment<FragmentApprovalsBinding, TutorialViewModel>(),
    OnTutorialListener {

    private lateinit var adapter: ViewPagerAdapter

    private val approvalOngoingFragment by lazyFast { adapter.getItem(0) as ApprovalOngoingFragment }

    override fun onResume() {
        super.onResume()
        if (hasInitialLoad) {
            hasInitialLoad = false
            initTutorialViewModel()
            init()
            initListener()
        }
    }

    private fun init() {
        binding.shadowToolbar.isVisible = isSME
        setupViewPager()
    }

    private fun initListener() {
        initEventBus()
        tutorialEngineUtil.setOnTutorialListener(this)
    }

    private fun initTutorialViewModel() {
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowTutorialHasTutorial -> {
                    if (it.hasTutorial &&
                        (activity as DashboardActivity).viewPager().currentItem == 2
                    ) {
                        startViewTutorial(false)
                    } else {
                        eventBus.settingsSyncEvent.emmit(
                            BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                        )
                    }
                }
            }
            if ((activity as DashboardActivity).viewPager().currentItem == 2 &&
                it is ShowTutorialError
            ) {
                handleOnError(it.throwable)
            }
        })
        eventBus.settingsSyncEvent.emmit(
            BaseEvent(SettingsSyncEvent.ACTION_DISABLE_NAVIGATION_BOTTOM)
        )
        viewModel.hasTutorial(TutorialScreenEnum.APPROVALS)
    }

    private fun initEventBus() {
        eventBus.settingsSyncEvent.flowable.subscribe {
            when (it.eventType) {
                SettingsSyncEvent.ACTION_RESET_TUTORIAL -> {
                    binding.tabLayoutAccount.setScrollPosition(0, 0f, true)
                    binding.viewPager.currentItem = 0
                }
                SettingsSyncEvent.ACTION_PUSH_TUTORIAL_APPROVAL -> {
                    isClickedHelpTutorial = true
                    binding.viewPager.currentItem = 0
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_DISABLE_NAVIGATION_BOTTOM)
                    )
                    startViewTutorial(true)
                }
            }
        }.addTo(disposables)
        eventBus.notificationSyncEvent.flowable.subscribe {
            when (it.eventType) {
                NotificationSyncEvent.ACTION_UPDATE_NOTIFICATION_APPROVAL -> {
                    hasInitialLoad = true
                }
            }
        }.addTo(disposables)
        eventBus.actionSyncEvent.emmit(BaseEvent(ActionSyncEvent.ACTION_LOAD_APPROVAL_ONGOING_LIST))
    }

    override fun onClickSkipButtonTutorial(spotlight: Spotlight) {
        isSkipTutorial = true
        viewModel.skipTutorial()
        spotlight.closeSpotlight()

    }

    override fun onClickOkButtonTutorial(spotlight: Spotlight) {
        spotlight.closeCurrentTarget()
    }

    override fun onStartedTutorial(view: View?, viewTarget: View) {
        //onStartedTutorial
        isSkipTutorial = false
    }

    override fun onEndedTutorial(view: View?, viewTarget: View) {
        if (isSkipTutorial) {
            eventBus.settingsSyncEvent.emmit(
                BaseEvent(SettingsSyncEvent.ACTION_TUTORIAL_APPROVAL_CLEAR_DATA)
            )
            eventBus.settingsSyncEvent.emmit(
                BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
            )
        } else {
            if (view != null) {
                if (view.id == binding.tabLayoutAccount.id) {
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_TUTORIAL_APPROVAL_TAP)
                    )
                }
            } else {
                if (isClickedHelpTutorial) {
                    isClickedHelpTutorial = false
                    tutorialEngineUtil.startTutorial(
                        getAppCompatActivity(),
                        binding.tabLayoutAccount,
                        R.layout.frame_tutorial_upper_left,
                        0f,
                        false,
                        getString(R.string.msg_tutorial_approval_tab),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                } else {
                    viewModel.setTutorial(TutorialScreenEnum.APPROVALS, false)
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_TUTORIAL_APPROVAL_CLEAR_DATA)
                    )
                    eventBus.settingsSyncEvent.emmit(
                        BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                    )
                }
            }
        }
    }

    private fun startViewTutorial(isShownTestData: Boolean) {
        if (isShownTestData) {
            eventBus.settingsSyncEvent.emmit(
                BaseEvent(SettingsSyncEvent.ACTION_TUTORIAL_APPROVAL_SHOW_DATA)
            )
        }
        tutorialEngineUtil.startTutorial(
            getAppCompatActivity(),
            R.drawable.ic_tutorial_approvals_orange,
            getString(R.string.title_tab_approvals),
            getString(R.string.msg_tutorial_approvals)
        )
    }

    private fun setupViewPager() {
        adapter = ViewPagerAdapter(
            childFragmentManager
        )
        adapter.addFragment(
            ApprovalOngoingFragment.newInstance(),
            FRAGMENT_ONGOING
        )
        adapter.addFragment(
            ApprovalDoneFragment.newInstance(),
            FRAGMENT_DONE
        )
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 0
        binding.tabLayoutAccount.setupWithViewPager(binding.viewPager, false)
        binding.tabLayoutAccount.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                //onTabReselected
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //onTabReselected
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (viewUtil.isSoftKeyboardShown(binding.constraintLayoutApproval))
                    viewUtil.dismissKeyboard(activity!!)
                when (tab?.position) {
                    0 -> {
                        binding.viewPager.currentItem = 0
                    }
                    1 -> {
                        binding.viewPager.currentItem = 1
                        approvalOngoingFragment.clearSelection()
                    }
                }
            }

        })
    }

    fun viewPager(): ViewPager = binding.viewPager

    companion object {
        private var FRAGMENT_ONGOING = "on-going"
        private var FRAGMENT_DONE = "done"
    }

    override val layoutId: Int
        get() = R.layout.fragment_approvals

    override val viewModelClassType: Class<TutorialViewModel>
        get() = TutorialViewModel::class.java
}


