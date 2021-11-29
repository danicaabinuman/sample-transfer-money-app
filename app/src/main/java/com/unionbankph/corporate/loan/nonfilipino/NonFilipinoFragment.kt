package com.unionbankph.corporate.loan.nonfilipino

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentNonFilipinoBinding
import com.unionbankph.corporate.loan.LoanActivity

class NonFilipinoFragment: BaseFragment<FragmentNonFilipinoBinding, NonFilipinoViewModel>(),
    NonFilipinoHandler
{

    private val activity by lazyFast { getAppCompatActivity() as LoanActivity }

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

        activity.apply {
            showProgress(false)
            setToolbarTitle(
                activity.binding.tvToolbar,
                ""
            )
        }

        /*binding.nonFilipinoErvData.withModels {
            nonFilipino {
                id(hashCode())
                onClickListener { model, parentView, clickedView, position ->
                    activity?.finish()
                }
            }
        }*/
    }

    private fun initObservers() {

        binding.handler = this

    }

    override fun gotoDashboard() {
        activity?.finish()
    }

}

