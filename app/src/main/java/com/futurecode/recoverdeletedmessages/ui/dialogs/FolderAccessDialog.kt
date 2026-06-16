package com.futurecode.recoverdeletedmessages.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.futurecode.recoverdeletedmessages.R

class FolderAccessDialog : DialogFragment() {

    companion object {
        const val REQUEST_KEY = "folder_access_result"
        private const val TAG = "FolderAccessDialog"

        fun show(fragmentManager: FragmentManager) {
            if (fragmentManager.findFragmentByTag(TAG) == null) {
                FolderAccessDialog().show(fragmentManager, TAG)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
           // .inflate(R.layout.dialog_folder_access, null)
            .inflate(R.layout.layout_storage_permission_bottom_sheet, null)

        val fm = parentFragmentManager

        view.findViewById<ImageView>(R.id.iv_close_sheet).setOnClickListener {
            fm.setFragmentResult(REQUEST_KEY, Bundle.EMPTY)
            dismiss()
        }

        view.findViewById<TextView>(R.id.btn_allow_permission).setOnClickListener {
            fm.setFragmentResult(REQUEST_KEY, Bundle.EMPTY)
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