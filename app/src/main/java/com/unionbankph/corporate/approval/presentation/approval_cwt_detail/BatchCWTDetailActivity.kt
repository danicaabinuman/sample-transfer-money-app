package com.unionbankph.corporate.approval.presentation.approval_cwt_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.notEmpty
import com.unionbankph.corporate.app.common.extension.notNullable
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.approval.presentation.approval_cwt.BatchCWTViewModel
import com.unionbankph.corporate.approval.presentation.approval_cwt.ShowBatchCWTDetailDismissLoading
import com.unionbankph.corporate.approval.presentation.approval_cwt.ShowBatchCWTDetailLoading
import com.unionbankph.corporate.approval.presentation.approval_cwt.ShowBatchDetailCWTError
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.ActivityBatchCwtDetailBinding
import com.unionbankph.corporate.databinding.ActivityBatchDetailBinding
import com.unionbankph.corporate.fund_transfer.data.model.CWTDetail
import com.unionbankph.corporate.fund_transfer.data.model.CWTItem


class BatchCWTDetailActivity :
    BaseActivity<ActivityBatchCwtDetailBinding, BatchCWTViewModel>() {

    private val type by lazyFast { intent.getStringExtra(EXTRA_TYPE) }

    private val cwtItem by lazyFast { JsonHelper.fromJson<CWTItem>(intent.getStringExtra(EXTRA_DATA)) }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.appBarLayout)
        setToolbarTitle(binding.viewToolbar.tvToolbar, cwtItem.title.notNullable())
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel.batchStateLiveData.observe(this, Observer {
            when (it) {
                is ShowBatchCWTDetailLoading -> {
                    binding.viewLoadingState.root.visibility(true)
                    binding.scrollView.visibility(false)
                }
                is ShowBatchCWTDetailDismissLoading -> {
                    binding.viewLoadingState.root.visibility(false)
                }
                is ShowBatchDetailCWTError -> {
                    handleOnError(it.throwable)
                }
            }
        })
        viewModel.cwtHeaderLiveData.observe(this, Observer {
            it?.let { cwtHeaders ->
                initCWTDetails(cwtHeaders)
            }
        })
        viewModel.getFundTransferCWTHeader(type.notNullable())
    }

    private fun initCWTDetails(cwtHeaders: MutableList<CWTDetail>) {
        binding.scrollView.visibility(true)
        cwtHeaders.forEachIndexed { index, cwtHeader ->
            val view = layoutInflater.inflate(R.layout.item_cwt_detail, null)
            val textViewCWTTitle = view.findViewById<TextView>(R.id.textViewCWTTitle)
            val textViewCWT = view.findViewById<TextView>(R.id.textViewCWT)
            val viewBorderTop = view.findViewById<View>(R.id.viewBorderTop)
            val cwtItemFindValue = cwtItem.cwtBody.find { it.key == cwtHeader.key }
            viewBorderTop.visibility(index != 0)
            textViewCWTTitle.text = cwtHeader.display
            textViewCWT.text = cwtItemFindValue?.display.notEmpty()
            binding.linearLayoutCWT.addView(view)
        }
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


    companion object {
        const val EXTRA_DATA = "data"
        const val EXTRA_TYPE = "type"
    }

    override val viewModelClassType: Class<BatchCWTViewModel>
        get() = BatchCWTViewModel::class.java

    override val bindingInflater: (LayoutInflater) -> ActivityBatchCwtDetailBinding
        get() = ActivityBatchCwtDetailBinding::inflate

}
