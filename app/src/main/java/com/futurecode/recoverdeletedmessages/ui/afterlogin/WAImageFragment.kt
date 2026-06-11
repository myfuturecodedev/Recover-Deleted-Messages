package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.futurecode.recoverdeletedmessages.adapter.MediaGridAdapter
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.data.MessageEntity
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAImageBinding
import com.futurecode.recoverdeletedmessages.utils.MediaPermissionHelper
import com.futurecode.recoverdeletedmessages.utils.UiState
import com.futurecode.recoverdeletedmessages.viewModel.RecoveryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.futurecode.recoverdeletedmessages.utils.StoragePermissionManager


//class WAImageFragment : BaseFragment<FragmentWAImageBinding>(FragmentWAImageBinding::inflate) {
//
//    private val TAG = "WAImageFragment_Log"
//    private val viewModel: RecoveryViewModel by viewModels()
//    private lateinit var mediaGridAdapter: MediaGridAdapter
//
//    private val selectedPathsSet = mutableSetOf<String>()
//    private var isBusinessMode = false
//
//    // =========================================================================
//    // FIXED: Added runtime flag to catch the asynchronous lifecycle racing loop
//    // =========================================================================
//    private var isWaitingForStorageCallback = false
//
//    private val permissionHelper = MediaPermissionHelper(
//        fragment = this,
//        isBusinessMode = isBusinessMode,
//        onPermissionGranted = {
//            // Reset the flag lock safely when selection comes back or triggers updates
//            this@WAImageFragment.isWaitingForStorageCallback = false
//
//            val currentMode = this@WAImageFragment.isBusinessMode
//            Log.d(TAG, "Permission granted callback triggered. Loading media files for mode Business: $currentMode")
//            viewModel.loadScannedMediaFiles(
//                categoryType = "PHOTO",
//                isBusinessMode = currentMode
//            )
//        }
//    ).apply {
//        registerLifecycleLauncher()
//    }
//    // =========================================================================
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val selectedApp = arguments?.getString("isBusinessMode") ?: "false"
//        isBusinessMode = selectedApp.uppercase() == "BUSINESS" || selectedApp.toBoolean()
//
//        permissionHelper.isBusinessMode = isBusinessMode
//        Log.d(TAG, "Selected app profile runtime resolved state: $isBusinessMode")
//
//        initializeRecyclerView()
//        setupActionDeckClickListeners()
//        observeMediaScannerPipeline()
//    }
//
//    private fun initializeRecyclerView() {
//        binding.tvMediaToolbarTitle.text = "WA Image"
//
//        mediaGridAdapter = MediaGridAdapter(
//            onCardClicked = { mediaItem ->
//                if (selectedPathsSet.isNotEmpty()) {
//                    handleGridSelectionToggle(mediaItem.localMediaUri)
//                } else {
//                    Log.d(TAG, "Open image viewer for layout file: ${mediaItem.localMediaUri}")
//                }
//            },
//            onCardLongPressed = { mediaItem ->
//                handleGridSelectionToggle(mediaItem.localMediaUri)
//            }
//        )
//
//        binding.rvMediaGrid.apply {
//            layoutManager = GridLayoutManager(requireContext(), 2)
//            adapter = mediaGridAdapter
//            setHasFixedSize(true)
//        }
//    }
//
//    private fun handleGridSelectionToggle(path: String?) {
//        if (path == null) return
//        if (selectedPathsSet.contains(path)) {
//            selectedPathsSet.remove(path)
//        } else {
//            selectedPathsSet.add(path)
//        }
//
//        binding.cardActionFooterDeck.visibility = if (selectedPathsSet.isNotEmpty()) View.VISIBLE else View.GONE
//        mediaGridAdapter.updateSelectionCache(selectedPathsSet)
//    }
//
//    private fun setupActionDeckClickListeners() {
//        binding.btnMediaBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
//
//        binding.btnActionDelete.setOnClickListener {
//            viewModel.deletePhysicalMediaFiles(selectedPathsSet.toList(), "PHOTO", isBusinessMode)
//            selectedPathsSet.clear()
//            binding.cardActionFooterDeck.visibility = View.GONE
//        }
//
//        binding.btnActionShare.setOnClickListener {
//            Log.d(TAG, "Dispatching system native content sharing sheet bundle hooks.")
//        }
//    }
//
//    private fun observeMediaScannerPipeline() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.mediaUiStateFlow.collectLatest { state ->
//                    when (state) {
//                        is UiState.Loading -> {
//                            binding.rvMediaGrid.visibility = View.GONE
//                        }
//                        is UiState.Success<*> -> {
//                            @Suppress("UNCHECKED_CAST")
//                            val filesList = state.data as? List<MessageEntity> ?: emptyList()
//                            Log.d(TAG, "Images scanned successfully. Count total: ${filesList.size}")
//
//                            binding.rvMediaGrid.visibility = View.VISIBLE
//                            mediaGridAdapter.submitList(filesList)
//                        }
//                        is UiState.Error -> {
//                            Log.e(TAG, "Fatal file exception thrown by media pipeline engine", state.exception)
//                            binding.rvMediaGrid.visibility = View.VISIBLE
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        binding.cardActionFooterDeck.visibility = View.GONE
//        Log.e("TAGppppppppp", "onResume Check Triggered")
//
//        permissionHelper.isBusinessMode = isBusinessMode
//
//        // =========================================================================
//        // FIXED LIFECYCLE BYPASS GATE:
//        // If the fragment is resuming because the user just came back from the SAF
//        // system picker, DO NOT check or trigger the bottom sheet dialog popup again.
//        // =========================================================================
//        if (!isWaitingForStorageCallback) {
//            permissionHelper.checkAndRequestPermission {
//                // This higher-order block runs exactly when the user clicks ALLOW inside your custom sheet
//                isWaitingForStorageCallback = true
//            }
//        } else {
//            Log.d(TAG, "Skipping duplicate check loop. Waiting for system SAF persistable registers to settle down.")
//        }
//        // =========================================================================
//    }
//
//    override fun onPause() {
//        permissionHelper.dismissPopupSilently()
//        super.onPause()
//    }
//}



