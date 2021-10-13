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

    var counter = 1
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
//        onAddingAffiliation2()
    }

    private fun onAddingAffiliation2() {
        val container: LinearLayout = findViewById(R.id.llAddedAffiliations)
        val count = container.childCount
        binding.toggleAffiliation.text = "Affiliation 1"

//        binding.toggleAffiliation.setOnClickListener {
//
//            when (fieldCount){
//                1 -> {
//                    binding.rlAffiliation.visibility(true)
//                }
//                2 -> {
//                    binding.rlAffiliation2.visibility(true)
//                }
//                3 -> {
//                    binding.rlAffiliation3.visibility(true)
//                }
//                4 -> {
//                    binding.rlAffiliation4.visibility(true)
//                }
//                5 -> {
//                    binding.rlAffiliation5.visibility(true)
//                }
//                6 -> {
//                    binding.rlAffiliation6.visibility(true)
//                }
//                7 -> {
//                    binding.rlAffiliation7.visibility(true)
//                }
//                8 -> {
//                    binding.rlAffiliation8.visibility(true)
//                }
//                9 -> {
//                    binding.rlAffiliation9.visibility(true)
//                }
//                10 -> {
//                    binding.rlAffiliation10.visibility(true)
//                    binding.toggleAffiliation.visibility(false)
//                }
//            }
//
//            fieldCount++
//            binding.toggleAffiliation.text = "Affiliation $fieldCount"
//
//        }
//
//        binding.btnAffiliation.setOnClickListener {
//            if (binding.showAffiliationLayout.affiliation.visibility == View.GONE){
//                binding.showAffiliationLayout.affiliation.visibility(true)
//            } else{
//                binding.showAffiliationLayout.affiliation.visibility(false)
//            }
//        }
//
//        val index = container.indexOfChild(binding.rlAffiliation)
//        binding.showAffiliationLayout.btnRemoveAffiliation.setOnClickListener {
//            container.removeViewAt(index)
//        }
//
//        val index2 = container.indexOfChild(binding.rlAffiliation2)
//        binding.showAffiliationLayout2.btnRemoveAffiliation.setOnClickListener {
//            container.removeViewAt(index2)
//        }
//        binding.btnAffiliation2.setOnClickListener {
//            if (binding.showAffiliationLayout2.affiliation.visibility == View.GONE){
//                binding.showAffiliationLayout2.affiliation.visibility(true)
//            } else{
//                binding.showAffiliationLayout2.affiliation.visibility(false)
//            }
//        }

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
            val index = container.indexOfChild(addAffiliationFields)
            val child = container.getChildAt(index)

            val datePicker = child.findViewById<TextInputEditText>(R.id.tie_date_of_issue)
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

            val button = child.findViewById<Button>(R.id.btnAffiliation)
            button.setOnClickListener {
                    val affiliationLayout = child.findViewById<View>(R.id.showAffiliationLayout)
                    val rl = child.findViewById<RelativeLayout>(R.id.rlAffiliations)

                    if (affiliationLayout.visibility == View.GONE){
                        affiliationLayout.visibility(true)
                        rl.background = ContextCompat.getDrawable(this, R.drawable.bg_transparent_orange_border_radius_8dp)
                    } else {
                        affiliationLayout.visibility(false)
                        rl.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    }

            }

            if (affiliationCounter == 10){
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