package com.unionbankph.corporate.settings.presentation.selector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.common.data.model.StateData
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivitySelectorBinding
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.form.SelectorData
import io.reactivex.rxkotlin.addTo

/**
 * Created by herald25santos on 2020-02-18
 */
class SelectorActivity :
    BaseActivity<ActivitySelectorBinding, SelectorViewModel>(),
    EpoxyAdapterCallback<Selector> {

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolBar.toolbar, binding.viewToolBar.appBarLayout)
        setDrawableBackButton(R.drawable.ic_close_white_24dp)
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initBinding()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.uiState.observe(this, androidx.lifecycle.Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Loading -> {
                        showLoading(
                            binding.viewLoadingState.root,
                            binding.srlSelector,
                            binding.rvSelector,
                            binding.textViewState
                        )
                    }
                    is UiState.Complete -> {
                        dismissLoading(
                            binding.viewLoadingState.root,
                            binding.srlSelector,
                            binding.rvSelector,
                            false
                        )
                    }
                    is UiState.Exit -> {
                        onBackPressed()
                    }
                }
            }
        })
        viewModel.items.observe(this, Observer {
            updateController(it)
            initView(it)
        })
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.buttonSelect.setOnClickListener {
            viewModel.onClickedSelect()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
        val menuEdit = menu.findItem(R.id.menu_edit)
        menuEdit.isVisible = true
        val menuEditView = menuEdit.actionView
        val textViewMenuTitle = menuEditView.findViewById<TextView>(R.id.textViewMenuTitle)
        textViewMenuTitle.text = formatString(
            if (viewModel.isAllSelected()) {
                R.string.action_deselect_all
            } else {
                R.string.action_select_all
            }
        )
        textViewMenuTitle.setOnClickListener {
            onOptionsItemSelected(menuEdit)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_edit -> {
                if (viewModel.isAllSelected()) {
                    viewModel.deSelectAll()
                } else {
                    viewModel.selectAll()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initBinding() {
        viewModel.selector.onNext(intent.getStringExtra(EXTRA_SELECTOR).notNullable())
        intent.getParcelableExtra<SelectorData>(EXTRA_ITEMS)?.let { viewModel.loadData(it) }
        viewModel.selectedCount
            .observeOn(schedulerProvider.ui())
            .subscribe {
                updateButton(it)
            }.addTo(disposables)
        viewModel.selector
            .observeOn(schedulerProvider.ui())
            .subscribe {
                setToolbarTitle(binding.viewToolBar.tvToolbar, it.capitalize())
            }.addTo(disposables)
    }

    private fun updateController(data: MutableList<StateData<Selector>>) {
        binding.rvSelector.withModels {
            data.forEachIndexed { index, stateData ->
                selectorItem {
                    id(stateData.data.id)
                    item(stateData.data)
                    hasSelected(stateData.state)
                    callbacks(this@SelectorActivity)
                    position(index)
                }
            }
        }
    }

    private fun initView(data: MutableList<StateData<Selector>>) {
        binding.buttonSelect.visibility(data.isNotEmpty())
    }

    private fun updateButton(count: Int) {
        if (count > 0) {
            binding.buttonSelect.text = if (viewModel.selector.value == CHANNEL_SELECTOR) {
                formatString(R.string.params_select_channels, count.toString())
            } else {
                formatString(R.string.params_select_statuses, count.toString())
            }
        } else {
            binding.buttonSelect.text = formatString(R.string.action_clear_selection)
        }
        invalidateOptionsMenu()
    }

    override fun onClickItem(view: View, data: Selector, position: Int) {
        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        viewModel.onClickedItem(position, checkBox.isChecked)
    }

    companion object {
        const val EXTRA_ITEMS = "items"
        const val EXTRA_SELECTOR = "selector"
        const val CHANNEL_SELECTOR = "channel"
        const val STATUS_SELECTOR = "status"
    }

    override val viewModelClassType: Class<SelectorViewModel>
        get() = SelectorViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivitySelectorBinding
        get() = ActivitySelectorBinding::inflate
}
