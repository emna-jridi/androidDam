# Ktor API Client Layer - Complete Implementation

## Overview
Complete Ktor HttpClient implementation for Android with Jetpack Compose integration. Includes all DTOs, models, API services, repositories, and dependency injection setup.

## Project Structure

```
data/
├── api/
│   ├── KtorClient.kt                 # Ktor HttpClient configuration
│   ├── ScanApiService.kt             # API service with suspend functions
│   └── models/
│       └── ApiModels.kt              # All request/response DTOs
├── repository/
│   └── ScanRepository.kt             # Repository pattern wrapper
├── local/
│   └── TokenManager.kt               # Auth token management (existing)
└── Config.kt                         # Base URL configuration (updated)

di/
└── ScanModule.kt                     # Hilt dependency injection

features/scan/
├── data/
│   ├── models.kt                     # Deprecated (use data/api/models instead)
│   └── UiModels.kt                   # UI-layer models with isSelected
├── presentation/
│   ├── ScanViewModel.kt              # Real API integration
│   ├── HomeViewModel.kt              # Dashboard with real data
│   ├── ScanViewModelFactory.kt       # ViewModel factory
│   ├── ScanScreen.kt                 # Scan UI screen
│   └── HomeScreen.kt                 # Home/Dashboard screen
```

## API Models & DTOs

### Request Models
```kotlin
// Start scan request
data class StartScanRequest(
    val deviceId: String,
    val platform: String = "android",
    val includeSystemApps: Boolean = false,
    val apps: List<StartScanAppDto>,
    val userId: String,
    val timestamp: Long
)

data class StartScanAppDto(
    val packageName: String,
    val displayName: String,
    val category: String?
)
```

### Response Models
```kotlin
// API response wrapper
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val timestamp: String?
)

// Scan response
data class StartScanResponse(
    val scanId: String,
    val status: String,
    val userId: String,
    val deviceId: String,
    val platform: String,
    val createdAt: String
)

// Scan status
data class ScanStatusResponse(
    val scanId: String,
    val status: String,           // PENDING, ANALYZING, COMPLETED, FAILED
    val progress: Int?,
    val totalApps: Int?,
    val scannedApps: Int?,
    val results: ScanResultsDto?
)

// App information
data class LocalAppInfo(
    val packageName: String,
    val displayName: String,
    val category: String?,
    val isSystemApp: Boolean = false,
    val permissions: List<String> = emptyList(),
    val trackers: List<SimpleTrackerInfo> = emptyList()
)

data class SimpleTrackerInfo(
    val name: String,
    val riskLevel: String? = null
)

// App details
data class AppDetailsResponse(
    val app: AppInfoDto,
    val history: List<AppScanHistoryDto> = emptyList()
)
```

## Ktor HttpClient Configuration

### KtorClient.kt Features
- **HTTP Timeout**: 60s request, 30s connect/socket
- **JSON Serialization**: kotlinx-serialization with lenient parsing
- **Logging**: Automatic request/response logging (LogLevel.INFO)
- **Retry Policy**: 2 automatic retries with exponential backoff
- **Authorization**: Bearer token injection from TokenManager
- **Base URL**: Configured via Config.BASE_URL

### Configuration
```kotlin
HttpTimeout {
    requestTimeoutMillis = 60000
    connectTimeoutMillis = 30000
    socketTimeoutMillis = 30000
}

ContentNegotiation.json() {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

HttpRequestRetry {
    maxRetries = 2
    exponentialDelay()
}
```

## ScanApiService - 4 Endpoints

### 1. Start Scan
```kotlin
suspend fun startScan(
    apps: List<String>,
    userId: String,
    deviceId: String,
    includeSystemApps: Boolean = false
): Result<StartScanResponse>
```
**Endpoint**: `POST /api/scan/start`
**Request**: StartScanRequest with app list
**Response**: StartScanResponse with scanId
**Error Handling**: Network errors thrown with clean messages

### 2. Get Scan Status
```kotlin
suspend fun getScanStatus(scanId: String): Result<ScanStatusResponse>
```
**Endpoint**: `GET /api/scan/status/{scanId}`
**Response**: ScanStatusResponse with progress/results
**Auto-Polling**: ViewModel polls every 2 seconds until COMPLETED

### 3. Get All Apps
```kotlin
suspend fun getAllApps(): Result<List<LocalAppInfo>>
```
**Endpoint**: `GET /api/scan/apps`
**Response**: List of LocalAppInfo (ready for UI selection)
**Caching**: Loaded once at screen init

### 4. Get App Details
```kotlin
suspend fun getAppDetails(packageName: String): Result<AppDetailsResponse>
```
**Endpoint**: `GET /api/scan/app/{packageName}`
**Response**: AppDetailsResponse with scan history
**Error Handling**: Converts IOException to Result.failure

## ScanRepository - Data Access Layer

