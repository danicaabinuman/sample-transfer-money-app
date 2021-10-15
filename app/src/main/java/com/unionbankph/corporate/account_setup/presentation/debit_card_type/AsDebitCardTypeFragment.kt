package com.unionbankph.corporate.account_setup.presentation.debit_card_type

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.account_setup.presentation.AccountSetupViewModel
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentAsDebitCardTypeBinding



class AsDebitCardTypeFragment :
    BaseFragment<FragmentAsDebitCardTypeBinding, AccountSetupViewModel>(),
    AsDebitCardTypeController.AsDebitCardTypeCallback {

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    private val controller by lazyFast {
        AsDebitCardTypeController(applicationContext)
    }
    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        accountSetupActivity.setIsScreenScrollable(false)
        accountSetupActivity.setToolbarButtonType(AccountSetupActivity.BUTTON_CLOSE)
        accountSetupActivity.showToolbarButton(true)
        accountSetupActivity.showProgress(true)
        accountSetupActivity.setProgressValue(2)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            accountSetupActivity.popBackStack()
        }
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
        initViewModel()
    }


    private fun initViewModel() {
        viewModel.debitCardState.observe(this, Observer {
            controller.setData(it)
        })

    }

    private fun initRecyclerView() {
        val linearLayoutManager = getLinearLayoutManager()
        binding.recyclerViewDebitCardType.layoutManager = linearLayoutManager
        binding.recyclerViewDebitCardType.setController(controller)
        controller.setDebitCardTypeCallback(this)
    }


    override fun onDebitCardType(id: Int) {
        accountSetupActivity.setDebitCardType(id)
        navigateNextScreen()
    }

    private fun navigateNextScreen(){
        findNavController().navigate(R.id.action_card_type_to_business_account_type)
    }


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAsDebitCardTypeBinding
        get() = FragmentAsDebitCardTypeBinding::inflate
    override val viewModelClassType: Class<AccountSetupViewModel>
        get() = AccountSetupViewModel::class.java




}