package com.unionbankph.corporate.approval.presentation.approval_activity_log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.approval.data.model.Transaction
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivityActivityLogBinding
import com.unionbankph.corporate.fund_transfer.data.model.ActivityLogDto


class ActivityLogActivity :
    BaseActivity<ActivityActivityLogBinding, ActivityLogViewModel>(),
    ActivityLogController.AdapterCallbacks {

    private lateinit var controller: ActivityLogController

    private var transaction: Transaction? = null

    private val page by lazyFast { intent.getStringExtra(EXTRA_PAGE) }

    private val id by lazyFast { intent.getStringExtra(EXTRA_ID) }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        if (page == PAGE_APPROVAL_DETAIL) {
            setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_activity_log))
        } else {
            setToolbarTitle(binding.viewToolbar.tvToolbar, getString(R.string.title_edit_log))
        }
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.state.observe(this, Observer {
            when (it) {
                is ShowActivityLogLoading -> {
                    if (!binding.swipeRefreshLayoutActivityLogs.isRefreshing)
                        binding.viewLoadingState.root.visibility = View.VISIBLE
                    if (binding.viewLoadingState.root.visibility == View.VISIBLE)
                        binding.swipeRefreshLayoutActivityLogs.isEnabled = false
                }
                is ShowActivityLogDismissLoading -> {
                    binding.swipeRefreshLayoutActivityLogs.isEnabled = true
                    if (!binding.swipeRefreshLayoutActivityLogs.isRefreshing)
                        binding.viewLoadingState.root.visibility = View.GONE
                    else
                        binding.swipeRefreshLayoutActivityLogs.isRefreshing = false
                }
                is ShowActivityLogSuccess -> {
                    binding.recyclerViewActivityLogs.visibility = View.VISIBLE
                    updateController()
                    showEmptyState()
                }
                is ShowActivityLogError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        fetchActivityLogs()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.swipeRefreshLayoutActivityLogs.apply {
            setColorSchemeResources(getAccentColor())
            setOnRefreshListener {
                fetchActivityLogs()
            }
        }
    }

    private fun fetchActivityLogs() {
        if (intent.getStringExtra(EXTRA_MODEL) != null) {
            transaction = JsonHelper.fromJson(intent.getStringExtra(EXTRA_MODEL))
            transaction?.id?.let {
                when (transaction?.channel) {
                    ChannelBankEnum.BILLS_PAYMENT.value -> {
                        viewModel.getBillsPaymentActivityLogs(it)
                    }
                    ChannelBankEnum.CHECK_WRITER.value -> {
                        viewModel.getCheckWriterActivityLogs(it)
                    }
                    else -> {
                        viewModel.getFundTransferActivityLogs(it)
                    }
                }
            }

        } else {
            when (page) {
                PAGE_BENEFICIARY_DETAIL -> {
                    viewModel.getBeneficiaryDetailActivityLogs(id)
                }
                PAGE_FREQUENT_BILLER_DETAIL -> {
                    viewModel.getFrequentBillerActivityLogs(id)
                }
            }
        }
    }

    override fun onBackPressed() {
        onBackPressed(false)
        overridePendingTransition(R.anim.anim_no_change, R.anim.anim_slide_down)
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

    private fun updateController() {
        controller.setData(viewModel.activityLogsHeader, viewModel.activityLogs)
    }

    override fun onClickItem(activityLog: ActivityLogDto) {
        //onClickItem
    }

    private fun initRecyclerView() {
        controller =
            ActivityLogController(
                this,
                this,
                viewUtil
            )
        binding.recyclerViewActivityLogs.setController(controller)
    }

    private fun showEmptyState() {
        if (viewModel.activityLogs.size > 0) {
            if (binding.textViewState.visibility == View.VISIBLE)
                binding.textViewState.visibility = View.GONE
        } else {
            binding.textViewState.visibility = View.VISIBLE
        }
    }

    companion object {
        const val EXTRA_PAGE = "page"
        const val EXTRA_MODEL = "model"
        const val EXTRA_ID = "id"

        const val PAGE_APPROVAL_DETAIL = "approval_detail"
        const val PAGE_BENEFICIARY_DETAIL = "beneficiary_detail"
        const val PAGE_FREQUENT_BILLER_DETAIL = "frequent_biller_detail"
        const val PAGE_CHECK_WRITER_DETAIL = "check_writer_detail"

    }

    override val viewModelClassType: Class<ActivityLogViewModel>
        get() = ActivityLogViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityActivityLogBinding
        get() = ActivityActivityLogBinding::inflate

}
