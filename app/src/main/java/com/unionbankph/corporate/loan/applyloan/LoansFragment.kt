package com.unionbankph.corporate.loan.applyloan
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.AutoModel
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.observe
import com.unionbankph.corporate.app.common.extension.showToast
import com.unionbankph.corporate.app.common.widget.recyclerview.PaginationScrollListener
import com.unionbankph.corporate.databinding.FragmentLoansBinding
import com.unionbankph.corporate.loan.LoanActivity
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class LoansFragment: BaseFragment<FragmentLoansBinding, LoansViewModel>(), LoansAdapterCallback {

    private val activity by lazyFast { getAppCompatActivity() as LoanActivity }

    private val controller by lazyFast {
        LoansFragmentController(applicationContext)
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

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        activity.setToolbarTitle(
            activity.binding.tvToolbar,
            getString(R.string.title_loans)
        )
    }

    private fun initObservers() {
        viewModel.apply {

            observe(loansViewState) {
                controller.setData(it)
            }
        }
    }

    private fun initViews() {

        binding.loansErvApplyLoans.setController(controller)

        controller.apply {
            commonQuestionListener = {
                showToast("test: $it")
            }
            applyLoansListener = {
                findNavController().navigate(R.id.nav_to_loansCalculatorFragment)
            }
            readyToBusiness = {
                findNavController().navigate(R.id.nav_to_loansCalculatorFragment)
            }
        setLoansHeaderAdapterCallback(this@LoansFragment)
        }
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

    override fun onCommonQuestionsEligible(expand: Boolean?) {
        expand?.let {
            viewModel.setCommonQuestionEligible(it)
        }
    }

    override fun onCommonQuestionRequirements(expand: Boolean?) {
        expand?.let {
            viewModel.setCommonQuestionRequirements(it)
        }    }

    override fun oncCommonQuestionBusiness(expand: Boolean?) {
        expand?.let {
            viewModel.setCommonQuestionBusiness(it)
        }    }

}

