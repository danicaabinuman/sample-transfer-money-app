package com.unionbankph.corporate.common.presentation.constant

import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.settings.presentation.form.Selector

class Constant {

    companion object {

        const val UNIONBANK_ONLY = "UNIONBANK_ONLY"
        const val OTHER_BANKS = "OTHER_BANKS"
        const val BILLS_PAYMENT = "BILLS_PAYMENT"
        const val EGOV_PAYMENT = "EGOV_PAYMENT"

        const val EMPTY = "-"

        const val REFRESH = "REFRESH"
        const val CLEAR = "CLEAR"

        const val STATUS_CONDITIONAL = "CONDITIONAL"
        const val STATUS_FOR_APPROVAL = "FOR_APPROVAL"
        const val STATUS_APPROVED = "APPROVED"
        const val STATUS_REMOVED = "REMOVED"
        const val STATUS_FOR_PROCESSING = "FOR_PROCESSING"
        const val STATUS_PROCESSING = "PROCESSING"
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_RELEASED = "RELEASED"
        const val STATUS_REVERSED = "REVERSED"
        const val STATUS_BANCNET_ERROR = "BANCNET_ERROR"
        const val STATUS_REVERSAL_FAILED = "REVERSAL_FAILED"
        const val STATUS_REJECTED = "REJECTED"
        const val STATUS_EXPIRED = "EXPIRED"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_SCHEDULED = "SCHEDULED"
        const val STATUS_SUCCESSFUL = "SUCCESSFUL"
        const val STATUS_CANCELLED = "CANCELLED"
        const val STATUS_PARTIALLY_RELEASED = "PARTIALLY_RELEASED"

        const val STATUS_NOTIFIED = "NOTIFIED"
        const val STATUS_PENDING_NEXT_STEP = "PENDING_NEXT_STEP"
        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_UNTOUCHED = "UNTOUCHED"
        const val STATUS_CREATED = "CREATED"

        const val PENDING_APPROVAL = "Pending Approval"
        const val TRANSFER_SUCCESSFUL = "Transfer Successful"
        const val PAYMENT_SUCCESSFUL = "Payment Successful"
        const val FULLY_APPROVED = "Fully Approved"
        const val TRANSFER_FAILED = "Transfer Failed"
        const val PAYMENT_FAILED = "Payment Failed"
        const val REJECTED = "Rejected"

        const val TYPE_SINGLE = "SINGLE"
        const val TYPE_BATCH = "BATCH"


        const val ACTION_SESSION_TIMEOUT = "session_timeout"

        const val COUNTRY_CODE_ID_PH = 175
        fun getDefaultCountryCode() = CountryCode(COUNTRY_CODE_ID_PH, "Philippines", "PH", 1, 1, "63")

        fun getDefaultCountryDao() = Selector(id = "204", value = "PHILIPPINES")

    }

    class Notification {
        companion object {
            const val ACTION_DIALOG = "dialog"
        }
    }

