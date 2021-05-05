package com.unionbankph.corporate.payment_link.presentation.payment_link_list

import android.content.Intent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.PaymentLinkModel
import com.unionbankph.corporate.payment_link.presentation.billing_details.BillingDetailsActivity
import com.unionbankph.corporate.transact.presentation.transact.TransactFragment
import kotlinx.android.synthetic.main.fragment_payment_link_list.*


class PaymentLinkListFragment : BaseFragment<PaymentLinkListViewModel>(R.layout.fragment_payment_link_list){

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[PaymentLinkListViewModel::class.java]
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
        setupInputs()
        setupOutputs()
    }

    private fun initViews(){

        editTextSearch.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                flLoading.visibility = View.VISIBLE
                viewModel.doSearch(editTextSearch.text.toString())
                return@OnEditorActionListener true
            }
            false
        })

    }

    private fun setupInputs(){
        flLoading.visibility = View.VISIBLE
        viewModel.getAllPaymentLinks()
    }
    private fun setupOutputs(){
        viewModel.paymentLinkListPaginatedResponse.observe(this, Observer {
            flLoading.visibility = View.GONE
            updateRecyclerView(it.data)
        })

        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Error -> {
                        flLoading.visibility = View.GONE
                        handleOnError(event.throwable)
                    }
                }
            }
        })
    }

    private fun updateRecyclerView(data: List<PaymentLinkModel>?) {
        val paymentLinksAdapter = PaymentLinkListAdapter(data as MutableList<PaymentLinkModel>)
        rvPaymentLinkList.adapter = paymentLinksAdapter
        paymentLinksAdapter.onItemClick = {
            it.referenceNo?.let { it1 -> showPaymentLinkDetails(it1) }
        }
    }


    private fun showPaymentLinkDetails(referenceNo: String){
        val intent = Intent(activity, BillingDetailsActivity::class.java)
        intent.putExtra(BillingDetailsActivity.EXTRA_REFERENCE_NUMBER,referenceNo)
        startActivity(intent)
    }

}