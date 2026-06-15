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
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAVideoBinding
import com.futurecode.recoverdeletedmessages.utils.MediaPermissionHelper
import com.futurecode.recoverdeletedmessages.utils.UiState
import com.futurecode.recoverdeletedmessages.viewModel.RecoveryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WAVideoFragment : BaseFragment<FragmentWAVideoBinding>(FragmentWAVideoBinding::inflate) {

    private val TAG = "WAVideoFragment_Log"
    private val viewModel: RecoveryViewModel by viewModels()
    private lateinit var mediaGridAdapter: MediaGridAdapter

    private val selectedPathsSet = mutableSetOf<String>()
    private val isBusinessMode = false

    // Handles your unified storage directory scanning permission contract cleanly
    private val permissionHelper = MediaPermissionHelper(
        fragment = this,
        isBusinessMode = isBusinessMode,
        onPermissionGranted = {
            // FIXED: Requests your unified viewmodel hub to specifically target video directory trees
            viewModel.loadStoredCategoryMedia(categoryType = "VIDEO", isBusinessMode = isBusinessMode)
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
        binding.tvMediaToolbarTitle.text = "WA Video"

        mediaGridAdapter = MediaGridAdapter(
            onCardClicked = { mediaItem ->
                if (selectedPathsSet.isNotEmpty()) {
                    handleGridSelectionToggle(mediaItem.localMediaUri)
                } else {
                    Log.d(TAG, "Open native fullscreen media player for file: ${mediaItem.localMediaUri}")
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

        // Toggles selection visibility on the bottom action deck
        binding.cardActionFooterDeck.visibility = if (selectedPathsSet.isNotEmpty()) View.VISIBLE else View.GONE
        mediaGridAdapter.updateSelectionCache(selectedPathsSet)
    }

    private fun setupActionDeckClickListeners() {
        binding.btnMediaBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.btnActionDelete.setOnClickListener {
            // FIXED: Hooked up to your unified ViewModel's bulk file deletion routine smoothly
          //  viewModel.deletePhysicalMediaFiles(selectedPathsSet.toList(), categoryType = "VIDEO", isBusinessMode = isBusinessMode)
            selectedPathsSet.clear()
            binding.cardActionFooterDeck.visibility = View.GONE
        }

        binding.btnActionShare.setOnClickListener {
            Log.d(TAG, "Triggering system send share sheet intent blocks.")
        }
    }

    private fun observeMediaScannerPipeline() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // FIXED: Listens to your new multi-media state UI flow stream instead of the old, removed audio channel stream
                viewModel.mediaUiStateFlow.collectLatest { state ->
                    when (state) {
                        is UiState.Loading -> {
                            Log.d(TAG, "Parsing local storage video directory structures...")
                            binding.rvMediaGrid.visibility = View.GONE
                        }
                        is UiState.Success -> {
                            val videosList = state.data
                            Log.d(TAG, "Success state: Found ${videosList.size} recovered videos.")
                            binding.rvMediaGrid.visibility = View.VISIBLE
                            mediaGridAdapter.submitList(videosList)
                        }
                        is UiState.Error -> {
                            Log.e(TAG, "Pipeline operation logged internal processing exceptions", state.exception)
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
        permissionHelper.checkAndRequestPermission()
    }

    override fun onPause() {
        permissionHelper.dismissPopupSilently()
        super.onPause()
    }
}