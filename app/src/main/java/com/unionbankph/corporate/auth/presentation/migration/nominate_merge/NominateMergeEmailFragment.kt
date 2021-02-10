package com.unionbankph.corporate.auth.presentation.migration.nominate_merge

import android.os.Bundle
import android.os.SystemClock
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.toHtmlSpan
import com.unionbankph.corporate.auth.data.model.LoginMigrationDto
import com.unionbankph.corporate.auth.presentation.migration.MigrationViewModel
import com.unionbankph.corporate.auth.presentation.migration.migration_merge.MigrationMergeActivity
import kotlinx.android.synthetic.main.fragment_nominate_email_taken.*

class NominateMergeEmailFragment : BaseFragment<MigrationViewModel>(R.layout.fragment_nominate_email_taken) {

    private lateinit var migrationMergeActivity: MigrationMergeActivity

    private lateinit var loginMigrationDto: LoginMigrationDto

    override fun onViewsBound() {
        super.onViewsBound()
        migrationMergeActivity = (activity as MigrationMergeActivity)
        loginMigrationDto = migrationMergeActivity.getLoginMigrationMergeInfo()
        initView(loginMigrationDto)
    }

    private fun initView(loginMigrationDto: LoginMigrationDto) {
        textViewEmailTakenDesc.text =
            if (migrationMergeActivity.getType() == MigrationMergeActivity.TYPE_ECREDITING) {
                formatString(
                    R.string.param_desc_ecred_email_address_taken,
                    loginMigrationDto.emailAddress,
                    loginMigrationDto.userId
                ).toHtmlSpan()
            } else {
                formatString(
                    R.string.param_desc_email_address_taken,
                    loginMigrationDto.emailAddress,
                    loginMigrationDto.corpId,
                    loginMigrationDto.userId
                ).toHtmlSpan()
            }
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        buttonMerge.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            mLastClickTime = SystemClock.elapsedRealtime()
            migrationMergeActivity.getViewPager().currentItem =
                migrationMergeActivity.getViewPager().currentItem.plus(1)
        }
        buttonNominateDifferentEmail.setOnClickListener {
            migrationMergeActivity.onBackPressed()
        }
    }

    companion object {
        fun newInstance(): NominateMergeEmailFragment {
            val fragment =
                NominateMergeEmailFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }
}
