package com.unionbankph.corporate.dao.presentation.reminders

import androidx.navigation.fragment.findNavController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.lazyFast
import com.unionbankph.corporate.app.common.extension.toHtmlSpan
import com.unionbankph.corporate.dao.presentation.DaoActivity
import kotlinx.android.synthetic.main.fragment_dao_reminders.*

class DaoRemindersFragment : BaseFragment<DaoRemindersViewModel>(R.layout.fragment_dao_reminders),
                             DaoActivity.ActionEvent {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
    }

    private fun init() {
        tv_reminder_1_sub_1.text = formatString(R.string.desc_dao_reminder_1_sub_1).toHtmlSpan()
        tv_reminder_1_sub_2.text = formatString(R.string.desc_dao_reminder_1_sub_2).toHtmlSpan()
        tv_reminder_3_sub.text = formatString(R.string.desc_dao_reminder_3_sub).toHtmlSpan()
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
}
