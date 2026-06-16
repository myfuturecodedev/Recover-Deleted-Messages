package com.futurecode.recoverdeletedmessages.ui.review

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAStickerPreviewBinding
import com.futurecode.recoverdeletedmessages.utils.Constants
import java.io.File

class WAStickerPreviewFragment : BaseFragment<FragmentWAStickerPreviewBinding>(FragmentWAStickerPreviewBinding::inflate) {

    private var currentPath: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       // FIXED: Successfully pulled matched bundle data fields
        currentPath = arguments?.getString("stickerPath") ?: ""
        val type = arguments?.getString("stickerType") ?: "sticker"

        Log.d("PreviewSticker", "Received Path: $currentPath | Type: $type")


        binding.btnMediaHelp.setOnClickListener {
            findNavController().navigate(R.id.action_global_guideFragment)
        }

        binding.btnMediaSelectAll.setOnClickListener {
            findNavController().navigate(R.id.action_global_guideFragment)
        }

        if (currentPath.isNotEmpty()) {
            val file = File(currentPath)
            if (file.exists()) {
                // Renders the sticker graphic safely onto layout canvas target containers
                Glide.with(requireContext())
                    .load(file)
                    .placeholder(R.color.figma_close_btn_bg)
                    .error(R.color.figma_close_btn_bg)
                    .into(binding.imageSticker) // Make sure your layout xml contains this ImageView ID
            } else {
                Log.e("PreviewSticker", "Physical file directory address does not exist on storage.")
            }
        }
    }
}