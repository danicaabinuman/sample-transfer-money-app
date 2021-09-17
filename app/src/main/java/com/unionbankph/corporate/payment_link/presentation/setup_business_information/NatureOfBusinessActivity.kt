package com.unionbankph.corporate.payment_link.presentation.setup_business_information

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import kotlinx.android.synthetic.main.activity_business_information.*
import kotlinx.android.synthetic.main.activity_business_information.viewToolbar
import kotlinx.android.synthetic.main.activity_nature_of_business.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.*
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.textViewTitle
import kotlinx.android.synthetic.main.widget_transparent_org_appbar.toolbar
import kotlinx.android.synthetic.main.widget_transparent_rmo_appbar.*

class NatureOfBusinessActivity :
    BaseActivity<NatureOfBusinessViewModel>(R.layout.activity_nature_of_business){

    var natureOfBusiness = arrayOf("Advertising Services", "Airline Tickets", "Apparel", "Automotive/Industrial", "Charities", "Clothing", "Consultancy")
    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(
            R.drawable.ic_msme_back_button_orange,
            R.color.colorSMEMediumOrange,
            true
        )
        setToolbarTitle(textViewTitle, "Nature of Business")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewsBound() {
        super.onViewsBound()
        btnSaveAndExit.visibility = View.GONE
        initListView()
    }

    private fun initListView(){
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, natureOfBusiness)
        listview.adapter = adapter

        et_search_nature_of_business.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s.toString())
                adapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        listview.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = adapter.getItem(position)
            val intent = Intent(this@NatureOfBusinessActivity, BusinessInformationActivity::class.java)
            intent.putExtra("selected", selectedItem)
            startActivity(intent)
        }

    }
}