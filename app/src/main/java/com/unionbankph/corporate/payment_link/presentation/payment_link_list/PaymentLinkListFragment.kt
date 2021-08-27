package com.unionbankph.corporate.payment_link.presentation.payment_link_list

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.dashboard.*
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.databinding.FragmentPaymentLinkListBinding
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkResponse
import com.unionbankph.corporate.payment_link.domain.model.response.GeneratePaymentLinkTransactionData
import com.unionbankph.corporate.payment_link.presentation.onboarding.RequestPaymentSplashActivity
import com.unionbankph.corporate.payment_link.presentation.payment_link_details.LinkDetailsActivity


class PaymentLinkListFragment :
    BaseFragment<FragmentPaymentLinkListBinding, PaymentLinkListViewModel>(){

    lateinit var mAdapter : PaymentLinkListAdapter
    var mDisableLazyLoading = false
    override fun onViewModelBound() {
        super.onViewModelBound()
    }

    override fun onViewsBound() {
        super.onViewsBound()

        initViews()
        setupOutputs()
    }

    override fun onResume() {
        super.onResume()
        mDisableLazyLoading = false
        viewModel.getAllPaymentLinks()
    }

    private fun initViews(){

        binding.editTextSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.flLoading.visibility = View.VISIBLE

                if(binding.editTextSearch.text!=null){
                    val searchString = binding.editTextSearch.text!!.toString()
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

        binding.rvPaymentLinkList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    if(!mDisableLazyLoading){
                        binding.rvPaymentLinkList.scrollToPosition((binding.rvPaymentLinkList.adapter?.itemCount ?: 1) - 1);
                        viewModel.getAllNextPaymentLinks()
                    }
                }
            }
        })

        mAdapter = PaymentLinkListAdapter(applicationContext)
        binding.rvPaymentLinkList.adapter = mAdapter
        mAdapter.onItemClick = {
            binding.flLoading.visibility = View.VISIBLE
            it.referenceNo?.let { it1 -> viewModel.getPaymentLinkDetails(it1) }
        }

    }


    private fun setupOutputs(){
        viewModel.paymentLinkListPaginatedResponse.observe(this, Observer {
            if(it.data?.size!! > 0){
                mDisableLazyLoading = false
                mAdapter.clearData()
                mAdapter.appendData(it.data!!)
            }else{
                mDisableLazyLoading = true
            }

        })

        viewModel.nextPaymentLinkListPaginatedResponse.observe(this, Observer {
            if(it.data?.size!! > 0){
                mDisableLazyLoading = false
                mAdapter.appendData(it.data!!)
            }else{
                mDisableLazyLoading = true
            }
        })

        viewModel.searchPaymentLinkListPaginatedResponse.observe(this, Observer {
            mDisableLazyLoading = true
            mAdapter.clearData()
            mAdapter.appendData(it.data!!)
        })


        viewModel.paymentLinkListState.observe(this, Observer {
            when (it) {
                is ShouldShowProgressLoading -> {
                    binding.flLoading.visibility = if (it.shouldShow)  View.VISIBLE else View.GONE
                }
                is ShouldShowNoAvailablePaymentLinks -> {
                    binding.flNoAvailablePaymenLinks.visibility = if (it.shouldShow)  View.VISIBLE else View.GONE
                }
                is ShouldShowLazyLoading -> {
                    binding.pbPaymentLinkListLoading.visibility = if (it.shouldShow)  View.VISIBLE else View.GONE
                }
                is ShouldShowRecyclerView -> {
                    binding.rvPaymentLinkList.visibility = if (it.shouldShow)  View.VISIBLE else View.GONE
                }

                is NoMoreAvailablePaymentLinks -> {
                    mDisableLazyLoading = true
                }

                is Error -> {
                    mDisableLazyLoading = true
                    handleOnError(it.throwable)
                }
            }
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
    }


    private fun showPaymentLinkDetails(generatePaymentLinkResponse: GeneratePaymentLinkResponse){
        val intent = Intent(applicationContext, LinkDetailsActivity::class.java)
        val responseJson = JsonHelper.toJson(generatePaymentLinkResponse)
        intent.putExtra(LinkDetailsActivity.EXTRA_GENERATE_PAYMENT_LINK_RESPONSE, responseJson)
        intent.putExtra(RequestPaymentSplashActivity.EXTRA_FROM_WHAT_TAB, DashboardViewModel.FROM_TRANSACT_TAB)
        startActivity(intent)
    }

    override val viewModelClassType: Class<PaymentLinkListViewModel>
        get() = PaymentLinkListViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPaymentLinkListBinding
        get() = FragmentPaymentLinkListBinding::inflate

}