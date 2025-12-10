package tn.esprit.dam.screens.scan

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import tn.esprit.dam.data.model.ScanApkResponse
import tn.esprit.dam.data.model.ScanResult
import tn.esprit.dam.data.repository.ScanRepository
import javax.inject.Inject

data class ScanApkUiState(
    val isLoading: Boolean = false,
    val result: ScanApkResponse? = null,
    val error: String? = null
)

@HiltViewModel
class ScanApkViewModel @Inject constructor(
    private val repository: ScanRepository
) : ViewModel() {

    private val _uiState = androidx.compose.runtime.mutableStateOf(ScanApkUiState())
    val uiState = _uiState

    fun handleApkUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = ScanApkUiState(isLoading = true)

                // 1️⃣ Read APK bytes
                val fileBytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    ?: throw Exception("Impossible de lire le fichier APK")

                // 2️⃣ Get file name
                val fileName = getFileName(context, uri) ?: "unknown.apk"

                // 3️⃣ Call repository
                val result = repository.scanApk(fileBytes, fileName)

                _uiState.value = when (result) {
                    is ScanResult.Loading<*> -> {
                        ScanApkUiState(isLoading = true)
                    }
                    is ScanResult.Success<*> -> {
                        val response = result.data as ScanApkResponse
                        ScanApkUiState(
                            isLoading = false,
                            result = response
                        )
                    }
                    is ScanResult.Failure<*> -> {
                        val error = result.exception
                        ScanApkUiState(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.value = ScanApkUiState(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) return it.getString(index)
        }
        return null
    }
}
