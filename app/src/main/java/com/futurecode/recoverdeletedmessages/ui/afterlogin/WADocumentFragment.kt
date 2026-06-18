package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import com.futurecode.recoverdeletedmessages.adapters.DocumentAdapter
import com.futurecode.recoverdeletedmessages.ads.interstitial_ad.FullScreenAdsHelper
import com.futurecode.recoverdeletedmessages.ads.native_ad.NativeAdsHelper
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentWADocumentBinding
import com.futurecode.recoverdeletedmessages.ui.dialogs.FolderAccessDialog
import com.futurecode.recoverdeletedmessages.utils.SafManager
import com.futurecode.recoverdeletedmessages.utils.Utils.setAdClickListener
import com.futurecode.recoverdeletedmessages.viewModel.MediaViewModel
import com.futurecode.recoverdeletedmessages.viewModel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class WADocumentFragment : BaseFragment<FragmentWADocumentBinding>(FragmentWADocumentBinding::inflate) {

    private val viewModel: MediaViewModel by viewModels { ViewModelFactory(MyApplication.app.repository) }
    private lateinit var adapter: DocumentAdapter

    private val safLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) SafManager.saveUri(requireContext(), uri)
        viewModel.scanDocuments(requireContext())
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

        adapter = DocumentAdapter(
            onItemClick = { item ->
                // Open document preview execution flow
                val uri = if (item.filePath.startsWith("content://")) {
                    Uri.parse(item.filePath)
                } else {
                    val file = File(item.filePath)
                    if (!file.exists()) return@DocumentAdapter
                    FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
                }
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Open with"))
            },
            onCardLongPressed = { item ->
                if (::adapter.isInitialized) {
                    isSelectAll = adapter.getSelectedItems().size == adapter.currentList.size
                    if (isSelectAll) binding.btnSettings.setImageResource(R.drawable.select_menu)
                    else binding.btnSettings.setImageResource(R.drawable.ic_menu)
                }
            },

            onAdBindingTriggered = { adBinding ->
                try {
                    // Renders native ads inside the matching position gaps seamlessly using live variables ids
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
        viewModel.scanDocuments(requireContext())

        lifecycleScope.launch {
            viewModel.documents.collectLatest { items ->
//                adapter.submitList(items)
//                binding.cardActionFooterDeck.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE

                // ⚠️ FIXED: Instead of standard submitList, feed the stream through custom wrapper logic
                adapter.submitDocumentsWithAds(items)
                binding.cardActionFooterDeck.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE

                isSelectAll = false
                binding.btnSettings.setImageResource(R.drawable.ic_menu)
            }
        }
    }
}
