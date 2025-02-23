package com.unionbankph.corporate.loan.reminders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentFewRemindersBinding
import com.unionbankph.corporate.loan.LoanActivity

class FewRemindersFragment : BaseFragment<FragmentFewRemindersBinding,
        FewRemindersViewModel>(), FewRemindersHandler
{

    private val activity by lazyFast { getAppCompatActivity() as LoanActivity }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFewRemindersBinding
        get() = FragmentFewRemindersBinding::inflate

    override val viewModelClassType: Class<FewRemindersViewModel>
        get() = FewRemindersViewModel::class.java

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        activity.apply {
            showProgress(false)
            setToolbarTitle(
                activity.binding.tvToolbar,
                ""
            )
        }
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initObservers()
    }

    private fun initObservers() {
        viewModel.apply {

        }
        binding.lifecycleOwner = this
        binding.handler = this
    }

    private fun initViews() {}

    override fun onNext() {

        findNavController().navigate(R.id.nav_to_citizenFragment)
    }

}

