package com.unionbankph.corporate.loan.citizen

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentCitizenBinding
import com.unionbankph.corporate.itemCitizen



class CitizenFragment :
    BaseFragment<FragmentCitizenBinding, CitizenViewModel>()  {

    private val controller by lazyFast {
        CitizenController(applicationContext)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCitizenBinding
        get() = FragmentCitizenBinding::inflate

    override val viewModelClassType: Class<CitizenViewModel>
        get() = CitizenViewModel::class.java


    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initViews()
        initObservers()
    }

    private fun initViews() {

        // for controller setup of epoxy recyclerview
        /*binding.citizenErvData.setController(controller)
        controller.whereClickListener = {
            Log.d("Test", "Test $it")
        }*/

        binding.citizenErvData.withModels {
            itemCitizen {
                id(hashCode())
                handler(
                    object : CitizenHandler {
                        override fun onWhere(status: Boolean) {
                            when (status) {
                                false -> findNavController().navigate(R.id.nav_to_nonFilipinoFragment)
                                else -> findNavController().navigate(R.id.nav_to_businessType)
                            }
                        }
                    }
                )
            }
        }
    }

    private fun initObservers() {}

}

