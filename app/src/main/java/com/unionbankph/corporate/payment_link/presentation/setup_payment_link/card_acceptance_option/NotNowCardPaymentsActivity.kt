package com.unionbankph.corporate.payment_link.presentation.setup_payment_link.card_acceptance_option

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.widget.RxSeekBar
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.SeekBarProgressChangeEvent
import com.unionbankph.corporate.R
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.navigation.Navigator
import com.unionbankph.corporate.app.common.widget.dialog.DialogFactory
import com.unionbankph.corporate.app.common.widget.edittext.autoformat.AutoFormatEditText
import com.unionbankph.corporate.app.dashboard.DashboardActivity
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import com.unionbankph.corporate.payment_link.presentation.request_payment.RequestForPaymentActivity
import com.unionbankph.corporate.payment_link.presentation.setup_payment_link.nominate_settlement_account.NominateSettlementAccountBottomSheet
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.supercharge.shimmerlayout.ShimmerLayout
import kotlinx.android.synthetic.main.activity_not_now_accept_card_payments.*
import kotlinx.android.synthetic.main.widget_transparent_appbar.*
import timber.log.Timber
import java.util.concurrent.TimeUnit


class NotNowCardPaymentsActivity :
    BaseActivity<NotNowCardPaymentsViewModel>(R.layout.activity_not_now_accept_card_payments),
    NominateSettlementAccountBottomSheet.OnNominateSettlementAccountListener {

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
        initToolbar(toolbar, viewToolbar)
        setDrawableBackButton(
            R.drawable.ic_msme_back_button_orange,
            R.color.dsColorMediumOrange,
            true
        )
    }

    override fun onViewModelBound() {
        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[NotNowCardPaymentsViewModel::class.java]

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

        initMonthVolumeViews()
        initAverageTransactionViews()

        buttonSelectAccount.setOnClickListener { openNominateAccounts() }
        include_settlement_account.setOnClickListener { openNominateAccounts() }

        viewModel.getAccounts()
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
        editTextMonthlyVolume = viewMonthlyVolume.findViewById(R.id.autoFormatEditText)
        seekBarMonthlyVolume = viewMonthlyVolume.findViewById(R.id.seekBar)

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
                    }
                }
            }) { Timber.e(it) }
            .addTo(disposables)
    }

    private fun initAverageTransactionViews() {
        editTextTransactionAmount = viewTransactionAmount.findViewById(R.id.autoFormatEditText)
        seekBarTransactionAmount = viewTransactionAmount.findViewById(R.id.seekBar)

        seekBarTransactionAmount.max = SLIDER_MAX_STEP
        seekBarTransactionAmount.progress = SLIDER_DEFAULT_STEP

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
            NominateSettlementAccountBottomSheet.newInstance(JsonHelper.toJson(accountList))
        nominateSettlementAccountBottomSheet.setOnNominateSettlementAccountListener(this)
        nominateSettlementAccountBottomSheet.show(
            supportFragmentManager,
            RequestForPaymentActivity::class.java.simpleName
        )
    }

    private fun displayDefaultAccount(account: Account) {
        buttonSelectAccount.visibility = View.GONE
        populateNominatedSettlementAccount(account)
        this.account = account
    }

    private fun showAccountSelectionButton(accountsResponse: MutableList<Account>) {
        buttonSelectAccount.visibility = View.VISIBLE
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
        include_settlement_account.visibility = View.VISIBLE
        val tvCorporateName: AppCompatTextView = include_settlement_account.findViewById(R.id.textViewCorporateName)
        val tvAccountName: AppCompatTextView = include_settlement_account.findViewById(R.id.textViewAccountName)
        val tvAccountNumber: AppCompatTextView = include_settlement_account.findViewById(R.id.textViewAccountNumber)
        val tvAvailableBalance: AppCompatTextView = include_settlement_account.findViewById(R.id.textViewAvailableBalance)
        val slAmount: ShimmerLayout = include_settlement_account.findViewById(R.id.shimmerLayoutAmount)
        val viewShimmer: View = include_settlement_account.findViewById(R.id.viewShimmer)

        tvCorporateName.text = accountData.name
        tvAccountNumber.text = accountData.accountNumber
        tvAccountName.text = accountData.productCodeDesc

        accountData.headers.forEach{ header ->
            header.name?.let { headerName ->
                if(headerName.equals("CURBAL",true)){
                    header.value?.let{ headerValue ->
                        slAmount.stopShimmerAnimation()
                        viewShimmer.visibility = View.GONE
                        tvAvailableBalance.visibility = View.VISIBLE
                        tvAvailableBalance.text = headerValue
                    }
                }
            }
        }
    }

    companion object {
        var TAG = this::class.java.simpleName

        const val SLIDER_TEP = 1
        const val SLIDER_MAX_STEP = 499
        const val SLIDER_DEFAULT_STEP = 24
        const val SLIDER_MIN_VALUE = 10000
        const val REQUEST_CODE = 200
    }
}