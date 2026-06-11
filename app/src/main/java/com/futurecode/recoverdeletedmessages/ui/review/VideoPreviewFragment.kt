package com.futurecode.recoverdeletedmessages.ui.review

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentVideoPreviewBinding
import java.io.File
import java.util.Locale

class VideoPreviewFragment : BaseFragment<FragmentVideoPreviewBinding>(FragmentVideoPreviewBinding::inflate) {

    private var isPlayingState = false
    private var isFullScreenMode = false
    private val handlerEngine = Handler(Looper.getMainLooper())

    // Fallback file context link path parameters
    private var targetVideoPath: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        extractArgumentsBundle()
        initializeVideoSurfaceEngine()
        setupInterfaceClickListeners()
    }

    private fun extractArgumentsBundle() {
        // Safe check argument keys loaded out of grid layout bundle configurations
        targetVideoPath = arguments?.getString("KEY_VIDEO_PATH") ?: ""
        val displayFilename = arguments?.getString("KEY_DISPLAY_NAME") ?: "VID-20260608...."
        binding.tvPlayerFilename.text = displayFilename
    }

    private fun initializeVideoSurfaceEngine() {
        if (targetVideoPath.isEmpty() || !File(targetVideoPath).exists()) {
            // Placeholder fallback resource for immediate layout presentation testing run passes
            return
        }

        binding.videoSurfaceView.setVideoURI(Uri.parse(targetVideoPath))
        binding.videoSurfaceView.setOnPreparedListener { mediaPlayer ->
            // Configure seekbar limit ranges matching target total running runtime lengths
            binding.playerSeekBar.max = binding.videoSurfaceView.duration
            binding.tvPlayerTimeTotal.text = formatTimestampString(binding.videoSurfaceView.duration)
            updateTimelineProgressLoop()
        }

        binding.videoSurfaceView.setOnCompletionListener {
            isPlayingState = false
            binding.ivPlayerCenterToggle.setImageResource(R.drawable.ic_video_play_overlay)
            binding.playerSeekBar.progress = 0
        }
    }

    private fun setupInterfaceClickListeners() {
        binding.btnMediaBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        // Center Click: Play / Pause Execution State Hooks
        binding.ivPlayerCenterToggle.setOnClickListener {
            if (binding.videoSurfaceView.isPlaying) {
                binding.videoSurfaceView.pause()
                binding.ivPlayerCenterToggle.setImageResource(R.drawable.ic_video_play_overlay)
            } else {
                binding.videoSurfaceView.start()
                binding.ivPlayerCenterToggle.setImageDrawable(null) // Hides central icon smoothly upon execution run
            }
        }

        // Top Right Expand Click: Toggle Scale layouts matching Screenshot 2026-06-09 at 2.56.21 PM.jpg
        binding.ivPlayerCenterToggle.setOnClickListener {
            isFullScreenMode = !isFullScreenMode
            val layoutParams = binding.videoSurfaceView.layoutParams as FrameLayout.LayoutParams

            if (isFullScreenMode) {
                // Expanded Scale View: Layout configuration changes fill container bounds fully
                layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
                layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
            } else {
                // Letterboxed Scale View: Default layout configuration bounds maintain natural file ratios
                layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
                layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT
                layoutParams.gravity = android.view.Gravity.CENTER
            }
            binding.videoSurfaceView.layoutParams = layoutParams
        }

        // Synchronize track progress movements matching timeline adjustments
        binding.playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.videoSurfaceView.seekTo(progress)
                    binding.tvPlayerTimeCurrent.text = formatTimestampString(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private val progressUpdaterRunnable = object : Runnable {
        override fun run() {
            if (binding.videoSurfaceView.isPlaying) {
                val currentPosition = binding.videoSurfaceView.currentPosition
                binding.playerSeekBar.progress = currentPosition
                binding.tvPlayerTimeCurrent.text = formatTimestampString(currentPosition)
            }
            handlerEngine.postDelayed(this, 1000)
        }
    }

    private fun updateTimelineProgressLoop() {
        handlerEngine.post(progressUpdaterRunnable)
    }

    private fun formatTimestampString(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        handlerEngine.removeCallbacks(progressUpdaterRunnable)
        super.onDestroyView()
    }
}