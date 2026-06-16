package com.futurecode.recoverdeletedmessages.ui.review

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentAudioPreviewBinding

class AudioPreviewFragment : BaseFragment<FragmentAudioPreviewBinding>(FragmentAudioPreviewBinding::inflate) {
    private val TAG = "AudioPlayerFragment_Log"
    private var isPlaying = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        extractBundleArguments()
        initializeClickListeners()

        binding.btnPlayerHelp.setOnClickListener {
            findNavController().navigate(R.id.action_global_guideFragment)
        }
    }

    private fun extractBundleArguments() {
        val displayTitle = arguments?.getString("KEY_AUDIO_TITLE") ?: "Audio-3554544...."
        binding.tvPlayerFilename.text = displayTitle

        // Setup initial default playback vector graphic asset state
        binding.btnPlayerPlayPauseContainer.setImageResource(R.drawable.ic_cross) // Renders the explicit pause symbol initially
    }

    private fun initializeClickListeners() {
        binding.btnPlayerBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Center Click: Toggle Play / Pause state switches
        binding.btnPlayerPlayPauseContainer.setOnClickListener {
            isPlaying = !isPlaying
            if (isPlaying) {
                // If audio is active, set the display to show the play icon arrow segment shape
                binding.btnPlayerPlayPauseContainer.setImageResource(R.drawable.ic_video_play_overlay)
                Log.d(TAG, "Audio Stream Paused")
            } else {
                // If audio is paused, swap resource back to show the default double vertical bar pause track
                binding.btnPlayerPlayPauseContainer.setImageResource(R.drawable.ic_cross)
                Log.d(TAG, "Audio Stream Playing")
            }
        }

        // Speed Chip Modifier Toggle Clicks Matrix
        binding.btnSpeed1x.setOnClickListener { updateSpeedSelectionState(currentSelected = 1) }
        binding.btnSpeed15x.setOnClickListener { updateSpeedSelectionState(currentSelected = 2) }
        binding.btnSpeed2x.setOnClickListener { updateSpeedSelectionState(currentSelected = 3) }

        // Core Action Deck Deletion Button Click Hook
        binding.btnActionDelete.setOnClickListener {
            Log.w(TAG, "Requesting backend layout engine to remove file uri path link.")
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun updateSpeedSelectionState(currentSelected: Int) {
        val context = requireContext()

        // Reset all speed text buttons to the unselected styling rules
        binding.btnSpeed1x.apply {
            background = null
            setTextColor(context.getColor(R.color.speed_btn_text_dark))
        }
        binding.btnSpeed15x.apply {
            background = null
            setTextColor(context.getColor(R.color.speed_btn_text_dark))
        }
        binding.btnSpeed2x.apply {
            background = null
            setTextColor(context.getColor(R.color.speed_btn_text_dark))
        }

        // Apply active background shapes and colors to the selected option
        when (currentSelected) {
            1 -> {
                binding.btnSpeed1x.setBackgroundResource(R.drawable.bg_solid_forest_green_btn)
                binding.btnSpeed1x.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                Log.d(TAG, "Playback speed scale configured to: 1.0x")
            }
            2 -> {
                binding.btnSpeed15x.setBackgroundResource(R.drawable.bg_solid_forest_green_btn)
                binding.btnSpeed15x.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                Log.d(TAG, "Playback speed scale configured to: 1.5x")
            }
            3 -> {
                binding.btnSpeed2x.setBackgroundResource(R.drawable.bg_solid_forest_green_btn)
                binding.btnSpeed2x.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                Log.d(TAG, "Playback speed scale configured to: 2.0x")
            }
        }
    }
}