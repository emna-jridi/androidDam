package tn.esprit.dam.features.vault.data.ai

import tn.esprit.dam.features.vault.domain.password.PasswordAnalysisMetrics
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import tn.esprit.dam.features.vault.data.repository.VaultRepository
import javax.inject.Inject

@Serializable
data class OllamaAdvice(
    val summary: String,
    val recommendations: List<String>,
    val tone: String
)

class OllamaPasswordAdvisor @Inject constructor(
    private val repository: VaultRepository
) {
    suspend fun getAdvice(metrics: PasswordAnalysisMetrics): OllamaAdvice {
        return repository.analyzeWithAi(metrics)
            .getOrElse {
                val isStrong = metrics.score >= 70
                OllamaAdvice(
                    summary = if (isStrong) "Local Analysis: Excellent password strength detected."
                              else "Local Analysis: Password could be strengthened.",
                    recommendations = if (isStrong) listOf("Store securely in Vault")
                                      else metrics.issues.take(3).ifEmpty { listOf("Increase length", "Use mixed characters") },
                    tone = if (isStrong) "positive" else "educational"
                )
            }
    }
}
