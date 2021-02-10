package com.unionbankph.corporate.fund_transfer.presentation.beneficiary_list

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.Typed3EpoxyController
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.notEmpty
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.fund_transfer.data.model.Beneficiary
import kotlinx.android.synthetic.main.item_manage_beneficiary.view.*
import kotlinx.android.synthetic.main.row_manage_beneficiary.view.*

/**
 * Created by herald25santos on 08/04/2019
 */
class
ManageBeneficiaryController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil,
    private val autoFormatUtil: AutoFormatUtil
) : Typed3EpoxyController<MutableList<Beneficiary>, Pageable, Boolean>() {

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorFooterModel: ErrorFooterModel_

    private lateinit var callbacks: EpoxyAdapterCallback<Beneficiary>

    override fun buildModels(
        data: MutableList<Beneficiary>,
        pageable: Pageable,
        isTableView: Boolean
    ) {
        data.forEachIndexed { index, beneficiary ->
            if (isTableView) {
                manageBeneficiaryRow {
                    id(beneficiary.id)
                    beneficiary(beneficiary)
                    context(context)
                    callbacks(callbacks)
                    viewUtil(viewUtil)
                    autoFormatUtil(autoFormatUtil)
                    position(index)
                }
            } else {
                manageBeneficiaryItem {
                    id(beneficiary.id)
                    beneficiary(beneficiary)
                    context(context)
                    callbacks(callbacks)
                    viewUtil(viewUtil)
                    autoFormatUtil(autoFormatUtil)
                    position(index)
                }
            }
        }
        loadingFooterModel.loading(pageable.isLoadingPagination)
            .addIf(pageable.isLoadingPagination && !isTableView, this)
        errorFooterModel.title(pageable.errorMessage)
            .callbacks(callbacks)
            .addIf(pageable.isFailed, this)
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setAdapterCallbacks(callbacks: EpoxyAdapterCallback<Beneficiary>) {
        this.callbacks = callbacks
    }
}

@EpoxyModelClass(layout = R.layout.item_manage_beneficiary)
abstract class ManageBeneficiaryItemModel :
    EpoxyModelWithHolder<ManageBeneficiaryItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var beneficiary: Beneficiary

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<Beneficiary>

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.apply {
            textViewBeneficiaryName.text = beneficiary.name
            textViewCreatedBy.text = ConstantHelper.Text.getRemarksCreated(
                context,
                beneficiary.createdBy,
                beneficiary.createdDate,
                viewUtil
            )
            textViewChannel.text =
                ConstantHelper.Text.getChannelByChannelId(beneficiary.channelId)
            textViewBeneficiaryCode.text = beneficiary.code.notEmpty()
            textViewReceivingBank.text =
                if (beneficiary.channelId == ChannelBankEnum.UBP_TO_UBP.getChannelId()) {
                    context.getString(R.string.value_receiving_bank_ubp)
                } else {
                    if (beneficiary.bankDetails != null) {
                        beneficiary.bankDetails?.name
                    } else {
                        beneficiary.swiftBankDetails?.bankName
                    }
                }
            textViewAccountNumber.text =
                viewUtil.getAccountNumberFormat(beneficiary.accountNumber)
            cardViewBeneficiary.setOnClickListener {
                callbacks.onClickItem(cardViewBeneficiary, beneficiary, position)
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var textViewBeneficiaryName: TextView
        lateinit var textViewCreatedBy: TextView
        lateinit var textViewChannel: TextView
        lateinit var textViewBeneficiaryCode: TextView
        lateinit var textViewReceivingBank: TextView
        lateinit var textViewAccountNumber: TextView
        lateinit var cardViewBeneficiary: androidx.cardview.widget.CardView
        lateinit var itemView: View

        override fun bindView(itemView: View) {
            textViewBeneficiaryName = itemView.textViewBeneficiaryName
            textViewCreatedBy = itemView.textViewCreatedBy
            textViewChannel = itemView.textViewChannel
            textViewBeneficiaryCode = itemView.textViewBeneficiaryCode
            textViewReceivingBank = itemView.textViewReceivingBank
            textViewAccountNumber = itemView.textViewAccountNumber
            cardViewBeneficiary = itemView.cardViewBeneficiary
            this.itemView = itemView
        }
    }
}

@EpoxyModelClass(layout = R.layout.row_manage_beneficiary)
abstract class ManageBeneficiaryRowModel :
    EpoxyModelWithHolder<ManageBeneficiaryRowModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var beneficiary: Beneficiary

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<Beneficiary>

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.apply {
            viewBorderTop.visibility(position == 0)
            textViewRowBeneficiaryName.text = beneficiary.name
            textViewRowChannel.text =
                ConstantHelper.Text.getChannelByChannelId(beneficiary.channelId)
            textViewRowBeneficiaryCode.text = beneficiary.code.notEmpty()
            textViewRowReceivingBank.text =
                if (beneficiary.channelId == ChannelBankEnum.UBP_TO_UBP.getChannelId()) {
                    context.getString(R.string.value_receiving_bank_ubp)
                } else {
                    if (beneficiary.bankDetails != null) {
                        beneficiary.bankDetails?.name
                    } else {
                        beneficiary.swiftBankDetails?.bankName
                    }
                }
            textViewRowAccountNumber.text =
                viewUtil.getAccountNumberFormat(beneficiary.accountNumber)
            itemView.setOnClickListener {
                callbacks.onClickItem(itemView, beneficiary, position)
            }
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var linearLayoutRow: LinearLayout
        lateinit var viewBackgroundRow: View
        lateinit var imageViewRowIcon: ImageView
        lateinit var textViewRowBeneficiaryName: TextView
        lateinit var textViewRowChannel: TextView
        lateinit var textViewRowBeneficiaryCode: TextView
        lateinit var textViewRowReceivingBank: TextView
        lateinit var textViewRowAccountNumber: TextView
        lateinit var viewBorderTop: View
        lateinit var itemView: View

        override fun bindView(itemView: View) {
            linearLayoutRow = itemView.linearLayoutRow
            viewBackgroundRow = itemView.viewBackgroundRow
            imageViewRowIcon = itemView.imageViewRowIcon
            textViewRowBeneficiaryName = itemView.textViewRowBeneficiaryName
            textViewRowChannel = itemView.textViewRowChannel
            textViewRowBeneficiaryCode = itemView.textViewRowBeneficiaryCode
            textViewRowReceivingBank = itemView.textViewRowReceivingBank
            textViewRowAccountNumber = itemView.textViewRowAccountNumber
            viewBorderTop = itemView.viewBorderTop
            this.itemView = itemView
        }
    }
}
