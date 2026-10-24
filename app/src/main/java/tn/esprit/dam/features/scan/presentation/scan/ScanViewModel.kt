package tn.esprit.dam.features.scan.presentation.scan

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import tn.esprit.dam.R
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.features.scan.data.repository.ScanRepository
import tn.esprit.dam.features.scan.domain.usecase.GetScannableAppsUseCase
import tn.esprit.dam.features.scan.domain.usecase.MultiScanEvent
import tn.esprit.dam.features.scan.domain.usecase.PollScanUseCase
import tn.esprit.dam.features.scan.domain.usecase.QuickMultiScanUseCase
import tn.esprit.dam.features.scan.domain.usecase.RiskCounts
import tn.esprit.dam.features.scan.domain.usecase.ScanApkUseCase
import tn.esprit.dam.features.scan.domain.usecase.ScanPollEvent
import tn.esprit.dam.features.scan.domain.usecase.errorMessage
import tn.esprit.dam.features.scan.presentation.model.LocalAppInfo
import tn.esprit.dam.features.scan.presentation.model.ScanState
import tn.esprit.dam.features.scan.presentation.model.ScanStatus
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: ScanRepository,
    private val getScannableApps: GetScannableAppsUseCase,
    private val quickMultiScan: QuickMultiScanUseCase,
    private val pollScan: PollScanUseCase,
    private val scanApkUseCase: ScanApkUseCase
) : ViewModel() {
    private val _scanState = MutableStateFlow(ScanState())
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _availableApps = MutableStateFlow<List<LocalAppInfo>>(emptyList())
    val availableApps: StateFlow<List<LocalAppInfo>> = _availableApps.asStateFlow()

    private var deviceId: String? = null

    private var pollingJob: Job? = null

    fun initialize(deviceId: String) {
        this.deviceId = deviceId
        loadAvailableApps()
    }

    // --- app selection -------------------------------------------------------------------------

    private fun loadAvailableApps() {
        viewModelScope.launch {
            _scanState.update { it.copy(status = ScanStatus.Loading) }
            val apps = getScannableApps(includeSystemApps = _scanState.value.showSystemApps)
            _availableApps.value = apps
            _scanState.update { it.copy(status = ScanStatus.Idle, totalApps = apps.size) }
        }
    }

    fun toggleAppSelection(packageName: String) {
        setSelection { app -> if (app.packageName == packageName) !app.isSelected else app.isSelected }
    }

    fun selectAllApps() = setSelection { true }

    fun deselectAllApps() = setSelection { false }

    private inline fun setSelection(crossinline selected: (LocalAppInfo) -> Boolean) {
        val apps = _availableApps.updateAndGet { list -> list.map { it.copy(isSelected = selected(it)) } }
        _scanState.update { it.copy(selectedApps = apps.filter { app -> app.isSelected }, error = null) }
    }

    fun toggleSystemApps() {
        _scanState.update { it.copy(showSystemApps = !it.showSystemApps) }
        loadAvailableApps()
    }

    // --- scanning ------------------------------------------------------------------------------

    fun startScan() {
        val selectedApps = _scanState.value.selectedApps
        if (selectedApps.isEmpty()) {
            _scanState.update { it.copy(error = UiText.Res(R.string.error_select_app)) }
            return
        }
        val deviceId = deviceId
        if (deviceId == null) {
            _scanState.update { it.copy(error = UiText.Res(R.string.error_user_not_initialized)) }
            return
        }

        launchScan {
            if (selectedApps.size > 1) runQuickMultiScan(selectedApps)
            else startServerScan(selectedApps, deviceId)
        }
    }

    private suspend fun runQuickMultiScan(selectedApps: List<LocalAppInfo>) {
        _scanState.update {
            it.copy(
                status = ScanStatus.Analyzing,
                error = null,
                analysisNote = UiText.Res(R.string.scan_note_quick),
                scannedApps = 0,
                totalApps = selectedApps.size,
                highRiskCount = 0,
                mediumRiskCount = 0,
                lowRiskCount = 0,
                averageScore = 0f
            )
        }

        quickMultiScan(selectedApps).collect { event ->
            when (event) {
                is MultiScanEvent.Progress -> _scanState.update {
                    it.copy(scannedApps = event.scannedApps, selectedApps = event.apps, averageScore = event.averageScore)
                        .withCounts(event.counts)
                }
                is MultiScanEvent.Done -> _scanState.update {
                    it.copy(
                        status = ScanStatus.Completed,
                        selectedApps = event.apps,
                        averageScore = event.averageScore,
                        analysisNote = UiText.Raw(event.note),
                        error = null,
                        mlExplanation = event.explanation,
                        mlRecommendations = event.recommendations,
                        mlRiskFactors = event.riskFactors,
                        mlSafetyTips = event.safetyTips,
                        analysisSource = event.analysisSource,
                        scanCompleted = true
                    ).withCounts(event.counts)
                }
            }
        }
        delay(UI_SETTLE_DELAY_MS)
    }

    private suspend fun startServerScan(selectedApps: List<LocalAppInfo>, deviceId: String) {
        _scanState.update {
            it.copy(status = ScanStatus.Analyzing, error = null, analysisNote = null, selectedApps = selectedApps)
        }

        val result = repository.startScan(
            apps = selectedApps.map { it.packageName },
            deviceId = deviceId,
            includeSystemApps = false
        )
        if (result !is ApiResult.Success) {
            fail(UiText.Raw(result.errorMessage().orEmpty()))
            return
        }
        val scanId = result.data.scanId
        if (scanId.isNullOrBlank()) {
            fail(UiText.Res(R.string.error_scan_id_empty))
            return
        }

        _scanState.update {
            it.copy(
                scanId = scanId,
                scannedApps = 0,
                averageScore = 0f,
                analysisNote = null
            ).withCounts(RiskCounts())
        }
        startPolling(scanId)
    }

    private fun startPolling(scanId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            pollScan(scanId).collect { event -> onPollEvent(event) }
        }
    }

    private fun onPollEvent(event: ScanPollEvent) {
        when (event) {
            is ScanPollEvent.Progress -> {
                val status = event.status
                _scanState.update {
                    it.copy(
                        scannedApps = status.scannedApps ?: it.scannedApps,
                        totalApps = status.totalApps ?: it.totalApps,
                        averageScore = status.results?.averageScore ?: 0f,
                        confidenceScore = status.results?.confidenceScore?.toInt()
                    ).withCounts(
                        RiskCounts(
                            high = status.results?.highRiskApps ?: 0,
                            medium = status.results?.mediumRiskApps ?: 0,
                            low = status.results?.lowRiskApps ?: 0
                        )
                    )
                }
            }
            is ScanPollEvent.Completed -> _scanState.update {
                it.copy(
                    status = ScanStatus.Completed,
                    selectedApps = listOf(event.app),
                    totalApps = 1,
                    scannedApps = 1,
                    averageScore = event.app.riskResult?.score?.toFloat() ?: 0f,
                    confidenceScore = event.confidenceScore,
                    scanCompleted = true
                ).withCounts(RiskCounts.of(listOf(event.app)))
            }
            ScanPollEvent.CompletedWithoutDetails -> _scanState.update {
                it.copy(
                    status = ScanStatus.Completed,
                    analysisNote = UiText.Res(R.string.scan_note_done_no_details),
                    scanCompleted = true
                )
            }
            ScanPollEvent.Failed -> fail(UiText.Res(R.string.error_scan_failed))
            ScanPollEvent.TimedOut -> fail(UiText.Res(R.string.error_scan_timeout))
        }
    }

    fun scanApk(uri: Uri) {
        launchScan {
            _scanState.update {
                it.copy(
                    status = ScanStatus.Analyzing,
                    error = null,
                    analysisNote = UiText.Res(R.string.scan_note_apk),
                    selectedApps = emptyList(),
                    totalApps = 1,
                    scannedApps = 0
                )
            }

            when (val result = scanApkUseCase(uri, deviceId)) {
                is ApiResult.Success -> {
                    val app = result.data.app
                    _scanState.update {
                        it.copy(
                            status = ScanStatus.Completed,
                            selectedApps = listOf(app),
                            totalApps = 1,
                            scannedApps = 1,
                            averageScore = app.riskResult?.score?.toFloat() ?: 0f,
                            analysisNote = UiText.Raw(result.data.note),
                            scanCompleted = true
                        ).withCounts(RiskCounts.of(listOf(app)))
                    }
                    delay(UI_SETTLE_DELAY_MS)
                }
                else -> fail(UiText.Raw(result.errorMessage().orEmpty()), clearNote = true)
            }
        }
    }

    /** Runs a scan step and turns any unexpected crash into a Failed state. */
    private fun launchScan(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                fail(UiText.Raw("${e.javaClass.simpleName}: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    private fun fail(error: UiText, clearNote: Boolean = false) {
        _scanState.update {
            it.copy(
                status = ScanStatus.Failed,
                error = error,
                analysisNote = if (clearNote) null else it.analysisNote
            )
        }
    }

    private fun ScanState.withCounts(counts: RiskCounts) = copy(
        highRiskCount = counts.high,
        mediumRiskCount = counts.medium,
        lowRiskCount = counts.low
    )

    // --- details & reset -----------------------------------------------------------------------

    fun clearError() {
        _scanState.update { it.copy(error = null) }
    }

    fun resetScan() {
        stopPolling()
        _availableApps.update { apps -> apps.map { it.copy(isSelected = false) } }
        _scanState.value = ScanState(totalApps = _availableApps.value.size)
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }

    private companion object {
        const val UI_SETTLE_DELAY_MS = 300L
    }
}
