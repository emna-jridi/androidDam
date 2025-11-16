package tn.esprit.dam.data.model


import kotlinx.serialization.Serializable

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
    val url: String,
    val fileName: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CreateAvatarDto(
    val userHash: String,
    val avatarStyle: String = "Circle",
    val topType: String,
    val hairColor: String,
    val clotheType: String,
    val clotheColor: String,
    val skinColor: String,
    val eyeType: String = "Default",
    val mouthType: String = "Smile",
    val facialHairType: String? = null,
    val facialHairColor: String? = null,
    val accessoriesType: String? = null,
    val eyebrowType: String? = null
)

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