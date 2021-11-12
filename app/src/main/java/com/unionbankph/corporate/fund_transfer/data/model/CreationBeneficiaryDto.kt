package com.unionbankph.corporate.fund_transfer.data.model

import android.os.Parcelable
import com.rilixtech.Country
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.data.model.Status
import com.unionbankph.corporate.app.common.extension.nullable
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.common.presentation.constant.Constant
import com.unionbankph.corporate.common.presentation.helper.JsonHelper
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.*
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import org.json.JSONObject
import timber.log.Timber

@Parcelize
@Serializable
data class CreationBeneficiaryDto(

    @SerialName("account_number")
    var accountNumber: String? = null,

    @SerialName("accounts")
    var accounts: MutableList<Account> = mutableListOf(),

    @SerialName("address")
    var address: String? = null,

    @SerialName("bank_details")
    var bankDetails: BankDetails? = null,

    @SerialName("channel_id")
    var channelId: Int? = null,

    @SerialName("code")
    var code: String? = null,

    @SerialName("country_code_id")
    var countryCodeId: String? = null,

    @SerialName("country_code")
    var countryCode: CountryCode? = null,

    @SerialName("created_by")
    var createdBy: String? = null,

    @SerialName("created_date")
    var createdDate: String? = null,

    @SerialName("email_address")
    var emailAddress: String? = null,

    @SerialName("id")
    var id: String? = null,

    @SerialName("mobile_number")
    var mobileNumber: String? = null,

    @SerialName("modified_by")
    var modifiedBy: String? = null,

    @SerialName("modified_date")
    var modifiedDate: String? = null,

    @SerialName("name")
    var name: String? = null,

    @SerialName("nickname")
    var nickname: String? = null,

    @SerialName("organization_id")
    var organizationId: String? = null,

    @SerialName("instapay_code")
    var instapayCode: String? = null,

    @SerialName("swift_bank_details")
    var swiftBankDetails: SwiftBankDetails? = null

) : Parcelable {

    @ExperimentalSerializationApi
    @Serializer(forClass = CreationBeneficiaryDto::class)
    companion object : KSerializer<CreationBeneficiaryDto> {

        override val descriptor: SerialDescriptor = buildClassSerialDescriptor(this.toString()) {
            element<String>("account_number", isOptional = true)
            element<String>("address", isOptional = true)
            element<String>("bank_details", isOptional = true)
            element<String>("channel_id", isOptional = true)
            element<String>("code", isOptional = true)
            element<String>("country_code_id", isOptional = true)
            element<String>("country_code", isOptional = true)
            element<String>("created_by", isOptional = true)
            element<String>("created_date", isOptional = true)
            element<String>("id", isOptional = true)
            element<String>("mobile_number", isOptional = true)
            element<String>("modified_by", isOptional = true)
            element<String>("modified_date", isOptional = true)
            element<String>("name", isOptional = true)
            element<String>("email_address", isOptional = true)
            element<String>("nickname", isOptional = true)
            element<String>("organization_id", isOptional = true)
            element<String>("instapay_code", isOptional = true)
            element<String>("accounts", isOptional = true)
            element<String>("swift_bank_details", isOptional = true)
        }

        override fun serialize(encoder: Encoder, value: CreationBeneficiaryDto) {
            val compositeEncoder = encoder.beginStructure(descriptor)
            encodeSerializableString(0, compositeEncoder, value.accountNumber)
            encodeSerializableString(1, compositeEncoder, value.address)
            compositeEncoder.encodeNullableSerializableElement(
                descriptor,
                2,
                BankDetails.serializer().nullable,
                value.bankDetails
            )
            compositeEncoder.encodeNullableSerializableElement(
                descriptor,
                3,
                Int.serializer().nullable,
                value.channelId
            )
            encodeSerializableString(4, compositeEncoder, value.code)
            encodeSerializableString(5, compositeEncoder, value.countryCodeId)
            compositeEncoder.encodeNullableSerializableElement(
                descriptor,
                6,
                CountryCode.serializer().nullable,
                value.countryCode
            )
            encodeSerializableString(7, compositeEncoder, value.createdBy)
            encodeSerializableString(8, compositeEncoder, value.createdDate)
            compositeEncoder.encodeNullableSerializableElement(
                descriptor,
                9,
                String.serializer().nullable,
                value.id
            )
            encodeSerializableString(10, compositeEncoder, value.mobileNumber)
            encodeSerializableString(11, compositeEncoder, value.modifiedBy)
            encodeSerializableString(12, compositeEncoder, value.modifiedDate)
            encodeSerializableString(13, compositeEncoder, value.name)
            encodeSerializableString(14, compositeEncoder, value.emailAddress)
            encodeSerializableString(15, compositeEncoder, value.nickname)
            encodeSerializableString(16, compositeEncoder, value.organizationId)
            encodeSerializableString(17, compositeEncoder, value.instapayCode)
            compositeEncoder.encodeNullableSerializableElement(
                descriptor,
                18,
                serializer<List<Account>>(),
                value.accounts
            )
            compositeEncoder.encodeNullableSerializableElement(
                descriptor,
                19,
                SwiftBankDetails.serializer().nullable,
                value.swiftBankDetails
            )
            compositeEncoder.endStructure(descriptor)
        }

        private fun encodeSerializableString(
            index: Int,
            compositeOutput: CompositeEncoder,
            value: String?
        ) {
            compositeOutput.encodeNullableSerializableElement(
                descriptor,
                index,
                String.serializer().nullable,
                value
            )
        }

        override fun deserialize(decoder: Decoder): CreationBeneficiaryDto {
            val jsonInput = decoder as? JsonDecoder ?: error("Can be deserialized only by JSON")
            val json = jsonInput.decodeJsonElement().toString()

            val jsonObject = JSONObject(json)
            val countryCodeId = jsonObject.get("country_code_id")

            // [BANK AID FIX], overwrite the null country code id to PH country code id
            when (countryCodeId) {
                "null" -> jsonObject.put(
                    "country_code_id",
                    Constant.COUNTRY_CODE_ID_PH.toString()
                )
            }

            val accounts : MutableList<Account> = when (jsonObject.has("accounts") &&
                    !jsonObject.isNull("accounts")) {
                true -> JsonHelper.fromListJson(jsonObject.get("accounts").toString())
                else -> mutableListOf()
            }

            return CreationBeneficiaryDto().apply {
                this.accountNumber = getJsonAsString(jsonObject,"account_number")
                this.address = getJsonAsString(jsonObject,"address")
                this.bankDetails = getJsonAsObject(jsonObject, "bank_details")
                this.channelId = getJsonAsString(jsonObject,"channel_id")?.toInt()
                this.code = getJsonAsString(jsonObject,"code")
                this.countryCode = getJsonAsObject(jsonObject, "country_code")
                this.countryCodeId = getJsonAsObject(jsonObject, "country_code_id")
                this.createdBy = getJsonAsString(jsonObject,"created_by")
                this.createdDate = getJsonAsString(jsonObject,"created_date")
                this.emailAddress = getJsonAsString(jsonObject,"email_address")
                this.id = getJsonAsString(jsonObject,"id")
                this.mobileNumber = getJsonAsString(jsonObject,"mobile_number")
                this.modifiedBy = getJsonAsString(jsonObject,"modified_by")
                this.modifiedDate = getJsonAsString(jsonObject,"modified_date")
                this.name = getJsonAsString(jsonObject,"name")
                this.nickname = getJsonAsString(jsonObject,"nickname")
                this.organizationId = getJsonAsString(jsonObject,"organization_id")
                this.instapayCode = getJsonAsString(jsonObject,"instapay_code")
                this.accounts = accounts
                this.swiftBankDetails = getJsonAsObject(jsonObject, "swift_bank_details")
            }
        }

        private fun getJsonAsString(jsonObject : JSONObject, key: String) : String? {
            return when (jsonObject.has(key) && !jsonObject.isNull(key)) {
                true -> jsonObject.get(key).toString()
                else -> String().nullable()
            }
        }

        inline fun <reified T> getJsonAsObject(jsonObject: JSONObject, key: String) : T? {
            return when (jsonObject.has(key) && !jsonObject.isNull(key)) {
                true -> JsonHelper.fromJson(jsonObject.get(key).toString()) as T
                else -> null
            }
        }
    }
}

