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
import com.unionbankph.corporate.databinding.FragmentNominateEmailTakenBinding

class NominateMergeEmailFragment :
    BaseFragment<FragmentNominateEmailTakenBinding, MigrationViewModel>() {

    private lateinit var migrationMergeActivity: MigrationMergeActivity

    private lateinit var loginMigrationDto: LoginMigrationDto

    override fun onViewsBound() {
        super.onViewsBound()
        migrationMergeActivity = (activity as MigrationMergeActivity)
        loginMigrationDto = migrationMergeActivity.getLoginMigrationMergeInfo()
        initView(loginMigrationDto)
    }

    private fun initView(loginMigrationDto: LoginMigrationDto) {
        binding.textViewEmailTakenDesc.text =
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
        binding.buttonMerge.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return@setOnClickListener
            mLastClickTime = SystemClock.elapsedRealtime()
            migrationMergeActivity.getViewPager().currentItem =
                migrationMergeActivity.getViewPager().currentItem.plus(1)
        }
        binding.buttonNominateDifferentEmail.setOnClickListener {
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

    override val layoutId: Int
        get() = R.layout.fragment_nominate_email_taken

    override val viewModelClassType: Class<MigrationViewModel>
        get() = MigrationViewModel::class.java
}
