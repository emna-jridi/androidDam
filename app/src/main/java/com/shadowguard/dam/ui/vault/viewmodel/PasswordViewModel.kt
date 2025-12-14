package com.shadowguard.dam.ui.vault.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowguard.dam.data.model.PasswordEntry
import com.shadowguard.dam.data.model.PasswordStrengthResponse
import com.shadowguard.dam.data.repository.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PasswordListUiState {
    object Loading : PasswordListUiState()
    data class Success(val passwords: List<PasswordEntry>) : PasswordListUiState()
    data class Error(val message: String) : PasswordListUiState()
}

data class PasswordDetailData(
    val entry: PasswordEntry,
    val decryptedPassword: String,
    val decryptedNotes: String?
)

class PasswordViewModel(private val repository: VaultRepository) : ViewModel() {

    private val _listState = MutableStateFlow<PasswordListUiState>(PasswordListUiState.Loading)
    val listState: StateFlow<PasswordListUiState> = _listState.asStateFlow()

    private val _selectedPassword = MutableStateFlow<PasswordDetailData?>(null)
    val selectedPassword: StateFlow<PasswordDetailData?> = _selectedPassword.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _generatedPassword = MutableStateFlow<String?>(null)
    val generatedPassword: StateFlow<String?> = _generatedPassword.asStateFlow()

    private val _strengthAnalysis = MutableStateFlow<PasswordStrengthResponse?>(null)
    val strengthAnalysis: StateFlow<PasswordStrengthResponse?> = _strengthAnalysis.asStateFlow()

    private val _saveState = MutableStateFlow<Result<Unit>?>(null)
    val saveState: StateFlow<Result<Unit>?> = _saveState.asStateFlow()

    private val _isPasswordSaving = MutableStateFlow(false)
    val isPasswordSaving: StateFlow<Boolean> = _isPasswordSaving.asStateFlow()

    init {
        loadPasswords()
    }

    fun loadPasswords() {
        viewModelScope.launch {
            _listState.value = PasswordListUiState.Loading
            
            val category = _selectedCategory.value
            val query = _searchQuery.value
            
            val result = when {
                query.isNotBlank() -> repository.searchPasswordEntries(query)
                category != null -> repository.getPasswordEntries(category)
                else -> repository.getPasswordEntries()
            }
            
            result
                .onSuccess { passwords ->
                    _listState.value = PasswordListUiState.Success(passwords)
                }
                .onFailure { error ->
                    _listState.value = PasswordListUiState.Error(error.message ?: "Failed to load passwords")
                }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        loadPasswords()
    }

    fun filterByCategory(category: String?) {
        _selectedCategory.value = category
        loadPasswords()
    }

    fun selectPassword(entry: PasswordEntry) {
        viewModelScope.launch {
            val passwordResult = repository.decryptPassword(entry)
            val notesResult = repository.decryptNotes(entry)
            
            if (passwordResult.isSuccess) {
                _selectedPassword.value = PasswordDetailData(
                    entry = entry,
                    decryptedPassword = passwordResult.getOrNull() ?: "",
                    decryptedNotes = notesResult.getOrNull()
                )
            }
        }
    }

    fun clearSelection() {
        _selectedPassword.value = null
    }

    fun createPassword(
        site: String,
        username: String,
        password: String,
        notes: String? = null,
        url: String? = null,
        category: String,
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _isPasswordSaving.value = true
            _saveState.value = null // Reset state
            repository.createPasswordEntry(
                site = site,
                username = username,
                password = password,
                notes = notes,
                url = url,
                category = category,
                tags = tags
            )
                .onSuccess {
                    loadPasswords()
                    _saveState.value = Result.success(Unit)
                    _isPasswordSaving.value = false
                }
                .onFailure { error ->
                    _listState.value = PasswordListUiState.Error(error.message ?: "Failed to create password")
                    _saveState.value = Result.failure(error)
                    _isPasswordSaving.value = false
                }
        }
    }

    fun clearSaveState() {
        _saveState.value = null
        _isPasswordSaving.value = false
    }

    fun updatePassword(
        id: String,
        site: String? = null,
        username: String? = null,
        password: String? = null,
        notes: String? = null,
        url: String? = null,
        category: String? = null,
        tags: List<String>? = null
    ) {
        viewModelScope.launch {
            repository.updatePasswordEntry(
                id = id,
                site = site,
                username = username,
                password = password,
                notes = notes,
                url = url,
                category = category,
                tags = tags
            )
                .onSuccess {
                    loadPasswords()
                    // Update selected if it's the current one
                    if (_selectedPassword.value?.entry?.id == id) {
                        selectPassword(it)
                    }
                }
                .onFailure { error ->
                    _listState.value = PasswordListUiState.Error(error.message ?: "Failed to update password")
                }
        }
    }

    fun deletePassword(id: String) {
        viewModelScope.launch {
            repository.deletePasswordEntry(id)
                .onSuccess {
                    loadPasswords()
                    if (_selectedPassword.value?.entry?.id == id) {
                        clearSelection()
                    }
                }
                .onFailure { error ->
                    _listState.value = PasswordListUiState.Error(error.message ?: "Failed to delete password")
                }
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
                .onSuccess {
                    loadPasswords()
                }
        }
    }

    fun generatePassword(
        length: Int = 16,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ) {
        val password = repository.generatePassword(
            length,
            includeUppercase,
            includeLowercase,
            includeNumbers,
            includeSymbols
        )
        _generatedPassword.value = password
        
        // Auto-analyze strength
        analyzePassword(password)
    }

    fun generatePassphrase(wordCount: Int = 4) {
        val passphrase = repository.generatePassphrase(wordCount)
        _generatedPassword.value = passphrase
        
        // Auto-analyze strength
        analyzePassword(passphrase)
    }

    fun analyzePassword(password: String) {
        viewModelScope.launch {
            repository.analyzePassword(password)
                .onSuccess { analysis ->
                    _strengthAnalysis.value = analysis
                }
        }
    }

    fun clearGeneratedPassword() {
        _generatedPassword.value = null
        _strengthAnalysis.value = null
    }
}
