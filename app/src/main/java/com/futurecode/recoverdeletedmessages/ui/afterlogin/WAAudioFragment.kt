package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import com.futurecode.recoverdeletedmessages.adapter.AudioAdapter
import com.futurecode.recoverdeletedmessages.adapter.AudioRecoveryAdapter
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAAudioBinding
import com.futurecode.recoverdeletedmessages.ui.dialogs.FolderAccessDialog
import com.futurecode.recoverdeletedmessages.utils.Constants
import com.futurecode.recoverdeletedmessages.utils.SafManager
import com.futurecode.recoverdeletedmessages.viewModel.MediaViewModel
import com.futurecode.recoverdeletedmessages.viewModel.ViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WAAudioFragment : BaseFragment<FragmentWAAudioBinding>(FragmentWAAudioBinding::inflate) {

    private val TAG = "WAAudioFragment_Debug"
    //private val viewModel: RecoveryViewModel by viewModels()
    private lateinit var audioAdapter: AudioRecoveryAdapter

    private var appMediaPlayerInstance: MediaPlayer? = null
    private var progressTrackerJob: Job? = null
    private var activePlaybackIndexPosition = -1


    private val viewModel: MediaViewModel by viewModels { ViewModelFactory(MyApplication.app.repository) }
    private lateinit var adapter: AudioAdapter

    private val safLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) SafManager.saveUri(requireContext(), uri)
        viewModel.scanAudios(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAudioBack.setOnClickListener { findNavController().popBackStack() }

        childFragmentManager.setFragmentResultListener(FolderAccessDialog.REQUEST_KEY, viewLifecycleOwner) { _, _ ->
            safLauncher.launch(SafManager.getInitialUri())
        }

        adapter = AudioAdapter { item ->
            val bundle = Bundle().apply {
                putString(Constants.ARG_AUDIO_PATH, item.filePath)
                putString(Constants.ARG_AUDIO_TYPE, Constants.MEDIA_TYPE_AUDIO)
            }
            findNavController().navigate(R.id.action_WAAudioFragment_to_AudioPlayerFragment, bundle)
        }
        binding.rvAudioHistoryList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAudioHistoryList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.rvAudioHistoryList.adapter = adapter

        if (!SafManager.hasSafPermission(requireContext())) {
            FolderAccessDialog.show(childFragmentManager)
        }
        viewModel.scanAudios(requireContext())

        lifecycleScope.launch {
            viewModel.audios.collectLatest { items ->
                adapter.submitList(items)
              //  binding.pbAudioLoading.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        adapter.releasePlayer()
        super.onDestroyView()
    }
}

