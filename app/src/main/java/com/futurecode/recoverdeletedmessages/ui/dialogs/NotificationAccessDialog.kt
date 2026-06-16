package com.futurecode.recoverdeletedmessages.ui.dialogs
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.futurecode.recoverdeletedmessages.R

class NotificationAccessDialog : DialogFragment() {

    companion object {
        private const val TAG = "NotificationAccessDialog"

        fun show(fragmentManager: FragmentManager) {
            if (fragmentManager.findFragmentByTag(TAG) == null) {
                NotificationAccessDialog().show(fragmentManager, TAG)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_notification_access, null)

        view.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            dismiss()
        }

        view.findViewById<Button>(R.id.btnAllow).setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            } catch (e: Exception) {
                // Settings not available on this device — ignore
            }
            dismiss()
        }

        return Dialog(requireContext()).apply {
            setContentView(view)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}