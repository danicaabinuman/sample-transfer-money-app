package com.unionbankph.corporate.settings.presentation.display

import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.CARD_VIEW_DISPLAY
import com.unionbankph.corporate.app.common.extension.LIST_VIEW_DISPLAY
import com.unionbankph.corporate.app.common.extension.setVisible
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.FragmentSettingsDisplayBinding
import io.reactivex.rxkotlin.addTo

class SettingsDisplayFragment :
    BaseFragment<FragmentSettingsDisplayBinding, SettingsDisplayViewModel>(),
    View.OnClickListener {

    override fun onViewModelBound() {
        super.onViewModelBound()
        initViewModel()
    }

    private fun initViewModel() {

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
        binding.constraintLayoutCardView.setOnClickListener(this)
        binding.constraintLayoutListView.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return
        mLastClickTime = SystemClock.elapsedRealtime()
        viewModel.onClickedDisplay(v.tag.toString())
    }

    private fun initBinding() {
        viewModel.isTableView.subscribe {
            if (it) {
                binding.imageViewListView.setVisible(true)
                binding.imageViewCardView.setVisible(false)
            } else {
                binding.imageViewListView.setVisible(false)
                binding.imageViewCardView.setVisible(true)
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
        binding.constraintLayoutCardView.tag = CARD_VIEW_DISPLAY
        binding.constraintLayoutListView.tag = LIST_VIEW_DISPLAY
    }

    override val viewModelClassType: Class<SettingsDisplayViewModel>
        get() = SettingsDisplayViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSettingsDisplayBinding
        get() = FragmentSettingsDisplayBinding::inflate
}
