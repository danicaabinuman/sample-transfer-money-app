package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding2.widget.RxSeekBar
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.SeekBarProgressChangeEvent
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.DialogFactory
import com.unionbankph.corporate.app.common.widget.edittext.autoformat.AutoFormatEditText
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.databinding.ActivityNotNowAcceptCardPaymentsBinding
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentActivity
import com.unionbankph.corporate.payment_link.presentation.setup_business_information.review_and_submit.ReviewAndSubmitActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account.NominateSettlementAccountFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.concurrent.TimeUnit


class NotNowCardPaymentsActivity :
    BaseActivity<ActivityNotNowAcceptCardPaymentsBinding,NotNowCardPaymentsViewModel>(),
    NominateSettlementAccountFragment.OnNominateSettlementAccountListener {

    private lateinit var buttonAction: Button

    private var account = Account()
    private var accountList = mutableListOf<Account>()

    private var isUpdateFromSlider = false
    private var isUpdateFromInput = false

    private lateinit var editTextMonthlyVolume: AutoFormatEditText
    private lateinit var seekBarMonthlyVolume: SeekBar

    private lateinit var editTextTransactionAmount: AutoFormatEditText
    private lateinit var seekBarTransactionAmount: SeekBar

    override fun afterLayout(savedInstanceState: Bundle?) {
        super.afterLayout(savedInstanceState)
        initToolbar(binding.viewToolbar.toolbar, binding.viewToolbar.root)
        setDrawableBackButton(
            R.drawable.ic_msme_back_button_orange,
            R.color.dsColorMediumOrange,
            true
        )
    }

    override fun onViewModelBound() {
        super.onViewModelBound()

        viewModel.eligibleAccount.observe(this, Observer {
            when (it) {
                is ShowHasSoleAccount -> displayDefaultAccount(it.account)
                is ShowHasMultipleAccount -> showAccountSelectionButton(it.accounts)
                is ShowNoEligibleAccounts -> showDialogToDashboard()
            }
        })

        viewModel.uiState.observe(this, Observer {
            it.getContentIfNotHandled().let { event ->
                when (event) {
                    is UiState.Loading -> showProgressAlertDialog(TAG)
                    is UiState.Complete -> dismissProgressAlertDialog()
                    is UiState.Error -> handleOnError(event.throwable)
                }
            }
        })

        viewModel.accountWithBalance.observe(this, Observer {
            displayDefaultAccount(it)
        })
    }

    override fun onInitializeListener() {
        super.onInitializeListener()

        disableReviewDetailsButton()

        initMonthVolumeViews()
        initAverageTransactionViews()

        binding.buttonSelectAccount.setOnClickListener { openNominateAccounts() }
        binding.includeSettlementAccount.root.setOnClickListener { openNominateAccounts() }

        viewModel.getAccounts()

        navigateToReviewDetails()
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

    private fun openNominateAccounts() {
        if (accountList.size > 1) {
            selectAccount()
        } else {
            openNoAvailableAccountsDialog()
        }
    }

    private fun initMonthVolumeViews() {
        editTextMonthlyVolume = binding.viewMonthlyVolume.autoFormatEditText
        seekBarMonthlyVolume = binding.viewMonthlyVolume.seekBar

        seekBarMonthlyVolume.max = SLIDER_MAX_STEP
        seekBarMonthlyVolume.progress = SLIDER_DEFAULT_STEP

        RxTextView.textChanges(editTextMonthlyVolume)
            .debounce(
                resources.getInteger(R.integer.time_edit_text_search_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe ({ amount ->
                onUserInputValue(amount.toString(), seekBarMonthlyVolume)
            }) { Timber.e(it) }
            .addTo(disposables)

        RxSeekBar.changeEvents(seekBarMonthlyVolume)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when (it) {
                    is SeekBarProgressChangeEvent -> {
                        onUserSeekerSetValue(it.progress(), editTextMonthlyVolume)
                        val setMonthlyProgress = seekBarMonthlyVolume.progress
                        val setTransactionProgress = seekBarTransactionAmount.progress
                        if (setMonthlyProgress < setTransactionProgress){
                            seekBarTransactionAmount.progress = setMonthlyProgress - 1
                        }
                    }
                }
            }) { Timber.e(it) }
            .addTo(disposables)
    }

    private fun initAverageTransactionViews() {
        editTextTransactionAmount = binding.viewTransactionAmount.autoFormatEditText1
        seekBarTransactionAmount = binding.viewTransactionAmount.seekBar1

        seekBarTransactionAmount.max = TRANSACTION_SLIDER_MAX_STEP
        seekBarTransactionAmount.progress = TRANSACTION_SLIDER_DEFAULT_STEP

        RxTextView.textChanges(editTextTransactionAmount)
            .debounce(
                resources.getInteger(R.integer.time_edit_text_search_debounce).toLong(),
                TimeUnit.MILLISECONDS
            )
            .subscribeOn(schedulerProvider.computation())
            .observeOn(schedulerProvider.ui())
            .subscribe ({ amount ->
                onUserInputValue(amount.toString(), seekBarTransactionAmount)
            }) { Timber.e(it) }
            .addTo(disposables)

        RxSeekBar.changeEvents(seekBarTransactionAmount)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when (it) {
                    is SeekBarProgressChangeEvent -> {
                        onUserSeekerSetValue(it.progress(), editTextTransactionAmount)
                        val transactionAmount = seekBarTransactionAmount.progress
                        val monthlyVolume = seekBarMonthlyVolume.progress
                        if (transactionAmount > monthlyVolume){
                            seekBarTransactionAmount.progress = monthlyVolume - 1
                        }
                    }
                }
            }) { Timber.e(it) }
            .addTo(disposables)
    }

    fun onUserInputValue(amount: String, viewToUpdate: View) {
        if (isUpdateFromSlider) {
            isUpdateFromSlider = false
            return
        }

        if (amount.isEmpty()) {
            editTextMonthlyVolume.setText("0")
            return
        }
        isUpdateFromInput = true

        updateSliderAndEditText(
            amount,
            viewToUpdate = viewToUpdate
        )
    }

    fun onUserSeekerSetValue(progress: Int, viewToUpdate: View) {

        if (isUpdateFromInput) {
            isUpdateFromInput = false
            return
        }
        isUpdateFromSlider = true

        clearEditTextFocus()
        updateSliderAndEditText(
            (progress + SLIDER_TEP).toString(),
            viewToUpdate = viewToUpdate
        )
    }

    private fun updateSliderAndEditText(
        updateValue: String = "0",
        viewToUpdate: View? = null
    ) {
        when {
            isUpdateFromInput -> {
                val value = (updateValue.replace(",", "").toInt() / SLIDER_MIN_VALUE)
                (viewToUpdate as SeekBar).progress = value
            }
            isUpdateFromSlider -> {
                val roundedValue = updateValue.toInt() * SLIDER_MIN_VALUE
                val formattedValue = "%,d".format(roundedValue)
                (viewToUpdate as AutoFormatEditText).setText(formattedValue)
            }
        }
    }

    private fun selectAccount() {
        val nominateSettlementAccountBottomSheet =
            NominateSettlementAccountFragment.newInstance(JsonHelper.toJson(accountList))
        nominateSettlementAccountBottomSheet.setOnNominateSettlementAccountListener(this)
        nominateSettlementAccountBottomSheet.show(
            supportFragmentManager,
            RequestForPaymentActivity::class.java.simpleName
        )
    }

    private fun displayDefaultAccount(account: Account) {
        binding.buttonSelectAccount.visibility = View.GONE
        populateNominatedSettlementAccount(account)
        this.account = account
    }

    private fun showAccountSelectionButton(accountsResponse: MutableList<Account>) {
        binding.buttonSelectAccount.visibility = View.VISIBLE
        accountList = accountsResponse
    }

    private fun showDialogToDashboard() {
        DialogFactory().createSMEDialog(
            this,
            isNewDesign = false,
            title = getString(R.string.title_no_available_accounts),
            iconResource = R.drawable.ic_money_box,
            description = getString(R.string.message_no_account_eligible),
            positiveButtonText = getString(R.string.btn_back_to_dashboard),
            onPositiveButtonClicked = {
                navigator.navigateClearStacks(
                    this,
                    DashboardActivity::class.java,
                    null,
                    true,
                    Navigator.TransitionActivity.TRANSITION_SLIDE_LEFT
                )
            }
        ).show()
    }

    private fun openNoAvailableAccountsDialog() {
        DialogFactory().createSMEDialog(
            this,
            isNewDesign = false,
            title = getString(R.string.title_no_available_accounts),
            iconResource = R.drawable.ic_money_box,
            description = getString(R.string.message_no_account_eligible),
            positiveButtonText = getString(R.string.action_got_it)
        ).show()
    }

    private fun clearEditTextFocus() {
        editTextMonthlyVolume.clearFocus()
        editTextTransactionAmount.clearFocus()
    }

    override fun onAccountSelected(account: Account) {
        populateNominatedSettlementAccount(account)
    }

    private fun populateNominatedSettlementAccount(accountData: Account) {
        binding.buttonSelectAccount.visibility(false)
        binding.includeSettlementAccount.root.visibility(true)
        binding.includeSettlementAccount.apply {
            textViewCorporateName.text = accountData.name
            textViewAccountNumber.text = accountData.accountNumber
            textViewAccountName.text = accountData.productCodeDesc

            accountData.headers.forEach{ header ->
                header.name?.let { headerName ->
                    if(headerName.equals("CURBAL",true)){
                        header.value?.let{ headerValue ->
                            shimmerLayoutAmount.stopShimmerAnimation()
                            viewShimmer.visibility = View.GONE
                            textViewAvailableBalance.visibility = View.VISIBLE
                            textViewAvailableBalance.text = headerValue
                        }
                    }
                }
            }
        }

        enableReviewDetailsButton()
    }

    private fun disableReviewDetailsButton(){
        binding.btnReviewDetails.isEnabled = false
    }

    private fun enableReviewDetailsButton(){
        binding.btnReviewDetails.isEnabled = true
    }
    private fun navigateToReviewDetails(){
        binding.btnReviewDetails.setOnClickListener {
            val intent = Intent(this, ReviewAndSubmitActivity::class.java)
            startActivity(intent)
        }
    }
    companion object {
        var TAG = this::class.java.simpleName

        const val SLIDER_TEP = 1
        const val SLIDER_MAX_STEP = 299
        const val SLIDER_DEFAULT_STEP = 24
        const val TRANSACTION_SLIDER_MAX_STEP = 298
        const val TRANSACTION_SLIDER_DEFAULT_STEP = 23
        const val SLIDER_MIN_VALUE = 10000
        const val REQUEST_CODE = 200
    }

    override val bindingInflater: (LayoutInflater) -> ActivityNotNowAcceptCardPaymentsBinding
        get() = ActivityNotNowAcceptCardPaymentsBinding::inflate
    override val viewModelClassType: Class<NotNowCardPaymentsViewModel>
        get() = NotNowCardPaymentsViewModel::class.java
}