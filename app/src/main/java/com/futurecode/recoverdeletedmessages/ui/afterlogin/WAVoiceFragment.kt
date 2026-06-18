package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import com.futurecode.recoverdeletedmessages.adapters.AudioAdapter
import com.futurecode.recoverdeletedmessages.ads.interstitial_ad.FullScreenAdsHelper
import com.futurecode.recoverdeletedmessages.ads.native_ad.NativeAdsHelper
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAVoiceBinding
import com.futurecode.recoverdeletedmessages.ui.dialogs.FolderAccessDialog
import com.futurecode.recoverdeletedmessages.utils.Constants
import com.futurecode.recoverdeletedmessages.utils.SafManager
import com.futurecode.recoverdeletedmessages.utils.Utils.setAdClickListener
import com.futurecode.recoverdeletedmessages.viewModel.MediaViewModel
import com.futurecode.recoverdeletedmessages.viewModel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WAVoiceFragment : BaseFragment<FragmentWAVoiceBinding>(FragmentWAVoiceBinding::inflate) {

    private val viewModel: MediaViewModel by viewModels { ViewModelFactory(MyApplication.app.repository) }
    private lateinit var adapter: AudioAdapter

    private val safLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) SafManager.saveUri(requireContext(), uri)
        viewModel.scanVoice(requireContext())
    }

    private var isSelectAll=false
    private lateinit var nativeAdsHelper: NativeAdsHelper
    private lateinit var fullScreenAdsHelper: FullScreenAdsHelper




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nativeAdsHelper= NativeAdsHelper(requireActivity())
        fullScreenAdsHelper= FullScreenAdsHelper(requireActivity())

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnHelpGuide.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            findNavController().navigate(R.id.action_global_guideFragment)
        }


        binding.btnSettings.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            if (::adapter.isInitialized && adapter.currentList.isNotEmpty()) {

                // 1. Dynamic conditional inversion loop
                isSelectAll = !isSelectAll

                // 2. Trigger the underlying adapter layout calculations
                adapter.toggleSelectAll()

                // 3. ✅ FIXED: Changing imageTintList instead of backgroundTintList for ImageView
                // ✅ FIXED: Direct vector image swap engine without tints filters
                if (isSelectAll) {
                    binding.btnSettings.setImageResource(R.drawable.select_menu)
                } else {
                    binding.btnSettings.setImageResource(R.drawable.ic_menu)
                }

            } else {
                Toast.makeText(requireContext(), "No items available to select", Toast.LENGTH_SHORT).show()
            }
        }

        childFragmentManager.setFragmentResultListener(FolderAccessDialog.REQUEST_KEY, viewLifecycleOwner) { _, _ ->
            safLauncher.launch(SafManager.getInitialUri())
        }

        // ✅ FIXED: Updated to support both click and long press event streams for selection matching
        adapter = AudioAdapter(
            onItemClick = { item ->
                val bundle = Bundle().apply {
                    putString(Constants.ARG_AUDIO_PATH, item.filePath)
                    putString(Constants.ARG_AUDIO_TYPE, Constants.MEDIA_TYPE_VOICE)
                }
                findNavController().navigate(R.id.action_WAAudioFragment_to_AudioPlayerFragment, bundle)
            },
            onCardLongPressed = { item ->
                // Automatically matches runtime select-all menu configurations state if clicked one by one
                if (::adapter.isInitialized) {
                    isSelectAll = adapter.getSelectedItems().size == adapter.currentList.size

                    // Dynamic checkmark wrapper sync
                    if (isSelectAll) {
                        binding.btnSettings.setImageResource(R.drawable.select_menu)
                    } else {
                        binding.btnSettings.setImageResource(R.drawable.ic_menu)
                    }
                }
            },
            onAdBindingTriggered = { adBinding ->
                try {
                    // Render native ads securely inside the matching position gaps using live variable IDs
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


        binding.rvMediaGrid.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMediaGrid.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.rvMediaGrid.adapter = adapter

        if (!SafManager.hasSafPermission(requireContext())) {
            FolderAccessDialog.show(childFragmentManager)
        }
        viewModel.scanVoice(requireContext())

        lifecycleScope.launch {
            viewModel.voiceNotes.collectLatest { items ->
//                adapter.submitList(items)
//                binding.cardActionFooterDeck.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE

                // ⚠️ Call the custom ad injection loop function instead of standard submitList
                adapter.submitAudioWithAds(items)

                binding.cardActionFooterDeck.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                isSelectAll = false
                binding.btnSettings.setImageResource(R.drawable.ic_menu)
            }
        }
    }

    override fun onDestroyView() {
        adapter.releasePlayer()
        super.onDestroyView()
    }
}
