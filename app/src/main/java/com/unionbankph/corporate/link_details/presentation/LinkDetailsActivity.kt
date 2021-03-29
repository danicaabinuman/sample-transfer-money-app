package com.unionbankph.corporate.link_details.presentation

import android.content.ClipData
import androidx.lifecycle.ViewModelProviders
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.base.BaseActivity
import com.unionbankph.corporate.app.common.platform.events.EventObserver
import com.unionbankph.corporate.common.presentation.viewmodel.state.UiState
import kotlinx.android.synthetic.main.activity_link_details.*


class LinkDetailsActivity : BaseActivity<LinkDetailsViewModel>(R.layout.activity_link_details) {


    override fun onViewModelBound() {

        super.onViewModelBound()
        viewModel = ViewModelProviders.of(
            this,
            viewModelFactory
        )[LinkDetailsViewModel::class.java]
        viewModel.uiState.observe(this, EventObserver {
            when (it) {
                is UiState.Loading -> {
                    viewLoadingState.isVisible = true
                    scroll_view.isVisible = false
                }
                is UiState.Complete -> {
                    viewLoadingState.isVisible = false
                }
                is UiState.Error -> {
                    handleOnError(it.throwable)
                }
            }
        })
    }

    override fun onViewsBound() {
        super.onViewsBound()
        initViews()
        setupInputs()
        setupOutputs()
    }

    private fun initViews(){

        ivBackButton.setOnClickListener{
            finish()
        }

        imgBtnShare.setOnClickListener{
            shareLink()
        }

        ibURLcopy.setOnClickListener{
            copyLink()
        }

    }

    private fun setupInputs() {
        viewModel.initBundleData(
            intent.getStringExtra(LinkDetailsActivity.EXTRA_AMOUNT).toString(),
            intent.getStringExtra(LinkDetailsActivity.EXTRA_PAYMENT_FOR).toString(),
            intent.getStringExtra(LinkDetailsActivity.EXTRA_NOTES),
            intent.getStringExtra(LinkDetailsActivity.EXTRA_SELECTED_EXPIRY).toString()
        )
    }

    private fun setupOutputs() {
        viewModel.linkDetailsResponse.observe(this, Observer {
            setupViews(it)
        })
    }

    private fun setupViews(linkDetailsResponse: LinkDetailsResponse) {

        linkDetailsRefNo.text = linkDetailsResponse.referenceId.toString()
        linkDetailsCreatedDate.text = linkDetailsResponse.createdDate
        linkDetailsAmount.text = linkDetailsResponse.amount.toString()
        linkDetailsDescription.text = linkDetailsResponse.description
        linkDetailsNotes.text = linkDetailsResponse.note
        tv_link_details_expiry.text = linkDetailsResponse.expireDate
        linkDetailsPaymentLink.text = linkDetailsResponse.link

    }


    private fun copyLink(){
        ibURLcopy.setOnClickListener{
            val copiedUrl = linkDetailsPaymentLink.text
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied to clipboard", copiedUrl)

//            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()

            clipboard.setPrimaryClip(clip)

            showToast("Copied to clipboard")
        }
    }

    private fun shareLink(){
        imgBtnShare.setOnClickListener {
            val intent = Intent()
            intent.action= Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, linkDetailsPaymentLink.text.toString())
            intent.type="text/plain"
            startActivity(Intent.createChooser(intent,"Share To:"))
        }
    }

    private fun dateFormat(){
        var date : Long
        val sdf : SimpleDateFormat
        var dateString : String

        date = System.currentTimeMillis()
        sdf = SimpleDateFormat("MMM dd, yyyy / h:mm a")

        dateString = sdf.format(date)
        linkDetailsCreatedDate.setText(dateString)
    }

    private fun generatedLinkResults(){

        val amount: String = intent.getStringExtra("amount").toString()
        val paymentFor: String = intent.getStringExtra("payment for").toString()
        val notes: String = intent.getStringExtra("notes").toString()
        val linkExpiry: String = intent.getStringExtra("selected expiry").toString()

        linkDetailsAmount.setText(amount)
        linkDetailsDescription.setText(paymentFor)
        linkDetailsNotes.setText(notes)
        tv_link_details_expiry.setText(linkExpiry)
    }



    companion object {
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_PAYMENT_FOR = "pament for"
        const val EXTRA_NOTES = "notes"
        const val EXTRA_SELECTED_EXPIRY = "selected expiry"
    }
}


