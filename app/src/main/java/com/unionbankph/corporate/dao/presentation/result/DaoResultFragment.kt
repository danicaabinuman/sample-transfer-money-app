package com.unionbankph.corporate.dao.presentation.result

import android.os.Bundle
import android.view.Gravity
import androidx.activity.addCallback
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.toHtmlSpan
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.auth.presentation.login.LoginActivity
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.databinding.FragmentDaoResultBinding

class DaoResultFragment :
    BaseFragment<FragmentDaoResultBinding, DaoResultViewModel>() {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initDaoActivity()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        binding.btnBack.setOnClickListener {
            navigateLogin()
        }
    }

    private fun init() {
        arguments?.getString(EXTRA_TYPE)?.let {
            when (it) {
                TYPE_REACH_OUT_SUCCESS -> {
                    binding.ivDaoLogo.setImageResource(R.drawable.logo_dao_success)
                    binding.tvTitle.text = formatString(R.string.title_congratulations)
                    binding.tvTitle.gravity = Gravity.CENTER
                    binding.tvDesc.gravity = Gravity.CENTER
                    binding.tvDesc.text = formatString(
                        R.string.param_dao_result_desc,
                        arguments?.getString(EXTRA_REFERENCE_NUMBER)
                    ).toHtmlSpan()
                    binding.tvContactInfo.text = formatString(
                        R.string.desc_contact_us,
                        formatString(R.string.unionbank_email)
                    ).toHtmlSpan()
                    binding.btnBack.text = formatString(R.string.action_back)
                }
                TYPE_REACH_OUT_HIT -> {
                    binding.ivDaoLogo.setImageResource(R.drawable.logo_dao_skip_success)
                    binding.tvTitle.text = formatString(R.string.title_dao_result_skip)
                    binding.tvTitle.gravity = Gravity.CENTER
                    binding.tvDesc.gravity = Gravity.START
                    binding.tvDesc.text = formatString(
                        R.string.param_dao_result_skip_desc,
                        arguments?.getString(EXTRA_ORGANIZATION_NAME),
                        arguments?.getString(EXTRA_REFERENCE_NUMBER),
                        arguments?.getString(EXTRA_PREFERRED_BRANCH),
                        arguments?.getString(EXTRA_PREFERRED_BRANCH_EMAIL)
                    ).toHtmlSpan()
                    binding.tvContactInfo.visibility(false)
                    binding.btnBack.text = formatString(R.string.action_back_to_homescreen)
                }
                TYPE_REACH_OUT_COMPLETED -> {
                    binding.ivDaoLogo.setImageResource(R.drawable.logo_dao_verification_completed)
                    binding.tvTitle.text = formatString(R.string.title_verification_completed)
                    binding.tvTitle.gravity = Gravity.CENTER
                    binding.tvDesc.gravity = Gravity.CENTER
                    binding.tvDesc.text = formatString(
                        R.string.param_dao_result_completed,
                        arguments?.getString(EXTRA_ORGANIZATION_NAME),
                        arguments?.getString(EXTRA_REFERENCE_NUMBER)
                    ).toHtmlSpan()
                    binding.tvContactInfo.visibility(false)
                    binding.btnBack.text = formatString(R.string.action_back_to_homescreen)
                }
            }
        }
    }

    private fun initDaoActivity() {
        daoActivity.setProgressValue(8)
        daoActivity.showToolBarDetails()
        daoActivity.clearToolbarDetails()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateLogin()
        }
    }

    private fun navigateLogin() {
        navigator.navigateClearStacks(
            getAppCompatActivity(),
            LoginActivity::class.java,
            Bundle().apply { putBoolean(LoginActivity.EXTRA_SPLASH_SCREEN, false) },
            true
        )
    }

    companion object {
        const val EXTRA_REFERENCE_NUMBER = "referenceNumber"
        const val EXTRA_ORGANIZATION_NAME = "organizationName"
        const val EXTRA_PREFERRED_BRANCH = "preferredBranch"
        const val EXTRA_PREFERRED_BRANCH_EMAIL = "preferredBranchEmail"
        const val EXTRA_TYPE = "type"

        const val TYPE_REACH_OUT_HIT = "reach_out_hit"
        const val TYPE_REACH_OUT_SUCCESS = "reach_out_success"
        const val TYPE_REACH_OUT_COMPLETED = "reach_out_completed"
    }

    override val layoutId: Int
        get() = R.layout.fragment_dao_result

    override val viewModelClassType: Class<DaoResultViewModel>
        get() = DaoResultViewModel::class.java

}
