package com.unionbankph.corporate.auth.presentation.migration.nominate_welcome

import android.os.Bundle
import android.os.SystemClock
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.auth.presentation.migration.MigrationMainActivity
import com.unionbankph.corporate.auth.presentation.migration.MigrationViewModel
import kotlinx.android.synthetic.main.fragment_nominate_welcome.*

class NominateWelcomeFragment : BaseFragment<MigrationViewModel>(R.layout.fragment_nominate_welcome) {

    private val migrationMainActivity by lazyFast { (activity as MigrationMainActivity) }

    override fun onViewsBound() {
        super.onViewsBound()
        val type = migrationMainActivity.intent.getStringExtra(MigrationMainActivity.EXTRA_TYPE)
        val fullName = if (type == MigrationMainActivity.TYPE_ECREDITING) {
            val eCredLoginOTPDto = migrationMainActivity.getECredLoginOTPDto()
            "Welcome\n${eCredLoginOTPDto.fullName}!"
        } else {
            val loginMigrationDto = migrationMainActivity.getLoginMigrationInfo()
            "Welcome\n${loginMigrationDto.firstName} ${loginMigrationDto.lastName}!"
        }
        textViewWelcomeTitle.text = fullName
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        buttonContinue.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            mLastClickTime = SystemClock.elapsedRealtime()
            migrationMainActivity.getViewPager().currentItem =
                migrationMainActivity.getViewPager().currentItem.plus(1)
        }
    }

    companion object {
        fun newInstance(): NominateWelcomeFragment {
            val fragment =
                NominateWelcomeFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }
}
