package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.futurecode.recoverdeletedmessages.adapter.AudioRecoveryAdapter
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAAudioBinding
import com.futurecode.recoverdeletedmessages.viewModel.RecoveryViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WAAudioFragment : BaseFragment<FragmentWAAudioBinding>(FragmentWAAudioBinding::inflate) {

    private val TAG = "WAAudioFragment_Debug"
    private val viewModel: RecoveryViewModel by viewModels()
    private lateinit var audioAdapter: AudioRecoveryAdapter

    private var appMediaPlayerInstance: MediaPlayer? = null
    private var progressTrackerJob: Job? = null
    private var activePlaybackIndexPosition = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRecyclerView()
        setupClickListeners()
        observeFileScanningPipeline()
    }

    private fun initializeRecyclerView() {
        binding.tvAudioToolbarTitle.text = "WA Audio"
        audioAdapter = AudioRecoveryAdapter(
            onPlayTriggered = { audioEntity, position -> handleAudioPlayback(audioEntity.localMediaUri, position) },
            onRowLongPressed = { }
        )
        binding.rvAudioHistoryList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = audioAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.btnAudioBack.setOnClickListener { findNavController().navigateUp() }
    }

    /**
     * FIXED: Uses repeatOnLifecycle(Lifecycle.State.STARTED) to ensure the
     * UI remains open and receptive to the scanner's background thread updates.
     */
    private fun observeFileScanningPipeline() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
//                viewModel.audioUiStateFlow.collectLatest { state ->
//                    when (state) {
//                        is UiState.Loading -> {
//                            Log.d(TAG, "UI State: Scanner is crawling folders... Showing loader.")
//                            binding.pbAudioLoading.visibility = View.VISIBLE
//                            binding.rvAudioHistoryList.visibility = View.GONE
//                        }
//                        is UiState.Success -> {
//                            Log.d(TAG, "UI State: Scan completed! Dispatched data records count: ${state.data.size}")
//                            binding.pbAudioLoading.visibility = View.GONE
//                            binding.rvAudioHistoryList.visibility = View.VISIBLE
//                            Log.d("dfghbefrv", "observeFileScanningPipeline: ${state.data}")
//
//                            // Deliver clean payload blocks directly to list adapter view layer
//                            audioAdapter.submitList(state.data)
//                        }
//                        is UiState.Error -> {
//                            Log.e(TAG, "UI State: Scanner encountered failure parameters mapping logs", state.exception)
//                            binding.pbAudioLoading.visibility = View.GONE
//                            binding.rvAudioHistoryList.visibility = View.VISIBLE
//                        }
//                    }
//                }
//            }
//        }
    }

    override fun onResume() {
        super.onResume()
        // Force the scanning engine to crawl folders the exact millisecond the view becomes visible
        Log.d(TAG, "onResume: Triggering explicit background storage refresh pass.")
        //viewModel.loadAudioFiles(isBusinessSelected = false)
    }

    private fun handleAudioPlayback(audioPath: String?, position: Int) {
        if (activePlaybackIndexPosition == position) { stopAudioEngine(); return }
        stopAudioEngine()
        if (audioPath.isNullOrEmpty()) return
        try {
            appMediaPlayerInstance = MediaPlayer().apply {
                setDataSource(audioPath)
                setAudioStreamType(android.media.AudioManager.STREAM_MUSIC)
                prepare()
                start()
            }
            activePlaybackIndexPosition = position
            trackProgress(position)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun trackProgress(position: Int) {
        progressTrackerJob = viewLifecycleOwner.lifecycleScope.launch {
            val player = appMediaPlayerInstance ?: return@launch
            while (player.isPlaying) {
                val ratio = (player.currentPosition.toFloat() / player.duration.toFloat() * 100).toInt()
                audioAdapter.setPlaybackProgressUpdate(position, ratio)
                delay(250)
            }
            stopAudioEngine()
        }
    }

    private fun stopAudioEngine() {
        progressTrackerJob?.cancel()
        appMediaPlayerInstance?.stop()
        appMediaPlayerInstance?.release()
        appMediaPlayerInstance = null
        activePlaybackIndexPosition = -1
        audioAdapter.clearPlaybackProgressTracking()
    }

    override fun onDestroyView() {
        stopAudioEngine()
        super.onDestroyView()
    }
}