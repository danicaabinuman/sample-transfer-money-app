package com.unionbankph.corporate.settings.data.constant

import android.content.Context
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.settings.presentation.form.Selector

/**
 * Created by herald25santos on 4/21/20
 */
class SingleSelectorData(private val context: Context) {

    fun getGovernmentIds(): MutableList<Selector> {
        val list = mutableListOf<Selector>()
        list.add(Selector(id = "1", value = context.formatString(R.string.title_tin)))
        list.add(Selector(id = "2", value = context.formatString(R.string.title_sss)))
        return list
    }

    fun getSalutations(): MutableList<Selector> {
        val list = mutableListOf<Selector>()
        list.add(Selector(id = "1", value = context.formatString(R.string.title_mr), type = "MR"))
        list.add(Selector(id = "2", value = context.formatString(R.string.title_mrs), type = "MRS"))
        list.add(Selector(id = "3", value = context.formatString(R.string.title_ms), type = "MS"))
        list.add(Selector(id = "4", value = context.formatString(R.string.title_other), type = "OTHER"))
        list.add(Selector(id = "5", value = context.formatString(R.string.title_dr), type = "DR"))
        list.add(Selector(id = "6", value = context.formatString(R.string.title_prof), type = "PROF"))
        return list
    }

    fun getSourceOfFunds(): MutableList<Selector> {
        val list = mutableListOf<Selector>()
        list.add(Selector(id = "A1000", value = context.formatString(R.string.title_income_from_business)))
        list.add(Selector(id = "B1000", value = context.formatString(R.string.title_employment_salary)))
        list.add(Selector(id = "C1000", value = context.formatString(R.string.title_savings_investments)))
        list.add(Selector(id = "D1000", value = context.formatString(R.string.title_allowance)))
        list.add(Selector(id = "E1000", value = context.formatString(R.string.title_remittance)))
        list.add(Selector(id = "F1000", value = context.formatString(R.string.title_pension)))
        list.add(Selector(id = "G1000", value = context.formatString(R.string.title_inheritance)))
        list.add(Selector(id = "H1000", value = context.formatString(R.string.title_others)))
        return list
    }

    fun getUSRecordTypes(): MutableList<Selector> {
        val list = mutableListOf<Selector>()
        list.add(Selector(id = "1", value = context.formatString(R.string.title_us_tin)))
        list.add(Selector(id = "2", value = context.formatString(R.string.title_us_tel_no)))
        list.add(Selector(id = "3", value = context.formatString(R.string.title_us_mailing_address)))
        return list
    }

    fun getNationality(): MutableList<Selector> {
        val list = mutableListOf<Selector>()
        list.add(Selector(id = "01", value = context.formatString(R.string.title_filipino)))
        list.add(Selector(id = "02", value = context.formatString(R.string.title_foreign)))
        return list
    }

    fun getGenders(): MutableList<Selector> {
        val list = mutableListOf<Selector>()
        list.add(Selector(id = "M", value = context.formatString(R.string.title_male)))
        list.add(Selector(id = "F", value = context.formatString(R.string.title_female)))
        return list
    }

    fun getCivilStatuses(): MutableList<Selector> {
        val list = mutableListOf<Selector>()
        list.add(Selector(id = "1", value = context.formatString(R.string.title_single)))
        list.add(Selector(id = "2", value = context.formatString(R.string.title_married)))
        list.add(Selector(id = "3", value = context.formatString(R.string.title_widowed)))
        return list
    }

    fun getOccupations(): MutableList<Selector> {
        val list = mutableListOf<Selector>()
        list.add(Selector(id = "02", value = context.formatString(R.string.title_sr_executive)))
        list.add(Selector(id = "03", value = context.formatString(R.string.title_jr_executive)))
        list.add(Selector(id = "04", value = context.formatString(R.string.title_businessman)))
        list.add(Selector(id = "05", value = context.formatString(R.string.title_accountant)))
        list.add(Selector(id = "06", value = context.formatString(R.string.title_account_executive)))
        list.add(Selector(id = "07", value = context.formatString(R.string.title_stockholder)))
        list.add(Selector(id = "08", value = context.formatString(R.string.title_office_personnel)))
        list.add(Selector(id = "09", value = context.formatString(R.string.title_financial_analysts)))
        list.add(Selector(id = "38", value = context.formatString(R.string.title_consultant)))
        list.add(Selector(id = "52", value = context.formatString(R.string.title_dental_surgeon)))
        list.add(Selector(id = "83", value = context.formatString(R.string.title_doctor)))
        list.add(Selector(id = "84", value = context.formatString(R.string.title_dentist)))
        list.add(Selector(id = "88", value = context.formatString(R.string.title_corporate_board_chairman)))
        list.add(Selector(id = "89", value = context.formatString(R.string.title_corporate_director)))
        return list
    }
}
