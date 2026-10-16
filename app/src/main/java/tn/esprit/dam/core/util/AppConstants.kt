package tn.esprit.dam.core.util

object AppConstants {
    object UI {
        const val ANIMATION_SHORT_MS = 300L
        const val ANIMATION_MEDIUM_MS = 500L
        const val ANIMATION_LONG_MS = 800L

        const val SEARCH_DEBOUNCE_MS = 500L
        const val INPUT_DEBOUNCE_MS = 300L

        const val POLLING_INTERVAL_MS = 3000L
        const val POLLING_MAX_DURATION_MS = 300000L

        const val PAGE_SIZE = 20
        const val INITIAL_PAGE = 0

        const val NETWORK_TIMEOUT_SECONDS = 30
        const val OPERATION_TIMEOUT_MS = 60000L
    }

    object Validation {
        const val EMAIL_MIN_LENGTH = 3
        const val EMAIL_MAX_LENGTH = 254

        const val PASSWORD_MIN_LENGTH = 6
        const val PASSWORD_MAX_LENGTH = 128
        const val PASSWORD_REQUIRES_UPPERCASE = true
        const val PASSWORD_REQUIRES_NUMBER = false
        const val PASSWORD_REQUIRES_SPECIAL = false

        const val NAME_MIN_LENGTH = 2
        const val NAME_MAX_LENGTH = 50

        const val USERNAME_MIN_LENGTH = 3
        const val USERNAME_MAX_LENGTH = 20

        const val OTP_LENGTH = 6
        const val OTP_RESEND_DELAY_SECONDS = 60
    }

    object ErrorMessages {
        const val ERROR_NO_INTERNET = "No internet connection"
        const val ERROR_NETWORK_TIMEOUT = "Network timeout"
        const val ERROR_SERVER_ERROR = "Server error"
        const val ERROR_REQUEST_FAILED = "Request failed"

        const val ERROR_UNAUTHORIZED = "Incorrect email or password"
        const val ERROR_TOKEN_EXPIRED = "Your session has expired"
        const val ERROR_INVALID_TOKEN = "Invalid token"
        const val ERROR_NOT_AUTHENTICATED = "You must log in"

        const val ERROR_EMAIL_REQUIRED = "Email required"
        const val ERROR_EMAIL_INVALID = "Invalid email"
        const val ERROR_PASSWORD_REQUIRED = "Password required"
        const val ERROR_PASSWORD_TOO_SHORT = "Minimum ${Validation.PASSWORD_MIN_LENGTH} characters"
        const val ERROR_NAME_REQUIRED = "Name required"
        const val ERROR_NAME_TOO_SHORT = "Minimum ${Validation.NAME_MIN_LENGTH} characters"

        const val ERROR_SCAN_FAILED = "Analysis error"
        const val ERROR_NO_APPS = "No applications found"
        const val ERROR_ANALYSIS_FAILED = "Risk analysis error"

        const val ERROR_UNKNOWN = "An unknown error occurred"
        const val ERROR_OPERATION_CANCELLED = "Operation cancelled"
    }

    object Risk {
        const val SCORE_CRITICAL = 40f
        const val SCORE_MEDIUM = 70f

        const val LEVEL_CRITICAL = "Critical"
        const val LEVEL_HIGH = "High"
        const val LEVEL_MEDIUM = "Medium"
        const val LEVEL_LOW = "Low"
        const val LEVEL_SAFE = "Safe"

        const val COLOR_CRITICAL = "#FF6B6B"
        const val COLOR_HIGH = "#FF8C42"
        const val COLOR_MEDIUM = "#FFA500"
        const val COLOR_LOW = "#FFD93D"
        const val COLOR_SAFE = "#4CAF50"
    }

    object Storage {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_DATA = "user_data"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_USER_ID = "user_id"
        const val KEY_DEVICE_ID = "device_id"

        const val KEY_CACHE_APPS = "cache_apps"
        const val KEY_CACHE_SCAN_RESULTS = "cache_scan_results"
        const val KEY_CACHE_TIMESTAMP = "cache_timestamp"

        const val KEY_THEME = "theme"
        const val KEY_LANGUAGE = "language"
        const val KEY_NOTIFICATIONS = "notifications_enabled"
    }

    object Features {
        const val ENABLE_DARK_MODE = true
        const val ENABLE_PUSH_NOTIFICATIONS = true
        const val ENABLE_ANALYTICS = true
        const val ENABLE_CRASH_REPORTING = true
        const val ENABLE_ML_DETECTION = true
    }

    object Logging {
        const val TAG_AUTH = "LoginViewModel"
        const val TAG_SCAN = "ScanViewModel"
        const val TAG_NETWORK = "NetworkClient"
        const val TAG_SECURITY = "SecurityManager"
        const val TAG_STORAGE = "StorageManager"
    }
}
