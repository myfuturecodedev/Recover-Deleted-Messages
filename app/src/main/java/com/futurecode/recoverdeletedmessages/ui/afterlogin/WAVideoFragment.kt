package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import com.futurecode.recoverdeletedmessages.adapters.MediaGridAdapter
import com.futurecode.recoverdeletedmessages.ads.interstitial_ad.FullScreenAdsHelper
import com.futurecode.recoverdeletedmessages.ads.native_ad.NativeAdsHelper
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAVideoBinding
import com.futurecode.recoverdeletedmessages.ui.dialogs.FolderAccessDialog
import com.futurecode.recoverdeletedmessages.utils.Constants
import com.futurecode.recoverdeletedmessages.utils.SafManager
import com.futurecode.recoverdeletedmessages.utils.Utils.setAdClickListener
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
    private var isSelectAll = false
    private lateinit var nativeAdsHelper: NativeAdsHelper
    private lateinit var fullScreenAdsHelper: FullScreenAdsHelper



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnMediaBack.setOnClickListener { findNavController().popBackStack() }
        nativeAdsHelper= NativeAdsHelper(requireActivity())
        fullScreenAdsHelper= FullScreenAdsHelper(requireActivity())

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

//        adapter = MediaGridAdapter(onItemClick = { item ->
//            val bundle = Bundle().apply {
//                putString(Constants.ARG_MEDIA_PATH, item.filePath)
//                putString(Constants.ARG_MEDIA_TYPE, Constants.MEDIA_TYPE_VIDEO)
//            }
//            findNavController().navigate(R.id.action_videos_to_viewer, bundle)
//        })



        // Initialize adapter instance
        adapter = MediaGridAdapter(
            onCardClicked = { item ->
                val bundle = Bundle().apply {
                    putString(Constants.ARG_MEDIA_PATH, item.filePath)
                    putString(Constants.ARG_MEDIA_TYPE, Constants.MEDIA_TYPE_VIDEO)
                }
                Log.d("WAStickerFragment", "Navigating with Path: ${item.filePath}")
                findNavController().navigate(R.id.action_videos_to_viewer, bundle)
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
                    // ✅ FIXED: Changed variable references to match your exact working IDs (.frame and .mainLayout)
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





        // =========================================================================
        // ✅ MASTER FIX: GRID MANAGER WITH DYNAMIC SPAN SIZE LOOKUP FOR ADS
        // =========================================================================
        val gridLayoutManager = GridLayoutManager(requireContext(), 3)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    1 -> 3 // Ad wali row poore 3 columns span block legi (Full Width)
                    else -> 1 // Normal stickers single cell grid check maintain karenge
                }
            }
        }
        binding.rvMediaGrid.layoutManager = gridLayoutManager
        binding.rvMediaGrid.adapter = adapter

        if (!SafManager.hasSafPermission(requireContext())) {
            FolderAccessDialog.show(childFragmentManager)
        }
        viewModel.scanVideos(requireContext())

        lifecycleScope.launch {
            viewModel.videos.collectLatest { items ->
              //  adapter.submitList(items)
                adapter.submitMediaWithAds(items)

                binding.cardActionFooterDeck.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}
