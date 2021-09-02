package com.unionbankph.corporate.fund_transfer.data.model

import android.os.Parcelable
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.JsonDecoder
import org.json.JSONObject

@Parcelize
@Serializable
data class BeneficiaryDetailDto(

    @SerialName("account_number")
    var accountNumber: String? = null,

    @SerialName("address")
    var address: String? = null,

    @SerialName("bank_details")
    var bankDetails: BankDetails? = null,

    @SerialName("channel_id")
    var channelId: Int? = null,

    @SerialName("code")
    var code: String? = null,

    @SerialName("brstn_code")
    var brstnCode: String? = null,

    @SerialName("firm_code")
    var firmCode: String? = null,

    @SerialName("country_code_id")
    var countryCodeId: String? = null,

    @SerialName("country_code")
    var countryCode: CountryCode? = null,

    @SerialName("created_by")
    var createdBy: String? = null,

    @SerialName("created_date")
    var createdDate: String? = null,

    @SerialName("id")
    var id: Int? = null,

    @SerialName("mobile_number")
    var mobileNumber: String? = null,

    @SerialName("modified_by")
    var modifiedBy: String? = null,

    @SerialName("modified_date")
    var modifiedDate: String? = null,

    @SerialName("name")
    var name: String? = null,

    @SerialName("email_address")
    var emailAddress: String? = null,

    @SerialName("nickname")
    var nickname: String? = null,

    @SerialName("organization_id")
    var organizationId: String? = null,

    @SerialName("instapay_code")
    var instapayCode: String? = null,

    @SerialName("accounts")
    var accounts: MutableList<Account>? = null,

    @SerialName("swift_bank_details")
    var swiftBankDetails: SwiftBankDetails? = null
) : Parcelable {

    @ExperimentalSerializationApi
    @Serializer(forClass = BeneficiaryDetailDto::class)
    companion object : KSerializer<BeneficiaryDetailDto> {
        override fun deserialize(decoder: Decoder): BeneficiaryDetailDto {
            val jsonInput = decoder as? JsonDecoder ?: error("Can be deserialized only by JSON")
            val json = jsonInput.decodeJsonElement().toString()

            val jsonObject = JSONObject(json)
            val countryCodeId = jsonObject.get("country_code_id")

            // [BANK AID FIX], overwrite the null country code id to PH country code id
            when (countryCodeId) {
                "null" -> jsonObject.put("country_code_id", "175")
            }

            val bankDetails : BankDetails? = when (jsonObject.has("bank_details") && !jsonObject.isNull("bank_details")) {
                true -> JsonHelper.fromJson(jsonObject.get("bank_details").toString())
                else -> null
            }

            val countryCode : CountryCode? = when (jsonObject.has("country_code") && !jsonObject.isNull("country_code")) {
                true -> JsonHelper.fromJson(jsonObject.get("country_code").toString())
                else -> null
            }

            val accounts : MutableList<Account>? = when (jsonObject.has("accounts") && !jsonObject.isNull("accounts")) {
                true -> JsonHelper.fromListJson(jsonObject.get("accounts").toString())
                else -> null
            }

            val swiftBankDetails : SwiftBankDetails? = when (jsonObject.has("swift_bank_details") && !jsonObject.isNull("swift_bank_details")) {
                true -> JsonHelper.fromJson(jsonObject.get("swift_bank_details").toString())
                else -> null
            }

            return BeneficiaryDetailDto().apply {
                this.accountNumber = jsonObject.get("account_number").toString()
                this.address = jsonObject.get("address").toString()
                this.bankDetails = bankDetails
                this.channelId = jsonObject.get("channel_id").toString()?.toInt() ?: null
                this.code = jsonObject.get("code").toString()
                this.brstnCode = jsonObject.get("brstn_code").toString()
                this.firmCode = jsonObject.get("firm_code").toString()
                this.countryCode = countryCode
                this.createdBy = jsonObject.get("created_by").toString()
                this.createdDate = jsonObject.get("created_date").toString()
                this.emailAddress = jsonObject.get("email_address").toString()
                this.id = jsonObject.get("id").toString()?.toInt()
                this.mobileNumber = jsonObject.get("mobile_number").toString()
                this.modifiedBy = jsonObject.get("modified_by").toString()
                this.modifiedDate = jsonObject.get("modified_date").toString()
                this.name = jsonObject.get("name").toString()
                this.nickname = jsonObject.get("nickname").toString()
                this.organizationId = jsonObject.get("organization_id").toString()
                this.instapayCode = jsonObject.get("instapay_code").toString()
                this.accounts = accounts
                this.swiftBankDetails = swiftBankDetails
            }
        }
    }
}
