package com.unionbankph.corporate.app.base

import android.animation.ObjectAnimator
import android.animation.StateListAnimator
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.App
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.setColor
import com.unionbankph.corporate.app.common.extension.setContextCompatBackgroundColor
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.bus.data.DataBus
import com.unionbankph.corporate.app.common.platform.bus.event.ActionSyncEvent
import com.unionbankph.corporate.app.common.platform.bus.event.EventBus
import com.unionbankph.corporate.app.common.platform.bus.event.base.BaseEvent
import com.unionbankph.corporate.app.common.platform.glide.GlideApp
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.MyContextWrapper
import com.unionbankph.corporate.app.common.widget.dialog.ProgressBarDialog
import com.unionbankph.corporate.app.common.widget.dialog.SessionTimeOutBottomSheet
import com.unionbankph.corporate.app.common.widget.tutorial.TutorialEngineUtil
import com.unionbankph.corporate.app.common.widget.validator.validation.RxValidationResult
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.app.service.SessionTimeOutJobService
import com.unionbankph.corporate.app.service.fcm.AutobahnFirebaseMessagingService
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.SettingsUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.approval.presentation.approval_detail.ApprovalDetailActivity
import com.unionbankph.corporate.auth.data.model.Error
import com.unionbankph.corporate.auth.data.model.Role
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.common.data.model.PushNotificationPayload
import com.unionbankph.corporate.common.data.source.local.cache.CacheManager
import com.unionbankph.corporate.common.data.source.local.sharedpref.SharedPreferenceUtil
import com.unionbankph.corporate.common.domain.exception.*
import com.unionbankph.corporate.common.domain.provider.SchedulerProvider
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.constant.NotificationLogTypeEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.*
import com.unionbankph.corporate.mcd.presentation.detail.CheckDepositDetailActivity
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.net.SocketTimeoutException
import javax.inject.Inject

