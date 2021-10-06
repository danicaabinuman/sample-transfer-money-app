package com.unionbankph.corporate.loan.applyloan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.showToast
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.databinding.FragmentLoansBinding

class LoansFragment: BaseFragment<FragmentLoansBinding, LoansViewModel>(), LoansAdapterCallback {

    private val controller by lazyFast {
        LoansFragmentController(applicationContext, viewUtil, autoFormatUtil)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentLoansBinding
        get() = FragmentLoansBinding::inflate

    override val viewModelClassType: Class<LoansViewModel>
        get() = LoansViewModel::class.java

    override fun onResume() {
        super.onResume()
        if (hasInitialLoad) {
            hasInitialLoad = false
            initViews()
            initViewModel()
        }
    }

    private fun initViews() {

        val linearLayoutManager = getLinearLayoutManager()
        binding.loansErvApplyLoans.layoutManager = linearLayoutManager
        binding.loansErvApplyLoans.addOnScrollListener(
            object : PaginationScrollListener(linearLayoutManager) {
                override val totalPageCount: Int
                    get() = viewModel.pageable.totalPageCount
                override val isLastPage: Boolean
                    get() = viewModel.pageable.isLastPage
                override val isLoading: Boolean
                    get() = viewModel.pageable.isLoadingPagination
                override val isFailed: Boolean
                    get() = viewModel.pageable.isFailed

                override fun loadMoreItems() {
                    if (!viewModel.pageable.isLoadingPagination) getLoans(false)
                }
            }
        )
        binding.loansErvApplyLoans.setController(controller)
        controller.setLoansHeaderAdapterCallback(this)
    }

    private fun initViewModel() {

        viewModel.loansViewState.observe(this, Observer {
            controller.setData(it, viewModel.pageable)

        })

    }

    private fun getLoans(
        isInitialLoading: Boolean,
        isTapToRetry: Boolean = false
    ) {
       /* if (isTapToRetry) {
            viewModel.pageable.refreshErrorPagination()
        }
        if (isInitialLoading) {
            viewModel.refreshedLoad()
        } else {
            viewModel.resetLoad()
        }
        viewModel.getCorporateUserOrganization(isInitialLoading)*/
    }

    override fun onApplyNow() {
        showToast("Apply Now")
    }

    override fun onReferenceCode() {
        showToast("Reference Code")
    }

    override fun onKeyFeatures(features: String?) {
        showToast("Key Features: $features")
    }

}

