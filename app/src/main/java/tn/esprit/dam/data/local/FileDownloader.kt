package tn.esprit.dam.data.local


import android.content.Context
import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

object FileDownloader {
    private const val TAG = "FileDownloader"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /**
     * Télécharger un fichier depuis une URL et le sauvegarder localement
     */
    suspend fun downloadFile(
        context: Context,
        url: String,
        fileName: String
    ): File? {
        return try {
            Log.d(TAG, "📥 Downloading: $url")

            // Télécharger le fichier
            val bytes: ByteArray = client.get(url).body()

            // Créer le dossier de cache si nécessaire
            val cacheDir = File(context.cacheDir, "avatars")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            // Sauvegarder le fichier
            val localFile = File(cacheDir, fileName)
            FileOutputStream(localFile).use { output ->
                output.write(bytes)
            }

            Log.d(TAG, "✅ Downloaded to: ${localFile.absolutePath}")
            localFile

        } catch (e: Exception) {
            Log.e(TAG, "❌ Download failed: ${e.message}", e)
            null
        }
    }

    /**
     * Supprimer un fichier du cache
     */
    fun deleteFile(context: Context, fileName: String): Boolean {
        return try {
            val file = File(File(context.cacheDir, "avatars"), fileName)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Delete failed: ${e.message}", e)
            false
        }
    }

    /**
     * Vérifier si un fichier existe dans le cache
     */
    fun fileExists(context: Context, fileName: String): Boolean {
        val file = File(File(context.cacheDir, "avatars"), fileName)
        return file.exists()
    }

    /**
     * Obtenir le chemin local d'un fichier
     */
    fun getLocalFilePath(context: Context, fileName: String): String? {
        val file = File(File(context.cacheDir, "avatars"), fileName)
        return if (file.exists()) file.absolutePath else null
    }
}