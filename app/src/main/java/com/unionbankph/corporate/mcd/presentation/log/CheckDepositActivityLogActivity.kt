package com.unionbankph.corporate.mcd.presentation.log

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.getAccentColor
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.databinding.ActivityCheckDepositLogsBinding
import com.unionbankph.corporate.mcd.data.model.SectionedCheckDepositLogs

class CheckDepositActivityLogActivity :
    BaseActivity<ActivityCheckDepositLogsBinding, CheckDepositActivityLogViewModel>() {

    private val controller by lazyFast { CheckDepositActivityLogController(this, viewUtil) }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setToolbarTitle(binding.viewToolbar.tvToolbar, formatString(R.string.title_activity_log))
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
                is ShowCheckDepositLogLoading -> {
                    showLoading(
                        binding.viewLoadingState.root,
                        binding.swipeRefreshLayoutActivityLogs,
                        binding.recyclerViewActivityLogs,
                        binding.textViewState
                    )
                }
                is ShowCheckDepositLogDismissLoading -> {
                    dismissLoading(
                        binding.viewLoadingState.root,
                        binding.swipeRefreshLayoutActivityLogs,
                        binding.recyclerViewActivityLogs
                    )
                }
                is ShowCheckDepositLogError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.sectionedCheckDepositLogsState.observe(this, Observer {
            updateController(it)
            showEmptyState(it)
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
        viewModel.getCheckDepositActivityLogs(intent.getStringExtra(EXTRA_ID).notNullable())
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

    private fun updateController(data: MutableList<SectionedCheckDepositLogs>) {
        controller.setData(data)
    }

    private fun initRecyclerView() {
        binding.recyclerViewActivityLogs.setController(controller)
    }

    private fun showEmptyState(data: MutableList<SectionedCheckDepositLogs>) {
        if (data.size > 0) {
            if (binding.textViewState.visibility == View.VISIBLE)
                binding.textViewState.visibility = View.GONE
        } else {
            binding.textViewState.visibility = View.VISIBLE
        }
    }

    companion object {
        const val EXTRA_ID = "id"
    }

    override val layoutId: Int
        get() = R.layout.activity_check_deposit_logs

    override val viewModelClassType: Class<CheckDepositActivityLogViewModel>
        get() = CheckDepositActivityLogViewModel::class.java
}
