package tn.esprit.dam.core.util
import timber.log.Timber

import android.content.Context
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

object FileDownloader {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun downloadFile(
        context: Context,
        url: String,
        fileName: String
    ): File? {
        return try {
            val bytes: ByteArray = client.get(url).body()

            val cacheDir = File(context.cacheDir, "avatars")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val localFile = File(cacheDir, fileName)
            FileOutputStream(localFile).use { output ->
                output.write(bytes)
            }

            localFile
        } catch (e: Exception) {
            Timber.w(e, "FileDownloader.downloadFile failed")
            null
        }
    }

    fun deleteFile(context: Context, fileName: String): Boolean {
        return try {
            val file = File(File(context.cacheDir, "avatars"), fileName)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.w(e, "FileDownloader.deleteFile failed")
            false
        }
    }

    fun fileExists(context: Context, fileName: String): Boolean {
        val file = File(File(context.cacheDir, "avatars"), fileName)
        return file.exists()
    }

    fun getLocalFilePath(context: Context, fileName: String): String? {
        val file = File(File(context.cacheDir, "avatars"), fileName)
        return if (file.exists()) file.absolutePath else null
    }
}
