package com.unionbankph.corporate.payment_link.presentation.setup_business_information

import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_request_payment.*

class BusinessInformationActivity : BaseActivity<BusinessInformationViewModel>(R.layout.activity_business_information),
    AdapterView.OnItemSelectedListener {

    var businessType = arrayOf("Manufacturer", "Wholesaler", "Service", "Importer", "Exporter", "Retailer")
    var business = "Wholesaler"

    override fun onViewsBound() {
        super.onViewsBound()

        natureOfBusiness()
    }
    private fun natureOfBusiness(){

        var aa = ArrayAdapter(this, android.R.layout.simple_list_item_1, businessType)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        with(dropdownPaymentLinkExport){
            adapter = aa
            setSelection(1, false)
            onItemSelectedListener = this@BusinessInformationActivity
            prompt = "SAMPLE PROMPT MESSAGE"
            gravity = Gravity.CENTER
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        business = parent?.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}