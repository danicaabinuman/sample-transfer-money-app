package com.unionbankph.corporate.notification.presentation.notification_log.notification_log_list

import android.graphics.Rect
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.RxView
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.setEnableView
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.FragmentSettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.NotificationSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.SettingsSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.widget.dialog.ConfirmationBottomSheet
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.data.model.StateData
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.callback.OnConfirmationPageCallBack
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.databinding.FragmentNotificationLogBinding
import com.unionbankph.corporate.notification.data.model.NotificationLogDto
import com.unionbankph.corporate.notification.presentation.notification_log.NotificationLogError
import com.unionbankph.corporate.notification.presentation.notification_log.NotificationLogViewModel
import com.unionbankph.corporate.notification.presentation.notification_log.ShowDismissLoading
import com.unionbankph.corporate.notification.presentation.notification_log.ShowEndlessDismissLoading
import com.unionbankph.corporate.notification.presentation.notification_log.ShowEndlessLoading
import com.unionbankph.corporate.notification.presentation.notification_log.ShowLoading
import com.unionbankph.corporate.notification.presentation.notification_log.ShowMarkAllAsRead
import com.unionbankph.corporate.notification.presentation.notification_log.ShowProgressBarDismissLoading
import com.unionbankph.corporate.notification.presentation.notification_log.ShowProgressBarLoading
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.concurrent.TimeUnit

