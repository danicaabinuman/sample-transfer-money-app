package com.unionbankph.corporate.fund_transfer.data.model

import android.os.Parcelable
import com.rilixtech.Country
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.data.model.Status
import com.unionbankph.corporate.auth.data.model.CountryCode
import com.unionbankph.corporate.fund_transfer.data.form.BeneficiaryForm
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.*
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
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
            element<String?>("account_number", isOptional = true)
            element<List<Account>?>("accounts", isOptional = true)
            element<String?>("address", isOptional = true)
            element<BankDetails?>("bank_details", isOptional = true)
            element<Int?>("channel_id", isOptional = true)
            element<String?>("code", isOptional = true)
            element<String?>("country_code_id", isOptional = true)
            element<CountryCode?>("country_code", isOptional = true)
            element<String?>("created_by", isOptional = true)
            element<String?>("created_date", isOptional = true)
            element<String?>("email_address", isOptional = true)
            element<String?>("id", isOptional = true)
            element<String?>("mobile_number", isOptional = true)
            element<String?>("modified_by", isOptional = true)
            element<String?>("modified_date", isOptional = true)
            element<String?>("name", isOptional = true)
            element<String?>("nickname", isOptional = true)
            element<String?>("organization_id", isOptional = true)
            element<String?>("instapay_code", isOptional = true)
            element<SwiftBankDetails?>("swift_bank_details", isOptional = true)
        }

        override fun deserialize(decoder: Decoder): CreationBeneficiaryDto {
            var accountNumber: String? = null
            var accounts: List<Account>? = null
            var address: String? = null
            var bankDetails: BankDetails? = null
            var channelId: Int? = null
            var code: String? = null
            var countryCodeId: String? = null
            var countryCode: CountryCode? = null
            var createdBy: String? = null
            var createdDate: String? = null
            var emailAddress: String? = null
            var id: String? = null
            var mobileNumber: String? = null
            var modifiedBy: String? = null
            var modifiedDate: String? = null
            var name: String? = null
            var nickName: String? = null
            var organizationId: String? = null
            var instapayCode: String? = null
            var swiftBankDetails: SwiftBankDetails? = null

            return try {
                val inp: CompositeDecoder = decoder.beginStructure(Status.descriptor)

                loop@ while (true) {
                    when (val i = inp.decodeElementIndex(Status.descriptor)) {
                        CompositeDecoder.DECODE_DONE -> break@loop
                        0 -> accountNumber = inp.decodeNullableSerializableElement(
                            Status.descriptor,
                            i,
                            String.serializer().nullable
                        )
                        1 -> accounts = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            serializer<List<Account>>()
                        )
                        2 -> address = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        3 -> bankDetails = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            BankDetails.serializer()
                        )
                        4 -> channelId = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            Int.serializer().nullable
                        )
                        5 -> code = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        6 -> countryCodeId = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        7 -> countryCode = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            CountryCode.serializer()
                        )
                        8 -> createdBy = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        9 -> createdDate = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        10 -> emailAddress = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        11 -> id = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        12 -> mobileNumber = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        13 -> modifiedBy = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        14 -> modifiedDate = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        15 -> name = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        16 -> nickName = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        17 -> organizationId = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        18 -> instapayCode = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        19 -> swiftBankDetails = inp.decodeSerializableElement(
                            descriptor,
                            i,
                            SwiftBankDetails.serializer()
                        )

                        else -> throw SerializationException("Unknown index $i")
                    }
                }
                inp.endStructure(Status.descriptor)

                CreationBeneficiaryDto().apply {
                    this.accountNumber = accountNumber
                    this.accounts = accounts?.toMutableList() ?: mutableListOf()
                    this.address = address
                    this.bankDetails = bankDetails
                    this.code = code
                    this.countryCodeId = countryCodeId
                    this.countryCode = countryCode
                    this.createdBy = createdBy
                    this.createdDate = createdDate
                    this.emailAddress = emailAddress
                    this.id = id
                    this.mobileNumber = mobileNumber
                    this.modifiedBy = modifiedBy
                    this.modifiedDate = modifiedDate
                    this.channelId = channelId
                    this.name = name
                    this.nickname = nickName
                    this.organizationId = organizationId
                    this.instapayCode = instapayCode
                    this.swiftBankDetails = swiftBankDetails
                }
            } catch (e: Exception) {
                val value = decoder.decodeString()

                Timber.e("Decoded exception $value")

                CreationBeneficiaryDto().apply {
                    this.accountNumber = accountNumber
                    this.accounts = accounts?.toMutableList() ?: mutableListOf()
                    this.address = address
                    this.bankDetails = bankDetails
                    this.code = code
                    this.countryCodeId = countryCodeId
                    this.countryCode = countryCode
                    this.createdBy = createdBy
                    this.createdDate = createdDate
                    this.emailAddress = emailAddress
                    this.id = id
                    this.mobileNumber = mobileNumber
                    this.modifiedBy = modifiedBy
                    this.channelId = channelId
                    this.modifiedDate = modifiedDate
                    this.name = name
                    this.nickname = nickName
                    this.organizationId = organizationId
                    this.instapayCode = instapayCode
                    this.swiftBankDetails = swiftBankDetails
                }
            }
        }
    }
}

