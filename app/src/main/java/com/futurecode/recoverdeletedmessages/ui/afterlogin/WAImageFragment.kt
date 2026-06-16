package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
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
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAImageBinding
import com.futurecode.recoverdeletedmessages.ui.dialogs.FolderAccessDialog
import com.futurecode.recoverdeletedmessages.utils.Constants
import com.futurecode.recoverdeletedmessages.utils.MediaPermissionHelper
import com.futurecode.recoverdeletedmessages.utils.SafManager
import com.futurecode.recoverdeletedmessages.utils.StoragePermissionManager
import com.futurecode.recoverdeletedmessages.viewModel.MediaViewModel
import com.futurecode.recoverdeletedmessages.viewModel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import kotlin.getValue

class WAImageFragment : BaseFragment<FragmentWAImageBinding>(FragmentWAImageBinding::inflate) {

    private val viewModel: MediaViewModel by viewModels { ViewModelFactory(MyApplication.app.repository) }
    private lateinit var adapter: MediaGridAdapter

    private val safLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) SafManager.saveUri(requireContext(), uri)
        viewModel.scanImages(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnMediaBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnMediaHelp.setOnClickListener {
            findNavController().navigate(R.id.action_global_guideFragment)
        }

        binding.btnMediaSelectAll.setOnClickListener {
            findNavController().navigate(R.id.action_global_guideFragment)
        }

        childFragmentManager.setFragmentResultListener(FolderAccessDialog.REQUEST_KEY, viewLifecycleOwner) { _, _ ->
            safLauncher.launch(SafManager.getInitialUri())
        }

        adapter = MediaGridAdapter(
            onItemClick = { item ->

                val bundle = Bundle().apply {
                    putString(Constants.ARG_MEDIA_PATH, item.filePath)
                    putString(Constants.ARG_MEDIA_TYPE, Constants.MEDIA_TYPE_IMAGE)
                }

                Log.d("TAGIIIIIIIIIIIIII", "onViewCreated: $item")
               // findNavController().navigate(R.id.action_photos_to_viewer, bundle)
            }
        )

        binding.rvMediaGrid.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvMediaGrid.adapter = adapter

        if (!SafManager.hasSafPermission(requireContext())) {
            FolderAccessDialog.show(childFragmentManager)
        }
        viewModel.scanImages(requireContext())

        lifecycleScope.launch {
            viewModel.images.collectLatest { items ->
                adapter.submitList(items)
                binding.cardActionFooterDeck.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.btnActionShare.setOnClickListener { shareSelected() }
        binding.btnActionDelete.setOnClickListener { deleteSelected() }
    }

    private fun shareSelected() {
        val selected = adapter.getSelectedItems()
        if (selected.isEmpty()) return
        val uris = ArrayList<Uri>()
        selected.forEach { item ->
            val uri = if (item.filePath.startsWith("content://")) {
                Uri.parse(item.filePath)
            } else {
                val file = File(item.filePath)
                if (!file.exists()) return@forEach
                FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
            }
            uris.add(uri)
        }
        if (uris.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share"))
        }
    }

    private fun deleteSelected() {
        adapter.getSelectedItems().forEach { item ->
            viewModel.deleteMedia(item)
            if (!item.filePath.startsWith("content://")) File(item.filePath).delete()
        }
        adapter.clearSelection()
    }
}