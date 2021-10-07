package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.showDatePicker
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option.upload_documents.CardAcceptanceUploadDocumentsActivity
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import io.reactivex.rxkotlin.addTo
import java.util.*
import com.unionbankph.corporate.databinding.ActivityYesAcceptCardPaymentsBinding

class YesAcceptCardPaymentsActivity : BaseActivity<ActivityYesAcceptCardPaymentsBinding,YesAcceptCardPaymentsViewModel>() {

    var counter = 0
    var affiliationCounter = 0
    var fieldCount = 1

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.root)
        setDrawableBackButton(R.drawable.ic_msme_back_button_orange, R.color.colorSMEMediumOrange, true)
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

    override fun onViewModelBound() {
        super.onViewModelBound()

    }

    override fun onViewsBound() {
        super.onViewsBound()

        buttonNextDisable()
        onAddingAffiliation()
    }

    private fun onAddingAffiliation(){
        var addAffiliationFields: View
        val container: LinearLayout = findViewById(R.id.llAddedAffiliations)
        binding.toggleAffiliation.text = "Affiliation $fieldCount"
        binding.toggleAffiliation.setOnClickListener {
            val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            addAffiliationFields = layoutInflater.inflate(R.layout.layout_on_added_affiliation, null)
            container.addView(addAffiliationFields, container.childCount)

            fieldCount++
            affiliationCounter++

            binding.toggleAffiliation.text = "Affiliation $fieldCount"
            val btnAffiliation = addAffiliationFields.findViewById<Button>(R.id.btnAffiliation)
            btnAffiliation.text = "Affiliation $affiliationCounter"

            val datePicker = addAffiliationFields.findViewById<TextInputEditText>(R.id.tie_date_of_issue)
            datePicker.setOnClickListener {
                showDatePicker(
                    minDate = Calendar.getInstance().apply { set(Calendar.YEAR, 1900) },
                    maxDate = Calendar.getInstance(),
                    callback = { _, year, monthOfYear, dayOfMonth ->
                        viewModel.dateOfIncorporationInput.onNext(
                            Calendar.getInstance().apply {
                                set(year, monthOfYear, dayOfMonth)
                            }
                        )
                    }
                )
                viewModel.dateOfIncorporationInput
                    .subscribe {
                        datePicker.setText(
                            viewUtil.getDateFormatByCalendar(
                                it,
                                DateFormatEnum.DATE_FORMAT_DATE.value
                            )
                        )
                    }.addTo(disposables)

            }

            btnAffiliation.setOnClickListener {
                    val affiliationLayout = addAffiliationFields.findViewById<View>(R.id.showAffiliationLayout)
                    val rl = addAffiliationFields.findViewById<RelativeLayout>(R.id.rlAffiliation)

                    counter++
                    val checker = counter % 2
                    if (checker == 1){
                        affiliationLayout.visibility(true)
                        rl.setBackgroundResource(R.drawable.bg_transparent_orange_border_radius_8dp)

                    } else if (checker == 0){
                        affiliationLayout.visibility(false)
                        rl.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    }

            }

            val affiliationCount = container.childCount
            if (affiliationCount == 9){
                binding.toggleAffiliation.visibility(false)
            }

            val removeAffiliation = addAffiliationFields.findViewById<Button>(R.id.btnRemoveAffiliation)
            removeAffiliation.setOnClickListener {
                val index = container.indexOfChild(addAffiliationFields)
                container.removeViewAt(index)
                fieldCount--
                binding.toggleAffiliation.text = "Affiliation $fieldCount"
            }
        }

    }

    private fun expand(layout: RelativeLayout){
        layout.visibility = View.VISIBLE
    }
//
    private fun collapse(layout: RelativeLayout){
        layout.visibility = View.GONE
    }

    private fun buttonNextEnable(){
        binding.btnNext.isEnabled = true
        binding.btnNext.setBackgroundResource(R.drawable.bg_gradient_orange_radius4)
        binding.btnNext.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorWhite))
        navigateToUploadDocuments()
    }

    private fun buttonNextDisable(){
        binding.btnNext.isEnabled = false
        binding.btnNext.setBackgroundResource(R.drawable.bg_gray_radius4_width1)
        binding.btnNext.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorBlack10))
    }

    fun checkedNoAffiliation(view: View) {
        if (view is CheckBox){
            val checked: Boolean = view.isChecked

            when (view.id){
                R.id.cb_NoOtherAffiliation -> {
                    if (checked){
                        buttonNextEnable()
                        binding.toggleAffiliation.isEnabled = false
                    } else {
                        buttonNextDisable()
                        binding.toggleAffiliation.isEnabled = true
                    }
                }
            }
        }
    }

    private fun navigateToUploadDocuments(){
        binding.btnNext.setOnClickListener {
            val intent = Intent(this, CardAcceptanceUploadDocumentsActivity::class.java)
            startActivity(intent)
        }

    }

    override val bindingInflater: (LayoutInflater) -> ActivityYesAcceptCardPaymentsBinding
        get() = ActivityYesAcceptCardPaymentsBinding::inflate
    override val viewModelClassType: Class<YesAcceptCardPaymentsViewModel>
        get() = YesAcceptCardPaymentsViewModel::class.java
}