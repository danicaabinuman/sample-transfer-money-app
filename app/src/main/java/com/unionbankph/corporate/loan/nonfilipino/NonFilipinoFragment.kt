package com.unionbankph.corporate.loan.nonfilipino

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.showToast
import com.unionbankph.corporate.databinding.FragmentCitizenBinding
import com.unionbankph.corporate.databinding.FragmentNonFilipinoBinding
import com.unionbankph.corporate.itemCitizen
import com.unionbankph.corporate.loan.citizen.CitizenHandler
import com.unionbankph.corporate.loan.citizen.CitizenViewModel
import com.unionbankph.corporate.nonFilipino

class NonFilipinoFragment: BaseFragment<FragmentNonFilipinoBinding, NonFilipinoViewModel>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentNonFilipinoBinding
        get() = FragmentNonFilipinoBinding::inflate

    override val viewModelClassType: Class<NonFilipinoViewModel>
        get() = NonFilipinoViewModel::class.java


    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initViews()
        initObservers()
    }

    private fun initViews() {

        binding.nonFilipinoErvData.withModels {
            nonFilipino {
                id(hashCode())
                onClickListener { model, parentView, clickedView, position ->
                    activity?.finish()
                }
            }
        }
    }

    private fun initObservers() {}

}

