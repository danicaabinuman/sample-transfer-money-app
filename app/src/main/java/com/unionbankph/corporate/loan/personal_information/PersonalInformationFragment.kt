package com.unionbankph.corporate.loan.personal_information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentPersonalInformationBinding
import com.unionbankph.corporate.loan.LoanActivity

class PersonalInformationFragment : BaseFragment<FragmentPersonalInformationBinding, PersonalInformationViewModel>(),
        PersonalInformationHandler
{

    private val activity by lazyFast { getAppCompatActivity() as LoanActivity }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPersonalInformationBinding
        get() = FragmentPersonalInformationBinding::inflate

    override val viewModelClassType: Class<PersonalInformationViewModel>
        get() = PersonalInformationViewModel::class.java


    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initObservers()
    }

    private fun initViews() {
    }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        activity.setToolbarTitle(
            activity.binding.tvToolbar,
            ""
        )
    }

    fun initObservers() {
        viewModel.apply {

        }

        binding.lifecycleOwner = this
        binding.handler = this
        binding.viewModel = viewModel
    }

    override fun onNext() {
    }
}

