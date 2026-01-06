package tn.esprit.dam.features.scan.data

import android.util.Log
import tn.esprit.dam.data.api.models.AppInfoDto
import tn.esprit.dam.data.api.models.StoreDataDto
import kotlinx.serialization.Serializable

/**
 * Service for enriching app data from multiple sources when information is incomplete.
 * Supports: Google Play Scraper, Gemini AI, OpenRouter, Ollama
 * 
 * Note: This service requires proper Retrofit API instances to be provided.
 * API configuration should be done in a separate Hilt module.
 */

@Serializable
data class PlayStoreData(
    val title: String? = null,
    val description: String? = null,
    val rating: Float? = null,
    val reviewCount: Int? = null,
    val installs: String? = null,
    val category: String? = null,
    val contentRating: String? = null,
    val developerName: String? = null,
    val developerEmail: String? = null
)

@Serializable
data class EnrichedAppInfo(
    val appInfo: AppInfoDto,
    val playStoreData: PlayStoreData? = null,
    val aiAnalysis: String? = null,
    val enrichedAt: Long = System.currentTimeMillis()
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>? = null
)

@Serializable
data class GeminiPart(
    val text: String? = null
)

@Serializable
data class OpenRouterResponse(
    val choices: List<OpenRouterChoice>? = null
)

@Serializable
data class OpenRouterChoice(
    val message: OpenRouterMessage? = null
)

@Serializable
data class OpenRouterMessage(
    val content: String? = null
)

@Serializable
data class OllamaResponse(
    val response: String? = null,
    val done: Boolean = false
)

/**
 * Main service for data enrichment
 * 
 * Usage:
 * ```
 * val enrichmentService = DataEnrichmentService(
 *     playScraperCallback = { searchTerm -> callPlayScraperApi(searchTerm) },
 *     geminiCallback = { prompt -> callGeminiApi(prompt) },
 *     openRouterCallback = { prompt -> callOpenRouterApi(prompt) },
 *     ollamaCallback = { prompt -> callOllamaApi(prompt) }
 * )
 *
 * val enrichedData = enrichmentService.enrichAppData(
 *     appInfo = appDto,
 *     sources = listOf(
 *         DataSource.PLAY_STORE_SCRAPER,
 *         DataSource.GEMINI,
 *         DataSource.OPEN_ROUTER,
 *         DataSource.OLLAMA
 *     )
 * )
 * ```
 */
class DataEnrichmentService(
    private val playScraperCallback: (suspend (String) -> PlayStoreData?)? = null,
    private val geminiCallback: (suspend (String) -> String?)? = null,
    private val openRouterCallback: (suspend (String) -> String?)? = null,
    private val ollamaCallback: (suspend (String) -> String?)? = null
) {
    companion object {
        private const val TAG = "DataEnrichmentService"
    }

    enum class DataSource {
        PLAY_STORE_SCRAPER,
        GEMINI,
        OPEN_ROUTER,
        OLLAMA
    }

    /**
     * Enriches app data from multiple sources
     */
    suspend fun enrichAppData(
        appInfo: AppInfoDto,
        sources: List<DataSource> = listOf(DataSource.PLAY_STORE_SCRAPER)
    ): EnrichedAppInfo {
        var enrichedData = EnrichedAppInfo(appInfo = appInfo)

        for (source in sources) {
            try {
                when (source) {
                    DataSource.PLAY_STORE_SCRAPER -> {
                        val storeData = enrichFromPlayStore(appInfo)
                        if (storeData != null) {
                            enrichedData = enrichedData.copy(playStoreData = storeData)
                        }
                    }
                    DataSource.GEMINI -> {
                        val analysis = enrichFromGemini(appInfo)
                        if (analysis != null) {
                            enrichedData = enrichedData.copy(aiAnalysis = analysis)
                        }
                    }
                    DataSource.OPEN_ROUTER -> {
                        val analysis = enrichFromOpenRouter(appInfo)
                        if (analysis != null) {
                            enrichedData = enrichedData.copy(aiAnalysis = analysis)
                        }
                    }
                    DataSource.OLLAMA -> {
                        val analysis = enrichFromOllama(appInfo)
                        if (analysis != null) {
                            enrichedData = enrichedData.copy(aiAnalysis = analysis)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to enrich from $source: ${e.message}")
            }
        }

        return enrichedData
    }

    private suspend fun enrichFromPlayStore(appInfo: AppInfoDto): PlayStoreData? {
        return try {
            playScraperCallback?.invoke(appInfo.displayName ?: appInfo.packageName)
        } catch (e: Exception) {
            Log.e(TAG, "PlayStore enrichment failed", e)
            null
        }
    }

    private suspend fun enrichFromGemini(appInfo: AppInfoDto): String? {
        return try {
            val prompt = buildGeminiPrompt(appInfo)
            geminiCallback?.invoke(prompt)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini enrichment failed", e)
            null
        }
    }

    private suspend fun enrichFromOpenRouter(appInfo: AppInfoDto): String? {
        return try {
            val prompt = buildOpenRouterPrompt(appInfo)
            openRouterCallback?.invoke(prompt)
        } catch (e: Exception) {
            Log.e(TAG, "OpenRouter enrichment failed", e)
            null
        }
    }

    private suspend fun enrichFromOllama(appInfo: AppInfoDto): String? {
        return try {
            val prompt = buildOllamaPrompt(appInfo)
            ollamaCallback?.invoke(prompt)
        } catch (e: Exception) {
            Log.e(TAG, "Ollama enrichment failed", e)
            null
        }
    }

    private fun buildGeminiPrompt(appInfo: AppInfoDto): String {
        return """
            Analyze the security risks for the app: ${appInfo.displayName}
            Package: ${appInfo.packageName}
            Permissions: ${appInfo.permissions.joinToString(", ")}
            Trackers: ${appInfo.trackers.map { it.name }.joinToString(", ")}
            
            Provide a concise security assessment including:
            1. Primary risk factors
            2. Data collection concerns
            3. Recommendations for users
        """.trimIndent()
    }

    private fun buildOpenRouterPrompt(appInfo: AppInfoDto): String {
        return """
            Analyze app security: ${appInfo.displayName} (${appInfo.packageName}). 
            Permissions: ${appInfo.permissions.take(5).joinToString(", ")}
            Trackers found: ${appInfo.trackers.size}
            Provide risk assessment and recommendations.
        """.trimIndent()
    }

    private fun buildOllamaPrompt(appInfo: AppInfoDto): String {
        return """
            System: You are a mobile security expert analyzing Android apps.
            
            Analyze this app for security risks:
            App: ${appInfo.displayName}
            Package: ${appInfo.packageName}
            Permissions: ${appInfo.permissions.joinToString(", ")}
            Trackers: ${appInfo.trackers.map { it.name }.joinToString(", ")}
            
            Provide a brief security assessment.
        """.trimIndent()
    }
}

