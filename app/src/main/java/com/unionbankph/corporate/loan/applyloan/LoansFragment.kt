package com.unionbankph.corporate.loan.applyloan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.AutoModel
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.showToast
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.databinding.FragmentLoansBinding
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

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
            initViews()
            initObservers()
    }

    private fun initObservers() {
        viewModel.apply {

        loansViewState.observe(viewLifecycleOwner, Observer {
            controller.setData(it, viewModel.pageable)
        })

        commonQuestions.observe(viewLifecycleOwner, {
            }
        )

        }
        binding.lifecycleOwner = this
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

        controller.apply {
            commonQuestionListener = {
                showToast("test: $it")
            }
            applyLoansListener = {
                findNavController().navigate(R.id.nav_to_loansCalculatorFragment)
            }
        setLoansHeaderAdapterCallback(this@LoansFragment)
        }
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

    override fun onSeeFullList() {
        showToast("See Full list")
    }

    override fun onReferenceCode() {
        showToast("Reference Code")
    }

    override fun onKeyFeatures(features: String?) {
        showToast("Key Features: $features")
    }

    override fun onCommonQuestions(item: CommonQuestions) {
        showToast("Key Features: $item")
    }

}

