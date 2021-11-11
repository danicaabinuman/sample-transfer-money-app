package com.unionbankph.corporate.loan.citizen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentCitizenBinding
import com.unionbankph.corporate.loan.LoanActivity


class CitizenFragment : BaseFragment<FragmentCitizenBinding, CitizenViewModel>(),
        CitizenHandler
{

    private val activity by lazyFast { getAppCompatActivity() as LoanActivity }

    private val controller by lazyFast {
        CitizenController(applicationContext)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCitizenBinding
        get() = FragmentCitizenBinding::inflate

    override val viewModelClassType: Class<CitizenViewModel>
        get() = CitizenViewModel::class.java


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

    override fun onWhere(status: Boolean) {
        when (status) {
            false -> findNavController().navigate(R.id.nav_to_nonFilipinoFragment)
            else -> findNavController().navigate(R.id.nav_to_businessType)
        }
    }

}

