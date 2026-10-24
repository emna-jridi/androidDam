package tn.esprit.dam.features.scan.domain.usecase

import android.net.Uri
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.features.scan.data.remote.dto.AppDetailsResponse
import tn.esprit.dam.features.scan.data.remote.dto.LatestScanResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanHistoryResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanResultResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanStatusResponse
import tn.esprit.dam.features.scan.data.remote.dto.SearchAppResponse
import tn.esprit.dam.features.scan.data.remote.dto.StartScanResponse
import tn.esprit.dam.features.scan.data.repository.ScanRepository
import tn.esprit.dam.features.scan.domain.RiskLevel
import tn.esprit.dam.features.scan.presentation.model.LocalAppInfo

class ScanUseCasesTest {

    // --- PollScanUseCase ------------------------------------------------------------------

    @Test
    fun `poll emits progress then completed with mapped app`() = runTest {
        val repo = FakeScanRepository(
            statuses = ArrayDeque(
                listOf(
                    ScanStatusResponse(scanId = "s1", status = "processing", percentage = 40),
                    ScanStatusResponse(scanId = "s1", status = "completed")
                )
            ),
            result = ApiResult.Success(
                ScanResultResponse(scanId = "s1", packageName = "com.app", appName = "App", globalRisk = "high", overallScore = 35f)
            )
        )

        val events = PollScanUseCase(repo)("s1").toList()

        assertTrue(events[0] is ScanPollEvent.Progress)
        val done = events[1] as ScanPollEvent.Completed
        assertEquals("com.app", done.app.packageName)
        assertEquals(RiskLevel.HIGH, done.app.riskResult?.riskLevel)
        assertEquals(2, events.size)
    }

    @Test
    fun `poll treats all-completed steps as completed`() = runTest {
        val repo = FakeScanRepository(
            statuses = ArrayDeque(listOf(ScanStatusResponse(scanId = "s1", status = "processing", percentage = 100))),
            result = ApiResult.ApiError("gone", 404)
        )

        val events = PollScanUseCase(repo)("s1").toList()

        assertEquals(listOf(ScanPollEvent.CompletedWithoutDetails), events)
    }

    @Test
    fun `poll stops on server failure`() = runTest {
        val repo = FakeScanRepository(statuses = ArrayDeque(listOf(ScanStatusResponse(scanId = "s1", status = "FAILED"))))

        assertEquals(listOf(ScanPollEvent.Failed), PollScanUseCase(repo)("s1").toList())
    }

    @Test
    fun `poll times out after the poll budget and ignores transient errors`() = runTest {
        val repo = FakeScanRepository(statuses = ArrayDeque()) // every poll is a network error

        val events = PollScanUseCase(repo)("s1").toList()

        assertEquals(listOf(ScanPollEvent.TimedOut), events)
        assertEquals(100, repo.statusCalls)
    }

    // --- QuickMultiScanUseCase ------------------------------------------------------------

    @Test
    fun `multi scan falls back to local risk and reports it`() = runTest {
        val apps = listOf(
            LocalAppInfo(packageName = "a.one", displayName = "One"),
            LocalAppInfo(packageName = "a.two", displayName = "Two")
        )

        val events = QuickMultiScanUseCase(FakeScanRepository())(apps).toList()

        assertEquals(3, events.size)
        assertEquals(1, (events[0] as MultiScanEvent.Progress).scannedApps)
        val done = events.last() as MultiScanEvent.Done
        assertEquals(2, done.apps.size)
        assertTrue(done.apps.all { it.riskResult != null })
        assertEquals("Analysis complete. 2 app(s) analyzed in local mode.", done.note)
        assertEquals(2, done.counts.high + done.counts.medium + done.counts.low)
    }

    @Test
    fun `risk counts bucket critical with high and unknown with low`() {
        val counts = RiskCounts.of(listOf(LocalAppInfo(packageName = "x", displayName = "x")))
        assertEquals(RiskCounts(high = 0, medium = 0, low = 1), counts)
    }

    private class FakeScanRepository(
        val statuses: ArrayDeque<ScanStatusResponse> = ArrayDeque(),
        val result: ApiResult<ScanResultResponse> = ApiResult.ApiError("none", 404)
    ) : ScanRepository {
        var statusCalls = 0
        private val offline = ApiResult.NetworkError(java.io.IOException("offline"))

        override suspend fun getScanStatus(scanId: String): ApiResult<ScanStatusResponse> {
            statusCalls++
            return statuses.removeFirstOrNull()?.let { ApiResult.Success(it) } ?: offline
        }

        override suspend fun getScanResult(scanId: String) = result
        override suspend fun getAppDetails(packageName: String): ApiResult<AppDetailsResponse> = offline
        override suspend fun startScan(
            apps: List<String>, deviceId: String, includeSystemApps: Boolean
        ): ApiResult<StartScanResponse> = offline
        override suspend fun getLatestScan(): ApiResult<LatestScanResponse> = offline
        override suspend fun searchApps(query: String, platform: String, limit: Int): ApiResult<SearchAppResponse> = offline
        override suspend fun getScanHistory(limit: Int, offset: Int): ApiResult<ScanHistoryResponse> = offline
        override suspend fun scanApk(uri: Uri, deviceId: String?): ApiResult<AppDetailsResponse> = offline
    }
}
