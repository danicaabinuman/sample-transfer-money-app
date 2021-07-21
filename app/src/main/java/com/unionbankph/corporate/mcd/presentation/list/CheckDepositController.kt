package com.unionbankph.corporate.mcd.presentation.list

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import com.airbnb.epoxy.*
import com.unionbankph.corporate.BuildConfig
import com.unionbankph.corporate.R
import com.unionbankph.corporate.app.common.extension.formatAccountNumber
import com.unionbankph.corporate.app.common.extension.formatString
import com.unionbankph.corporate.app.common.extension.setContextCompatTextColor
import com.unionbankph.corporate.app.common.extension.visibility
import com.unionbankph.corporate.app.util.AutoFormatUtil
import com.unionbankph.corporate.app.util.ViewUtil
import com.unionbankph.corporate.common.data.form.Pageable
import com.unionbankph.corporate.common.presentation.callback.EpoxyAdapterCallback
import com.unionbankph.corporate.common.presentation.constant.DateFormatEnum
import com.unionbankph.corporate.common.presentation.helper.ConstantHelper
import com.unionbankph.corporate.databinding.ItemCheckDepositBinding
import com.unionbankph.corporate.databinding.RowCheckDepositBinding
import com.unionbankph.corporate.mcd.data.model.CheckDeposit

class CheckDepositController
constructor(
    private val context: Context,
    private val viewUtil: ViewUtil,
    private val autoFormatUtil: AutoFormatUtil
) : Typed3EpoxyController<MutableList<CheckDeposit>, Pageable, Boolean>() {

    private lateinit var callbacks: EpoxyAdapterCallback<CheckDeposit>

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun buildModels(
        data: MutableList<CheckDeposit>,
        pageable: Pageable,
        isTableView: Boolean
    ) {
        data.forEachIndexed { position, checkDeposit ->
            if (isTableView) {
                checkDepositRow {
                    id(checkDeposit.id)
                    checkDeposit(checkDeposit)
                    callbacks(callbacks)
                    context(context)
                    viewUtil(viewUtil)
                    autoFormatUtil(autoFormatUtil)
                    position(position)
                }
            } else {
                checkDepositItem {
                    id(checkDeposit.id)
                    checkDeposit(checkDeposit)
                    callbacks(callbacks)
                    context(context)
                    viewUtil(viewUtil)
                    autoFormatUtil(autoFormatUtil)
                    position(position)
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

    fun setAdapterCallbacks(callbacks: EpoxyAdapterCallback<CheckDeposit>) {
        this.callbacks = callbacks
    }
}

@EpoxyModelClass(layout = R.layout.item_check_deposit)
abstract class CheckDepositItemModel : EpoxyModelWithHolder<CheckDepositItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var checkDeposit: CheckDeposit

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<CheckDeposit>

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    var position: Int = 0

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            textViewRemarks.text = checkDeposit.remarks ?: context.formatString(
                R.string.params_check_deposit_to,
                viewUtil.getAccountNumberFormat(checkDeposit.targetAccount)
            )
            textViewCreatedBy.text = ConstantHelper.Text.getRemarksCreated(
                context,
                checkDeposit.createdBy,
                checkDeposit.createdDate,
                viewUtil
            )
            textViewCheckNumber.text = checkDeposit.checkNumber
            textViewAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
                checkDeposit.checkAmount?.value.toString(),
                checkDeposit.checkAmount?.currency
            )
            textViewDateOnCheck.text = viewUtil.getDateFormatByDateString(
                checkDeposit.checkDate,
                DateFormatEnum.DATE_FORMAT_ISO_WITHOUT_T.value,
                DateFormatEnum.DATE_FORMAT_DATE.value
            )
            textViewDepositTo.text = checkDeposit.targetAccount.formatAccountNumber()
            textViewStatus.setContextCompatTextColor(
                ConstantHelper.Color.getTextColor(checkDeposit.status)
            )
            textViewStatus.text = checkDeposit.status?.description
            cardView.setOnClickListener {
                callbacks.onClickItem(it, checkDeposit, position)
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: ItemCheckDepositBinding

        override fun bindView(itemView: View) {
            binding = ItemCheckDepositBinding.bind(itemView)
        }
    }
}

@EpoxyModelClass(layout = R.layout.row_check_deposit)
abstract class CheckDepositRowModel : EpoxyModelWithHolder<CheckDepositRowModel.Holder>() {

    @EpoxyAttribute
    lateinit var context: Context

    @EpoxyAttribute
    lateinit var autoFormatUtil: AutoFormatUtil

    @EpoxyAttribute
    lateinit var viewUtil: ViewUtil

    @EpoxyAttribute
    lateinit var checkDeposit: CheckDeposit

    @EpoxyAttribute
    var position: Int = 0

    @EpoxyAttribute
    lateinit var callbacks: EpoxyAdapterCallback<CheckDeposit>

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.binding.apply {
            viewBorderTop.visibility(position == 0)
            textViewRowRemarks.text = context.formatString(
                R.string.params_check_deposit_to,
                viewUtil.getAccountNumberFormat(checkDeposit.targetAccount)
            )
            textViewRowCheckNumber.text = checkDeposit.checkNumber
            textViewRowAmount.text = autoFormatUtil.formatWithTwoDecimalPlaces(
                checkDeposit.checkAmount?.value.toString(),
                checkDeposit.checkAmount?.currency
            )
            textViewRowDateOnCheck.text = viewUtil.getDateFormatByDateString(
                checkDeposit.checkDate,
                DateFormatEnum.DATE_FORMAT_ISO_Z.value,
                DateFormatEnum.DATE_FORMAT_DATE.value
            )
            textViewRowDepositTo.text = checkDeposit.targetAccount
            textViewRowStatus.setContextCompatTextColor(
                ConstantHelper.Color.getTextColor(checkDeposit.status)
            )
            textViewRowStatus.text = checkDeposit.status?.description
            linearLayoutRow.setOnClickListener {
                callbacks.onClickItem(it, checkDeposit, position)
            }
        }
    }

    class Holder : EpoxyHolder() {

        lateinit var binding: RowCheckDepositBinding

        override fun bindView(itemView: View) {
            binding = RowCheckDepositBinding.bind(itemView)
        }
    }
}
