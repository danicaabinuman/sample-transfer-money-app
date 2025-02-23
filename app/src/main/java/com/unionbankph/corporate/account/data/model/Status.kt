package com.unionbankph.corporate.account.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
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


@Parcelize
@Serializable
data class Status(

    @SerialName("type")
    val type: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("color")
    val color: String? = null
) : Parcelable {
    @Serializer(forClass = Status::class)
    companion object : KSerializer<Status> {

        override val descriptor: SerialDescriptor = buildClassSerialDescriptor(this.toString()) {
            element<String>("type", isOptional = true)
            element<String>("description", isOptional = true)
            element<String>("color", isOptional = true)
        }

        override fun serialize(encoder: Encoder, value: Status) {
            val compositeOutput: CompositeEncoder = encoder.beginStructure(descriptor)
            compositeOutput.encodeNullableSerializableElement(
                descriptor,
                0,
                String.serializer(),
                value.type
            )
            compositeOutput.encodeNullableSerializableElement(
                descriptor,
                1,
                String.serializer(),
                value.description
            )
            compositeOutput.encodeNullableSerializableElement(
                descriptor,
                2,
                String.serializer(),
                value.color
            )
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): Status {
            var type: String? = null
            var description: String? = null
            var color: String? = null
            return try {
                val inp: CompositeDecoder = decoder.beginStructure(descriptor)

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
                        2 -> color = inp.decodeNullableSerializableElement(
                            descriptor,
                            i,
                            String.serializer().nullable
                        )
                        else -> throw SerializationException("Unknown index $i")
                    }
                }
                inp.endStructure(descriptor)
                Status(type, description, color)
            } catch (e: Exception) {
                val value = decoder.decodeString()
                Status(value, value, color)
            }
        }
    }
}
