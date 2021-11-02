package com.unionbankph.corporate.account_setup.presentation.citizenship

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.addCallback
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.databinding.FragmentAsCitizenshipBinding
import io.reactivex.rxkotlin.addTo
import timber.log.Timber


class AsCitizenshipFragment :
    BaseFragment<FragmentAsCitizenshipBinding, AsCitizenshipViewModel>() {

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        accountSetupActivity.apply {
            showProgress(true)
            setProgressValue(5)
            showToolbarButton(false)
            setBackButtonIcon(AccountSetupActivity.BACK_ARROW_ICON)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            accountSetupActivity.popBackStack()
        }
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.state.observe(viewLifecycleOwner, EventObserver {
            accountSetupActivity.viewModel.setCitizenship(it)
            findNavController().navigate( when (it) {
                Constant.Citizenship.FILIPINO -> R.id.action_sole_prop_personal_info
                else -> R.id.action_non_filipino
            })
        })

        val selectedCitizenship = accountSetupActivity.viewModel.state.value?.citizenship
        selectedCitizenship?.let {
            viewModel.citizenshipInput.onNext(it)
        }
    }

    override fun onViewsBound() {
        super.onViewsBound()

        initViewBindings()
    }

    private fun initViewBindings() {
        viewModel.citizenshipInput.subscribe {
            setSelectedButton(it)
        }.addTo(disposables)
    }

    private fun setSelectedButton(selectedCitizenship: String) {
        Timber.e("selected $selectedCitizenship")
        binding.apply {
            btnAsCitizenshipYes.isChecked = selectedCitizenship.equals(Constant.Citizenship.FILIPINO, true)
            btnAsCitizenshipNo.isChecked = selectedCitizenship.equals(Constant.Citizenship.NON_FILIPINO, true)
        }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.btnAsCitizenshipYes.setOnClickListener { viewModel.isFilipino() }
        binding.btnAsCitizenshipNo.setOnClickListener { viewModel.isNonFilipino() }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAsCitizenshipBinding
        get() = FragmentAsCitizenshipBinding::inflate

    override val viewModelClassType: Class<AsCitizenshipViewModel>
        get() = AsCitizenshipViewModel::class.java
}