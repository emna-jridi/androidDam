package tn.esprit.dam.core.network

import tn.esprit.dam.BuildConfig

object Config {
    const val BASE_URL: String = BuildConfig.BASE_ROOT
    const val API_ROOT: String = BuildConfig.API_ROOT
    const val ROOT_URL: String = BuildConfig.BASE_ROOT
    const val API_URL: String = BuildConfig.BASE_ROOT + "/api"

    object Endpoints {
        val AUTH: String get() = "$API_URL/auth"
        val USERS: String get() = "$API_URL/users"
        val SCAN: String get() = "$API_URL/scan"
        val VAULT: String get() = "$API_URL/vault"
        val PASSWORDS: String get() = "$API_URL/vault/passwords"
        val DARKWEB: String get() = "$API_URL/darkweb"
        val ALERTS: String get() = "$API_URL/alerts"
        val AVATAR: String get() = "$API_URL/v1/avatar"
        val REPORT: String get() = "$API_URL/report"
        val AVATAR_UPLOADS: String get() = "$BASE_URL/uploads/avatars"

        fun avatarUploadUrl(fileName: String): String = "$AVATAR_UPLOADS/$fileName"
        fun reportUrl(packageName: String): String = "$REPORT/$packageName"
    }

    object Timeouts {
        const val CONNECT_TIMEOUT_MS: Long = 30_000L
        const val SOCKET_TIMEOUT_MS: Long = 30_000L
        const val REQUEST_TIMEOUT_MS: Long = 30_000L
        const val LONG_REQUEST_TIMEOUT_MS: Long = 300_000L
    }

    object AI {
        const val DEFAULT_OLLAMA_HOST: String = "http://localhost:11434"
        const val DEFAULT_OLLAMA_MODEL: String = "llama3"
    }
}
