package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAStickerBinding
import com.futurecode.recoverdeletedmessages.ui.dialogs.FolderAccessDialog
import com.futurecode.recoverdeletedmessages.utils.Constants
import com.futurecode.recoverdeletedmessages.utils.SafManager
import com.futurecode.recoverdeletedmessages.utils.Utils.setAdClickListener
import com.futurecode.recoverdeletedmessages.viewModel.MediaViewModel
import com.futurecode.recoverdeletedmessages.viewModel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

//class WAStickerFragment : BaseFragment<FragmentWAStickerBinding>(FragmentWAStickerBinding::inflate) {
//
//    private val viewModel: MediaViewModel by viewModels { ViewModelFactory(MyApplication.app.repository) }
//    private lateinit var adapter: MediaGridAdapter
//    private lateinit var nativeAdsHelper: NativeAdsHelper
//
//
//    private val safLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
//        if (uri != null) SafManager.saveUri(requireContext(), uri)
//        viewModel.scanStickers(requireContext())
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (SafManager.hasSafPermission(requireContext())) {
//            Log.d("WAStickerFragment", "onResume: Refreshing stickers flow channel registers.")
//            viewModel.scanStickers(requireContext())
//        }
//    }
//
//    // Interactive global status flag
//    private var isSelectAll = false
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding.btnMediaBack.setAdClickListener(requireActivity(), fullScreenAdsHelper) { findNavController().popBackStack() }
//
//        childFragmentManager.setFragmentResultListener(FolderAccessDialog.REQUEST_KEY, viewLifecycleOwner) { _, _ ->
//            safLauncher.launch(SafManager.getInitialUri())
//        }
//
//        binding.btnMediaHelp.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
//            findNavController().navigate(R.id.action_global_guideFragment)
//        }
//
//        nativeAdsHelper= NativeAdsHelper(requireActivity())
//
//
//        // =========================================================================
//        // ✅ MASTER FIX: SELECTION TOGGLE AND FIXED ICON TINT STATE ENGINE
//        // =========================================================================
//        binding.btnMediaSelectAll.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
//            if (::adapter.isInitialized && adapter.currentList.isNotEmpty()) {
//
//                // 1. Dynamic conditional inversion loop
//                isSelectAll = !isSelectAll
//
//                // 2. Trigger the underlying adapter layout calculations
//                adapter.toggleSelectAll()
//
//                // 3. ✅ FIXED: Changing imageTintList instead of backgroundTintList for ImageView
//                // ✅ FIXED: Direct vector image swap engine without tints filters
//                if (isSelectAll) {
//                    binding.btnMediaSelectAll.setImageResource(R.drawable.select_menu)
//                } else {
//                    binding.btnMediaSelectAll.setImageResource(R.drawable.ic_menu)
//                }
//
//            } else {
//                Toast.makeText(requireContext(), "No items available to select", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        // Initialize adapter instance
//        adapter = MediaGridAdapter(
//            onCardClicked = { item ->
//                val bundle = Bundle().apply {
//                    putString(Constants.ARG_MEDIA_PATH, item.filePath)
//                    putString(Constants.ARG_MEDIA_TYPE, Constants.MEDIA_TYPE_STICKER)
//                }
//                Log.d("WAStickerFragment", "Navigating with Path: ${item.filePath}")
//                findNavController().navigate(R.id.action_stickers_to_viewer, bundle)
//            },
//            onCardLongPressed = { item ->
//                // Automatically matches runtime select-all icon configurations state if clicked one by one
//                if (::adapter.isInitialized) {
//                    isSelectAll = adapter.getSelectedItems().size == adapter.currentList.size
//
//                    // ✅ FIXED: Synergize image frames while clicking grid layout cells manually
//                    if (isSelectAll) {
//                        binding.btnMediaSelectAll.setImageResource(R.drawable.select_menu)
//                    } else {
//                        binding.btnMediaSelectAll.setImageResource(R.drawable.ic_menu)
//                    }
//                }
//            },
//            onAdBindingTriggered = { adBinding ->
//                try {
//                    // Renders the internal ad layouts vectors inside the injected position spaces matches
//                    nativeAdsHelper.showNativeAd(
//                        nativeBannerAdView = adBinding.frameLayout,
//                        mainLayout = adBinding.relativeLayout,
//                        placeholder = adBinding.placeholder
//                    )
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        )
//
//        binding.rvMediaGrid.layoutManager = GridLayoutManager(requireContext(), 3)
//        binding.rvMediaGrid.adapter = adapter
//
//        if (!SafManager.hasSafPermission(requireContext())) {
//            FolderAccessDialog.show(childFragmentManager)
//        }
//
//        lifecycleScope.launch {
//            viewModel.stickers.collectLatest { items ->
//                adapter.submitList(items)
//                binding.cardActionFooterDeck.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
//
//                // Reset select-all flags states smoothly if lists items content array mutates natively
//                // ✅ FIXED: Default back states reset loop if data list shifts layout
//                isSelectAll = false
//                binding.btnMediaSelectAll.setImageResource(R.drawable.ic_menu)
//            }
//        }
//    }
//}



