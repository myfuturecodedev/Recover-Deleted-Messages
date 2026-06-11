package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.futurecode.recoverdeletedmessages.adapter.DocumentListAdapter
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.data.MessageEntity
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAVoiceBinding
import com.futurecode.recoverdeletedmessages.utils.MediaPermissionHelper
import com.futurecode.recoverdeletedmessages.utils.UiState
import com.futurecode.recoverdeletedmessages.viewModel.RecoveryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class WAVoiceFragment : BaseFragment<FragmentWAVoiceBinding>(FragmentWAVoiceBinding::inflate) {
    private val TAG = "WAAudioFragment_Log"
    private val viewModel: RecoveryViewModel by viewModels()
    private lateinit var audioListAdapter: DocumentListAdapter

    private val selectedPathsSet = mutableSetOf<String>()
    private val isBusinessMode = false

    // Sets up the unified permission contract helper to target your audio folders
    private val permissionHelper = MediaPermissionHelper(
        fragment = this,
        isBusinessMode = isBusinessMode,
        onPermissionGranted = {
            viewModel.loadScannedMediaFiles(categoryType = "AUDIO", isBusinessMode = isBusinessMode)
        }
    ).apply {
        registerLifecycleLauncher()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRecyclerView()
        setupActionDeckClickListeners()
        observeMediaScannerPipeline()
    }

    private fun initializeRecyclerView() {
        binding.tvMediaToolbarTitle.text = "WA Audio"

        // Audio files reuse DocumentListAdapter since the item design matches the custom row specification
        audioListAdapter = DocumentListAdapter(
            onDocClicked = { audioItem ->
                if (selectedPathsSet.isNotEmpty()) {
                    handleSelectionToggle(audioItem.localMediaUri)
                } else {
                    Log.d(TAG, "Triggering audio playback listener stream for: ${audioItem.localMediaUri}")
                }
            },
            onDocLongPressed = { audioItem ->
                handleSelectionToggle(audioItem.localMediaUri)
            },
            onInlineShareClicked = { audioItem ->
                Log.d(TAG, "Inline operational share event triggered for file path: ${audioItem.localMediaUri}")
            }
        )

        binding.rvMediaGrid.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = audioListAdapter
            setHasFixedSize(true)
        }
    }

    private fun handleSelectionToggle(path: String?) {
        if (path == null) return
        if (selectedPathsSet.contains(path)) {
            selectedPathsSet.remove(path)
        } else {
            selectedPathsSet.add(path)
        }

        binding.cardActionFooterDeck.visibility = if (selectedPathsSet.isNotEmpty()) View.VISIBLE else View.GONE
        audioListAdapter.updateSelectionCache(selectedPathsSet)
    }

    private fun setupActionDeckClickListeners() {
        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.btnActionDelete.setOnClickListener {
            viewModel.deletePhysicalMediaFiles(selectedPathsSet.toList(), "AUDIO", isBusinessMode)
            selectedPathsSet.clear()
            binding.cardActionFooterDeck.visibility = View.GONE
        }
    }

    private fun observeMediaScannerPipeline() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mediaUiStateFlow.collectLatest { state ->
                    when (state) {
                        is UiState.Loading -> binding.rvMediaGrid.visibility = View.GONE
                        is UiState.Success -> {
                            binding.rvMediaGrid.visibility = View.VISIBLE
                            // Automatically maps audio entities under clean chronological headings
                            val structuredTimelineList = sortAudioIntoTimelineSections(state.data)
                            audioListAdapter.submitList(structuredTimelineList)
                        }
                        is UiState.Error -> binding.rvMediaGrid.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    /**
     * Timeline Sorter Algorithm: Parses raw listings and inserts row group headers
     * matching your Figma layout precisely (TODAY, YESTERDAY, LAST WEEK).
     */
    private fun sortAudioIntoTimelineSections(rawFiles: List<MessageEntity>): List<MessageEntity> {
        if (rawFiles.isEmpty()) return emptyList()

        val todayList = mutableListOf<MessageEntity>()
        val yesterdayList = mutableListOf<MessageEntity>()
        val lastWeekList = mutableListOf<MessageEntity>()

        val calToday = Calendar.getInstance()
        val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        rawFiles.forEach { file ->
            val fileCal = Calendar.getInstance().apply { timeInMillis = file.timestamp }

            if (fileCal.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
                fileCal.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR)) {
                todayList.add(file)
            } else if (fileCal.get(Calendar.YEAR) == calYesterday.get(Calendar.YEAR) &&
                fileCal.get(Calendar.DAY_OF_YEAR) == calYesterday.get(Calendar.DAY_OF_YEAR)) {
                yesterdayList.add(file)
            } else {
                lastWeekList.add(file)
            }
        }

        val compositeResult = mutableListOf<MessageEntity>()

        if (todayList.isNotEmpty()) {
            compositeResult.add(
                MessageEntity(
                    id = -1L, chatId = "", senderName = "", textContent = "Today",
                    timestamp = 0L, messageType = "HEADER", localMediaUri = null,
                    isPackageBusiness = false, isUnread = false
                )
            )
            compositeResult.addAll(todayList.sortedByDescending { it.timestamp })
        }
        if (yesterdayList.isNotEmpty()) {
            compositeResult.add(
                MessageEntity(
                    id = -2L, chatId = "", senderName = "", textContent = "Yesterday",
                    timestamp = 0L, messageType = "HEADER", localMediaUri = null,
                    isPackageBusiness = false, isUnread = false
                )
            )
            compositeResult.addAll(yesterdayList.sortedByDescending { it.timestamp })
        }
        if (lastWeekList.isNotEmpty()) {
            compositeResult.add(
                MessageEntity(
                    id = -3L, chatId = "", senderName = "", textContent = "Last Week",
                    timestamp = 0L, messageType = "HEADER", localMediaUri = null,
                    isPackageBusiness = false, isUnread = false
                )
            )
            compositeResult.addAll(lastWeekList.sortedByDescending { it.timestamp })
        }

        return compositeResult
    }

    override fun onResume() {
        super.onResume()
        binding.cardActionFooterDeck.visibility = View.GONE
        permissionHelper.checkAndRequestPermission()
    }

    override fun onPause() {
        permissionHelper.dismissPopupSilently()
        super.onPause()
    }
}