class NotificationLogFragment :
    BaseFragment<FragmentNotificationLogBinding, NotificationLogViewModel>(),
    EpoxyAdapterCallback<NotificationLogDto>,
    OnTutorialListener, OnConfirmationPageCallBack {

    private var markAllAsReadBottomSheet: ConfirmationBottomSheet? = null

    private val controller by lazyFast {
        NotificationLogController(
            applicationContext,
            viewUtil
        )
    }

    private val imageViewMarkAllAsRead by lazyFast {
        (activity as DashboardActivity).imageViewMarkAllAsRead()
    }

    private val pageable by lazyFast { Pageable() }

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
                        startAlertsTutorial()
                    } else {
                        eventBus.settingsSyncEvent.emmit(
                            BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                        )
                    }
                }
                is ShowTutorialError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    private fun initViewModel() {
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowLoading -> {
                    showLoading(
                        binding.viewLoadingState.viewLoadingLayout,
                        binding.swipeRefreshLayoutNotificationLog,
                        binding.recyclerViewNotificationLog,
                        binding.textViewState
                    )
                }
                is ShowDismissLoading -> {
                    dismissLoading(
                        binding.viewLoadingState.viewLoadingLayout,
                        binding.swipeRefreshLayoutNotificationLog,
                        binding.recyclerViewNotificationLog
                    )
                }
                is ShowEndlessLoading -> {
                    pageable.isLoadingPagination = true
                    updateController(getNotificationLogs())
                }
                is ShowEndlessDismissLoading -> {
                    pageable.isLoadingPagination = false
                    updateController(getNotificationLogs())
                }
                is ShowProgressBarLoading -> {
                    showProgressAlertDialog(
                        NotificationLogFragment::class.java.simpleName
                    )
                }
                is ShowProgressBarDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowMarkAllAsRead -> {
                    val notificationLogBadgeCount = it.notificationLogBadgeCount
                    if (notificationLogBadgeCount > 0) {
                        val badgeCountAlertsSharedPref =
                            sharedPreferenceUtil.badgeCountAlertsSharedPref()
                        badgeCountAlertsSharedPref.set(0)
                        updateShortCutBadgeCount()
                        eventBus.notificationSyncEvent.emmit(
                            BaseEvent(NotificationSyncEvent.ACTION_UPDATE_NOTIFICATION_ALERTS)
                        )
                    }
                    binding.swipeRefreshLayoutNotificationLog.isRefreshing = true
                    getNotificationLogs(true)
                }
                is NotificationLogError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.notifications.observe(this, Observer {
            it?.let {
                if (pageable.isInitialLoad) {
                    showEmptyState(it)
                    updateMarkAllAsRead(it)
                }
                updateController(it)
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        binding.swipeRefreshLayoutNotificationLog.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                getNotificationLogs(true)
            }
        }
        RxView.clicks(imageViewMarkAllAsRead)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                showMarkAllAsReadBottomSheet()
            }.addTo(disposables)
        tutorialEngineUtil.setOnTutorialListener(this)
    }

    override fun onClickItem(view: View, data: NotificationLogDto, position: Int) {
        eventBus.fragmentSettingsSyncEvent.emmit(
            BaseEvent(
                FragmentSettingsSyncEvent.ACTION_CLICK_NOTIFICATION_LOG_ITEM,
                JsonHelper.toJson(data)
            )
        )
    }

    override fun onTapToRetry() {
        getNotificationLogs(isInitialLoading = false, isTapToRetry = true)
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
        if (view != null) {
            when (view) {
                imageViewMarkAllAsRead -> {
                    val constraintSet = ConstraintSet()
                    val constraintLayoutTutorial =
                        viewTarget.findViewById<ConstraintLayout>(
                            R.id.constraintLayoutTutorial
                        )
                    val imgArrow = viewTarget.findViewById<ImageView>(R.id.imageViewArrow)
                    val rect = Rect()
                    view.getGlobalVisibleRect(rect)
                    var paramsSource: ViewGroup.LayoutParams? = imgArrow.layoutParams
                    if (paramsSource == null) paramsSource = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    val widthSpecSource = viewUtil.makeMeasureSpec(paramsSource.width)
                    val heightSpecSource = viewUtil.makeMeasureSpec(paramsSource.height)
                    imgArrow.measure(widthSpecSource, heightSpecSource)
                    val marginTop = rect.bottom + resources.getDimension(
                        R.dimen.content_group_spacing
                    ).toInt()
                    val marginLeft =
                        rect.right - ((rect.right - rect.left) / 2) -
                                imgArrow.measuredWidth - ((rect.right - rect.left) / 2)
                    constraintSet.clone(constraintLayoutTutorial)
                    constraintSet.connect(
                        imgArrow.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID,
                        ConstraintSet.TOP, marginTop
                    )
                    constraintSet.connect(
                        imgArrow.id, ConstraintSet.START, ConstraintSet.PARENT_ID,
                        ConstraintSet.START, marginLeft
                    )
                    constraintSet.applyTo(constraintLayoutTutorial)
                }
            }
        }
    }

    override fun onEndedTutorial(view: View?, viewTarget: View) {
        if (!isSkipTutorial && view == null) {
            if (isClickedHelpTutorial) {
                isClickedHelpTutorial = false
                if (imageViewMarkAllAsRead.isVisible) {
                    tutorialEngineUtil.startTutorial(
                        getAppCompatActivity(),
                        imageViewMarkAllAsRead,
                        R.layout.frame_tutorial_upper_right,
                        getCircleFloatSize(imageViewMarkAllAsRead),
                        true,
                        getString(R.string.msg_tutorial_mark_all_as_read),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                }
            } else {
                tutorialViewModel.setTutorial(TutorialScreenEnum.ALERTS, false)
                eventBus.settingsSyncEvent.emmit(
                    BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
                )
            }
        } else {
            eventBus.settingsSyncEvent.emmit(
                BaseEvent(SettingsSyncEvent.ACTION_ENABLE_NAVIGATION_BOTTOM)
            )
        }
    }

    override fun onClickPositiveButtonDialog(data: String?, tag: String?) {
        markAllAsReadBottomSheet?.dismiss()
        viewModel.markAllAsReadNotificationLogs()
    }

    override fun onClickNegativeButtonDialog(data: String?, tag: String?) {
        markAllAsReadBottomSheet?.dismiss()
    }

    private fun initEventBus() {
        eventBus.actionSyncEvent.flowable.subscribe(
            { eventBus ->
                when (eventBus.eventType) {
                    ActionSyncEvent.ACTION_UPDATE_NOTIFICATION_LOG_ITEM -> {
                        val notifications = getNotificationLogs()
                        notifications.filter { it.data.id == eventBus.payload?.toLong() }
                            .forEach { it.state = true }
                        updateController(notifications)
                    }
                    ActionSyncEvent.ACTION_LOAD_NOTIFICATION_LOG_LIST -> {
                        getNotificationLogs(true)
                    }
                    ActionSyncEvent.ACTION_NAVIGATE_NOTIFICATION_LOG_DETAIL -> {
                        this.eventBus.fragmentSettingsSyncEvent.emmit(
                            BaseEvent(
                                FragmentSettingsSyncEvent.ACTION_CLICK_NOTIFICATION_LOG_ITEM,
                                JsonHelper.toJson(
                                    NotificationLogDto(
                                        id = eventBus.payload?.toLong(),
                                        isRead = false
                                    )
                                )
                            )
                        )
                    }
                }
            }, {
                Timber.e(it, "actionSyncEvent")
            }
        ).addTo(disposables)
        eventBus.settingsSyncEvent.flowable.subscribe {
            if (it.eventType == SettingsSyncEvent.ACTION_PUSH_TUTORIAL_NOTIFICATIONS) {
                isClickedHelpTutorial = true
                eventBus.settingsSyncEvent.emmit(
                    BaseEvent(SettingsSyncEvent.ACTION_DISABLE_NAVIGATION_BOTTOM)
                )
                startAlertsTutorial()
            } else if (it.eventType == SettingsSyncEvent.ACTION_TUTORIAL_ALERTS) {
                eventBus.settingsSyncEvent.emmit(
                    BaseEvent(SettingsSyncEvent.ACTION_DISABLE_NAVIGATION_BOTTOM)
                )
                tutorialViewModel.hasTutorial(TutorialScreenEnum.ALERTS)
            }
        }.addTo(disposables)
    }

    private fun getCircleFloatSize(view: View): Float {
        return view.height.toFloat() - resources.getDimension(R.dimen.view_tutorial_radius_padding)
    }

    private fun updateController(data: MutableList<StateData<NotificationLogDto>>) {
        controller.setData(data, pageable)
    }

    fun getNotificationLogs(isInitialLoading: Boolean, isTapToRetry: Boolean = false) {
        if (isTapToRetry) {
            pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            pageable.resetPagination()
        } else {
            pageable.resetLoad()
        }
        viewModel.getNotificationLogs(pageable, isInitialLoading)
    }

    private fun initRecyclerView() {
        val linearLayoutManager = getLinearLayoutManager()
        binding.recyclerViewNotificationLog.layoutManager = linearLayoutManager
        binding.recyclerViewNotificationLog.addOnScrollListener(
            object : PaginationScrollListener(linearLayoutManager) {
                override val totalPageCount: Int
                    get() = pageable.totalPageCount
                override val isLastPage: Boolean
                    get() = pageable.isLastPage
                override val isLoading: Boolean
                    get() = pageable.isLoadingPagination
                override val isFailed: Boolean
                    get() = pageable.isFailed

                override fun loadMoreItems() {
                    if (!pageable.isLoadingPagination) getNotificationLogs(false)
                }
            }
        )
        binding.recyclerViewNotificationLog.setController(controller)
        controller.setAdapterCallbacks(this)
    }

    private fun showEmptyState(data: MutableList<StateData<NotificationLogDto>>) {
        if (data.size > 0) {
            eventBus.actionSyncEvent.emmit(
                BaseEvent(ActionSyncEvent.ACTION_UPDATE_MARK_ALL_AS_READ_ICON, "1")
            )
            if (binding.textViewState.visibility == View.VISIBLE) binding.textViewState.visibility = View.GONE
        } else {
            eventBus.actionSyncEvent.emmit(
                BaseEvent(ActionSyncEvent.ACTION_UPDATE_MARK_ALL_AS_READ_ICON, "0")
            )
            binding.textViewState.visibility = View.VISIBLE
        }
    }

    private fun startAlertsTutorial() {
        tutorialEngineUtil.startTutorial(
            getAppCompatActivity(),
            R.drawable.ic_tutorial_alerts_orange,
            getString(R.string.title_tab_notifications),
            getString(R.string.msg_tutorial_alerts)
        )
    }

    private fun updateMarkAllAsRead(data: MutableList<StateData<NotificationLogDto>>) {
        imageViewMarkAllAsRead.setEnableView(data.isNotEmpty())
    }

    private fun showMarkAllAsReadBottomSheet() {
        markAllAsReadBottomSheet = ConfirmationBottomSheet.newInstance(
            R.drawable.ic_outline_check_white_medium,
            formatString(R.string.title_mark_all_as_read),
            getString(R.string.desc_mark_all_as_read),
            formatString(R.string.action_yes),
            formatString(R.string.action_no)
        )
        markAllAsReadBottomSheet?.setOnConfirmationPageCallBack(this)
        markAllAsReadBottomSheet?.show(
            childFragmentManager,
            NotificationLogFragment::class.java.simpleName
        )
    }

    private fun getNotificationLogs(): MutableList<StateData<NotificationLogDto>> {
        return viewModel.notifications.value ?: mutableListOf()
    }

    override val layoutId: Int
        get() = R.layout.fragment_notification_log

    override val viewModelClassType: Class<NotificationLogViewModel>
        get() = NotificationLogViewModel::class.java

}
