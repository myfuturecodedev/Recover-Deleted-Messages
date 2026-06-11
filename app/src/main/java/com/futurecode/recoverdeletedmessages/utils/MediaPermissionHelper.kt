package com.futurecode.recoverdeletedmessages.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

//class MediaPermissionHelper(
//    val fragment: Fragment,
//    var isBusinessMode: Boolean,
//    val onPermissionGranted: () -> Unit
//) {
//    private val TAG = "MediaPermissionHelper"
//    private var storageSheetDialog: com.google.android.material.bottomsheet.BottomSheetDialog? = null
//    private lateinit var folderPickerLauncher: ActivityResultLauncher<Intent>
//
//    /**
//     * Attaches structural initialization registers onto the parent Fragment container framework.
//     */
//    fun registerLifecycleLauncher() {
//        folderPickerLauncher = fragment.registerForActivityResult(
//            ActivityResultContracts.StartActivityForResult()
//        ) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val directoryUri = result.data?.data ?: return@registerForActivityResult
//                try {
//                    // Persist structural read access permissions securely across device reboots
//                    fragment.requireContext().contentResolver.takePersistableUriPermission(
//                        directoryUri,
//                        Intent.FLAG_GRANT_READ_URI_PERMISSION
//                    )
//                    Log.d(TAG, "SAF Persistent Directory Access Secured: $directoryUri")
//                    onPermissionGranted()
//                } catch (e: Exception) {
//                    Log.e(TAG, "Failed persisting URI storage access flags token", e)
//                }
//            }
//        }
//    }
//
//    /**
//     * Verifies system access permissions during the core application foreground sync loop pass.
//     */
//    fun checkAndRequestPermission() {
//        val context = fragment.requireContext()
//        val isAccessAllowed = StoragePermissionManager.isMediaDirectoryAccessGranted(context, isBusinessMode)
//
//        if (isAccessAllowed) {
//            storageSheetDialog?.dismiss()
//            onPermissionGranted()
//        } else {
//            storageSheetDialog = StoragePermissionManager.verifyAccessAndPromptSheet(
//                context = context,
//                layoutInflater = fragment.layoutInflater,
//                isBusiness = isBusinessMode,
//                onActionTriggered = { configurationIntent ->
//                    folderPickerLauncher.launch(configurationIntent)
//                }
//            )
//        }
//    }
//
//    /**
//     * Dismisses the layout dialog window popup layout container block safely.
//     */
//    fun dismissPopupSilently() {
//        storageSheetDialog?.dismiss()
//        storageSheetDialog = null
//    }
//}



class MediaPermissionHelper(
    val fragment: Fragment,
    var isBusinessMode: Boolean,
    val onPermissionGranted: () -> Unit
) {
    private val TAG = "MediaPermissionHelper"
    private var storageSheetDialog: com.google.android.material.bottomsheet.BottomSheetDialog? = null
    private lateinit var folderPickerLauncher: ActivityResultLauncher<Intent>

    /**
     * Attaches structural initialization registers onto the parent Fragment container framework.
     */
    fun registerLifecycleLauncher() {
        folderPickerLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val directoryUri = result.data?.data ?: return@registerForActivityResult
                try {
                    // Persist structural read access permissions securely across device reboots
                    fragment.requireContext().contentResolver.takePersistableUriPermission(
                        directoryUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    Log.d(TAG, "SAF Persistent Directory Access Secured: $directoryUri")
                    onPermissionGranted()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed persisting URI storage access flags token", e)
                }
            } else {
                // FIXED: If user cancels or backs out without allowing, trigger callback
                // to reset state variables in fragment properly
                onPermissionGranted()
            }
        }
    }

    /**
     * Verifies system access permissions during the core application foreground sync loop pass.
     * FIXED: Added higher-order function lambda parameter 'onIntentDispatched' to bypass lifecycle racing bugs.
     */
    fun checkAndRequestPermission(onIntentDispatched: () -> Unit = {}) {
        val context = fragment.requireContext()
        val isAccessAllowed = StoragePermissionManager.isMediaDirectoryAccessGranted(context, isBusinessMode)

        Log.e("TAGppppppppp", "onResume Check Triggered | Is Permission Allowed: $isAccessAllowed")

        if (isAccessAllowed) {
            storageSheetDialog?.dismiss()
            onPermissionGranted()
        } else {
            storageSheetDialog = StoragePermissionManager.verifyAccessAndPromptSheet(
                context = context,
                layoutInflater = fragment.layoutInflater,
                isBusiness = isBusinessMode,
                onActionTriggered = { configurationIntent ->
                    // FIXED: Notify fragment right before loading external activity window to safe-lock onResume checks
                    onIntentDispatched()
                    folderPickerLauncher.launch(configurationIntent)
                }
            )
        }
    }

    /**
     * Dismisses the layout dialog window popup layout container block safely.
     */
    fun dismissPopupSilently() {
        storageSheetDialog?.dismiss()
        storageSheetDialog = null
    }
}