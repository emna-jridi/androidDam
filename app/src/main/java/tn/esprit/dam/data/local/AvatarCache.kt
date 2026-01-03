package tn.esprit.dam.data.local


import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object AvatarCache {
    private const val TAG = "AvatarCache"
    private const val PREF_NAME = "avatar_cache"
    private const val KEY_AVATAR_FILE = "avatar_file_"

    // âœ… Use centralized config
    private const val BASE_URL = tn.esprit.dam.data.Config.ROOT_URL

    /**
     * Télécharger et mettre en cache un avatar
     * âœ… Prend le nom du fichier et construit l'URL
     */
    suspend fun cacheAvatar(
        context: Context,
        userId: String,
        avatarFileName: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📧 Caching avatar for user: $userId")

            // Vérifier si déjà en cache
            val cachedFileName = getCachedAvatarFileName(context, userId)
            if (cachedFileName == avatarFileName && FileDownloader.fileExists(context, avatarFileName)) {
                Log.d(TAG, "âœ… Avatar already cached")
                return@withContext FileDownloader.getLocalFilePath(context, avatarFileName)
            }

            // ✅ Construire l'URL complète à partir du nom du fichier
            val avatarUrl = "$BASE_URL/uploads/avatars/$avatarFileName"

            // Télécharger le nouvel avatar
            val localFile = FileDownloader.downloadFile(context, avatarUrl, avatarFileName)

            if (localFile != null) {
                // Sauvegarder les métadonnées
                saveCacheMetadata(context, userId, avatarFileName)
                Log.d(TAG, "âœ… Avatar cached successfully")
                localFile.absolutePath
            } else {
                Log.e(TAG, "âŒ Failed to cache avatar")
                null
            }

        } catch (e: Exception) {
            Log.e(TAG, "âŒ Cache error: ${e.message}", e)
            null
        }
    }

    /**
     * Récupérer le chemin local de l'avatar
     */
    fun getCachedAvatarPath(context: Context, userId: String): String? {
        val fileName = getCachedAvatarFileName(context, userId)
        return if (fileName != null && FileDownloader.fileExists(context, fileName)) {
            FileDownloader.getLocalFilePath(context, fileName)
        } else {
            null
        }
    }

    /**
     * Supprimer l'avatar du cache
     */
    fun clearAvatarCache(context: Context, userId: String) {
        val fileName = getCachedAvatarFileName(context, userId)
        if (fileName != null) {
            FileDownloader.deleteFile(context, fileName)
        }

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_AVATAR_FILE + userId)
            .apply()
    }

    /**
     * Supprimer tous les avatars du cache
     */
    fun clearAllCache(context: Context) {
        val cacheDir = File(context.cacheDir, "avatars")
        if (cacheDir.exists()) {
            cacheDir.listFiles()?.forEach { it.delete() }
        }

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    // ========== Private Methods ==========

    private fun saveCacheMetadata(
        context: Context,
        userId: String,
        fileName: String
    ) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_AVATAR_FILE + userId, fileName)
            .apply()
    }

    private fun getCachedAvatarFileName(context: Context, userId: String): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_AVATAR_FILE + userId, null)
    }
}
