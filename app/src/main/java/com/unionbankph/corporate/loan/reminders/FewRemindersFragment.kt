package com.unionbankph.corporate.loan.reminders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.databinding.FragmentFewRemindersBinding
import com.unionbankph.corporate.itemFewReminders

class FewRemindersFragment : BaseFragment<FragmentFewRemindersBinding,
        FewRemindersViewModel>()
{

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFewRemindersBinding
        get() = FragmentFewRemindersBinding::inflate

    override val viewModelClassType: Class<FewRemindersViewModel>
        get() = FewRemindersViewModel::class.java

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initViews()
    }

    private fun initViews() {

        binding.fewFragmentErvData.withModels {
            itemFewReminders {
                id(hashCode())
                onClickListener { model, parentView, clickedView, position ->
                    findNavController().navigate(R.id.nav_to_citizenFragment)
                }
            }
        }
    }

}