class WAImageFragment : BaseFragment<FragmentWAImageBinding>(FragmentWAImageBinding::inflate) {

    private val TAG = "WAImageFragment_Log"
    private val viewModel: RecoveryViewModel by viewModels()
    private lateinit var mediaGridAdapter: MediaGridAdapter

    private val selectedPathsSet = mutableSetOf<String>()
    private var isBusinessMode = false

    // =========================================================================
    // FIXED: Added runtime flag to catch the asynchronous lifecycle racing loop
    // =========================================================================
    private var isWaitingForStorageCallback = false

    private val permissionHelper = MediaPermissionHelper(
        fragment = this,
        isBusinessMode = isBusinessMode,
        onPermissionGranted = {
            // Reset the flag lock safely when selection comes back or triggers updates
            this@WAImageFragment.isWaitingForStorageCallback = false

            val currentMode = this@WAImageFragment.isBusinessMode
            Log.d(TAG, "Permission granted callback triggered. Loading media files for mode Business: $currentMode")
            viewModel.loadScannedMediaFiles(
                categoryType = "PHOTO",
                isBusinessMode = currentMode
            )
        }
    ).apply {
        registerLifecycleLauncher()
    }
    // =========================================================================

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedApp = arguments?.getString("isBusinessMode") ?: "false"
        isBusinessMode = selectedApp.uppercase() == "BUSINESS" || selectedApp.toBoolean()

        permissionHelper.isBusinessMode = isBusinessMode
        Log.d(TAG, "Selected app profile runtime resolved state: $isBusinessMode")

        initializeRecyclerView()
        setupActionDeckClickListeners()
        observeMediaScannerPipeline()
    }

    private fun initializeRecyclerView() {
        binding.tvMediaToolbarTitle.text = "WA Image"

        mediaGridAdapter = MediaGridAdapter(
            onCardClicked = { mediaItem ->
                if (selectedPathsSet.isNotEmpty()) {
                    handleGridSelectionToggle(mediaItem.localMediaUri)
                } else {
                    Log.d(TAG, "Open image viewer for layout file: ${mediaItem.localMediaUri}")
                }
            },
            onCardLongPressed = { mediaItem ->
                handleGridSelectionToggle(mediaItem.localMediaUri)
            }
        )

        binding.rvMediaGrid.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = mediaGridAdapter
            setHasFixedSize(true)
        }
    }

    private fun handleGridSelectionToggle(path: String?) {
        if (path == null) return
        if (selectedPathsSet.contains(path)) {
            selectedPathsSet.remove(path)
        } else {
            selectedPathsSet.add(path)
        }

        binding.cardActionFooterDeck.visibility = if (selectedPathsSet.isNotEmpty()) View.VISIBLE else View.GONE
        mediaGridAdapter.updateSelectionCache(selectedPathsSet)
    }

    private fun setupActionDeckClickListeners() {
        binding.btnMediaBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.btnActionDelete.setOnClickListener {
            viewModel.deletePhysicalMediaFiles(selectedPathsSet.toList(), "PHOTO", isBusinessMode)
            selectedPathsSet.clear()
            binding.cardActionFooterDeck.visibility = View.GONE
        }

        binding.btnActionShare.setOnClickListener {
            Log.d(TAG, "Dispatching system native content sharing sheet bundle hooks.")
        }
    }

    private fun observeMediaScannerPipeline() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mediaUiStateFlow.collectLatest { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.rvMediaGrid.visibility = View.GONE
                        }
                        is UiState.Success<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            val filesList = state.data as? List<MessageEntity> ?: emptyList()
                            Log.d(TAG, "Images scanned successfully. Count total: ${filesList.size}")

                            binding.rvMediaGrid.visibility = View.VISIBLE
                            mediaGridAdapter.submitList(filesList)
                        }
                        is UiState.Error -> {
                            Log.e(TAG, "Fatal file exception thrown by media pipeline engine", state.exception)
                            binding.rvMediaGrid.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.cardActionFooterDeck.visibility = View.GONE

        // =========================================================================
        // FIXED LOGS ADDED: This directly checks and prints the current storage state
        // =========================================================================
        val isAllowed = StoragePermissionManager.isMediaDirectoryAccessGranted(requireContext(), isBusinessMode)
        Log.e("TAGppppppppp", "onResume Check Triggered | Is Permission Allowed: $isAllowed")
        // =========================================================================

        permissionHelper.isBusinessMode = isBusinessMode

        // =========================================================================
        // FIXED LIFECYCLE BYPASS GATE:
        // If the fragment is resuming because the user just came back from the SAF
        // system picker, DO NOT check or trigger the bottom sheet dialog popup again.
        // =========================================================================
        if (!isWaitingForStorageCallback) {
            permissionHelper.checkAndRequestPermission {
                // This higher-order block runs exactly when the user clicks ALLOW inside your custom sheet
                isWaitingForStorageCallback = true
            }
        } else {
            Log.d(TAG, "Skipping duplicate check loop. Waiting for system SAF persistable registers to settle down.")
        }
        // =========================================================================
    }

    override fun onPause() {
        permissionHelper.dismissPopupSilently()
        super.onPause()
    }
}