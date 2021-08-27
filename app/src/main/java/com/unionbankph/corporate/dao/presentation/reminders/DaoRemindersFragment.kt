package com.unionbankph.corporate.dao.presentation.reminders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.toHtmlSpan
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.databinding.FragmentDaoRemindersBinding

class DaoRemindersFragment :
    BaseFragment<FragmentDaoRemindersBinding, DaoRemindersViewModel>(),
    DaoActivity.ActionEvent {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    private fun init() {
        binding.tvReminder1Sub1.text = formatString(R.string.desc_dao_reminder_1_sub_1).toHtmlSpan()
        binding.tvReminder1Sub2.text = formatString(R.string.desc_dao_reminder_1_sub_2).toHtmlSpan()
        binding.tvReminder3Sub.text = formatString(R.string.desc_dao_reminder_3_sub).toHtmlSpan()
        daoActivity.showToolBarDetails()
        daoActivity.setToolBarDesc(formatString(R.string.title_reminders))
        daoActivity.showButton(true)
        daoActivity.showProgress(true)
        daoActivity.setProgressValue(4)
        daoActivity.setActionEvent(this)
    }

    override fun onClickNext() {
        findNavController().navigate(R.id.action_company_information_step_one)
    }

    override val viewModelClassType: Class<DaoRemindersViewModel>
        get() = DaoRemindersViewModel::class.java

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDaoRemindersBinding
        get() = FragmentDaoRemindersBinding::inflate
}
