package com.unionbankph.corporate.account_setup.presentation.account_purpose

import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding2.widget.RxSeekBar
import com.jakewharton.rxbinding2.widget.SeekBarProgressChangeEvent
import com.unionbankph.corporate.account_setup.presentation.AccountSetupActivity
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.databinding.FragmentAccountPurposeBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import timber.log.Timber

class AccountPurposeFragment :
    BaseFragment<FragmentAccountPurposeBinding, AccountPurposeViewModel>(), AccountPurposeHandler {

    private val accountSetupActivity by lazyFast { getAppCompatActivity() as AccountSetupActivity }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAccountPurposeBinding
        get() = FragmentAccountPurposeBinding::inflate
    override val viewModelClassType: Class<AccountPurposeViewModel>
        get() = AccountPurposeViewModel::class.java

    override fun onNext() {

    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        initObservers()
    }


    private fun initViews() {
        RxSeekBar.changeEvents(binding.accountPurposeWasMonthlyTransactions.seekBar)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when (it) {
                    is SeekBarProgressChangeEvent -> {

                    }
                }
            }) { Timber.e(it) }
            .addTo(disposables)
    }

    private fun initObservers() {
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }
}