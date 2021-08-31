package com.unionbankph.corporate.fund_transfer.data.form

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.*
import timber.log.Timber

@Parcelize
@Serializable
data class BeneficiaryForm(

    @SerialName("name")
    var name: String? = null,

    @SerialName("nickname")
    var nickName: String? = null,

    @SerialName("address")
    var address: String? = null,

    @SerialName("email_address")
    var emailAddress: String? = null,

    @SerialName("code")
    var code: String? = null,

    @SerialName("swift_code")
    var swiftCode: String? = null,

    @SerialName("instapay_code")
    var instapayCode: String? = null,

    @SerialName("bank_name")
    var bankName: String? = null,

    @SerialName("account_number")
    var accountNumber: String? = null,

    @SerialName("bank_address")
    var bankAddress: String? = null,

    @SerialName("channel_id")
    var channelId: Int? = null,

    @SerialName("country_code_id")
    var countryCodeId: Int? = null,

    @SerialName("mobile_number")
    var mobileNumber: String? = null,

    @SerialName("organization_id")
    var organizationId: String? = null,

    @SerialName("brstn_code")
    var brstnCode: String? = null,

    @SerialName("firm_code")
    var firmCode: String? = null,

    @SerialName("all_accounts_selected")
    var allAccountsSelected: Boolean? = null,

    @SerialName("account_ids")
    var accountIds: MutableList<String>? = null,

    @SerialName("excluded_account_ids")
    var excludedAccountIds: MutableList<Int> = mutableListOf()
)

    : Parcelable {

    @ExperimentalSerializationApi
    @Serializer(forClass = BeneficiaryForm::class)
    companion object : KSerializer<BeneficiaryForm> {


        override val descriptor: SerialDescriptor = buildClassSerialDescriptor(this.toString()) {

            element<String>("name")
            element<String>("nickname")
            element<String>("address")
            element<String>("email_address", isOptional = true)
            element<String>("code", isOptional = true)
            element<String>("swift_code", isOptional = true)
            element<String>("instapay_code", isOptional = true)
            element<String>("bank_name", isOptional = true)
            element<String>("account_number")
            element<String>("bank_address", isOptional = true)
            element<Int>("channel_id")
            element<Int?>("country_code_id", isOptional = true)
            element<String>("mobile_number", isOptional = true)
            element<String>("organization_id", isOptional = true)
            element<String>("brstn_code", isOptional = true)
            element<String>("firm_code", isOptional = true)
            element<Boolean>("all_accounts_selected", isOptional = true)
            element<MutableList<String>>("account_ids", isOptional = true)
            element<MutableList<Int>>("excluded_account_ids", isOptional = true)
        }

//        {
//            "name": "autben1473",
//            "address": "",
//            "code": "autben1473",
//            "bank_name": "UnionBank of the Philippines",
//            "account_number": "000770008690",
//            "channel_id": 3,
//            "country_code_id": 175,
//            "all_accounts_selected": false,
//            "account_ids": [
//            "4838213",
//            "4838211",
//            "4838212"
//            ]
//        }

        @ExperimentalSerializationApi
//        override fun serialize(encoder: Encoder, value: BeneficiaryForm) {
//            val compositeOutput: CompositeEncoder = encoder.beginStructure(descriptor)
////            compositeOutput.encodeNullableSerializableElement(
////                descriptor,
////                0,
////                String.serializer(),
////                value.organizationId
////            )
//            compositeOutput.encodeSerializableElement(
//                descriptor,
//                1,
//                String.serializer(),
//                value.name!!
//            )
////            value.nickName?.let {
////                compositeOutput.encodeSerializableElement(
////                    descriptor,
////                    2,
////                    String.serializer(),
////                    it
////                )
////            }
//            value.address?.let {
//                compositeOutput.encodeSerializableElement(
//                    descriptor,
//                    3,
//                    String.serializer(),
//                    it
//                )
//            }
//            value.code?.let {
//                compositeOutput.encodeSerializableElement(
//                    descriptor,
//                    4,
//                    String.serializer(),
//                    it
//                )
//            }
////            value.swiftCode?.let {
////                compositeOutput.encodeSerializableElement(
////                    descriptor,
////                    5,
////                    String.serializer(),
////                    it
////                )
////            }
////            value.instapayCode?.let {
////                compositeOutput.encodeSerializableElement(
////                    descriptor,
////                    6,
////                    String.serializer(),
////                    it
////                )
////            }
//            value.bankName?.let {
//                compositeOutput.encodeSerializableElement(
//                    descriptor,
//                    7,
//                    String.serializer(),
//                    it
//                )
//            }
//            value.accountNumber?.let {
//                compositeOutput.encodeSerializableElement(
//                    descriptor,
//                    8,
//                    String.serializer(),
//                    it
//                )
//            }
////            value.bankAddress?.let {
////                compositeOutput.encodeSerializableElement(
////                    descriptor,
////                    9,
////                    String.serializer(),
////                    it
////                )
////            }
//            value.channelId?.let {
//                compositeOutput.encodeSerializableElement(
//                    descriptor,
//                    10,
//                    Int.serializer(),
//                    it
//                )
//            }
////            value.countryCodeId?.let {
////                compositeOutput.encodeSerializableElement(
////                    descriptor,
////                    11,
////                    Int.serializer(),
////                    it
////                )
////            }
////            value.mobileNumber?.let {
////                compositeOutput.encodeSerializableElement(
////                    descriptor,
////                    12,
////                    String.serializer(),
////                    it
////                )
////            }
////            value.brstnCode?.let {
////                compositeOutput.encodeSerializableElement(
////                    descriptor,
////                    13,
////                    String.serializer(),
////                    it
////                )
////            }
////            value.firmCode?.let {
////                compositeOutput.encodeSerializableElement(
////                    descriptor,
////                    14,
////                    String.serializer(),
////                    it
////                )
////            }
////            value.allAccountsSelected?.let {
////                compositeOutput.encodeSerializableElement(
////                    descriptor,
////                    15,
////                    Boolean.serializer(),
////                    it
////                )
////            }
//
//
//            compositeOutput.endStructure(descriptor)
//        }


        override fun deserialize(decoder: Decoder): BeneficiaryForm {
            decoder.decodeStructure(descriptor) {
                var name: String? = null
                var nickName: String? = null
                var address: String? = null
                var emailAddress: String? = null
                var code: String? = null
                var swiftCode: String? = null
                var instapayCode: String? = null
                var bankName: String? = null
                var accountNumber: String? = null
                var bankAddress: String? = null
                var channelId: Int? = null
                var countryCodeId: Int? = null
                var mobileNumber: String? = null
                var organizationId: String? = null
                var brstnCode: String? = null
                var firmCode: String? = null
                var allAccountsSelected: Boolean? = null
                var accountIds: List<String>? = null
                var excludedAccountIds: List<Int> = mutableListOf()


                return try {
//                    val inp: CompositeDecoder = decoder.beginStructure(descriptor)

                    loop@ while (true) {
                        when (val i = decodeElementIndex(descriptor)) {
                            CompositeDecoder.DECODE_DONE -> break@loop

                            0 -> name = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                String.serializer().nullable
                            )
                            1 -> nickName = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                String.serializer().nullable
                            )
                            2 -> address = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                String.serializer().nullable
                            )
                            3 -> emailAddress = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                String.serializer().nullable
                            )
                            4 -> code = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                String.serializer().nullable
                            )
                            5 -> swiftCode = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                String.serializer().nullable
                            )
                            6 -> instapayCode = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                String.serializer().nullable
                            )
                            7 -> bankName = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                String.serializer().nullable
                            )
                            8 -> accountNumber = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                String.serializer().nullable
                            )
                            9 -> bankAddress = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                String.serializer().nullable
                            )
                            10 -> channelId = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                Int.serializer().nullable
                            )
                            11 -> countryCodeId = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                Int.serializer().nullable,
                                null
                            )
                            12 -> mobileNumber = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                String.serializer().nullable
                            )
                            13 -> organizationId = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                String.serializer().nullable
                            )
                            14 -> brstnCode = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                String.serializer().nullable
                            )
                            15 -> firmCode = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                String.serializer().nullable
                            )
                            16 -> allAccountsSelected = decodeNullableSerializableElement(
                                descriptor,
                                i,
                                Boolean.serializer().nullable
                            )
                            17 -> accountIds = decodeSerializableElement(
                                descriptor,
                                i,
                                serializer<List<String>>()
                            )
                            18 -> excludedAccountIds = decodeSerializableElement(
                                descriptor,
                                i,
                                serializer<List<Int>>()
                            )

                            else -> throw SerializationException("Unknown index $i")
                        }
                    }
