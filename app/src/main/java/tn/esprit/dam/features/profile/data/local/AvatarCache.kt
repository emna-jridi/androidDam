package tn.esprit.dam.features.profile.data.local
import timber.log.Timber

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import tn.esprit.dam.core.util.FileDownloader

object AvatarCache {
    private const val PREF_NAME = "avatar_cache"
    private const val KEY_AVATAR_FILE = "avatar_file_"

    private const val BASE_URL = tn.esprit.dam.core.network.Config.ROOT_URL

    suspend fun cacheAvatar(
        context: Context,
        userId: String,
        avatarFileName: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val cachedFileName = getCachedAvatarFileName(context, userId)
            if (cachedFileName == avatarFileName && FileDownloader.fileExists(context, avatarFileName)) {
                return@withContext FileDownloader.getLocalFilePath(context, avatarFileName)
            }

            val avatarUrl = tn.esprit.dam.core.network.Config.Endpoints.avatarUploadUrl(avatarFileName)

            val localFile = FileDownloader.downloadFile(context, avatarUrl, avatarFileName)

            if (localFile != null) {
                saveCacheMetadata(context, userId, avatarFileName)
                localFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.w(e, "AvatarCache.cacheAvatar failed")
            null
        }
    }

    fun getCachedAvatarPath(context: Context, userId: String): String? {
        val fileName = getCachedAvatarFileName(context, userId)
        return if (fileName != null && FileDownloader.fileExists(context, fileName)) {
            FileDownloader.getLocalFilePath(context, fileName)
        } else {
            null
        }
    }

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

/** Injectable entry point to [AvatarCache], so ViewModels don't need a Context. */
@Singleton
class AvatarStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun cacheAvatar(userId: String, avatarFileName: String): String? =
        AvatarCache.cacheAvatar(context, userId, avatarFileName)

    fun clearAll() = AvatarCache.clearAllCache(context)
}
