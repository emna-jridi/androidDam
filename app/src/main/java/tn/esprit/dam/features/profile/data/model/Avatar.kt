package tn.esprit.dam.features.profile.data.model

import kotlinx.serialization.Serializable
import tn.esprit.dam.core.network.Config

@Serializable
data class AvatarConfig(
    val avatarStyle: String = "Circle",
    val topType: String = "ShortHairShortFlat",
    val hairColor: String = "Black",
    val clotheType: String = "BlazerShirt",
    val clotheColor: String = "Gray01",
    val skinColor: String = "Light",
    val eyeType: String = "Default",
    val mouthType: String = "Smile",
    val facialHairType: String? = null,
    val facialHairColor: String? = null,
    val accessoriesType: String? = null,
    val eyebrowType: String? = null
)

@Serializable
data class Avatar(
    val userHash: String,
    val config: AvatarConfig,
    val fileName: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    /** The backend only sends the file name; the image is served under /uploads/avatars. */
    val url: String get() = Config.Endpoints.avatarUploadUrl(fileName)
}

@Serializable
data class UpdateAvatarDto(
    val avatarStyle: String? = null,
    val topType: String? = null,
    val hairColor: String? = null,
    val clotheType: String? = null,
    val clotheColor: String? = null,
    val skinColor: String? = null,
    val eyeType: String? = null,
    val mouthType: String? = null,
    val facialHairType: String? = null,
    val facialHairColor: String? = null,
    val accessoriesType: String? = null,
    val eyebrowType: String? = null
)

@Serializable
data class AvatarResponse(
    val success: Boolean = true,
    val avatar: Avatar
)
