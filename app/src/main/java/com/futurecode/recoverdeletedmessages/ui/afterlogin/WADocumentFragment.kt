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
import com.futurecode.recoverdeletedmessages.databinding.FragmentWADocumentBinding
import com.futurecode.recoverdeletedmessages.utils.MediaPermissionHelper
import com.futurecode.recoverdeletedmessages.utils.UiState
import com.futurecode.recoverdeletedmessages.viewModel.RecoveryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class WADocumentFragment : BaseFragment<FragmentWADocumentBinding>(FragmentWADocumentBinding::inflate) {

    private val TAG = "WADocumentFragment_Log"
    private val viewModel: RecoveryViewModel by viewModels()
    private lateinit var docListAdapter: DocumentListAdapter

    private val selectedPathsSet = mutableSetOf<String>()
    private val isBusinessMode = false

    private val permissionHelper = MediaPermissionHelper(
        fragment = this,
        isBusinessMode = isBusinessMode,
        onPermissionGranted = {
            viewModel.loadScannedMediaFiles(
                categoryType = "DOCUMENT",
                isBusinessMode = isBusinessMode
            )
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
        binding.tvMediaToolbarTitle.text = "WA Document"

        docListAdapter = DocumentListAdapter(
            onDocClicked = { docItem ->
                if (selectedPathsSet.isNotEmpty()) {
                    handleSelectionToggle(docItem.localMediaUri)
                } else {
                    Log.d(
                        TAG,
                        "Launch native document reader viewing pass: ${docItem.localMediaUri}"
                    )
                }
            },
            onDocLongPressed = { docItem ->
                handleSelectionToggle(docItem.localMediaUri)
            },
            onInlineShareClicked = { docItem ->
                Log.d(
                    TAG,
                    "Inline operational dispatch target share fired for: ${docItem.localMediaUri}"
                )
            }
        )

        binding.rvMediaGrid.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = docListAdapter
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

        binding.cardActionFooterDeck.visibility =
            if (selectedPathsSet.isNotEmpty()) View.VISIBLE else View.GONE
        docListAdapter.updateSelectionCache(selectedPathsSet)
    }

    private fun setupActionDeckClickListeners() {
        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.btnActionDelete.setOnClickListener {
            viewModel.deletePhysicalMediaFiles(
                selectedPathsSet.toList(),
                "DOCUMENT",
                isBusinessMode
            )
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
                            // Transforms and groups the files into timeline sections
                            val structuredTimelineList = sortFilesIntoTimelineSections(state.data)
                            docListAdapter.submitList(structuredTimelineList)
                        }

                        is UiState.Error -> binding.rvMediaGrid.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    /**
     * Timeline Sorter Algorithm: Parses list items and inserts row headers
     * matching your Figma layout categories (TODAY, YESTERDAY, LAST WEEK).
     */
    private fun sortFilesIntoTimelineSections(rawFiles: List<MessageEntity>): List<MessageEntity> {
        if (rawFiles.isEmpty()) return emptyList()

        val todayList = mutableListOf<MessageEntity>()
        val yesterdayList = mutableListOf<MessageEntity>()
        val lastWeekList = mutableListOf<MessageEntity>()

        val calToday = Calendar.getInstance()
        val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        rawFiles.forEach { file ->
            val fileCal = Calendar.getInstance().apply { timeInMillis = file.timestamp }

            if (fileCal.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
                fileCal.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR)
            ) {
                todayList.add(file)
            } else if (fileCal.get(Calendar.YEAR) == calYesterday.get(Calendar.YEAR) &&
                fileCal.get(Calendar.DAY_OF_YEAR) == calYesterday.get(Calendar.DAY_OF_YEAR)
            ) {
                yesterdayList.add(file)
            } else {
                lastWeekList.add(file)
            }
        }
        val compositeResult = mutableListOf<MessageEntity>()

        if (todayList.isNotEmpty()) {
            // FIXED: Provided explicit argument mappings, setting localMediaUri to null for text group dividers
            compositeResult.add(
                MessageEntity(
                    id = -1L,
                    chatId = "",
                    senderName = "",
                    textContent = "Today",
                    timestamp = 0L,
                    messageType = "HEADER",
                    localMediaUri = null,
                    isPackageBusiness = false,
                    isUnread = false
                )
            )
            compositeResult.addAll(todayList.sortedByDescending { it.timestamp })
        }

        if (yesterdayList.isNotEmpty()) {
            // FIXED: Provided explicit argument mappings
            compositeResult.add(
                MessageEntity(
                    id = -2L,
                    chatId = "",
                    senderName = "",
                    textContent = "Yesterday",
                    timestamp = 0L,
                    messageType = "HEADER",
                    localMediaUri = null,
                    isPackageBusiness = false,
                    isUnread = false
                )
            )
            compositeResult.addAll(yesterdayList.sortedByDescending { it.timestamp })
        }

        if (lastWeekList.isNotEmpty()) {
            // FIXED: Provided explicit argument mappings
            compositeResult.add(
                MessageEntity(
                    id = -3L,
                    chatId = "",
                    senderName = "",
                    textContent = "Last Week",
                    timestamp = 0L,
                    messageType = "HEADER",
                    localMediaUri = null,
                    isPackageBusiness = false,
                    isUnread = false
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