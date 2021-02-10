package com.unionbankph.corporate.settings.presentation.display

import android.os.SystemClock
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.CARD_VIEW_DISPLAY
import com.unionbankph.corporate.app.common.extension.LIST_VIEW_DISPLAY
import com.unionbankph.corporate.app.common.extension.setVisible
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_settings_display.*

class SettingsDisplayFragment :
    BaseFragment<SettingsDisplayViewModel>(R.layout.fragment_settings_display),
    View.OnClickListener {

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[SettingsDisplayViewModel::class.java]
        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Error -> {
                        handleOnError(event.throwable)
                    }
                }
            }
        })
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        constraintLayoutCardView.setOnClickListener(this)
        constraintLayoutListView.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return
        mLastClickTime = SystemClock.elapsedRealtime()
        viewModel.onClickedDisplay(v.tag.toString())
    }

    private fun initBinding() {
        viewModel.isTableView.subscribe {
            if (it) {
                imageViewListView.setVisible(true)
                imageViewCardView.setVisible(false)
            } else {
                imageViewListView.setVisible(false)
                imageViewCardView.setVisible(true)
            }
        }.addTo(disposables)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initBinding()
        init()
    }

    private fun init() {
        (activity as DashboardActivity).imageViewHelp().visibility = View.GONE
        (activity as DashboardActivity).setToolbarTitle(
            getString(R.string.title_display),
            hasBackButton = true,
            hasMenuItem = false
        )
        constraintLayoutCardView.tag = CARD_VIEW_DISPLAY
        constraintLayoutListView.tag = LIST_VIEW_DISPLAY
    }
}
