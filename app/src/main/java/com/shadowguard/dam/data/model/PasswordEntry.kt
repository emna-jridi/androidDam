package com.shadowguard.dam.data.model

import kotlinx.serialization.Serializable

/**
 * Password entry models for vault
 */
@Serializable
data class PasswordEntry(
    val id: String,
    val site: String,
    val username: String,
    val encryptedPassword: String,
    val encryptedNotes: String? = null,
    val url: String? = null,
    val category: String = "other",
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val strengthScore: Int? = null,
    val strengthLevel: String? = null,
    val estimatedCrackTime: String? = null,
    val strengthIssues: List<String>? = null,
    val aiRecommendations: List<String>? = null,
    val lastPasswordChange: String,
    val lastAccessed: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreatePasswordEntryRequest(
    val site: String,
    val username: String,
    val encryptedPassword: String,
    val encryptedNotes: String? = null,
    val url: String? = null,
    val category: String = "other",
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false
)

@Serializable
data class CreatePasswordEntryResponse(
    val message: String,
    val entry: PasswordEntry
)

@Serializable
data class UpdatePasswordEntryRequest(
    val site: String? = null,
    val username: String? = null,
    val encryptedPassword: String? = null,
    val encryptedNotes: String? = null,
    val url: String? = null,
    val category: String? = null,
    val tags: List<String>? = null,
    val isFavorite: Boolean? = null
)

@Serializable
data class PasswordEntriesResponse(
    val count: Int,
    val entries: List<PasswordEntry>
)

@Serializable
data class PasswordEntryResponse(
    val entry: PasswordEntry
)

@Serializable
data class AnalyzePasswordRequest(
    val password: String
)

@Serializable
data class PasswordStrengthResponse(
    val score: Int,
    val level: String,
    val crackTime: String,
    val issues: List<String>,
    val recommendations: List<String>
)

// Categories enum
object PasswordCategory {
    const val SOCIAL = "social"
    const val EMAIL = "email"
    const val BANKING = "banking"
    const val WORK = "work"
    const val SHOPPING = "shopping"
    const val ENTERTAINMENT = "entertainment"
    const val OTHER = "other"
    
    val ALL = listOf(SOCIAL, EMAIL, BANKING, WORK, SHOPPING, ENTERTAINMENT, OTHER)
}
