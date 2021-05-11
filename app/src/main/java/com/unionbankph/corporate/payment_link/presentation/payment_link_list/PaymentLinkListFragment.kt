package com.unionbankph.corporate.payment_link.presentation.payment_link_list

import android.content.Intent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.domain.model.PaymentLinkModel
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkTransactionData
import com.unionbankph.corporate.payment_link.presentation.payment_link_details.LinkDetailsActivity
import kotlinx.android.synthetic.main.activity_payment_link.*
import kotlinx.android.synthetic.main.activity_payment_link.editTextSearch
import kotlinx.android.synthetic.main.activity_payment_link.flLoading
import kotlinx.android.synthetic.main.activity_payment_link.rvPaymentLinkList
import kotlinx.android.synthetic.main.fragment_payment_link_list.*
import kotlinx.android.synthetic.main.fragment_send_request.*


class PaymentLinkListFragment : BaseFragment<PaymentLinkListViewModel>(R.layout.fragment_payment_link_list){

    lateinit var mAdapter : PaymentLinkListAdapter
    var mDisableLazyLoading = false
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

                if(editTextSearch.text!=null){
                    val searchString = editTextSearch.text!!.toString()
                    if(searchString.isNotEmpty()){
                        viewModel.doSearch(searchString)
                    }else{
                        viewModel.getAllPaymentLinks()
                    }
                }else{
                    viewModel.getAllPaymentLinks()
                }

                return@OnEditorActionListener true
            }
            false
        })

        rvPaymentLinkList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    if(!mDisableLazyLoading){
                        pbPaymentLinkListLoading.visibility = View.VISIBLE
                        rvPaymentLinkList.scrollToPosition((rvPaymentLinkList.adapter?.itemCount ?: 1) - 1);
                        viewModel.getAllNextPaymentLinks()
                    }
                }
            }
        })


    }

    override fun onResume() {
        super.onResume()

        mDisableLazyLoading = false
        mAdapter = PaymentLinkListAdapter(applicationContext)
        rvPaymentLinkList.adapter = mAdapter
        mAdapter.onItemClick = {
            flLoading.visibility = View.VISIBLE
            it.referenceNo?.let { it1 -> viewModel.getPaymentLinkDetails(it1) }
        }

        flLoading.visibility = View.VISIBLE
        viewModel.getAllPaymentLinks()
    }
    private fun setupOutputs(){
        viewModel.paymentLinkListPaginatedResponse.observe(this, Observer {
            flLoading.visibility = View.GONE
            if(it.data?.size!! > 0){
                mDisableLazyLoading = false
                mAdapter.clearData()
                mAdapter.appendData(it.data!!)
            }else{
                mDisableLazyLoading = true
            }

        })

        viewModel.nextPaymentLinkListPaginatedResponse.observe(this, Observer {
            pbPaymentLinkListLoading.visibility = View.GONE
            if(it.data?.size!! > 0){
                mDisableLazyLoading = false
                mAdapter.appendData(it.data!!)
            }else{
                mDisableLazyLoading = true
            }
        })

        viewModel.searchPaymentLinkListPaginatedResponse.observe(this, Observer {
            flLoading.visibility = View.GONE
            mAdapter.clearData()
            mAdapter.appendData(it.data!!)
        })

        viewModel.paymentLinkDetailsResponse.observe(this, Observer {
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
                        pbPaymentLinkListLoading.visibility = View.GONE
                        handleOnError(event.throwable)
                    }
                }
            }
        })
    }


    private fun showPaymentLinkDetails(generatePaymentLinkResponse: GeneratePaymentLinkResponse){
        val intent = Intent(applicationContext, LinkDetailsActivity::class.java)
        val responseJson = JsonHelper.toJson(generatePaymentLinkResponse)
        intent.putExtra(LinkDetailsActivity.EXTRA_GENERATE_PAYMENT_LINK_RESPONSE, responseJson)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}