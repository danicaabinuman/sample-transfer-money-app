package com.unionbankph.corporate.corporate.presentation.organization

import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.takusemba.spotlight.Spotlight
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.tutorial.OnTutorialListener
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.auth.data.model.UserDetails
import com.unionbankph.corporate.common.presentation.constant.GravityEnum
import com.unionbankph.corporate.common.presentation.constant.OverlayAnimationEnum
import com.unionbankph.corporate.common.presentation.constant.TutorialScreenEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialError
import com.unionbankph.corporate.common.presentation.viewmodel.ShowTutorialHasTutorial
import com.unionbankph.corporate.common.presentation.viewmodel.TutorialViewModel
import com.unionbankph.corporate.corporate.data.model.CorporateUsers
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_organization.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*

class OrganizationActivity :
    BaseActivity<OrganizationViewModel>(R.layout.activity_organization),
    OrganizationController.AdapterCallbacks,
    OnTutorialListener {

    private var corporateUsers: MutableList<CorporateUsers> = mutableListOf()

    private var tutorialCorporateUsers: MutableList<CorporateUsers> = mutableListOf()

    private var userDetails: UserDetails? = null

    private var tutorialUserDetail: UserDetails? = null

    private var isLoading: Boolean = false

    private lateinit var controller: OrganizationController

    private lateinit var tutorialController: OrganizationController

    private var organizationName: String = ""

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
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
                        startViewTutorial(false)
                    }
                }
                is ShowTutorialError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        tutorialViewModel.hasTutorial(TutorialScreenEnum.PROFILE)
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[OrganizationViewModel::class.java]
        viewModel.state.observe(this, Observer {

            when (it) {
                is ShowOrganizationLoading -> {
                    if (!swipeRefreshLayoutUser.isRefreshing) {
                        isLoading = true
                        updateController()
                    }
                }
                is ShowOrganizationDismissLoading -> {
                    if (!swipeRefreshLayoutUser.isRefreshing) {
                        isLoading = false
                        updateController()
                    } else {
                        swipeRefreshLayoutUser.isRefreshing = false
                    }
                }
                is ShowOrganizationSwitchOrgLoading -> {
                    showProgressAlertDialog(
                        OrganizationActivity::class.java.simpleName
                    )
                }
                is ShowOrganizationSwitchOrgDismissLoading -> {
                    dismissProgressAlertDialog()
                }
                is ShowOrganizationActiveCorporate -> {
                    recyclerViewUser.visibility = View.VISIBLE
                    tutorialUserDetail = it.userDetails
                    userDetails = it.userDetails
                    organizationName = userDetails?.role?.organizationName.notNullable()
                    updateController()
                    setToolbarTitle(
                        textViewTitle,
                        textViewCorporationName,
                        getString(R.string.title_profile),
                        userDetails?.role?.organizationName.notNullable()
                    )
                    if (corporateUsers.isNotEmpty()) {
                        swipeRefreshLayoutUser.isRefreshing = true
                    }
                    viewModel.getCorporateUsers()
                }
                is ShowOrganizationSuccessSwitchOrganization -> {
                    navigator.reNavigateActivity(
                        this,
                        DashboardActivity::class.java,
                        Bundle().apply { putBoolean(DashboardActivity.EXTRA_SWITCH_ORG, true) },
                        isClear = true,
                        isAnimated = true,
                        transitionActivity = Navigator.TransitionActivity.TRANSITION_FLIP
                    )
                }
                is ShowOrganizationError -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        viewModel.getUserDetails()
        tutorialEngineUtil.setOnTutorialListener(this)
        controller = OrganizationController(this, this, viewUtil)
        tutorialController = OrganizationController(this, this, viewUtil)
        recyclerViewUser.setController(controller)
        recyclerViewTutorialUser.setController(tutorialController)
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initDataBus()
        swipeRefreshLayoutUser.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                viewModel.getCorporateUsers()
            }
        }
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
                startViewTutorial(true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
    }

    private fun initDataBus() {
        dataBus.corporateUserDataBus.flowable.subscribe {
            corporateUsers = it.filter { corporateUser ->
                corporateUser.organizationId != userDetails?.role?.organizationId
            }.toMutableList()
            // setupBadge(it)
            updateController()
        }.addTo(disposables)
    }

    override fun onClickItem(id: String) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return
        mLastClickTime = SystemClock.elapsedRealtime()
        viewModel.switchOrganization(id)
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
            clearTutorialData()
        } else {
            if (view == null) {
                if (isClickedHelpTutorial) {
                    isClickedHelpTutorial = false
                    val rect = Rect()
                    val rect2 = Rect()
                    val cardView = recyclerViewTutorialUser
                        .findViewHolderForAdapterPosition(2)?.itemView
                    val cardViewEnd = recyclerViewTutorialUser
                        .findViewHolderForAdapterPosition(2 + tutorialCorporateUsers.size)?.itemView
                    val totalHeight = cardView?.height!! *
                            (tutorialCorporateUsers.size + 1) +
                            resources.getDimension(R.dimen.content_group_spacing).toInt()
                    cardView.getGlobalVisibleRect(rect)
                    cardViewEnd?.getGlobalVisibleRect(rect2)
                    tutorialEngineUtil.startTutorial(
                        this,
                        cardView,
                        totalHeight,
                        Rect(rect.left, rect.top, rect.right, rect2.bottom),
                        R.layout.frame_tutorial_upper_left,
                        0f,
                        false,
                        getString(R.string.msg_tutorial_user_sample),
                        GravityEnum.BOTTOM,
                        OverlayAnimationEnum.ANIM_EXPLODE
                    )
                } else {
                    tutorialViewModel.setTutorial(TutorialScreenEnum.PROFILE, false)
                }
            } else {
                clearTutorialData()
            }
        }
    }

    private fun updateController() {
        userDetails?.role?.organizationName = organizationName
        controller.setData(corporateUsers, userDetails, isLoading)
    }

    private fun updateTutorialController() {
        tutorialUserDetail?.role?.organizationName = TUTORIAL_ORG_NAME
        tutorialController.setData(tutorialCorporateUsers, tutorialUserDetail, false)
    }

    private fun clearTutorialData() {
        tutorialCorporateUsers.clear()
        recyclerViewTutorialUser.visibility = View.GONE
        recyclerViewUser.visibility = View.VISIBLE
        setToolbarTitle(
            textViewTitle,
            textViewCorporationName,
            getString(R.string.title_profile),
            organizationName
        )
    }

    private fun startViewTutorial(isShownTestData: Boolean) {
        if (isShownTestData) {
            setToolbarTitle(TUTORIAL_ORG_NAME, formatString(R.string.title_dashboard_header_user))
            val parseUsers: String = viewUtil.loadJSONFromAsset(this, "users")
            tutorialCorporateUsers = JsonHelper.fromListJson(parseUsers)
            recyclerViewUser.visibility = View.GONE
            updateTutorialController()
            recyclerViewTutorialUser.visibility = View.VISIBLE
        }
        tutorialEngineUtil.startTutorial(
            this,
            R.drawable.ic_tutorial_profile_orange,
            getString(R.string.title_tab_user),
            getString(R.string.msg_tutorial_user)
        )
    }

    private fun setToolbarTitle(orgName: String, toolbar: String) {
        setToolbarTitle(
            textViewTitle,
            textViewCorporationName,
            toolbar,
            orgName
        )
    }

    companion object {
        const val TUTORIAL_ORG_NAME = "ABC Company"
    }
}