abstract class BaseActivity<VM : ViewModel>(layoutId: Int) :
    AppCompatActivity(layoutId),
    SessionTimeOutBottomSheet.OnBottomSheetSessionTimeOutListener {

    lateinit var viewModel: VM

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var eventBus: EventBus

    @Inject
    lateinit var dataBus: DataBus

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var viewUtil: ViewUtil

    @Inject
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil

    @Inject
    lateinit var cacheManager: CacheManager

    @Inject
    lateinit var settingsUtil: SettingsUtil

    @Inject
    lateinit var tutorialEngineUtil: TutorialEngineUtil

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var autoFormatUtil: AutoFormatUtil

    lateinit var generalViewModel: GeneralViewModel

    lateinit var tutorialViewModel: TutorialViewModel

    private var dialog: ProgressBarDialog? = null

    private var sessionTimeOutBottomSheet: SessionTimeOutBottomSheet? = null

    private var sessionAuthAlertDialog: MaterialDialog? = null

    val disposables = CompositeDisposable()

    var isSkipTutorial: Boolean = false

    var isValidForm: Boolean = false

    var isClickedHelpTutorial: Boolean = false

    var isInitialSubmitForm: Boolean = true

    var isDismissedSessionTimeOut: Boolean = true

    var mLastClickTime: Long = 0

    val isSME = App.isSME()

    protected open fun beforeLayout(savedInstanceState: Bundle?) = Unit
    protected open fun afterLayout(savedInstanceState: Bundle?) = Unit
    protected open fun onViewsBound() = Unit
    protected open fun onInitializeListener() = Unit
    protected open fun onViewModelBound() {
        initViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initDependencyInjection()
        initStatusBar()
        beforeLayout(savedInstanceState)
        super.onCreate(savedInstanceState)
        initCheckOrientationSupport()
        registerReceiver()
        afterLayout(savedInstanceState)
        onViewModelBound()
        onViewsBound()
        onInitializeListener()
        doBindService()
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart")
        LocalBroadcastManager.getInstance(this).registerReceiver(
            sessionTimeoutReceiver,
            IntentFilter(Constant.ACTION_SESSION_TIMEOUT)
        )
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop")
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sessionTimeoutReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        GlideApp.get(this).clearMemory()
        unRegisterReceiver()
        disposables.clear()
        disposables.dispose()
        initOnDestroy()
        doUnBindService()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(
            R.anim.anim_backward_left_to_right, R.anim.anim_backward_right_to_left
        )
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        Timber.d("onUserInteraction")
        dismissSessionTimeOutBottomSheet()
        startSessionJobService()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(getMyContext(newBase))
    }

    private fun getMyContext(context: Context): Context {
        return MyContextWrapper.wrap(context, "en")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        Timber.d("onNewIntent")
        initPushNotificationLandingPage()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (App.isSME()) {
            for (i in 0 until menu.size()) {
                val menuItem = menu.getItem(i)
                menuItem.icon?.let {
                    it.mutate()
                    it.setColor(this, R.color.colorInfo)
                }
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    fun onBackPressed(isAnimated: Boolean) {
        super.onBackPressed()
        if (isAnimated) overridePendingTransition(
            R.anim.anim_backward_left_to_right, R.anim.anim_backward_right_to_left
        )
    }

    override fun onClickBottomSheetAction() {
        dismissSessionTimeOutBottomSheet()
    }

    override fun onDismissSessionTimeOutDialog() {
        sessionTimeOutBottomSheet = null
        isDismissedSessionTimeOut = true
        if (App.isActivityVisible()) {
            startSessionJobService()
        }
    }

    private fun initViewModel() {
        generalViewModel =
            ViewModelProviders.of(this, viewModelFactory)[GeneralViewModel::class.java]
        generalViewModel.state.observe(this, Observer {
            when (it) {
                is ShowGeneralLoading -> {
                    showProgressAlertDialog(BaseActivity::class.java.simpleName)
                }
                is ShowGeneralDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowGeneralSuccessSwitchOrganization -> {
                    val bundle = Bundle()
                    bundle.putString(AutobahnFirebaseMessagingService.EXTRA_DATA, it.data)
                    navigator.reNavigateActivity(
                        this,
                        DashboardActivity::class.java,
                        bundle,
                        isClear = true,
                        isAnimated = true,
                        transitionActivity = Navigator.TransitionActivity.TRANSITION_FLIP
                    )
                }
                is ShowGeneralLogoutUser -> {
                    logoutUser()
                }
            }
        })
        generalViewModel.badgeCountLiveData.observe(this, Observer {
            viewUtil.setShortCutBadge(this, it)
        })
    }

    private fun initOnDestroy() {
        dismissSessionAuthDialog()
        dismissSessionTimeOutBottomSheet()
    }

    private fun dismissSessionAuthDialog() {
        if (sessionAuthAlertDialog != null && sessionAuthAlertDialog?.isShowing == true) {
            sessionAuthAlertDialog?.dismiss()
        }
    }

    private fun dismissSessionTimeOutBottomSheet() {
        if (sessionTimeOutBottomSheet != null && sessionTimeOutBottomSheet?.isVisible == true) {
            sessionTimeOutBottomSheet?.dismiss()
            sessionTimeOutBottomSheet = null
        }
    }

    fun logoutUser() {
        navigator.navigateClearStacks(
            this,
            LoginActivity::class.java,
            Bundle().apply {
                putBoolean(LoginActivity.EXTRA_SPLASH_SCREEN, false)
            },
            true,
            Navigator.TransitionActivity.TRANSITION_SLIDE_RIGHT
        )
    }

    fun backToDashBoard() {
        navigator.navigateClearUpStack(
            this,
            DashboardActivity::class.java,
            null,
            isClear = true,
            isAnimated = true
        )
    }

    private fun initDependencyInjection() {
        AndroidInjection.inject(this)
    }

    private fun initStatusBar() {
        if (isSME && (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP ||
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1)
        ) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorDarkGray)
        }
    }

    private fun initCheckOrientationSupport() {
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun showSessionTimeOutBottomSheet(tag: String, initialSecond: Long) {
        sessionTimeOutBottomSheet = SessionTimeOutBottomSheet.newInstance(initialSecond)
        sessionTimeOutBottomSheet?.setOnSessionTimeOutListener(this)
        sessionTimeOutBottomSheet?.show(supportFragmentManager, tag)
    }

    private val notificationDialogReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == null) return
            showConfirmationDialog(intent)
        }
    }

    private fun showConfirmationDialog(intent: Intent) {
        val code = intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_CODE)
        if (intent.action == Constant.Notification.ACTION_DIALOG) {
            when (code) {
                NotificationLogTypeEnum.CORP_USER_LOGOUT.name -> {
                    showSessionAuthAlertDialog(intent)
                }
                NotificationLogTypeEnum.TRANSACTION_STATUS_CHANGED.name,
                NotificationLogTypeEnum.MOBILE_CHECK_DEPOSIT.name -> {
                    showSwitchOrganizationDialog(intent)
                }
                NotificationLogTypeEnum.FORGET_DEVICE.name,
                NotificationLogTypeEnum.TRUST_DEVICE.name,
                NotificationLogTypeEnum.UNTRUST_DEVICE.name -> {
                    showTrustedDeviceDialog(intent)
                }
            }
        }
    }

    private fun showSessionAuthAlertDialog(intent: Intent) {
        App.stopSessionJobService(this)
        val message =
            intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_MESSAGE)
        sessionAuthAlertDialog = materialDialogLogoutUser(
            this@BaseActivity,
            message.notNullable()
        )
        sessionAuthAlertDialog?.show()
    }

    private fun showTrustedDeviceDialog(intent: Intent) {
        val data = JsonHelper.fromJson<PushNotificationPayload>(
            intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA)
        )
        MaterialDialog(this).show {
            lifecycleOwner(this@BaseActivity)
            title(text = data.title)
            message(text = data.message.notNullable())
            positiveButton(
                res = R.string.action_close,
                click = {
                    it.dismiss()
                    navigator.reNavigateActivity(
                        this@BaseActivity,
                        LoginActivity::class.java,
                        Bundle().apply {
                            putBoolean(LoginActivity.EXTRA_SPLASH_SCREEN, false)
                        },
                        isClear = true,
                        isAnimated = true,
                        transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_RIGHT
                    )
                }
            )
        }
    }

    private fun showSwitchOrganizationDialog(intent: Intent) {
        val data = JsonHelper.fromJson<PushNotificationPayload>(
            intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA)
        )
        MaterialDialog(this).show {
            lifecycleOwner(this@BaseActivity)
            title(R.string.title_push_switch_account)
            message(R.string.msg_push_switch_account)
            positiveButton(
                res = R.string.action_yes,
                click = {
                    it.dismiss()
                    generalViewModel.switchOrganization(
                        data.roleId.notNullable(),
                        intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA)
                    )
                }
            )
             negativeButton(
                res = R.string.action_no,
                click = {
                    it.dismiss()
                }
            )
        }
    }

    fun initTransparency() {
        if (Build.VERSION.SDK_INT in 19..20) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isSME) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    fun setMargins(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        if (view.layoutParams is ViewGroup.MarginLayoutParams) {
            val p = view.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(left, top, right, bottom)
            view.requestLayout()
        }
    }

    fun initToolbar(toolbar: Toolbar?, appBarLayout: View) {
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowCustomEnabled(true)
            it.setDisplayShowTitleEnabled(false)
        }
        if (App.isSME()) {
            appBarLayout.setContextCompatBackgroundColor(R.color.colorWhite)
            toolbar?.context?.setTheme(R.style.ToolbarSME)
        } else {
            removeElevation(appBarLayout)
            appBarLayout.setContextCompatBackgroundColor(R.color.colorTransparent)
        }
    }

    fun initToolbar(toolbar: Toolbar?, appBarLayout: View, hasBackButton: Boolean) {
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(hasBackButton)
            it.setDisplayHomeAsUpEnabled(hasBackButton)
            it.setDisplayShowCustomEnabled(hasBackButton)
            it.setDisplayShowTitleEnabled(false)
        }
        if (App.isSME()) {
            appBarLayout.setContextCompatBackgroundColor(R.color.colorWhite)
            toolbar?.context?.setTheme(R.style.ToolbarSME)
        } else {
            removeElevation(appBarLayout)
            appBarLayout.setContextCompatBackgroundColor(R.color.colorTransparent)
        }
    }

    fun removeElevation(appBarLayout: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val stateListAnimator = StateListAnimator()
            stateListAnimator.addState(
                IntArray(0),
                ObjectAnimator.ofFloat(appBarLayout, "elevation", 0f)
            )
            appBarLayout.stateListAnimator = stateListAnimator
        } else {
            ViewCompat.setElevation(appBarLayout, 0f)
        }
    }

    fun addElevation(appBarLayout: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val stateListAnimator = StateListAnimator()
            stateListAnimator.addState(
                IntArray(0),
                ObjectAnimator.ofFloat(appBarLayout, "elevation", 5f)
            )
            appBarLayout.stateListAnimator = stateListAnimator
        } else {
            ViewCompat.setElevation(appBarLayout, 5f)
        }
    }

    fun getStatusBarHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        } else 0
    }

    fun getNavHeight(): Int {
        val resources = resources
        return if (hasNavBar(resources)) {
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                resources.getDimensionPixelSize(resourceId)
            } else 0
        } else 0
    }

    private fun hasNavBar(resources: Resources): Boolean {
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
    }

    fun setToolbarTitle(tvToolbar: TextView?, title: String) {
        tvToolbar?.text = title
    }

    fun setToolbarTitle(
        textViewTitle: TextView?,
        textViewOrganizationName: TextView?,
        title: String,
        orgName: String
    ) {
        textViewTitle?.text = title
        textViewOrganizationName?.text = orgName
    }

    private fun registerReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
            notificationDialogReceiver,
            IntentFilter(Constant.Notification.ACTION_DIALOG)
        )
    }

    private fun unRegisterReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationDialogReceiver)
    }

    private fun doBindService() {
        Timber.d("doBindService")
        startSessionJobService()
    }

    private fun doUnBindService() {
        Timber.d("doUnBindService")
        stopSessionJobService()
    }

    fun getLinearLayoutManager(): LinearLayoutManager {
        return LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
    }

    fun showProgressAlertDialog(tag: String) {
        dialog = ProgressBarDialog.newInstance()
        dialog?.show(supportFragmentManager, tag)
    }

    fun dismissProgressAlertDialog() {
        dialog?.dismiss()
    }

    fun getProgressAlertDialog() = dialog

    fun setDrawableBackButton(resource: Int) {
        val arrowDrawable = ContextCompat.getDrawable(this, resource)
        arrowDrawable?.setColorFilter(
            ContextCompat.getColor(
                this,
                if (App.isSME())
                    R.color.colorInfo
                else R.color.colorWhiteDirty
            ), PorterDuff.Mode.SRC_ATOP
        )
        supportActionBar?.setHomeAsUpIndicator(arrowDrawable)
    }

    fun showLoading(
        viewLoadingState: View,
        swipeRefreshLayout: SwipeRefreshLayout?,
        view: View,
        textView: TextView?,
        headerTableView: View? = null
    ) {
        if (swipeRefreshLayout == null) {
            viewLoadingState.visibility = View.VISIBLE
        }
        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing) {
            viewLoadingState.visibility = View.VISIBLE
            textView?.visibility = View.GONE
            headerTableView?.visibility(false)
        }
        if (viewLoadingState.visibility == View.VISIBLE) {
            swipeRefreshLayout?.isEnabled = false
            view.visibility = View.INVISIBLE
        }
    }

    fun dismissLoading(
        viewLoadingState: View,
        swipeRefreshLayout: SwipeRefreshLayout?,
        view: View,
        enableSRL: Boolean = true
    ) {
        if (swipeRefreshLayout == null) {
            viewLoadingState.visibility = View.GONE
        }
        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing) {
            viewLoadingState.visibility = View.GONE
        } else {
            swipeRefreshLayout?.isRefreshing = false
        }
        swipeRefreshLayout?.isEnabled = enableSRL
        view.visibility = View.VISIBLE
    }

    fun initSetError(
        textInputEditTextObservable: Observable<RxValidationResult<EditText>>
    ) {
        textInputEditTextObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                Timber.d("initSetError: ${it.isProper}")
                viewUtil.setError(it)
            }.addTo(disposables)
    }

    fun initSetCounterFlowError(
        textInputEditTextObservable: Observable<RxValidationResult<EditText>>
    ) {
        textInputEditTextObservable
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe {
                Timber.d("initSetCounterFlowError: ${it.isProper}")
                viewUtil.setCounterFlowError(it)
            }.addTo(disposables)
    }

    fun handleOnError(throwable: Throwable?) {
        Timber.d("throwable?.message: ${throwable?.message}")
        when (throwable) {
            is ApiErrorException -> {
                val error = JsonHelper.fromJson<Error>(throwable.message)
                showMaterialDialogError(title = error.heading, message = error.message)
            }
            is NoConnectivityException,
            is SomethingWentWrongException -> {
                showMaterialDialogError(message = throwable.message)
            }
            is UnderMaintenanceException -> {
                showMaterialDialogError(
                    title = formatString(R.string.error_title_under_maintenance),
                    message = throwable.message
                )
            }
            is SocketTimeoutException -> {
                showMaterialDialogError(message = formatString(R.string.error_request_time_out))
            }
            is SessionExpiredException,
            is InvalidTokenException -> {
                showInvalidTokenDialog(message = throwable.message)
            }
            else -> {
                showMaterialDialogError(message = formatString(R.string.error_something_went_wrong))
            }
        }
        Timber.e(throwable, "handleOnError")
    }

    fun initPushNotificationLandingPage() {
        Timber.d("initPushNotificationLandingPage")
        if (intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA) != null) {
            val pushNotificationPayload = JsonHelper.fromJson<PushNotificationPayload>(
                intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA)
            )
            when (pushNotificationPayload.code) {
                NotificationLogTypeEnum.TRANSACTION_STATUS_CHANGED.name,
                NotificationLogTypeEnum.MOBILE_CHECK_DEPOSIT.name -> {
                    if (settingsUtil.isLoggedIn()) {
                        try {
                            val role = cacheManager.getObject(CacheManager.ROLE) as? Role
                            if (role?.roleId != pushNotificationPayload.roleId) {
                                intent.action = Constant.Notification.ACTION_DIALOG
                                intent.putExtra(
                                    AutobahnFirebaseMessagingService.EXTRA_CODE,
                                    pushNotificationPayload.code
                                )
                                showConfirmationDialog(intent)
                            } else {
                                if (pushNotificationPayload.code == NotificationLogTypeEnum.MOBILE_CHECK_DEPOSIT.name) {
                                    navigateCheckDepositDetails(pushNotificationPayload.id)
                                } else {
                                    navigateApprovalDetails()
                                }
                                Timber.d("pushNotificationPayload: $pushNotificationPayload")
                            }
                        } catch (e: Exception) {
                            showMaterialDialogError(message = formatString(R.string.error_something_went_wrong))
                        }
                    }
                }
                NotificationLogTypeEnum.ANNOUNCEMENT.name,
                NotificationLogTypeEnum.NEW_DEVICE_LOGIN.name,
                NotificationLogTypeEnum.FILE_UPLOADED.name,
                NotificationLogTypeEnum.NEW_FEATURES.name,
                NotificationLogTypeEnum.MAINTENANCE.name,
                NotificationLogTypeEnum.NEW_FEATURES.name,
                NotificationLogTypeEnum.ORGANIZATION_UPDATES.name -> {
                    if (settingsUtil.isLoggedIn()) {
                        eventBus.actionSyncEvent.emmit(
                            BaseEvent(
                                ActionSyncEvent.ACTION_NAVIGATE_ALERTS_TAB,
                                pushNotificationPayload.id.notNullable()
                            )
                        )
                    }
                }
            }
        }
    }

    private fun navigateCheckDepositDetails(id: String?) {
        val bundle = Bundle().apply {
            putString(CheckDepositDetailActivity.EXTRA_ID, id)
        }
        navigator.navigate(
            this,
            CheckDepositDetailActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    private fun navigateApprovalDetails() {
        val bundle = Bundle()
        bundle.putString(
            AutobahnFirebaseMessagingService.EXTRA_DATA,
            intent.getStringExtra(AutobahnFirebaseMessagingService.EXTRA_DATA)
        )
        navigator.navigate(
            this,
            ApprovalDetailActivity::class.java,
            bundle,
            isClear = false,
            isAnimated = true,
            transitionActivity = Navigator.TransitionActivity.TRANSITION_SLIDE_UP
        )
    }

    fun showMaterialDialogError(
        title: String? = formatString(R.string.title_error),
        message: String?
    ) {
        MaterialDialog(this).show {
            lifecycleOwner(this@BaseActivity)
            title(text = title ?: formatString(R.string.title_error))
            message(text = message.notNullable())
            positiveButton(
                res = R.string.action_close,
                click = {
                    it.dismiss()
                }
            )
        }
    }

    private fun showInvalidTokenDialog(message: String?) {
        MaterialDialog(this).show {
            lifecycleOwner(this@BaseActivity)
            title(R.string.title_error)
            cancelOnTouchOutside(false)
            message(text = message.notNullable())
            positiveButton(
                res = R.string.action_close,
                click = {
                    it.dismiss()
                    logoutUser()
                }
            )
        }
    }

    private fun materialDialogLogoutUser(
        activity: AppCompatActivity,
        message: String
    ): MaterialDialog {
        return MaterialDialog(this).apply {
            lifecycleOwner(this@BaseActivity)
            cancelOnTouchOutside(false)
            message(text = message)
            positiveButton(
                res = R.string.action_close,
                click = {
                    it.dismiss()
                    val notificationManager =
                        activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(R.id.notificationLogout)
                    logoutUser()
                }
            )
        }
    }

    private fun startSessionJobService() {
        if (settingsUtil.isLoggedIn()) {
            App.startSessionJobService(this)
            Handler().postDelayed(
                {
                    isDismissedSessionTimeOut = false
                }, 2000
            )
        }
    }

    private fun stopSessionJobService() {
        if (!settingsUtil.isLoggedIn()) {
            App.stopSessionJobService(this)
        }
    }

    private val sessionTimeoutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Constant.ACTION_SESSION_TIMEOUT) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val type = intent.getStringExtra(SessionTimeOutJobService.EXTRA_SESSION_TYPE)
                    if (type == SessionTimeOutJobService.TYPE_SESSION_TIMER) {
                        val timerCountDown = intent.getLongExtra(
                            SessionTimeOutJobService.EXTRA_SESSION_TIMER_COUNTDOWN,
                            0
                        )
                        Timber.d("timerCountDown: $timerCountDown")
                        val timeSessionDialog =
                            resources.getInteger(R.integer.time_session_out_dialog).toLong()
                        if (timerCountDown <= timeSessionDialog &&
                            sessionTimeOutBottomSheet == null &&
                            !isDismissedSessionTimeOut
                        ) {
                            runOnUiThread {
                                showSessionTimeOutBottomSheet(
                                    DIALOG_SESSION_TIME_OUT,
                                    timerCountDown
                                )
                            }
                        }
                        if (sessionTimeOutBottomSheet != null && !isDismissedSessionTimeOut) {
                            sessionTimeOutBottomSheet?.setSessionTimerDesc(timerCountDown)
                        }
                    } else if (type == SessionTimeOutJobService.TYPE_SESSION_TIMEOUT) {
                        Timber.d("timeout")
                        sessionTimeOutBottomSheet?.dismiss()
                        generalViewModel.logout()
                    }
                }
            }
        }
    }

    fun isTableView(): Boolean = sharedPreferenceUtil.isTableView().get()

    companion object {
        const val DIALOG_SESSION_TIME_OUT = "dialog_session_time_out"
    }
}
