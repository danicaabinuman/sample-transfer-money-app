package com.unionbankph.corporate.dao.presentation.type_of_business

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseFragment
import com.unionbankph.corporate.app.common.extension.*
import com.unionbankph.corporate.app.common.widget.validator.validation.RxCombineValidator
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.dao.presentation.DaoActivity
import com.unionbankph.corporate.databinding.FragmentDaoTypeOfBusinessBinding
import com.unionbankph.corporate.settings.presentation.form.Selector
import com.unionbankph.corporate.settings.presentation.single_selector.SingleSelectorTypeEnum
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.annotation.concurrent.ThreadSafe

class DaoTypeOfBusinessFragment :
    BaseFragment<FragmentDaoTypeOfBusinessBinding, DaoTypeOfBusinessViewModel>(),
    DaoActivity.ActionEvent {

    private val daoActivity by lazyFast { getAppCompatActivity() as DaoActivity }

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initDaoActivity()
    }

    override fun onViewsBound() {
        super.onViewsBound()
        init()
        initBinding()
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
    }

    override fun onInitializeListener() {
        super.onInitializeListener()
        initEventBus()
        initClickListener()
        initRadioGroupListener()
    }

    private fun initRadioGroupListener() {
        binding.rgTypeOfBusiness.setOnCheckedChangeListener { _, checkedId ->
            initRadioButtonListener(checkedId)
        }
        initToolTipListener(
            formatString(R.string.title_sole_proprietorship),
            formatString(R.string.msg_tooltip_sole_propriotorship),
            binding.rbSoleProprietorship
        )
        initToolTipListener(
            formatString(R.string.title_partnership),
            formatString(R.string.msg_tooltip_partnership),
            binding.rbPartnership
        )
        initToolTipListener(
            formatString(R.string.title_corporation),
            formatString(R.string.msg_tooltip_corporation),
            binding.rbCorporation
        )
        initToolTipListener(
            formatString(R.string.title_cooperative),
            formatString(R.string.msg_tooltip_cooperative),
            binding.rbCooperative
        )
        initToolTipListener(
            formatString(R.string.title_non_government),
            formatString(R.string.msg_tooltip_non_government),
            binding.rbNonGovernment
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initToolTipListener(title: String, message: String, rb: AppCompatRadioButton) {
        rb.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= rb.right -
                    rb.compoundDrawables[2].bounds.width()) {
                    showToolTip(
                        title,
                        message
                    )
                    return@OnTouchListener true
                }
            }
            false
        })
    }

    private fun initRadioButtonListener(checkedId: Int) {
        binding.tilOthers.visibility(checkedId == R.id.rb_others)
        if (checkedId == R.id.rb_others) {
            binding.tieOthers.clear()
        } else {
            binding.tieOthers.setText(Constant.EMPTY)
        }
    }

    private fun validateForm() {
        val tieOthersObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieOthers
        )
        val tieBusinessIndustryObservable = viewUtil.rxTextChanges(
            isFocusChanged = true,
            isValueChanged = true,
            minLength = resources.getInteger(R.integer.min_length_field),
            maxLength = resources.getInteger(R.integer.max_length_field_100),
            editText = binding.tieBusinessIndustry
        )
        initSetError(tieOthersObservable)
        initSetError(tieBusinessIndustryObservable)
        RxCombineValidator(
            tieOthersObservable,
            tieBusinessIndustryObservable
        )
            .asObservable()
            .distinctUntilChanged()
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .doOnNext {
                viewModel.isValidFormInput.onNext(it)
            }
            .subscribe()
            .addTo(disposables)
    }

    private fun initClickListener() {
        binding.tieBusinessIndustry.setOnClickListener {
            navigateSingleSelector(SingleSelectorTypeEnum.TYPE_OF_BUSINESS.name)
        }
    }

    private fun initEventBus() {
        eventBus.inputSyncEvent.flowable.subscribe {
            if (it.eventType == SingleSelectorTypeEnum.TYPE_OF_BUSINESS.name) {
                val selector = JsonHelper.fromJson<Selector>(it.payload)
                viewModel.typeOfIndustry.onNext(selector)
            }
        }.addTo(disposables)
    }

    private fun navigateSingleSelector(selectorType: String) {
        val action =
            DaoTypeOfBusinessFragmentDirections.actionSelectorActivity(selectorType)
        findNavController().navigate(action)
    }

    private fun initBinding() {
        viewModel.typeOfIndustry
            .subscribe {
                binding.tieBusinessIndustry.setText(it.value)
            }.addTo(disposables)
    }

    private fun init() {
        validateForm()
        binding.tieOthers.refresh()
        val refNumber = arguments?.getString(EXTRA_REFERENCE_NUMBER)
        Timber.d("refNumber: $refNumber")
    }

    private fun initDaoActivity() {
        daoActivity.showToolBarDetails()
        daoActivity.setToolBarDesc(formatString(R.string.title_type_of_business))
        daoActivity.showButton(true)
        daoActivity.setActionEvent(this)
        daoActivity.setProgressValue(3)
        daoActivity.showProgress(true)
    }

    private fun showMissingFieldDialog() {
        showMaterialDialogError(
            message = formatString(R.string.msg_error_missing_fields)
        )
    }

    private fun clearFormFocus() {
        viewUtil.dismissKeyboard(getAppCompatActivity())
        binding.constraintLayout.requestFocus()
        binding.constraintLayout.isFocusableInTouchMode = true
    }

    override fun onClickNext() {
        if (viewModel.hasValidForm()) {
            clearFormFocus()
            findNavController().navigate(R.id.action_reminders)
        } else {
            showMissingFieldDialog()
            binding.tieOthers.refresh()
            binding.tieBusinessIndustry.refresh()
        }
    }

    @ThreadSafe
    companion object {
        const val EXTRA_REFERENCE_NUMBER = "referenceNumber"
    }

    override val layoutId: Int
        get() = R.layout.fragment_dao_type_of_business

    override val viewModelClassType: Class<DaoTypeOfBusinessViewModel>
        get() = DaoTypeOfBusinessViewModel::class.java
}
