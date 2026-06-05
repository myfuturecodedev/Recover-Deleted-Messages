package com.futurecode.recoverdeletedmessages.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.futurecode.recoverdeletedmessages.R

class ProgressBarUtils {
    val isLoading: Boolean
        get() = dialogBuilder != null && dialogBuilder!!.isShowing

    companion object {
        @Volatile
        var dialogBuilder: Dialog? = null

        fun showProgressDialog(context: Context) {
            // 1. Agar Activity khatam ho rhi hai ya destroy ho chuki hai, toh kuch mat karo
            if (context is Activity && (context.isFinishing || context.isDestroyed)) {
                return
            }

            // 2. Purane chal rahe dialog ko safely band karo
            safelyDismissDialog()

            // 3. Naya dialog show karo
            try {
                dialogBuilder = Dialog(context).apply {
                    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val dialogView: View = inflater.inflate(R.layout.progress_dialog, null)
                    setContentView(dialogView)
                    setCancelable(false)
                    setCanceledOnTouchOutside(false)
                    show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                dialogBuilder = null
            }
        }

        fun hideProgressDialog() {
            // Safely dismiss karo jab hide karna ho
            safelyDismissDialog()
        }

        // Ek alag se helper function jo dialog ko bina crash kiye band karega
        private fun safelyDismissDialog() {
            try {
                if (dialogBuilder?.isShowing == true) {
                    dialogBuilder?.dismiss()
                }
            } catch (e: Exception) {
                // Har tareeqe ka WindowManager ya IllegalArgumentException yahan handle ho jayega
                e.printStackTrace()
            } finally {
                // Reference clear karna zaroori hai taaki Memory Leak na ho
                dialogBuilder = null
            }
        }
    }
}