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
import androidx.recyclerview.widget.GridLayoutManager
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import com.futurecode.recoverdeletedmessages.adapter.MediaGridAdapter
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAVideoBinding
import com.futurecode.recoverdeletedmessages.ui.dialogs.FolderAccessDialog
import com.futurecode.recoverdeletedmessages.utils.Constants
import com.futurecode.recoverdeletedmessages.utils.SafManager
import com.futurecode.recoverdeletedmessages.viewModel.MediaViewModel
import com.futurecode.recoverdeletedmessages.viewModel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest

import kotlinx.coroutines.launch
import kotlin.getValue

class WAVideoFragment : BaseFragment<FragmentWAVideoBinding>(FragmentWAVideoBinding::inflate) {

    private val viewModel: MediaViewModel by viewModels { ViewModelFactory(MyApplication.app.repository) }
    private lateinit var adapter: MediaGridAdapter

    private val safLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) SafManager.saveUri(requireContext(), uri)
        viewModel.scanVideos(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnMediaBack.setOnClickListener { findNavController().popBackStack() }

        childFragmentManager.setFragmentResultListener(FolderAccessDialog.REQUEST_KEY, viewLifecycleOwner) { _, _ ->
            safLauncher.launch(SafManager.getInitialUri())
        }

        adapter = MediaGridAdapter(onItemClick = { item ->
            val bundle = Bundle().apply {
                putString(Constants.ARG_MEDIA_PATH, item.filePath)
                putString(Constants.ARG_MEDIA_TYPE, Constants.MEDIA_TYPE_VIDEO)
            }
            findNavController().navigate(R.id.action_videos_to_viewer, bundle)
        })
        binding.rvMediaGrid.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvMediaGrid.adapter = adapter

        if (!SafManager.hasSafPermission(requireContext())) {
            FolderAccessDialog.show(childFragmentManager)
        }
        viewModel.scanVideos(requireContext())

        lifecycleScope.launch {
            viewModel.videos.collectLatest { items ->
                adapter.submitList(items)
                binding.cardActionFooterDeck.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}
