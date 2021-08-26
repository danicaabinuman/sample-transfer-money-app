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
import com.unionbankph.corporate.databinding.ItemManageBeneficiaryBinding
import com.unionbankph.corporate.databinding.RowManageBeneficiaryBinding
import com.unionbankph.corporate.fund_transfer.data.model.Beneficiary

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

    private lateinit var callbacks: EpoxyAdapterCallback<Beneficiary>

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorFooterModel: ErrorFooterModel_


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
                    context(this@ManageBeneficiaryController.context)
                    callbacks(this@ManageBeneficiaryController.callbacks)
                    viewUtil(this@ManageBeneficiaryController.viewUtil)
                    autoFormatUtil(this@ManageBeneficiaryController.autoFormatUtil)
                    position(index)
                }
            } else {
                manageBeneficiaryItem {
                    id(beneficiary.id)
                    beneficiary(beneficiary)
                    context(this@ManageBeneficiaryController.context)
                    callbacks(this@ManageBeneficiaryController.callbacks)
                    viewUtil(this@ManageBeneficiaryController.viewUtil)
                    autoFormatUtil(this@ManageBeneficiaryController.autoFormatUtil)
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
        holder.binding.apply {
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

        lateinit var binding: ItemManageBeneficiaryBinding

        override fun bindView(itemView: View) {
            binding = ItemManageBeneficiaryBinding.bind(itemView)
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
        holder.binding.apply {
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
            holder.binding.root.setOnClickListener {
                callbacks.onClickItem(holder.binding.root, beneficiary, position)
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: RowManageBeneficiaryBinding

        override fun bindView(itemView: View) {
            binding = RowManageBeneficiaryBinding.bind(itemView)
        }
    }
}
