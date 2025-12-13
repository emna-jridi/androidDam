# Ktor API Client - Quick Reference

## Endpoints Summary

| Method | Endpoint | Request | Response | ViewModel Method |
|--------|----------|---------|----------|-----------------|
| POST | `/api/scan/start` | StartScanRequest | StartScanResponse | `startScan(apps, userId, deviceId)` |
| GET | `/api/scan/status/{scanId}` | - | ScanStatusResponse | `pollScanStatus(scanId)` |
| GET | `/api/scan/apps` | - | List<LocalAppInfo> | `getAllApps()` (auto-init) |
| GET | `/api/scan/app/{packageName}` | - | AppDetailsResponse | `getAppDetails(packageName)` |

## File Organization

### API Layer
```
data/api/
├── KtorClient.kt              → Singleton HttpClient instance
├── ScanApiService.kt          → 4 suspend functions
└── models/ApiModels.kt        → All DTOs & serializable classes
```

### Data Layer
```
data/repository/
└── ScanRepository.kt          → Result<T> wrappers around API service
```

### UI/Presentation Layer
```
features/scan/
├── data/UiModels.kt          → UI state (LocalAppInfo + isSelected)
└── presentation/
    ├── ScanViewModel.kt       → State management + API calls
    ├── HomeViewModel.kt       → Dashboard state
    ├── ScanScreen.kt          → App selection & results UI
    └── HomeScreen.kt          → Risk dashboard UI
```

### DI Layer
```
di/ScanModule.kt              → Hilt singleton bindings
```

## State Flow Diagram

```
initialize(userId, deviceId)
            ↓
getAllApps() → availableApps StateFlow
            ↓
User selects apps
            ↓
startScan()
    ↓
startScan() API call
    ↓
pollScanStatus() (every 2s)
    ↓
status == "COMPLETED" → Stop polling
    ↓
Display results
```

## ViewModel Constructor

Both ScanViewModel and HomeViewModel require:
```kotlin
ScanViewModel(
    context: Context,
    tokenManager: TokenManager,
    repository: ScanRepository
)
```

Get from Hilt:
```kotlin
val viewModel: ScanViewModel = hiltViewModel()
```

Or manual:
```kotlin
val factory = ScanViewModelFactory(context, tokenManager, repository)
val viewModel = ViewModelProvider(this, factory)[ScanViewModel::class.java]
```

## Key Features

✅ **Suspend Functions**: All API calls are suspend functions (async)
✅ **Result<T>**: Safe error handling with onSuccess/onFailure
✅ **Auto-Polling**: ScanStatus polled every 2 seconds
✅ **Token Injection**: Bearer token added to all requests
✅ **Error Messages**: Clean, user-friendly error text
✅ **Timeouts**: 60s request timeout, 30s connect
✅ **Logging**: Automatic request/response logging
✅ **Retries**: 2 automatic retries with exponential backoff
✅ **Serialization**: Lenient JSON parsing (ignoreUnknownKeys)
✅ **Trackers**: Always List<SimpleTrackerInfo>, never wrapped

## DTO Relationships

```
StartScanRequest
├── StartScanAppDto[]
└── userId, deviceId, platform

    ↓ (API Call)

StartScanResponse (contains scanId)
└── Can poll status with scanId

    ↓ (Poll with scanId)

ScanStatusResponse
├── status: String (ANALYZING, COMPLETED, etc)
└── results: ScanResultsDto
    ├── totalScanned, highRiskApps, mediumRiskApps, lowRiskApps
    └── averageScore

    ↓ (List apps)

LocalAppInfo[]
├── packageName, displayName, category
├── isSystemApp, permissions
└── trackers: SimpleTrackerInfo[]

    ↓ (Get details for one app)

AppDetailsResponse
├── app: AppInfoDto
│   ├── AppInfoDto (same as LocalAppInfo + analysis)
│   └── scanResults: AnalysisResultDto
│       ├── aiRiskScore, aiRiskLevel
│       ├── aiSummary, aiRecommendations
│       └── permissionsScore, trackersScore
└── history: AppScanHistoryDto[]
```

## ScanState Properties

| Property | Type | Purpose |
|----------|------|---------|
| scanId | String? | Current scan ID from API |
| status | String | IDLE, LOADING, ANALYZING, COMPLETED, FAILED |
| selectedApps | List<LocalAppInfo> | Apps user selected (with isSelected=true) |
| totalApps | Int | Total available apps |
| scannedApps | Int | Progress counter (from poll) |
| highRiskCount | Int | Apps with high risk |
| mediumRiskCount | Int | Apps with medium risk |
| lowRiskCount | Int | Apps with low risk |
| averageScore | Float | Average security score |
| error | String? | Error message if any |

## Common Patterns

### Pattern 1: Load apps on init
```kotlin
LaunchedEffect(Unit) {
    viewModel.initialize(userId, deviceId)  // Calls getAllApps()
}
```

### Pattern 2: Start scan with selected apps
```kotlin
viewModel.startScan(includeSystemApps = false)
// Automatically starts polling
```

### Pattern 3: Display app details
```kotlin
viewModel.getAppDetails(packageName)
val details by viewModel.selectedAppDetails.collectAsState()
when (details) {
    is AppDetailsUIState.Loading -> LoadingUI()
    is AppDetailsUIState.Success -> DetailsUI(details.data)
    is AppDetailsUIState.Error -> ErrorUI(details.message)
}
```

### Pattern 4: Error handling
```kotlin
LaunchedEffect(scanState.error) {
    if (scanState.error != null) {
        showSnackbar(scanState.error!!)
        viewModel.clearError()
    }
}
```

## API Response Structure

All endpoints return:
```json
{
  "success": true,
  "data": { ... },
  "message": "Optional message",
  "timestamp": "2024-12-10T10:30:00Z"
}
```

## Base URL Configuration

Update in `data/Config.kt`:
```kotlin
const val BASE_URL: String = "http://192.168.1.6:3000"

// For emulator testing:
const val BASE_URL: String = "http://10.0.2.2:3000"
```

## Troubleshooting

### Apps not loading
- Check `initialize()` was called
- Verify Config.BASE_URL is correct
- Check TokenManager has valid token
- View logs: Logcat filter "io.ktor"

### Polling not starting
- Verify `startScan()` succeeded and returned scanId
- Check ViewModel is not in FAILED status
- Polling automatically starts on successful scan

### Token not sent
- Verify TokenManager.getToken() returns non-empty string
- Check Authorization header in logs
- Ensure TokenManager is properly injected

### JSON parsing errors
- Check API response matches ApiModels
- Verify `ignoreUnknownKeys = true` is set
- Check SimpleTrackerInfo is not wrapped in object

## Debug Logging

Enable detailed Ktor logging:
```kotlin
install(Logging) {
    logger = Logger.DEFAULT
    level = LogLevel.ALL  // Change from INFO to ALL
}
```

Check logcat:
```bash
adb logcat | grep "io.ktor"
```
