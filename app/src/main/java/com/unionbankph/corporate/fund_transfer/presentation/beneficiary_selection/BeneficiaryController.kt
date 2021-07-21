package com.unionbankph.corporate.fund_transfer.presentation.beneficiary_selection

import android.content.Context
import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.AutoModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.Typed2EpoxyController
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.ErrorFooterModel_
import com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.LoadingFooterModel_
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.ChannelBankEnum
import com.unionbankph.corporate.databinding.ItemBeneficiaryBinding
import com.unionbankph.corporate.fund_transfer.data.model.Beneficiary

class BeneficiaryController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil
) : Typed2EpoxyController<MutableList<Beneficiary>, Pageable>() {

    @AutoModel
    lateinit var loadingFooterModel: LoadingFooterModel_

    @AutoModel
    lateinit var errorFooterModel: ErrorFooterModel_

    private lateinit var callbacks: EpoxyAdapterCallback<Beneficiary>

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(beneficiaries: MutableList<Beneficiary>, pageable: Pageable) {
        beneficiaries.forEachIndexed { position, beneficiary ->
            beneficiaryItem {
                id(beneficiary.id)
                beneficiary(beneficiary)
                context(this@BeneficiaryController.context)
                callbacks(this@BeneficiaryController.callbacks)
                viewUtil(this@BeneficiaryController.viewUtil)
            }
        }
        loadingFooterModel.loading(pageable.isLoadingPagination)
            .addIf(pageable.isLoadingPagination, this)
        errorFooterModel.title(pageable.errorMessage)
            .callbacks(callbacks)
            .addIf(pageable.isFailed, this)
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        // Best practice is to throw in debug so you are aware of any issues that Epoxy notices.
        // Otherwise Epoxy does its best to swallow these exceptions and continue gracefully
        throw exception
    }

    fun setEpoxyAdapterCallback(callbacks: EpoxyAdapterCallback<Beneficiary>) {
        this.callbacks = callbacks
    }
}

@EpoxyModelClass(layout = R.layout.item_beneficiary)
abstract class BeneficiaryItemModel : EpoxyModelWithHolder<BeneficiaryItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var beneficiary: Beneficiary

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<Beneficiary>

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            textViewTitle.text = beneficiary.name
            textViewCode.text = beneficiary.code
            textViewName.text =
                when {
                    beneficiary.swiftBankDetails != null ->
                        beneficiary.swiftBankDetails?.bankName
                    beneficiary.channelId == ChannelBankEnum.UBP_TO_UBP.getChannelId() ->
                        context.getString(R.string.value_receiving_bank_ubp)
                    else ->
                        beneficiary.bankDetails?.name
                }
            textViewAccountNumber.text =
                viewUtil.getAccountNumberFormat(beneficiary.accountNumber)

            root.setOnClickListener {
                callbacks.onClickItem(root, beneficiary, position)
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemBeneficiaryBinding

        override fun bindView(itemView: View) {
            binding = ItemBeneficiaryBinding.bind(itemView)
        }
    }
}
