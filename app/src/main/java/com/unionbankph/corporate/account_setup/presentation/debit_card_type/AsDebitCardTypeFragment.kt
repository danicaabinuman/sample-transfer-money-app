package com.unionbankph.corporate.account_setup.presentation.debit_card_type

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.data.DebitCardState
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.account_setup.presentation.AccountSetupViewModel
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme.GenericMenuItem
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.databinding.FragmentAsDebitCardTypeBinding



class AsDebitCardTypeFragment :
    BaseFragment<FragmentAsDebitCardTypeBinding, AccountSetupViewModel>(),
    AsDebitCardTypeController.AsDebitCardTypeCallback {

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }
    private var debitCardState = DebitCardState()

    private val controller by lazyFast {
        AsDebitCardTypeController(applicationContext)
    }
    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            accountSetupActivity.popBackStack()
        }
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initRecyclerView()
        initDefaultState()
    }

    private fun initDefaultState()  {

        val existingSelectedCard = accountSetupActivity.getExistingDebitCardType()
        debitCardState.apply {
            lastCardSelected = existingSelectedCard
            cards = getDefaultCardItems(existingSelectedCard)
        }
        controller.setData(debitCardState)
    }

    private fun getDefaultCardItems(lastSelectedItem: Int?): MutableList<GenericMenuItem> {

        val debitCardItems = mutableListOf(
            GenericMenuItem(
                Constant.DebitCardType.GETGO.toString(),
                requireContext().getString(R.string.title_getgo_debit),
                requireContext().getString(R.string.msg_debit_type),
                null,
                requireContext().getString(R.string.drawable_getgo),
                false,
                true,
                true
            ),
            GenericMenuItem(
                Constant.DebitCardType.PLAYEVERYDAY.toString(),
                requireContext().getString(R.string.title_play_everyday),
                requireContext().getString(R.string.msg_debit_type),
                null,
                requireContext().getString(R.string.drawable_play_everyday),
                false,
                true,
                true
            ))

        if (lastSelectedItem != null) {
            debitCardItems.find { it.id?.toInt() == lastSelectedItem }.apply {
                this?.isSelected = true
            }
        }

        return debitCardItems
    }

    private fun initRecyclerView() {
        val linearLayoutManager = getLinearLayoutManager()
        binding.recyclerViewDebitCardType.layoutManager = linearLayoutManager
        binding.recyclerViewDebitCardType.setController(controller)
        controller.setDebitCardTypeCallback(this)
    }


    override fun onDebitCardType(position: Int, id: Int) {
        accountSetupActivity.setDebitCardType(id)
        debitCardState.apply {
            if(lastCardSelected != null){
                cards[lastCardSelected!!].apply { isSelected = false }
            }
            cards[position].apply { isSelected = true }
            lastCardSelected = position
        }
        controller.setData(debitCardState)
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