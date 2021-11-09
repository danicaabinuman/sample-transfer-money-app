package com.unionbankph.corporate.app.common.widget.recyclerview.itemmodel.sme

import android.content.Context
import android.os.Parcelable
import com.unionbankph.corporate.R
import com.unionbankph.corporate.common.presentation.constant.Constant
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class GenericMenuItem(
    var id: String? = null,
    var title: String? = null,
    var subtitle: String? = null,
    var action: String? = null,
    var src: String? = null,
    var isSelected: Boolean? = null,
    var isEnabled: Boolean? = null,
    var isVisible: Boolean? = null
) : Parcelable {


    companion object {

        fun generateMegaMenuItems(context: Context): MutableList<GenericMenuItem> {
            return mutableListOf(
                GenericMenuItem(
                    id = Constant.MegaMenu.MSME_REQUEST_PAYMENT,
                    title = context.getString(R.string.mega_menu_request_payment),
                    src = "request_payment",
                    action = Constant.MegaMenu.MSME_REQUEST_PAYMENT,
                    isVisible = true,
                    isEnabled = true
                ),
                GenericMenuItem(
                    id = Constant.MegaMenu.MSME_TRANSFER_FUNDS,
                    title = context.getString(R.string.mega_menu_transfer_funds),
                    src = "transfer_funds",
                    action = Constant.MegaMenu.MSME_TRANSFER_FUNDS,
                    isVisible = true,
                    isEnabled = false
                ),
                GenericMenuItem(
                    id = Constant.MegaMenu.MSME_PAY_BILLS,
                    title = context.getString(R.string.mega_menu_pay_bills),
                    src = "transfer_funds",
                    action = Constant.MegaMenu.MSME_PAY_BILLS,
                    isVisible = true,
                    isEnabled = false
                ),
                GenericMenuItem(
                    id = Constant.MegaMenu.MSME_APPLY_LOAN,
                    title = context.getString(R.string.mega_menu_apply_loan),
                    src = "apply_loan",
                    action = Constant.MegaMenu.MSME_APPLY_LOAN,
                    isVisible = true,
                    isEnabled = true
                ),
                GenericMenuItem(
                    id = Constant.MegaMenu.MSME_DEPOSIT_CHECK,
                    title = context.getString(R.string.mega_menu_deposit_check),
                    src = "deposit_check",
                    action = Constant.MegaMenu.MSME_DEPOSIT_CHECK,
                    isVisible = true,
                    isEnabled = true
                ),
                GenericMenuItem(
                    id = Constant.MegaMenu.MSME_GENERATE_QR,
                    title = context.getString(R.string.mega_menu_generate_qr),
                    src = "generate_qr",
                    action = Constant.MegaMenu.MSME_GENERATE_QR,
                    isVisible = true,
                    isEnabled = true
                )
            )
        }
    }
}
