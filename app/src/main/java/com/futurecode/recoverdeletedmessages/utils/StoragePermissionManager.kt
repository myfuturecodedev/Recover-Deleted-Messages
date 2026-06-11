package com.futurecode.recoverdeletedmessages.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.futurecode.recoverdeletedmessages.databinding.LayoutStoragePermissionBottomSheetBinding

object StoragePermissionManager {

    /**
     * Checks whether persistable file tree access authorization for the global Android/media exists.
     * Works flawlessly across all Android versions by normalizing the structural URI segments.
     */
    fun isMediaDirectoryAccessGranted(context: Context, isBusiness: Boolean): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return true

        val persistedUriPermissions = context.contentResolver.persistedUriPermissions

        for (permissionToken in persistedUriPermissions) {
            val uri = permissionToken.uri

            if (permissionToken.isReadPermission && uri.authority == "com.android.externalstorage.documents") {
                try {
                    // Extract structural document layout identity tag (e.g., "primary:Android/media")
                    val documentId = DocumentsContract.getTreeDocumentId(uri)
                    val normalizedId = documentId.replace("\\", "/").lowercase()

                    // Verifies if the granted scope covers the core 'android/media' root folder context completely
                    if (normalizedId.contains("android/media")) {
                        android.util.Log.d("StoragePermManager", "Global Media Root Permission Active: $normalizedId")
                        return true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return false
    }

    /**
     * Spawns the contextual bottom action sheet dialog overlay wrapper interface safely.
     */
    fun verifyAccessAndPromptSheet(
        context: Context,
        layoutInflater: LayoutInflater,
        isBusiness: Boolean,
        onActionTriggered: (Intent) -> Unit
    ): BottomSheetDialog? {

        if (isMediaDirectoryAccessGranted(context, isBusiness)) return null

        val dialog = BottomSheetDialog(context).apply {
            val sheetBinding = LayoutStoragePermissionBottomSheetBinding.inflate(layoutInflater)
            setContentView(sheetBinding.root)
            setCancelable(false)

            val systemNativeContainer = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            systemNativeContainer?.setBackgroundColor(android.graphics.Color.TRANSPARENT)

            sheetBinding.ivCloseSheet.setOnClickListener { dismiss() }

            sheetBinding.btnAllowPermission.setOnClickListener {
                dismiss()

                val storageProviderAuthority = "com.android.externalstorage.documents"
                val rootMediaId = "primary:Android/media"

                // Base tree structure mapping representation
                val baseRootTreeUri = Uri.parse("content://$storageProviderAuthority/tree/$rootMediaId")

                // =========================================================================
                // FIXED: VERSION-WISE SYSTEM INTENT BINDING PIPELINE
                // =========================================================================
                val targetInitialUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13, 14, 15, aur 16+ ke liye explicit multi-layer node mapping target
                    DocumentsContract.buildDocumentUriUsingTree(baseRootTreeUri, rootMediaId)
                } else {
                    // Android 11 aur 12 ke liye direct stable flat base document tree target
                    DocumentsContract.buildTreeDocumentUri(storageProviderAuthority, rootMediaId)
                }

                val targetedPickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION

                    )
                    // Forces the system engine view context layer to strictly clamp down at Android/media root screen
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, targetInitialUri)
                }
                onActionTriggered(targetedPickerIntent)
                // =========================================================================
            }
            show()
        }
        return dialog
    }
}