package com.unionbankph.corporate.open_account.presentation.reminder

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding2.view.RxView
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.open_account.presentation.OpenAccountActivity
import com.unionbankph.corporate.open_account.presentation.OpenAccountViewModel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_oa_reminder.*
import java.util.concurrent.TimeUnit

class OAReminderFragment :
    BaseFragment<OpenAccountViewModel>(R.layout.fragment_oa_reminder) {

    private val openAccountActivity by lazyFast { getAppCompatActivity() as OpenAccountActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)

        openAccountActivity.setIsScreenScrollable(false)
    }

    override fun onViewsBound() {
        super.onViewsBound()

        RxView.clicks(btn_oa_reminder_next)
            .throttleFirst(
                resources.getInteger(R.integer.time_button_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribe {
                findNavController().navigate(R.id.action_reminder_to_enter_name)
            }.addTo(disposables)
    }
}