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
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAGIFBinding
import com.futurecode.recoverdeletedmessages.utils.MediaPermissionHelper
import com.futurecode.recoverdeletedmessages.utils.UiState
import com.futurecode.recoverdeletedmessages.viewModel.RecoveryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WAGIFFragment : BaseFragment<FragmentWAGIFBinding>(FragmentWAGIFBinding::inflate) {

    private val TAG = "WAGIFFragment_Log"

    private val viewModel: RecoveryViewModel by viewModels()
    private lateinit var mediaGridAdapter: MediaGridAdapter

    private val selectedPathsSet = mutableSetOf<String>()
    private val isBusinessMode = false // Standard WhatsApp profile target environment indicator

    // Instantiates your unified standalone permission controller abstraction layer
    private val permissionHelper = MediaPermissionHelper(
        fragment = this,
        isBusinessMode = isBusinessMode,
        onPermissionGranted = {
            // Requests your unified engine to parse WhatsApp Animated GIFs specifically
            viewModel.loadScannedMediaFiles(categoryType = "GIF", isBusinessMode = isBusinessMode)
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
        // Enforces your top toolbar panel title matching your layout requirements
        binding.tvMediaToolbarTitle.text = "WA GIF"

        mediaGridAdapter = MediaGridAdapter(
            onCardClicked = { mediaItem ->
                if (selectedPathsSet.isNotEmpty()) {
                    handleGridSelectionToggle(mediaItem.localMediaUri)
                } else {
                    Log.d(TAG, "GIF item clicked: ${mediaItem.localMediaUri}")
                }
            },
            onCardLongPressed = { mediaItem ->
                handleGridSelectionToggle(mediaItem.localMediaUri)
            }
        )

        binding.rvMediaGrid.apply {
            // Using a standard span count of 2 to keep consistency across media grids
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

        // Toggles selection visibility on the bottom action deck card container layout
        binding.cardActionFooterDeck.visibility = if (selectedPathsSet.isNotEmpty()) View.VISIBLE else View.GONE
        mediaGridAdapter.updateSelectionCache(selectedPathsSet)
    }

    private fun setupActionDeckClickListeners() {
        binding.btnMediaBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.btnActionDelete.setOnClickListener {
            // Bulk deletes checked GIFs from local disk space through the central engine hub
            viewModel.deletePhysicalMediaFiles(selectedPathsSet.toList(), "GIF", isBusinessMode)
            selectedPathsSet.clear()
            binding.cardActionFooterDeck.visibility = View.GONE
        }

        binding.btnActionShare.setOnClickListener {
            Log.d(TAG, "Dispatching system native animated GIF sharing intent bundle hooks.")
        }
    }

    private fun observeMediaScannerPipeline() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mediaUiStateFlow.collectLatest { state ->
                    when (state) {
                        is UiState.Loading -> {
                            Log.d(TAG, "Crawling animated GIF directory structures...")
                            binding.rvMediaGrid.visibility = View.GONE
                        }
                        is UiState.Success -> {
                            val filesList = state.data
                            Log.d(TAG, "GIFs scanned successfully. Count total: ${filesList.size}")

                            binding.rvMediaGrid.visibility = View.VISIBLE
                            mediaGridAdapter.submitList(filesList)
                        }
                        is UiState.Error -> {
                            Log.e(TAG, "Fatal exception thrown inside GIF pipeline engine", state.exception)
                            binding.rvMediaGrid.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Resets the contextual bottom layout bar on navigation focus return
        binding.cardActionFooterDeck.visibility = View.GONE
        permissionHelper.checkAndRequestPermission()
    }

    override fun onPause() {
        permissionHelper.dismissPopupSilently()
        super.onPause()
    }
}