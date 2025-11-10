// data/api/ShadowGuardApi.kt
package tn.esprit.dam.data.api

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import tn.esprit.dam.data.*
import java.io.File

class ShadowGuardApi(private val context: Context) {
    // ✅ Utiliser l'URL de ApiProvider
    private val baseUrl = ApiProvider.BASE_URL

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
                encodeDefaults = true
                explicitNulls = false
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("KtorClient", message)
                }
            }
            level = LogLevel.BODY
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 180000  // 3 minutes
            connectTimeoutMillis = 30000   // 30 secondes
            socketTimeoutMillis = 180000
        }

        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, request ->
                val clientException = exception as? ClientRequestException
                    ?: return@handleResponseExceptionWithRequest
                val exceptionResponse = clientException.response
                Log.e("KtorClient", "HTTP ${exceptionResponse.status}: ${request.url}")
            }
        }

        // ✅ Configuration par défaut
        defaultRequest {
            url(baseUrl)
        }
    }

    // ✅ CORRECTION : Le endpoint doit correspondre au backend
    suspend fun analyzeInstalledApps(dto: AnalyzeInstalledAppsDto): AnalyzeInstalledAppsResponse {
        return client.post("$baseUrl/scan/installed") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }

    // Search app by name
    suspend fun searchApp(query: String): SearchResponse {
        return client.get("$baseUrl/scan/search") {
            parameter("query", query)
        }.body()
    }

    // Get app details
    suspend fun getAppDetails(packageName: String): AppDetails {
        return client.get("$baseUrl/scan/app/$packageName").body()
    }

    // Upload and scan APK
    suspend fun scanApk(apkFile: File): ScanResult {
        return client.post("$baseUrl/scan/apk") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("apk", apkFile.readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "application/vnd.android.package-archive")
                        append(HttpHeaders.ContentDisposition, "filename=${apkFile.name}")
                    })
                }
            ))
        }.body()
    }

    // Quick metadata analysis
    suspend fun analyzeMetadata(packageName: String): ScanResult {
        return client.post("$baseUrl/scan/metadata") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("packageName" to packageName))
        }.body()
    }

    // Compare apps
    suspend fun compareApps(packageNames: List<String>): CompareResult {
        return client.post("$baseUrl/scan/compare") {
            contentType(ContentType.Application.Json)
            setBody(CompareRequest(packageNames))
        }.body()
    }

    // Get scan history
    suspend fun getScanHistory(deviceId: String): ScanHistory {
        return client.get("$baseUrl/scan/history") {
            parameter("deviceId", deviceId)
        }.body()
    }

    // Search apps in registry
    suspend fun searchAppsRegistry(query: String): SearchResponse {
        return client.get("$baseUrl/apps/search") {
            parameter("query", query)
        }.body()
    }

    // Get top safe apps
    suspend fun getTopSafeApps(): List<AppDetails> {
        return client.get("$baseUrl/apps/top/safe").body()
    }

    // Get top dangerous apps
    suspend fun getTopDangerousApps(): List<AppDetails> {
        return client.get("$baseUrl/apps/top/dangerous").body()
    }

    private fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)

        return packages.map { packageInfo ->
            AppInfo(
                packageName = packageInfo.packageName,
                name = packageInfo.applicationInfo?.loadLabel(pm).toString(),
                version = packageInfo.versionName ?: "Unknown",
                permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()
            )
        }
    }

    fun close() {
        client.close()
    }
}