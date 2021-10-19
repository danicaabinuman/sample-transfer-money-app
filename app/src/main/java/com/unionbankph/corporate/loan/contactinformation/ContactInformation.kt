package com.unionbankph.corporate.loan.contactinformation

import android.content.Context
import com.unionbankph.corporate.R

data class ContactInformation(
    val id: Int? = null,
    val title: String? = null,
    val image: Int? = null,
    var isSelected: Boolean? = null
) {
    companion object {
        fun generateContactInformation(context: Context): /*MutableList*/List<ContactInformation> {
            return listOf/*mutableListOf*/(
                ContactInformation(
                    id = 0,
                    title = context.getString(R.string.title_inventory),
                    image = R.drawable.ic_inventory,
                    isSelected = false
                ),
                ContactInformation(
                    id = 1,
                    title = context.getString(R.string.title_payroll),
                    image = R.drawable.ic_payroll,
                    isSelected = false
                ),
                ContactInformation(
                    id = 2,
                    title = context.getString(R.string.title_operations),
                    image = R.drawable.ic_operations,
                    isSelected = false
                ),
                ContactInformation(
                    id = 3,
                    title = context.getString(R.string.title_capital_investment),
                    image = R.drawable.ic_capital_investment,
                    isSelected = false
                ),
                ContactInformation(
                    id = 4,
                    title = context.getString(R.string.title_equipment),
                    image = R.drawable.ic_equipment,
                    isSelected = false
                ),
                ContactInformation(
                    id = 5,
                    title = context.getString(R.string.title_infrastructure),
                    image = R.drawable.ic_infrastructure,
                    isSelected = false
                )
            )

        }
    }
}