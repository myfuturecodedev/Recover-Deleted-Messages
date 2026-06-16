package com.futurecode.recoverdeletedmessages.utils


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import androidx.documentfile.provider.DocumentFile

object SafManager {

    private const val TAG = "SafManager"
    private const val PREFS = "saf_prefs"
    private const val KEY_WA_URI = "wa_media_tree_uri"

    fun hasSafPermission(context: Context): Boolean {
        val stored = getStoredUri(context) ?: return false
        val uri = Uri.parse(stored)
        return context.contentResolver.persistedUriPermissions.any {
            it.uri == uri && it.isReadPermission
        }
    }

    fun saveUri(context: Context, uri: Uri) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: Exception) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e2: Exception) {
                Log.w(TAG, "Could not take write permission, read-only: ${e2.message}")
            }
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_WA_URI, uri.toString()).apply()
    }

    private fun getStoredUri(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_WA_URI, null)

    // Returns a URI hint for the SAF picker to open at Android/media
    fun getInitialUri(): Uri? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DocumentsContract.buildDocumentUri(
                "com.android.externalstorage.documents",
                "primary:Android/media"
            )
        } else null
    } catch (e: Exception) { null }

    data class SafFile(val uri: String, val name: String, val lastModified: Long)

    // Get WA media files from SAF tree URI for a given folder name
    fun getWhatsAppMediaFiles(context: Context, folderName: String): List<SafFile> {
        if (!hasSafPermission(context)) return emptyList()
        val stored = getStoredUri(context) ?: return emptyList()
        val rootUri = Uri.parse(stored)
        val results = mutableListOf<SafFile>()

        // Try personal WhatsApp
        results.addAll(navigateAndList(context, rootUri, listOf("com.whatsapp", "WhatsApp", "Media", folderName)))
        // Try WA Business
        results.addAll(navigateAndList(context, rootUri, listOf("com.whatsapp.w4b", "WhatsApp Business", "Media", folderName)))

        return results.sortedByDescending { it.lastModified }
    }

    private fun navigateAndList(context: Context, rootUri: Uri, segments: List<String>): List<SafFile> {
        return try {
            var current: DocumentFile = DocumentFile.fromTreeUri(context, rootUri) ?: return emptyList()
            for (seg in segments) {
                current = current.findFile(seg) ?: return emptyList()
            }
            current.listFiles()
                .filter { it.isFile && !it.name.orEmpty().startsWith(".") }
                .map { SafFile(it.uri.toString(), it.name ?: "file", it.lastModified()) }
        } catch (e: Exception) {
            Log.w(TAG, "SAF navigation failed for $segments: ${e.message}")
            emptyList()
        }
    }
}
