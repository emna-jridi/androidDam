package tn.esprit.dam.core.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class ApiError(
    @Serializable(with = FlexibleMessageSerializer::class)
    val message: String? = null,
    val error: String? = null,
    val statusCode: Int? = null,
)

object FlexibleMessageSerializer : KSerializer<String?> {
    override val descriptor = PrimitiveSerialDescriptor("FlexibleMessage", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String?) {
        if (value != null) encoder.encodeString(value) else encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): String? {
        return try {
            decoder.decodeString()
        } catch (_: Exception) {
            null
        }
    }
}
