package com.unionbankph.corporate.payment_link.presentation.setup_business_information.nature_of_business

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.databinding.ActivityNatureOfBusinessBinding
import com.unionbankph.corporate.payment_link.presentation.setup_business_information.business_information_forms.BusinessInformationActivity

class NatureOfBusinessActivity :
    BaseActivity<ActivityNatureOfBusinessBinding, NatureOfBusinessViewModel>(){

    var natureOfBusiness = arrayOf("Advertising Services", "Airline Tickets", "Apparel", "Automotive/Industrial", "Charities", "Clothing", "Consultancy")
    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.root)
        setDrawableBackButton(
            R.drawable.ic_msme_back_button_orange,
            R.color.colorSMEMediumOrange,
            true
        )
        setToolbarTitle(binding.viewToolbar.textViewTitle, getString(R.string.nature_of_business))
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
        binding.viewToolbar.btnSaveAndExit.visibility = View.GONE
        initListView()
    }

    private fun initListView(){
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, natureOfBusiness)
        binding.listview.adapter = adapter

        binding.etSearchNatureOfBusiness.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s.toString())
                adapter.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        binding.listview.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = adapter.getItem(position)
            val intent = Intent(this@NatureOfBusinessActivity, BusinessInformationActivity::class.java)
            intent.putExtra(SELECTED_NATURE_OF_BUSINESS, selectedItem)
            startActivity(intent)
        }

    }

    companion object{
        const val SELECTED_NATURE_OF_BUSINESS = "selected_nature_of_business"
    }
    override val bindingInflater: (LayoutInflater) -> ActivityNatureOfBusinessBinding
        get() = ActivityNatureOfBusinessBinding::inflate
    override val viewModelClassType: Class<NatureOfBusinessViewModel>
        get() = NatureOfBusinessViewModel::class.java
}