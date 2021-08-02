package com.unionbankph.corporate.common.data.model

import kotlinx.serialization.*
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class ContextualClassStatus(

    @SerialName("type")
    val type: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("detailed_description")
    val detailedDescription: String? = null,

    @SerialName("remarks")
    val remarks: String? = null,

    @SerialName("contextual_class")
    val contextualClass: String? = null,

    @SerialName("title")
    val title: String? = null,

    @SerialName("message")
    val message: String? = null
) {
    @OptIn(ExperimentalSerializationApi::class)
    @Serializer(forClass = ContextualClassStatus::class)
    companion object : KSerializer<ContextualClassStatus> {

        override val descriptor: SerialDescriptor = buildClassSerialDescriptor(this.toString()) {
            element<String>("type", isOptional = true)
            element<String>("description", isOptional = true)
            element<String>("detailed_description", isOptional = true)
            element<String>("remarks", isOptional = true)
            element<String>("contextual_class", isOptional = true)
            element<String>("title", isOptional = true)
            element<String>("message", isOptional = true)
        }

        override fun serialize(encoder: Encoder, value: ContextualClassStatus) {
            val compositeOutput: CompositeEncoder = encoder.beginStructure(descriptor)
            encodeSerializable(0, compositeOutput, value.type)
            encodeSerializable(1, compositeOutput, value.description)
            encodeSerializable(2, compositeOutput, value.detailedDescription)
            encodeSerializable(3, compositeOutput, value.remarks)
            encodeSerializable(4, compositeOutput, value.contextualClass)
            encodeSerializable(5, compositeOutput, value.title)
            encodeSerializable(6, compositeOutput, value.message)
            compositeOutput.endStructure(descriptor)
        }

        private fun encodeSerializable(
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

        override fun deserialize(input: Decoder): ContextualClassStatus {
            var type: String? = null
            var description: String? = null
            var detailedDescription: String? = null
            var remarks: String? = null
            var contextualClass: String? = null
            var title: String? = null
            var message: String? = null
            return try {
                val inp: CompositeDecoder = input.beginStructure(descriptor)

                loop@ while (true) {
                    when (val i = inp.decodeElementIndex(descriptor)) {
                        CompositeDecoder.DECODE_DONE -> break@loop
                        0 -> type = inp.decodeNullableSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        1 -> description = inp.decodeNullableSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        2 -> detailedDescription = inp.decodeNullableSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        3 -> remarks = inp.decodeNullableSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        4 -> contextualClass = inp.decodeNullableSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        5 -> title = inp.decodeNullableSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        6 -> message = inp.decodeNullableSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        else -> throw SerializationException("Unknown index $i")
                    }
                }
                inp.endStructure(descriptor)
                ContextualClassStatus(
                    type,
                    description,
                    detailedDescription,
                    remarks,
                    contextualClass,
                    title,
                    message
                )
            } catch (e: Exception) {
                val value = input.decodeString()
                ContextualClassStatus(value, value, null)
            }
        }
    }
}
