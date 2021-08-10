package com.unionbankph.corporate.app.dashboard.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.presentation.account_list.AccountFragment
import com.unionbankph.corporate.account.presentation.account_list.AccountViewModel
import com.unionbankph.corporate.account.presentation.account_list.AccountsController
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.runPostDelayed
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.common.data.model.PermissionCollection
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import kotlinx.android.synthetic.main.fragment_dashboard.*
import timber.log.Timber

class DashboardFragment :
    BaseFragment<DashboardFragmentViewModel>(R.layout.fragment_dashboard),
    DashboardAdapterCallback {

    private val controller by lazyFast {
        DashboardController(applicationContext, viewUtil, autoFormatUtil)
    }

    override fun onResume() {
        super.onResume()
        if (hasInitialLoad) {
            hasInitialLoad = false
            initRecyclerView()
//            initListener()
//            initTutorialViewModel()
            initViewModel()
        }
    }



    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[DashboardFragmentViewModel::class.java]

        viewModel.dashboardViewState.observe(this, Observer {


            controller.setData(it, viewModel.pageable)
        })

        setDashboardActionItems()
    }

    private fun setDashboardActionItems() {
        val parseDashboardActions = viewUtil.loadJSONFromAsset(
            getAppCompatActivity(),
            DASHBOARD_ACTIONS
        )
        val dashboardActions = JsonHelper.fromListJson<ActionItem>(parseDashboardActions)
        viewModel.setActionItems(dashboardActions)
    }

    override fun onViewsBound() {
        super.onViewsBound()

        buttonTest.setOnClickListener {
            viewModel.setName()
        }
    }

    private fun initRecyclerView() {
        val linearLayoutManager = GridLayoutManager(context, 2)
        recyclerViewDashboard.layoutManager = linearLayoutManager
//        recyclerViewDashboard.addOnScrollListener(
//            object : PaginationScrollListener(linearLayoutManager) {
//                override val totalPageCount: Int
//                    get() = viewModel.pageable.totalPageCount
//                override val isLastPage: Boolean
//                    get() = viewModel.pageable.isLastPage
//                override val isLoading: Boolean
//                    get() = viewModel.pageable.isLoadingPagination
//                override val isFailed: Boolean
//                    get() = viewModel.pageable.isFailed
//
//                override fun loadMoreItems() {
//                    if (!viewModel.pageable.isLoadingPagination) getAccounts(false)
//                }
//            }
//        )
        recyclerViewDashboard.setController(controller)
        controller.setAdapterCallbacks(this)
    }

    override fun onDashboardActionEmit(id: String) {
        Timber.e("Dashboard Item Clicked " + id)
    }

    override fun onAccountClickItem(account: String, position: Int) {

    }

    override fun onTapErrorRetry(id: String, position: Int) {

    }

    override fun onTapToRetry() {

    }

    companion object {
        const val DASHBOARD_ACTIONS = "dashboard_actions"
    }
}