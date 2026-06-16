package com.futurecode.recoverdeletedmessages.ui.review

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAGifsPreviewBinding
import com.futurecode.recoverdeletedmessages.viewModel.MediaViewModel
import com.futurecode.recoverdeletedmessages.viewModel.ViewModelFactory
import java.io.File

class WAGifsPreviewFragment : BaseFragment<FragmentWAGifsPreviewBinding>(FragmentWAGifsPreviewBinding::inflate) {

    private val TAG = "WAGifsPreview_Log"

    private val viewModel: MediaViewModel by viewModels { ViewModelFactory(MyApplication.app.repository) }

    private var currentGifPath: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Extract Navigation Bundle Arguments
        currentGifPath = arguments?.getString("gifPath") ?: ""
        val gifType = arguments?.getString("gifType") ?: "gif"

        Log.d(TAG, "Loaded GIF Viewport Target Path: $currentGifPath")

        if (currentGifPath.isNotEmpty()) {
            val file = File(currentGifPath)
            if (file.exists()) {
                // ✅ FIXED: Set the Toolbar filename text dynamically from the physical file name
                binding.tvPlayerFilename.text = file.name

                // Render the animated GIF asset securely using Glide's structural loop logic
                Glide.with(requireContext())
                    .asGif()
                    .load(file)
                    .placeholder(R.color.figma_close_btn_bg)
                    .error(R.color.figma_close_btn_bg)
                    .into(binding.imageGif) // XML ID 'image_gif' maps to 'imageGif' via ViewBinding
            } else {
                Log.e(TAG, "File directory address trace missing on storage.")
                binding.tvPlayerFilename.text = "GIF Preview"
            }
        }

        // 2. Setup Action Click Listeners
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Toolbar Back Arrow Button Hook
        binding.btnMediaBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Footer Action: Resend / Send onto WhatsApp Directly
        binding.btnPlayerActionResend.setOnClickListener {
            shareOrResendMediaFile(shareDirectlyToWhatsApp = true)
        }

        // Footer Action: System Native Native Content Sharing Sheet
        binding.btnPlayerActionShare.setOnClickListener {
            shareOrResendMediaFile(shareDirectlyToWhatsApp = false)
        }

        binding.btnMediaHelp.setOnClickListener {
            findNavController().navigate(R.id.action_global_guideFragment)
        }

        binding.btnMediaSelectAll.setOnClickListener {
            findNavController().navigate(R.id.action_global_guideFragment)
        }

        // Footer Action: Delete Physical File Target
        binding.btnPlayerActionDelete.setOnClickListener {
            if (currentGifPath.isNotEmpty()) {
                val file = File(currentGifPath)
                if (file.exists() && file.delete()) {
                    Log.d(TAG, "File deleted successfully from storage. Popping stack back.")
                    findNavController().popBackStack()
                }
            }
        }
    }

    /**
     * Helper system layout intent framework to handle file payloads dispatching
     */
    private fun shareOrResendMediaFile(shareDirectlyToWhatsApp: Boolean) {
        if (currentGifPath.isEmpty()) return
        val file = File(currentGifPath)
        if (!file.exists()) return

        try {
            // Using FileProvider to safely secure URI exposure bounds compliance models
            val contentUri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/gif"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (shareDirectlyToWhatsApp) {
                    setPackage("com.whatsapp") // Redirect execution loops straight to original WA core activity
                }
            }
            startActivity(Intent.createChooser(intent, "Share GIF via"))
        } catch (e: Exception) {
            Log.e(TAG, "Sharing pipeline flow execution exceptions logged", e)
        }
    }
}