//                    endStructure(descriptor)

                    Timber.e("coountryy " + countryCodeId)

                    BeneficiaryForm().apply {
                        this.organizationId = organizationId
                        this.name = name
                        this.nickName = nickName
                        this.address = address
                        this.emailAddress = emailAddress
                        this.code = code
                        this.swiftCode = swiftCode
                        this.instapayCode = instapayCode
                        this.bankName = bankName
                        this.accountNumber = accountNumber
                        this.bankAddress = bankAddress
                        this.channelId = channelId
                        countryCodeId?.let {
                            this.countryCodeId = countryCodeId
                        }
                        this.mobileNumber = mobileNumber
                        this.brstnCode = brstnCode
                        this.firmCode = firmCode
                        this.allAccountsSelected = allAccountsSelected
                        this.accountIds = accountIds?.toMutableList()
                        this.excludedAccountIds = excludedAccountIds.toMutableList()
                    }
                } catch (e: Exception) {
                    val value = decoder.decodeString()
                    Timber.e("Exception Decoded Value : $value")
                    BeneficiaryForm().apply {
                        this.organizationId = organizationId
                        this.name = name
                        this.nickName = nickName
                        this.address = address
                        this.address = emailAddress
                        this.code = code
                        this.swiftCode = swiftCode
                        this.instapayCode = instapayCode
                        this.bankName = bankName
                        this.accountNumber = accountNumber
                        this.bankAddress = bankAddress
                        this.channelId = channelId
                        this.countryCodeId = countryCodeId
                        this.mobileNumber = mobileNumber
                        this.brstnCode = brstnCode
                        this.firmCode = firmCode
                        this.allAccountsSelected = allAccountsSelected
                        this.accountIds = accountIds?.toMutableList()
                        this.excludedAccountIds = excludedAccountIds.toMutableList()
                    }
                }
            }
        }
    }

