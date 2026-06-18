package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
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
import com.futurecode.recoverdeletedmessages.adapters.MediaGridAdapter
import com.futurecode.recoverdeletedmessages.ads.interstitial_ad.FullScreenAdsHelper
import com.futurecode.recoverdeletedmessages.ads.native_ad.NativeAdsHelper
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAImageBinding
import com.futurecode.recoverdeletedmessages.ui.dialogs.FolderAccessDialog
import com.futurecode.recoverdeletedmessages.utils.Constants
import com.futurecode.recoverdeletedmessages.utils.MediaPermissionHelper
import com.futurecode.recoverdeletedmessages.utils.SafManager
import com.futurecode.recoverdeletedmessages.utils.StoragePermissionManager
import com.futurecode.recoverdeletedmessages.utils.Utils.setAdClickListener
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

    private var isSelectAll = false

    private lateinit var nativeAdsHelper: NativeAdsHelper
    lateinit var fullScreenAdsHelper: FullScreenAdsHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nativeAdsHelper= NativeAdsHelper(requireActivity())
        fullScreenAdsHelper= FullScreenAdsHelper(requireActivity())

        binding.btnMediaBack.setOnClickListener { findNavController().popBackStack() }


        binding.btnMediaHelp.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            findNavController().navigate(R.id.action_global_guideFragment)
        }


        binding.btnMediaSelectAll.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            if (::adapter.isInitialized && adapter.currentList.isNotEmpty()) {

                // 1. Dynamic conditional inversion loop
                isSelectAll = !isSelectAll

                // 2. Trigger the underlying adapter layout calculations
                adapter.toggleSelectAll()

                // 3. ✅ FIXED: Changing imageTintList instead of backgroundTintList for ImageView
                // ✅ FIXED: Direct vector image swap engine without tints filters
                if (isSelectAll) {
                    binding.btnMediaSelectAll.setImageResource(R.drawable.select_menu)
                } else {
                    binding.btnMediaSelectAll.setImageResource(R.drawable.ic_menu)
                }

            } else {
                Toast.makeText(requireContext(), "No items available to select", Toast.LENGTH_SHORT).show()
            }
        }


        childFragmentManager.setFragmentResultListener(FolderAccessDialog.REQUEST_KEY, viewLifecycleOwner) { _, _ ->
            safLauncher.launch(SafManager.getInitialUri())
        }


        adapter = MediaGridAdapter(
            onCardClicked = { item ->
                val bundle = Bundle().apply {
                    putString(Constants.ARG_MEDIA_PATH, item.filePath)
                    putString(Constants.ARG_MEDIA_TYPE, Constants.MEDIA_TYPE_IMAGE)
                }
                Log.d("WAStickerFragment", "Navigating with Path: ${item.filePath}")
              //  findNavController().navigate(R.id.action_photos_to_viewer, bundle)
            },
            onCardLongPressed = { item ->
                // Automatically matches runtime select-all icon configurations state if clicked one by one
                if (::adapter.isInitialized) {
                    isSelectAll = adapter.getSelectedItems().size == adapter.currentList.size

                    // ✅ FIXED: Synergize image frames while clicking grid layout cells manually
                    if (isSelectAll) {
                        binding.btnMediaSelectAll.setImageResource(R.drawable.select_menu)
                    } else {
                        binding.btnMediaSelectAll.setImageResource(R.drawable.ic_menu)
                    }
                }
            },
            onAdBindingTriggered = { adBinding ->
                try {
                    // Renders the internal ad layouts vectors inside the injected position spaces matches
                    nativeAdsHelper.showNativeAd(
                        nativeBannerAdView = adBinding.frameLayout,
                        mainLayout = adBinding.relativeLayout,
                        placeholder = adBinding.placeholder
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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

        binding.btnActionShare.setAdClickListener(requireActivity(), fullScreenAdsHelper) { shareSelected() }
        binding.btnActionDelete.setAdClickListener(requireActivity(), fullScreenAdsHelper) { deleteSelected() }
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