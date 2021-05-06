package com.unionbankph.corporate.payment_link.presentation.payment_link_list

import android.content.Intent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.PaymentLinkModel
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkTransactionData
import com.unionbankph.corporate.payment_link.presentation.payment_link_details.LinkDetailsActivity
import kotlinx.android.synthetic.main.activity_payment_link.*
import kotlinx.android.synthetic.main.fragment_send_request.*


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
        setupOutputs()
    }

    private fun initViews(){

        editTextSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
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

    override fun onResume() {
        super.onResume()
        setupInputs()
    }
    private fun setupOutputs(){
        viewModel.paymentLinkListPaginatedResponse.observe(this, Observer {
            flLoading.visibility = View.GONE
            updateRecyclerView(it.data)
        })

        viewModel.paymentLinkDetailsResponse.observe(this, Observer {
            flLoading.visibility = View.GONE
            showPaymentLinkDetails(
                    GeneratePaymentLinkResponse(
                            it.paymentDetails?.paymentLink,
                            it.referenceNo,
                            it.paymentDetails?.amount.toString(),
                            it.paymentDetails?.description.toString(),
                            it.paymentDetails?.status.toString(),
                            it.paymentDetails?.paymentFor.toString(),
                            it.expiry,
                            it.payorDetails?.mobileNumber.toString(),
                            GeneratePaymentLinkTransactionData(
                                    it.transactionId,
                                    it.createdDate,
                                    it.createdDate,
                                    it.createdDate,
                                    it.createdDate

                            )
                    )
            )
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
        val paymentLinksAdapter = PaymentLinkListAdapter(applicationContext, data as MutableList<PaymentLinkModel>)
        rvPaymentLinkList.adapter = paymentLinksAdapter
        paymentLinksAdapter.onItemClick = {
            flLoading.visibility = View.VISIBLE
            it.referenceNo?.let { it1 -> viewModel.getPaymentLinkDetails(it1) }
        }
    }


    private fun showPaymentLinkDetails(generatePaymentLinkResponse: GeneratePaymentLinkResponse){
        val intent = Intent(applicationContext, LinkDetailsActivity::class.java)
        val responseJson = JsonHelper.toJson(generatePaymentLinkResponse)
        intent.putExtra(LinkDetailsActivity.EXTRA_GENERATE_PAYMENT_LINK_RESPONSE,responseJson)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}