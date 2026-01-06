package tn.esprit.dam.features.scan.data

import android.util.Log
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Ktor-based HTTP client for data enrichment APIs
 * Supports: Google Play Scraper, Gemini AI, OpenRouter, Ollama
 */
class KtorEnrichmentClient(private val httpClient: HttpClient) {

    private val json = Json { ignoreUnknownKeys = true }
    private val TAG = "KtorEnrichmentClient"

    /**
     * Call Gemini API for app security analysis
     */
    suspend fun callGemini(prompt: String, apiKey: String): String? {
        return try {
            val response = httpClient.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent") {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "contents" to listOf(
                        mapOf("parts" to listOf(
                            mapOf("text" to prompt)
                        ))
                    )
                ))
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()
                extractGeminiText(responseBody)
            } else {
                Log.e(TAG, "Gemini API error: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API call failed: ${e.message}", e)
            null
        }
    }

    /**
     * Call OpenRouter API (supports multiple LLM models)
     */
    suspend fun callOpenRouter(
        prompt: String,
        apiKey: String,
        model: String = "openai/gpt-3.5-turbo"
    ): String? {
        return try {
            val response = httpClient.post("https://openrouter.ai/api/v1/chat/completions") {
                header("Authorization", "Bearer $apiKey")
                header("HTTP-Referer", "com.esprit.dam")
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "model" to model,
                    "messages" to listOf(
                        mapOf(
                            "role" to "user",
                            "content" to prompt
                        )
                    ),
                    "temperature" to 0.7,
                    "max_tokens" to 1000
                ))
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()
                extractOpenRouterContent(responseBody)
            } else {
                Log.e(TAG, "OpenRouter API error: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "OpenRouter API call failed: ${e.message}", e)
            null
        }
    }

    /**
     * Call local Ollama instance for private AI analysis
     */
    suspend fun callOllama(
        prompt: String,
        ollamaHost: String = "http://localhost:11434",
        model: String = "mistral"
    ): String? {
        return try {
            val response = httpClient.post("$ollamaHost/api/generate") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "model" to model,
                    "prompt" to prompt,
                    "stream" to false,
                    "num_predict" to 500
                ))
            }

            if (response.status == HttpStatusCode.OK) {
                val responseBody = response.bodyAsText()
                extractOllamaResponse(responseBody)
            } else {
                Log.e(TAG, "Ollama API error: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ollama API call failed: ${e.message}", e)
            null
        }
    }

    /**
     * Call Google Play Scraper API for app store information
     */
    suspend fun callPlayScraper(packageName: String): PlayStoreData? {
        return try {
            // Using Google Play API via scraper service
            val response = httpClient.get("https://play.google.com/store/apps/details") {
                parameter("id", packageName)
            }

            if (response.status == HttpStatusCode.OK) {
                val html = response.bodyAsText()
                parsePlayStoreHtml(html)
            } else {
                Log.w(TAG, "Play Store scrape failed: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Play Store scrape failed: ${e.message}", e)
            null
        }
    }

    // ========== Response Parsers ==========

    private fun extractGeminiText(responseBody: String): String? {
        return try {
            val jsonObject = json.parseToJsonElement(responseBody).jsonObject
            val candidates = jsonObject["candidates"]?.jsonArray
            val content = candidates?.get(0)?.jsonObject?.get("content")?.jsonObject
            val parts = content?.get("parts")?.jsonArray
            parts?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Gemini response: ${e.message}")
            null
        }
    }

    private fun extractOpenRouterContent(responseBody: String): String? {
        return try {
            val jsonObject = json.parseToJsonElement(responseBody).jsonObject
            val choices = jsonObject["choices"]?.jsonArray
            val message = choices?.get(0)?.jsonObject?.get("message")?.jsonObject
            message?.get("content")?.jsonPrimitive?.content
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse OpenRouter response: ${e.message}")
            null
        }
    }

    private fun extractOllamaResponse(responseBody: String): String? {
        return try {
            val jsonObject = json.parseToJsonElement(responseBody).jsonObject
            jsonObject["response"]?.jsonPrimitive?.content
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Ollama response: ${e.message}")
            null
        }
    }

    private fun parsePlayStoreHtml(html: String): PlayStoreData? {
        return try {
            PlayStoreData(
                title = extractFromJson(html, "\"name\":\"", "\""),
                rating = extractFromJson(html, "\"rating\":", ",").toFloatOrNull() ?: 0f,
                reviewCount = extractFromJson(html, "\"reviewCount\":", ",").toIntOrNull() ?: 0,
                description = extractFromJson(html, "\"description\":\"", "\"").take(500),
                category = extractFromJson(html, "\"category\":\"", "\"")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Play Store HTML: ${e.message}")
            null
        }
    }

    private fun extractFromJson(text: String, startPattern: String, endPattern: String): String {
        return try {
            val startIdx = text.indexOf(startPattern)
            if (startIdx < 0) return ""
            val contentStart = startIdx + startPattern.length
            val endIdx = text.indexOf(endPattern, contentStart)
            if (endIdx < 0) return ""
            text.substring(contentStart, endIdx)
        } catch (e: Exception) {
            ""
        }
    }
}