//        override fun deserialize(decoder: Decoder): BeneficiaryForm {
//
//            var name: String? = null
//            var nickName: String? = null
//            var address: String? = null
//            var emailAddress: String? = null
//            var code: String? = null
//            var swiftCode: String? = null
//            var instapayCode: String? = null
//            var bankName: String? = null
//            var accountNumber: String? = null
//            var bankAddress: String? = null
//            var channelId: Int? = null
//            var countryCodeId: Int? = null
//            var mobileNumber: String? = null
//            var organizationId: String? = null
//            var brstnCode: String? = null
//            var firmCode: String? = null
//            var allAccountsSelected: Boolean? = null
//            var accountIds: List<String>? = null
//            var excludedAccountIds: List<Int> = mutableListOf()
//
//            return try {
//                val inp: CompositeDecoder = decoder.beginStructure(descriptor)
//
//                loop@ while (true) {
//                    when (val i = inp.decodeElementIndex(descriptor)) {
//                        CompositeDecoder.DECODE_DONE -> break@loop
//
//                        0 -> name = inp.decodeNullableSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        1 -> nickName = inp.decodeNullableSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        2 -> address = inp.decodeNullableSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        3 -> emailAddress = inp.decodeNullableSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        4 -> code = inp.decodeNullableSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        5 -> swiftCode = inp.decodeNullableSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        6 -> instapayCode = inp.decodeNullableSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        7 -> bankName = inp.decodeNullableSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        8 -> accountNumber = inp.decodeNullableSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        9 -> bankAddress = inp.decodeNullableSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        10 -> channelId = inp.decodeNullableSerializableElement(
//                            descriptor,
//                            i,
//                            Int.serializer().nullable
//                        )
//                        11 -> countryCodeId = inp.decodeIntElement(
//                            descriptor,
//                            i
//                        )
//                        12 -> mobileNumber = inp.decodeNullableSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        13 -> organizationId = inp.decodeNullableSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        14 -> brstnCode = inp.decodeNullableSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        15 -> firmCode = inp.decodeNullableSerializableElement(
//                            descriptor,
//                            i,
//                            String.serializer().nullable
//                        )
//                        16 -> allAccountsSelected = inp.decodeNullableSerializableElement(
//                            descriptor,
//                            i,
//                            Boolean.serializer().nullable
//                        )
//                        17 -> accountIds = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            serializer<List<String>>()
//                        )
//                        18 -> excludedAccountIds = inp.decodeSerializableElement(
//                            descriptor,
//                            i,
//                            serializer<List<Int>>()
//                        )
//
//                        else -> throw SerializationException("Unknown index $i")
//                    }
//                }
//                inp.endStructure(descriptor)
//
//                BeneficiaryForm().apply {
//                    this.organizationId = organizationId
//                    this.name = name
//                    this.nickName = nickName
//                    this.address = address
//                    this.emailAddress = emailAddress
//                    this.code = code
//                    this.swiftCode = swiftCode
//                    this.instapayCode = instapayCode
//                    this.bankName = bankName
//                    this.accountNumber = accountNumber
//                    this.bankAddress = bankAddress
//                    this.channelId = channelId
//                    this.countryCodeId = countryCodeId
//                    this.mobileNumber = mobileNumber
//                    this.brstnCode = brstnCode
//                    this.firmCode = firmCode
//                    this.allAccountsSelected = allAccountsSelected
//                    this.accountIds = accountIds?.toMutableList()
//                    this.excludedAccountIds = excludedAccountIds.toMutableList()
//                }
//            } catch (e: Exception) {
//                val value = decoder.decodeString()
//                Timber.e("Exception Decoded Value : $value")
//                BeneficiaryForm().apply {
//                    this.organizationId = organizationId
//                    this.name = name
//                    this.nickName = nickName
//                    this.address = address
//                    this.address = emailAddress
//                    this.code = code
//                    this.swiftCode = swiftCode
//                    this.instapayCode = instapayCode
//                    this.bankName = bankName
//                    this.accountNumber = accountNumber
//                    this.bankAddress = bankAddress
//                    this.channelId = channelId
//                    this.countryCodeId = countryCodeId
//                    this.mobileNumber = mobileNumber
//                    this.brstnCode = brstnCode
//                    this.firmCode = firmCode
//                    this.allAccountsSelected = allAccountsSelected
//                    this.accountIds = accountIds?.toMutableList()
//                    this.excludedAccountIds = excludedAccountIds.toMutableList()
//                }
//            }
//        }
//    }
}
