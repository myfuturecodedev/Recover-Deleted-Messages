package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import com.futurecode.recoverdeletedmessages.adapter.AudioAdapter
import com.futurecode.recoverdeletedmessages.adapter.DocumentListAdapter
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.data.MessageEntity
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAVoiceBinding
import com.futurecode.recoverdeletedmessages.ui.dialogs.FolderAccessDialog
import com.futurecode.recoverdeletedmessages.utils.Constants
import com.futurecode.recoverdeletedmessages.utils.MediaPermissionHelper
import com.futurecode.recoverdeletedmessages.utils.SafManager
import com.futurecode.recoverdeletedmessages.utils.StoragePermissionManager
import com.futurecode.recoverdeletedmessages.utils.UiState
import com.futurecode.recoverdeletedmessages.viewModel.MediaViewModel
import com.futurecode.recoverdeletedmessages.viewModel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class WAVoiceFragment : BaseFragment<FragmentWAVoiceBinding>(FragmentWAVoiceBinding::inflate) {

    private val viewModel: MediaViewModel by viewModels { ViewModelFactory(MyApplication.app.repository) }
    private lateinit var adapter: AudioAdapter

    private val safLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) SafManager.saveUri(requireContext(), uri)
        viewModel.scanVoice(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        childFragmentManager.setFragmentResultListener(FolderAccessDialog.REQUEST_KEY, viewLifecycleOwner) { _, _ ->
            safLauncher.launch(SafManager.getInitialUri())
        }

        adapter = AudioAdapter { item ->
            val bundle = Bundle().apply {
                putString(Constants.ARG_AUDIO_PATH, item.filePath)
                putString(Constants.ARG_AUDIO_TYPE, Constants.MEDIA_TYPE_VOICE)
            }
           findNavController().navigate(R.id.action_WAAudioFragment_to_AudioPlayerFragment, bundle)
        }
        binding.rvMediaGrid.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMediaGrid.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.rvMediaGrid.adapter = adapter

        if (!SafManager.hasSafPermission(requireContext())) {
            FolderAccessDialog.show(childFragmentManager)
        }
        viewModel.scanVoice(requireContext())

        lifecycleScope.launch {
            viewModel.voiceNotes.collectLatest { items ->
                adapter.submitList(items)
                binding.cardActionFooterDeck.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        adapter.releasePlayer()
        super.onDestroyView()
    }
}