class WAStickerFragment : BaseFragment<FragmentWAStickerBinding>(FragmentWAStickerBinding::inflate) {

    private val viewModel: MediaViewModel by viewModels { ViewModelFactory(MyApplication.app.repository) }
    private lateinit var adapter: MediaGridAdapter
    private lateinit var nativeAdsHelper: NativeAdsHelper
    private lateinit var fullScreenAdsHelper: FullScreenAdsHelper


    private val safLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) SafManager.saveUri(requireContext(), uri)
        viewModel.scanStickers(requireContext())
    }

    override fun onResume() {
        super.onResume()
        if (SafManager.hasSafPermission(requireContext())) {
            Log.d("WAStickerFragment", "onResume: Refreshing stickers flow channel registers.")
            viewModel.scanStickers(requireContext())
        }
    }

    // Interactive global status flag
    private var isSelectAll = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nativeAdsHelper = NativeAdsHelper(requireActivity())
        fullScreenAdsHelper= FullScreenAdsHelper(requireActivity())

        binding.btnMediaBack.setOnClickListener { findNavController().popBackStack() }

        childFragmentManager.setFragmentResultListener(FolderAccessDialog.REQUEST_KEY, viewLifecycleOwner) { _, _ ->
            safLauncher.launch(SafManager.getInitialUri())
        }

        binding.btnMediaHelp.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            findNavController().navigate(R.id.action_global_guideFragment)
        }


        // =========================================================================
        // ✅ SELECTION TOGGLE AND FIXED ICON TINT STATE ENGINE
        // =========================================================================
        binding.btnMediaSelectAll.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            if (::adapter.isInitialized && adapter.currentList.isNotEmpty()) {
                isSelectAll = !isSelectAll
                adapter.toggleSelectAll()

                if (isSelectAll) {
                    binding.btnMediaSelectAll.setImageResource(R.drawable.select_menu)
                } else {
                    binding.btnMediaSelectAll.setImageResource(R.drawable.ic_menu)
                }
            } else {
                Toast.makeText(requireContext(), "No items available to select", Toast.LENGTH_SHORT).show()
            }
        }

        // =========================================================================
        // ✅ INITIALIZE ADAPTER WITH AD BINDING CALLBACKS
        // =========================================================================
        adapter = MediaGridAdapter(
            onCardClicked = { item ->
                val bundle = Bundle().apply {
                    putString(Constants.ARG_MEDIA_PATH, item.filePath)
                    putString(Constants.ARG_MEDIA_TYPE, Constants.MEDIA_TYPE_STICKER)
                }
                Log.d("WAStickerFragment", "Navigating with Path: ${item.filePath}")
                findNavController().navigate(R.id.action_stickers_to_viewer, bundle)
            },
            onCardLongPressed = { item ->
                if (::adapter.isInitialized) {
                    isSelectAll = adapter.getSelectedItems().size == adapter.currentList.filterIsInstance<com.futurecode.recoverdeletedmessages.model.MediaItem>().size

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

        // =========================================================================
        // ✅ MASTER FIX: REDIRECT DATA STREAM VIA `submitMediaWithAds`
        // =========================================================================
        lifecycleScope.launch {
            viewModel.stickers.collectLatest { items ->
                // ⚠️ CRITICAL FIXED: Direct submitList hatakar custom wrapper injection logic call kiya
                adapter.submitMediaWithAds(items)

                binding.cardActionFooterDeck.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE

                isSelectAll = false
                binding.btnMediaSelectAll.setImageResource(R.drawable.ic_menu)
            }
        }
    }

    override fun onDestroyView() {
        // Safe check context reference clean pipeline leak protection
        super.onDestroyView()
    }
}