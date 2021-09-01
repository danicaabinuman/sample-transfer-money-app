package com.unionbankph.corporate.fund_transfer.data.model

import android.os.Parcel
import android.os.Parcelable
import com.unionbankph.corporate.account.data.model.Account
import com.unionbankph.corporate.account.data.model.Status
import com.unionbankph.corporate.auth.data.model.CountryCode
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

//@Parcelize
@Serializable
data class Beneficiary(

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

    @SerialName("country_code_id")
    var countryCodeId: String? = null,

    @SerialName("brstn_code")
    var brstnCode: String? = null,

    @SerialName("firm_code")
    var firmCode: String? = null,

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
)

//    : Parcelable {
//
//    @ExperimentalSerializationApi
//    @Serializer(forClass = Beneficiary::class)
//    companion object : KSerializer<Beneficiary> {
//
//        override val descriptor: SerialDescriptor = buildClassSerialDescriptor(this.toString()){
//            element<String?>("account_number", isOptional = true)
//            element<String?>("address", isOptional = true)
//            element<BankDetails?>("bank_details", isOptional = true)
//            element<Int?>("channel_id", isOptional = true)
//            element<String?>("code", isOptional = true)
//            element<String?>("country_code_id", isOptional = true)
//            element<String?>("brstn_code", isOptional = true)
//            element<String>("firm_code", isOptional = true)
//            element<CountryCode?>("country_code", isOptional = true)
//            element<String?>("created_by", isOptional = true)
//            element<String?>("created_date", isOptional = true)
//            element<String?>("email_address", isOptional = true)
//            element<Int?>("id", isOptional = true)
//            element<String?>("mobile_number", isOptional = true)
//            element<String?>("modified_by", isOptional = true)
//            element<String?>("modified_date", isOptional = true)
//            element<String?>("name", isOptional = true)
//            element<String?>("nickname", isOptional = true)
//            element<String?>("organization_id", isOptional = true)
//            element<String?>("instapay_code", isOptional = true)
//            element<List<Account>?>("accounts", isOptional = true)
//            element<SwiftBankDetails?>("swift_bank_details", isOptional = true)
//        }
//
//        override fun deserialize(decoder: Decoder): Beneficiary {
//            var accountNumber: String? = null
//            var address: String? = null
//            var bankDetails: BankDetails? = null
//            var channelId: Int? = null
//            var code: String? = null
//            var countryCodeId: String? = null
//            var brstnCode: String? = null
//            var firmCode: String? = null
//            var countryCode: CountryCode? = null
//            var createdBy: String? = null
//            var createdDate: String? = null
//            var emailAddress: String? = null
//            var id: Int? = null
//            var mobileNumber: String? = null
//            var modifiedBy: String? = null
//            var modifiedDate: String? = null
//            var name: String? = null
//            var nickName: String? = null
//            var organizationId: String? = null
//            var instapayCode: String? = null
//            var accounts: List<Account>? = null
//            var swiftBankDetails: SwiftBankDetails? = null
//
//            return try {
//                val inp: CompositeDecoder = decoder.beginStructure(Status.descriptor)
//
//                loop@ while (true) {
//                    when (val i = inp.decodeElementIndex(Status.descriptor)) {
//                        CompositeDecoder.DECODE_DONE -> break@loop
//                        0 -> accountNumber = inp.decodeNullableSerializableElement(
//                            Status.descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        1 -> address = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        2 -> bankDetails = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            BankDetails.serializer()
//                        )
//                        3 -> channelId = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            Int.serializer().nullable
//                        )
//                        4 -> code = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        5 -> countryCodeId = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        6 -> brstnCode = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        7 -> firmCode = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        8 -> countryCode = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            CountryCode.serializer()
//                        )
//                        9 -> createdBy = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        10 -> createdDate = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        11 -> emailAddress = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        12 -> id = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            Int.serializer().nullable
//                        )
//                        13 -> mobileNumber = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        14 -> modifiedBy = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        15 -> modifiedDate = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        16 -> name = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        17 -> nickName = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        18 -> organizationId = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        19 -> instapayCode = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        20 -> accounts = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            serializer<List<Account>>()
//                        )
//                        21 -> swiftBankDetails = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            SwiftBankDetails.serializer()
//                        )
//                        else -> throw SerializationException("Unknown index $i")
//                    }
//                }
//                inp.endStructure(Status.descriptor)
//
//                Beneficiary().apply {
//                    this.accountNumber = accountNumber
//                    this.address = address
//                    this.bankDetails = bankDetails
//                    this.code = code
//                    this.countryCodeId = countryCodeId
//                    this.brstnCode = brstnCode
//                    this.firmCode = firmCode
//                    this.countryCode = countryCode
//                    this.createdBy = createdBy
//                    this.createdDate = createdDate
//                    this.emailAddress = emailAddress
//                    this.id = id
//                    this.mobileNumber = mobileNumber
//                    this.modifiedBy = modifiedBy
//                    this.modifiedDate = modifiedDate
//                    this.channelId = channelId
//                    this.name = name
//                    this.nickname = nickName
//                    this.organizationId = organizationId
//                    this.instapayCode = instapayCode
//                    this.accounts = accounts?.toMutableList() ?: mutableListOf()
//                    this.swiftBankDetails = swiftBankDetails
//                }
//            } catch (e: Exception) {
//                val value = decoder.decodeString()
//
//                Timber.e("Decoded exception $value")
//
//                Beneficiary().apply {
//                    this.accountNumber = accountNumber
//                    this.address = address
//                    this.bankDetails = bankDetails
//                    this.code = code
//                    this.countryCodeId = countryCodeId
//                    this.brstnCode = brstnCode
//                    this.firmCode = firmCode
//                    this.countryCode = countryCode
//                    this.createdBy = createdBy
//                    this.createdDate = createdDate
//                    this.emailAddress = emailAddress
//                    this.id = id
//                    this.mobileNumber = mobileNumber
//                    this.modifiedBy = modifiedBy
//                    this.channelId = channelId
//                    this.modifiedDate = modifiedDate
//                    this.name = name
//                    this.nickname = nickName
//                    this.organizationId = organizationId
//                    this.instapayCode = instapayCode
//                    this.accounts = accounts?.toMutableList() ?: mutableListOf()
//                    this.swiftBankDetails = swiftBankDetails
//                }
//            }
//        }
//    }
//}
