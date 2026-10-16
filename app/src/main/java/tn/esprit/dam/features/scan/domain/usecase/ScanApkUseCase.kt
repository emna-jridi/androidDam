package tn.esprit.dam.features.scan.domain.usecase

import android.net.Uri
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.network.map
import tn.esprit.dam.features.scan.data.repository.ScanRepository
import tn.esprit.dam.features.scan.presentation.model.LocalAppInfo
import javax.inject.Inject

data class ApkScanResult(val app: LocalAppInfo, val note: String)

/** Uploads an APK for a deep (MobSF + AI) analysis and maps the report for the UI. */
class ScanApkUseCase @Inject constructor(
    private val repository: ScanRepository
) {
    suspend operator fun invoke(uri: Uri, deviceId: String?): ApiResult<ApkScanResult> =
        repository.scanApk(uri, deviceId).map { details ->
            ApkScanResult(
                app = details.toLocalAppInfo(
                    fallbackPackage = details.packageName ?: "unknown",
                    fallbackName = details.appName ?: "APK"
                ),
                note = if (details.app?.scanResults?.aiStatus == "ok") {
                    "Complete APK analysis: MobSF + AI + Heuristics"
                } else {
                    "APK analysis: MobSF + Local heuristics"
                }
            )
        }
}
