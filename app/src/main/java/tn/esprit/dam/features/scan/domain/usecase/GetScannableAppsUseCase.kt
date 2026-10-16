package tn.esprit.dam.features.scan.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tn.esprit.dam.features.scan.data.local.AppScanner
import tn.esprit.dam.features.scan.presentation.model.LocalAppInfo
import javax.inject.Inject

/** Installed apps the user can pick for a scan, optionally hiding system/OEM packages. */
class GetScannableAppsUseCase @Inject constructor(
    private val appScanner: AppScanner
) {
    suspend operator fun invoke(includeSystemApps: Boolean): List<LocalAppInfo> = withContext(Dispatchers.IO) {
        val apps = appScanner.getInstalledApps()
        if (includeSystemApps) apps
        else apps.filter { !it.isSystemApp && !isSystemPackage(it.packageName) }
    }

    private fun isSystemPackage(packageName: String): Boolean =
        SYSTEM_PREFIXES.any { packageName.startsWith(it) }

    private companion object {
        val SYSTEM_PREFIXES = listOf(
            "android",
            "com.android.",
            "com.google.android.",
            "com.samsung.android.",
            "com.sec.android.",
            "com.huawei.",
            "com.miui.",
            "com.xiaomi.",
            "com.oneplus.",
            "com.oppo.",
            "com.vivo.",
            "com.coloros.",
            "com.bbk.",
            "com.qualcomm.",
            "com.mediatek."
        )
    }
}
