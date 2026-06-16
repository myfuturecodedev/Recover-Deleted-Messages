package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentAudioPlayerBinding
import com.futurecode.recoverdeletedmessages.utils.Constants
import java.io.File
import android.content.Intent

import android.util.Log

import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.futurecode.recoverdeletedmessages.R

class AudioPlayerFragment : BaseFragment<FragmentAudioPlayerBinding>(FragmentAudioPlayerBinding::inflate) {

    private val TAG = "AudioPlayerFragment_Log"
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var playbackSpeed = 1.0f
    private var currentPath: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentPath = arguments?.getString(Constants.ARG_AUDIO_PATH) ?: return
        val isContentUri = currentPath.startsWith("content://")

        val title = if (isContentUri) {
            Uri.parse(currentPath).lastPathSegment
                ?.let { Uri.decode(it).substringAfterLast('/') }
                ?: "audio"
        } else {
            File(currentPath).name
        }

        binding.tvAudioTitle.text = title
        binding.ivBack.setOnClickListener { findNavController().popBackStack() }


        binding.btnPlayerHelp.setOnClickListener {
            findNavController().navigate(R.id.action_global_guideFragment)
        }

        initPlayer(currentPath, isContentUri)
        setupControls()
    }

    private fun initPlayer(path: String, isContentUri: Boolean) {
        mediaPlayer?.release()
        try {
            mediaPlayer = MediaPlayer().apply {
                if (isContentUri) {
                    setDataSource(requireContext(), Uri.parse(path))
                } else {
                    setDataSource(path)
                }
                prepare()
                setOnCompletionListener { onPlaybackComplete() }
            }
            val duration = mediaPlayer?.duration ?: 0
            binding.seekBar.max = duration
            binding.tvTotalTime.text = formatDuration(duration.toLong())
            binding.tvCurrentTime.text = "0:00"
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Cannot play this file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupControls() {
        // FIXED: Click trigger pointing onto correct XML layout ID
        binding.btnPlayerPlayPauseContainer.setOnClickListener { togglePlayback() }

        // FIXED: Speed controller decks wired to 1x, 1.5x, and 2x buttons
        binding.btnSpeed1x.setOnClickListener {
            playbackSpeed = 1.0f
            applySpeed()
            updateSpeedUI()
        }

        binding.btnSpeed15x.setOnClickListener {
            playbackSpeed = 1.5f
            applySpeed()
            updateSpeedUI()
        }

        binding.btnSpeed2x.setOnClickListener {
            playbackSpeed = 2.0f
            applySpeed()
            updateSpeedUI()
        }

        // Previous button action
        binding.btnPlayerPrev.setOnClickListener {
            mediaPlayer?.seekTo(0)
            binding.seekBar.progress = 0
            binding.tvCurrentTime.text = "0:00"
        }

        // Next button action (Fast-forwards to end bounds)
        binding.btnPlayerNext.setOnClickListener {
            val duration = mediaPlayer?.duration ?: 0
            mediaPlayer?.seekTo(duration)
            binding.seekBar.progress = duration
        }

        // Footer Actions Integration Layout
        binding.btnActionShare.setOnClickListener { shareAudioFile() }
        binding.btnActionDelete.setOnClickListener { deleteAudioFile() }
        binding.btnActionResend.setOnClickListener { shareAudioFile(directToWhatsApp = true) }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer?.seekTo(progress)
                binding.tvCurrentTime.text = formatDuration(progress.toLong())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun togglePlayback() {
        val player = mediaPlayer ?: return
        if (isPlaying) {
            player.pause()
            handler.removeCallbacks(updateSeekBar)
            binding.btnPlayerPlayPauseContainer.setImageResource(R.drawable.ic_play_preview)
        } else {
            player.start()
            applySpeed()
            handler.post(updateSeekBar)
            binding.btnPlayerPlayPauseContainer.setImageResource(R.drawable.ic_pause)
        }
        isPlaying = !isPlaying
    }

    private val updateSeekBar = object : Runnable {
        override fun run() {
            val current = mediaPlayer?.currentPosition ?: 0
            binding.seekBar.progress = current
            binding.tvCurrentTime.text = formatDuration(current.toLong())
            handler.postDelayed(this, 200)
        }
    }

    private fun applySpeed() {
        mediaPlayer?.let {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val params = it.playbackParams
                params.speed = playbackSpeed
                it.playbackParams = params
            }
        }
    }

    private fun updateSpeedUI() {
        val context = requireContext()

        // Reset all speed tab indicator designs
        binding.btnSpeed1x.apply {
            background = null
            setTextColor(ContextCompat.getColor(context, R.color.speed_btn_text_dark))
        }
        binding.btnSpeed15x.apply {
            background = null
            setTextColor(ContextCompat.getColor(context, R.color.speed_btn_text_dark))
        }
        binding.btnSpeed2x.apply {
            background = null
            setTextColor(ContextCompat.getColor(context, R.color.speed_btn_text_dark))
        }

        // Apply solid highlight style on chosen selection
        when (playbackSpeed) {
            1.0f -> binding.btnSpeed1x.apply {
                setBackgroundResource(R.drawable.bg_solid_forest_green_btn)
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
            }
            1.5f -> binding.btnSpeed15x.apply {
                setBackgroundResource(R.drawable.bg_solid_forest_green_btn)
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
            }
            2.0f -> binding.btnSpeed2x.apply {
                setBackgroundResource(R.drawable.bg_solid_forest_green_btn)
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
            }
        }
    }

    private fun shareAudioFile(directToWhatsApp: Boolean = false) {
        if (currentPath.isEmpty()) return
        val file = File(currentPath)
        if (!file.exists()) return

        try {
            val shareUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (directToWhatsApp) {
                    setPackage("com.whatsapp")
                }
            }
            startActivity(Intent.createChooser(intent, "Share Audio via"))
        } catch (e: Exception) {
            Log.e(TAG, "Share compilation logic fail", e)
        }
    }

    private fun deleteAudioFile() {
        if (currentPath.isEmpty()) return
        val file = File(currentPath)
        if (file.exists() && file.delete()) {
            Toast.makeText(requireContext(), "File deleted successfully", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun onPlaybackComplete() {
        isPlaying = false
        binding.seekBar.progress = 0
        binding.tvCurrentTime.text = "0:00"
        binding.btnPlayerPlayPauseContainer.setImageResource(R.drawable.ic_play_preview)
        handler.removeCallbacks(updateSeekBar)
    }

    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        handler.removeCallbacks(updateSeekBar)
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroyView()
    }
}
