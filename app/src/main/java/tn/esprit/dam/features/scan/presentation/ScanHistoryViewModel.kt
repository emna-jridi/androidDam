package tn.esprit.dam.features.scan.presentation

import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.data.api.models.ApiResult
import tn.esprit.dam.data.api.models.ScanHistoryItemDto
import tn.esprit.dam.data.repository.ScanRepository
import javax.inject.Inject

data class ScanHistoryUiState(
    val isLoading: Boolean = false,
    val scans: List<ScanHistoryItemDto> = emptyList(),
    val error: String? = null,
    val total: Int = 0,
    val hasMore: Boolean = true,
    val currentPage: Int = 0
)

@HiltViewModel
class ScanHistoryViewModel @Inject constructor(
    private val repository: ScanRepository,
    @ApplicationContext private val context: android.content.Context
) : ViewModel() {

    companion object {
        private const val TAG = "ScanHistoryViewModel"
        private const val PAGE_SIZE = 10
    }

    private val _uiState = MutableStateFlow(ScanHistoryUiState())
    val uiState: StateFlow<ScanHistoryUiState> = _uiState.asStateFlow()

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            val user = TokenManager.getUser(context)
            val token = TokenManager.getAccessToken(context)
            val userId = user?.id ?: token?.let { decodeUserIdFromToken(it) }

            if (userId == null) {
                Log.e(TAG, "âŒ No user ID found - user not logged in")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Utilisateur non connecté"
                )
                return@launch
            }

            Log.d(TAG, "📋 Loading scan history for user: $userId (page ${_uiState.value.currentPage})")
            val offset = _uiState.value.currentPage * PAGE_SIZE
            val result = repository.getScanHistory(
                userId = userId,
                limit = PAGE_SIZE,
                offset = offset
            )

            when (result) {
                is ApiResult.Success -> {
                    val historyData = result.data
                    Log.d(TAG, "âœ… History loaded: ${historyData.scans.size} scans, total: ${historyData.total}")

                    val merged = if (_uiState.value.currentPage == 0) {
                        historyData.scans
                    } else {
                        _uiState.value.scans + historyData.scans
                    }

                    val sorted = merged
                        .distinctBy { it.scanId }
                        .sortedByDescending { parseDateToMillis(it.createdAt) }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        scans = sorted,
                        total = historyData.total,
                        hasMore = historyData.scans.size == PAGE_SIZE,
                        error = null
                    )
                }
                is ApiResult.ApiError -> {
                    Log.e(TAG, "âŒ API error loading history: ${result.message} (code: ${result.code})")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erreur API: ${result.message}"
                    )
                }
                is ApiResult.NetworkError -> {
                    Log.e(TAG, "âŒ Network error loading history", result.exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erreur réseau: ${result.exception.message ?: "vérifiez votre connexion"}"
                    )
                }
                is ApiResult.SerializationError -> {
                    Log.e(TAG, "âŒ Serialization error loading history", result.exception)
                    Log.e(TAG, "Raw response: ${result.rawResponse?.take(500)}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erreur de données: ${result.exception.message}"
                    )
                }
            }
        }
    }

    fun loadMore() {
        if (!_uiState.value.isLoading && _uiState.value.hasMore) {
            _uiState.value = _uiState.value.copy(
                currentPage = _uiState.value.currentPage + 1
            )
            loadHistory()
        }
    }

    fun refresh() {
        _uiState.value = ScanHistoryUiState()
        loadHistory()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun decodeUserIdFromToken(token: String): String? {
        val parts = token.split(".")
        if (parts.size < 2) return null

        return runCatching {
            val payload = String(
                Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            )
            val json = Json { ignoreUnknownKeys = true }
            val element = json.decodeFromString<JsonElement>(payload)
            element.jsonObject["sub"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()
    }

    private fun parseDateToMillis(value: String): Long {
        return runCatching {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            inputFormat.parse(value)?.time ?: 0L
        }.getOrDefault(0L)
    }
}
