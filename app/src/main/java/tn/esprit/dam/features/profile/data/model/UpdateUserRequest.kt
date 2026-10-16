package tn.esprit.dam.features.profile.data.model

import kotlinx.serialization.Serializable

/** Body of PATCH /users/me. */
@Serializable
data class UpdateUserRequest(val name: String? = null)