    class Permissions {
        companion object {
            const val CODE_FT_CREATETRANSACTIONS_UNIONBANK = "FT_CREATETRANSACTIONS_UNIONBANK"
            const val CODE_FT_CREATETRANSACTIONS_PESONET = "FT_CREATETRANSACTIONS_PESONET"
            const val CODE_FT_CREATETRANSACTIONS_PDDTS = "FT_CREATETRANSACTIONS_PDDTS"
            const val CODE_FT_CREATETRANSACTIONS_SWIFT = "FT_CREATETRANSACTIONS_SWIFT"
            const val CODE_FT_CREATETRANSACTIONS_INSTAPAY = "FT_CREATETRANSACTIONS_INSTAPAY"
            const val CODE_FT_CREATETRANSACTIONS_EON = "FT_CREATETRANSACTIONS_EON"
            const val CODE_FT_CREATETRANSACTIONS_OWN = "FT_CREATETRANSACTIONS_OWN"
            const val CODE_FT_CREATETRANSACTIONS = "FT_CREATETRANSACTIONS"
            const val CODE_FT_CREATETRANSACTIONS_ADHOC = "FT_CREATETRANSACTIONS_ADHOC"
            const val CODE_FT_CREATETRANSACTIONS_BENEFICIARYMASTER =
                "FT_CREATETRANSACTIONS_BENEFICIARYMASTER"
            const val CODE_FT_VIEWTRANSACTIONS_VIEWDETAILS = "FT_VIEWTRANSACTIONS_VIEWDETAILS"
            const val CODE_FT_VIEWTRANSACTIONS = "FT_VIEWTRANSACTIONS"
            const val CODE_FT_SCHEDULEDTRANSACTIONS = "FT_SCHEDULEDTRANSACTIONS"
            const val CODE_FT_DELETESCHEDULED = "FT_DELETESCHEDULED"
            const val CODE_FT_CHANNELS = "FT_CHANNELS"

            const val CODE_BM_CREATEBENEFICIARYMASTER = "BM_CREATEBENEFICIARYMASTER"
            const val CODE_BM_VIEWBENEFICIARYMASTER = "BM_VIEWBENEFICIARYMASTER"

            const val CODE_BTR_VIEWTRANSACTIONS = "BTR_VIEWTRANSACTIONS"
            const val CODE_BTR_VIEWBALANCES = "BTR_VIEWBALANCES"

            const val CODE_BP_CREATEBILLSPAYMENT_ADHOC = "BP_CREATEBILLSPAYMENT_ADHOC"
            const val CODE_BP_CREATEBILLSPAYMENT = "BP_CREATEBILLSPAYMENT"
            const val CODE_BP_SCHEDULEDPAYMENTS = "BP_SCHEDULEDPAYMENTS"
            const val CODE_BP_DELETESCHEDULEDPAYMENTS = "BP_DELETESCHEDULEDPAYMENTS"
            const val CODE_BP_VIEWBPHISTORY = "BP_VIEWBPHISTORY"
            const val CODE_BP_CREATEBILLSPAYMENT_FREQUENT = "BP_CREATEBILLSPAYMENT_FREQUENT"
            const val CODE_BP_CREATEFREQUENTBILLER = "BP_CREATEFREQUENTBILLER"
            const val CODE_BP_DELETEFREQUENTBILLER = "BP_DELETEFREQUENTBILLER"
            const val CODE_BP_VIEWBPHISTORY_DETAILS = "BP_VIEWBPHISTORY_DETAILS"

            const val CODE_RCD_MOBILE_CHECK = "RCD_MOBILE_CHECK"
        }
    }

    class FormType {
        companion object {
            const val FORM_TYPE_DATE = "date"
            const val FORM_TYPE_SELECT = "select"
            const val FORM_TYPE_NUMERIC_PADDING = "numeric_padding"
            const val FORM_TYPE_STRING = "string"
        }
    }

    class ContextualClass {
        companion object {
            const val INACTIVE = "INACTIVE"
            const val INFO = "INFO"
            const val WARNING = "WARNING"
            const val SUCCESS = "SUCCESS"
            const val DANGER = "DANGER"
        }
    }

    class IntegerValues {
        companion object {
            const val BIR_SERVICE_ID = "7868"
            const val SSS_SERVICE_ID = "7779"
        }
    }

    class PaymentMethod {
        companion object {
            const val INSTAPAY = "INSTAPAY"
            const val UB_ONLINE = "UB ONLINE"
            const val BAYAD_CENTER = "BAYD"
            const val ECPAY = "ECPY"
            const val LBC = "LBC"
            const val PALAWAN = "PALAWAN"
            const val CEBUANA_LHUILLER = "CEBUANALHUILLIER"
            const val GRABPAY = "GRABPAY"
            const val GCASH = "GCASH"
            const val UNKNOWN = "Unknown"
        }
    }
}
