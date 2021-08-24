package com.unionbankph.corporate.open_account.presentation.personalise_settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_oa_personalise_settings.*
import java.util.concurrent.TimeUnit


class OAPersonaliseSettingsFragment : BaseFragment<OAPersonaliseSettingsViewModel>(R.layout.fragment_oa_personalise_settings)  {
    private val formDisposable = CompositeDisposable()

    private val openAccountActivity by lazyFast { getAppCompatActivity() as OpenAccountActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
        openAccountActivity.setIsScreenScrollable(false)
    }

    override fun onViewsBound() {
        super.onViewsBound()

        RxView.clicks(buttonNext)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                findNavController().navigate(R.id.action_permission_settings_to_confirmation_message)
            }.addTo(formDisposable)
    }


}