Wraps ScanApiService with clean interface:
```kotlin
class ScanRepository(context, tokenManager) {
    suspend fun startScan(...): Result<StartScanResponse>
    suspend fun getScanStatus(...): Result<ScanStatusResponse>
    suspend fun getAllApps(): Result<List<LocalAppInfo>>
    suspend fun getAppDetails(...): Result<AppDetailsResponse>
}
```

All methods return `Result<T>` for safe error handling.

## ViewModel Integration

### ScanViewModel
```kotlin
// Initialize with user/device IDs
fun initialize(userId: String, deviceId: String)

// State management
val scanState: StateFlow<ScanState>      // Current scan state
val availableApps: StateFlow<List<LocalAppInfo>>  // Loaded apps
val selectedAppDetails: StateFlow<AppDetailsUIState?>

// Actions
fun startScan(includeSystemApps: Boolean)     // Calls API
fun pollScanStatus(scanId: String)            // Auto-polls every 2s
fun toggleAppSelection(packageName: String)
fun selectAllApps() / deselectAllApps()
fun getAppDetails(packageName: String)

// Lifecycle
override fun onCleared()  // Cancels polling job
```

### HomeViewModel
```kotlin
fun loadDashboard()  // Fetches apps via getAllApps()
val homeState: StateFlow<HomeState>

// Calculates risk distribution from app data
// Shows risky apps (apps with trackers/permissions)
// Tracks scan history
```

## Error Handling Strategy

All API calls return `Result<T>`:
```kotlin
repository.startScan(apps, userId, deviceId)
    .onSuccess { response ->
        // Handle success
        _scanState.value = _scanState.value.copy(
            status = "ANALYZING",
            scanId = response.scanId
        )
    }
    .onFailure { error ->
        // Handle error
        _scanState.value = _scanState.value.copy(
            status = "FAILED",
            error = error.message ?: "Unknown error"
        )
    }
```

## Dependency Injection Setup

### Hilt Module
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ScanModule {
    @Provides
    fun provideScanRepository(context, tokenManager): ScanRepository

    @Provides
    fun provideScanApiService(context, tokenManager): ScanApiService
}
```

### Usage in ViewModel
```kotlin
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: ScanRepository
) : ViewModel() { ... }
```

Or with factory:
```kotlin
val factory = ScanViewModelFactory(context, tokenManager, repository)
val viewModel = ViewModelProvider(this, factory)[ScanViewModel::class.java]
```

## UI State Models

### LocalAppInfo (UI Layer)
```kotlin
data class LocalAppInfo(
    val packageName: String,
    val displayName: String,
    val category: String?,
    val isSystemApp: Boolean,
    val permissions: List<String>,
    val trackers: List<SimpleTrackerInfo>,
    val isSelected: Boolean  // ← UI selection state
)
```

### ScanState
```kotlin
data class ScanState(
    val scanId: String?,
    val status: String,  // IDLE, LOADING, ANALYZING, COMPLETED, FAILED
    val selectedApps: List<LocalAppInfo>,
    val totalApps: Int,
    val scannedApps: Int,
    val highRiskCount: Int,
    val mediumRiskCount: Int,
    val lowRiskCount: Int,
    val averageScore: Float,
    val error: String?
)
```

## Configuration

### Update Config.kt
```kotlin
object Config {
    const val BASE_URL: String = "http://192.168.1.6:3000"
    // For emulator: "http://10.0.2.2:3000"
}
```

### Required Gradle Dependencies
```gradle
// Ktor Client
implementation("io.ktor:ktor-client-android:2.3.0")
implementation("io.ktor:ktor-client-logging:2.3.0")
implementation("io.ktor:ktor-client-content-negotiation:2.3.0")

// Serialization
implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

// Hilt
implementation("com.google.dagger:hilt-android:2.44")
kapt("com.google.dagger:hilt-compiler:2.44")
implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

// Jetpack
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
```

## Usage Example

### In Composable Screen
```kotlin
@Composable
fun ScanScreen(
    userId: String,
    deviceId: String,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val scanState by viewModel.scanState.collectAsState()
    val availableApps by viewModel.availableApps.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize(userId, deviceId)
    }

    // Apps are loaded from API
    // Scan starts by calling viewModel.startScan()
    // Status polled automatically every 2s
}
```

## Data Flow

```
ScanScreen (UI)
    ↓
ScanViewModel (State Management)
    ↓
ScanRepository (Data Access)
    ↓
ScanApiService (HTTP Client)
    ↓
Ktor HttpClient
    ↓
Backend API
```

## Testing Mock Data

For testing without backend:
1. Create mock implementations of ScanRepository
2. Pass to ViewModel via factory
3. Or use fake responses in ScanApiService

## Notes

- **Trackers**: Always `List<SimpleTrackerInfo>`, never wrapped
- **Serialization**: Uses kotlinx with lenient parsing for flexibility
- **Token Injection**: Automatically added to all requests via TokenManager
- **Error Messages**: User-friendly, shown in UI
- **Polling**: Automatic every 2 seconds until completion
- **Memory**: ViewModel cleanup cancels polling jobs

---

**Last Updated**: December 10, 2025
**Status**: Production Ready
