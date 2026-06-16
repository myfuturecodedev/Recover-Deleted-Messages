package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import com.futurecode.recoverdeletedmessages.ads.interstitial_ad.FullScreenAdsHelper
import com.futurecode.recoverdeletedmessages.ads.native_ad.NativeAdsHelper
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentSettingBinding

/**
 * Fragment responsible for displaying the user settings panel.
 * Extends BaseFragment to inherit core architecture, view bindings, and preference frameworks.
 */

class SettingFragment : BaseFragment<FragmentSettingBinding>(FragmentSettingBinding::inflate) {
    private lateinit var nativeAdsHelper: NativeAdsHelper
    lateinit var fullScreenAdsHelper: FullScreenAdsHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // super.onViewCreated automatically executes checkAndShowInAppBanner() via BaseFragment hooks

        initViews()
        setupClickListeners()

        val packageName: String = requireContext().packageName

        Log.d(TAG, "onViewCreated: $packageName")

        nativeAdsHelper= NativeAdsHelper(requireActivity())
        fullScreenAdsHelper= FullScreenAdsHelper(requireActivity())
        loadNativeAds()
    }

    /**
     * Initializes UI component states and displays configurations dynamically.
     */
    private fun initViews() {
        // Pixel-perfect item adjustments or configuration tracking states go here
    }

    /**
     * Configures the user click channel triggers for your settings row items.
     */
    private fun setupClickListeners() {
        // Unified navigation structure actions matching your dashboard card layout panel
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }


    fun loadNativeAds(){
        nativeAdsHelper = NativeAdsHelper(requireActivity())
        nativeAdsHelper?.showNativeAd(
            nativeBannerAdView = binding.nativeAds3.frame,
            mainLayout = binding.nativeAds3.mainLayout,
            placeholder = binding.nativeAds3.placeholder
        )
    }
}