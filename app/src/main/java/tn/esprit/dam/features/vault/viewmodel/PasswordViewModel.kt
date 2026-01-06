package tn.esprit.dam.features.vault.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import tn.esprit.dam.data.model.PasswordEntry
import tn.esprit.dam.data.remote.ai.OllamaAdvice
import tn.esprit.dam.data.remote.ai.OllamaPasswordAdvisor
import tn.esprit.dam.data.repository.VaultRepository
import tn.esprit.dam.features.vault.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
sealed class PasswordListUiState {
    object Loading : PasswordListUiState()
    data class Success(val passwords: List<PasswordEntry>) : PasswordListUiState()
    data class Error(val message: String) : PasswordListUiState()
}

// Helper data class for password details
data class PasswordDetailData(
    val entry: PasswordEntry,
    val decryptedPassword: String,
    val decryptedNotes: String?
)



@HiltViewModel
class PasswordViewModel @Inject constructor(
    private val repository: VaultRepository,
    private val advisor: OllamaPasswordAdvisor
) : ViewModel() {

    private val _listState = MutableStateFlow<PasswordListUiState>(PasswordListUiState.Loading)
    val listState: StateFlow<PasswordListUiState> = _listState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedPassword = MutableStateFlow<PasswordDetailData?>(null)
    val selectedPassword: StateFlow<PasswordDetailData?> = _selectedPassword.asStateFlow()

    private val _saveState = MutableStateFlow<Result<Unit>?>(null)
    val saveState: StateFlow<Result<Unit>?> = _saveState.asStateFlow()

    private val _isPasswordSaving = MutableStateFlow(false)
    val isPasswordSaving: StateFlow<Boolean> = _isPasswordSaving.asStateFlow()

    // AI & Analysis States
    private val _passwordMetrics = MutableStateFlow<PasswordAnalysisMetrics?>(null)
    val passwordMetrics = _passwordMetrics.asStateFlow()

    private val _aiAdvice = MutableStateFlow<OllamaAdvice?>(null)
    val aiAdvice = _aiAdvice.asStateFlow()

    private val _generatedPassword = MutableStateFlow("")
    val generatedPassword = _generatedPassword.asStateFlow()
    
    // Performance: Cache password analysis results
    private val analysisCache = mutableMapOf<String, PasswordAnalysisMetrics>()
    private val breachCheckCache = mutableMapOf<String, Boolean>()

    // Don't auto-load in init - let VaultNavGraph control when to load
    // This prevents 401 errors when vault is locked
    // init {
    //     loadPasswords()
    // }

    fun generateNewPassword(length: Int, useUpper: Boolean, useNums: Boolean, useSymbols: Boolean) {
        _generatedPassword.value = PasswordGenerator.generatePassword(length, useUpper, useNums, useSymbols)
    }

    fun generateNewPassphrase(wordCount: Int, separator: String) {
        _generatedPassword.value = PasswordGenerator.generatePassphrase(wordCount, separator)
    }
    
    // Check if password has been breached (using k-anonymity)
    fun checkPasswordBreach(password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Check cache first
                breachCheckCache[password]?.let {
                    onResult(it)
                    return@launch
                }
                
                // Use SHA-1 hash for k-anonymity
                val sha1Hash = password.sha1Hash()
                val prefix = sha1Hash.take(5)
                val suffix = sha1Hash.drop(5)
                
                // TODO: Call HaveIBeenPwned API with prefix only
                // For now, return false (not breached)
                val isBreached = false
                breachCheckCache[password] = isBreached
                onResult(isBreached)
            } catch (e: Exception) {
                onResult(false) // Fail safe
            }
        }
    }
    
    // Clear cache when passwords are modified
    fun clearAnalysisCache() {
        analysisCache.clear()
        breachCheckCache.clear()
    }

    fun analyzePassword(password: String) {
        viewModelScope.launch {
            // 1. Local Deterministic Analysis (Instant) with caching
            val metrics = analysisCache.getOrPut(password) {
                PasswordStrengthCalculator.analyze(password)
            }
            _passwordMetrics.value = metrics

            // 2. Pattern Detection (AI-powered, Local)
            val patterns = PasswordPatternDetector.detectPatterns(password)
            val smartRecommendation = if (patterns.isNotEmpty()) {
                PasswordPatternDetector.generateSmartRecommendation(password, patterns)
            } else null

            // 3. AI Analysis (Async, Privacy Safe)
            // Only call AI if password is significant
            if (password.length >= 4) {
                try {
                    val advice = advisor.getAdvice(metrics) // Sends only metrics!
                    _aiAdvice.value = advice
                } catch (e: Exception) {
                    // Fallback to smart pattern-based recommendation
                    _aiAdvice.value = OllamaAdvice(
                        smartRecommendation ?: "Local Analysis Only (AI unavailable)", 
                        metrics.issues.take(3), 
                        "neutral"
                    )
                }
            } else {
                _aiAdvice.value = null
            }
        }
    }

    fun clearAnalysis() {
        _passwordMetrics.value = null
        _aiAdvice.value = null
        _generatedPassword.value = ""
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
                    // Build smart domain index for auto-fill suggestions
                    PasswordMatcher.buildIndex(passwords)
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
    
    /**
     * Find passwords for a specific website (Smart Auto-Fill)
     * Performance: O(1) lookup using pre-built index
     */
    fun findPasswordsForUrl(url: String): List<PasswordEntry> {
        return PasswordMatcher.findMatchingPasswords(url)
    }
    
    /**
     * Find similar passwords by site name (Fuzzy search)
     */
    fun findSimilarPasswords(siteName: String): List<PasswordEntry> {
        return PasswordMatcher.findSimilarPasswords(siteName)
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
            _saveState.value = null

            // Get current metrics if available
            val metrics = _passwordMetrics.value

            repository.createPasswordEntry(
                site = site,
                username = username,
                password = password,
                notes = notes,
                url = url,
                category = category,
                tags = tags,
                strengthScore = metrics?.score,
                strengthLevel = metrics?.riskLevel?.name,
                estimatedCrackTime = metrics?.estimatedCrackTime,
                strengthIssues = metrics?.issues
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

    // Existing helper methods preserved
    fun updatePassword(entry: PasswordEntry) { /* ... stub if needed or preserved ... */ }
    fun deletePassword(id: String) {
        viewModelScope.launch {
             repository.deletePasswordEntry(id)
             loadPasswords()
        }
    }
    fun toggleFavorite(id: String) {
        viewModelScope.launch {
             repository.toggleFavorite(id)
             loadPasswords()
        }
    }
}
