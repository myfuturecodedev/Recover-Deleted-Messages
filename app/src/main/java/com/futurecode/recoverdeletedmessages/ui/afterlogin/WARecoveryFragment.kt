package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.ads.interstitial_ad.FullScreenAdsHelper
import com.futurecode.recoverdeletedmessages.ads.native_ad.NativeAdsHelper
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentWARecoveryBinding
import com.futurecode.recoverdeletedmessages.utils.NotificationPermissionHelper
import com.futurecode.recoverdeletedmessages.utils.Utils.setAdClickListener

class WARecoveryFragment : BaseFragment<FragmentWARecoveryBinding>(FragmentWARecoveryBinding::inflate) {

    // 1. Define a type-safe Enum to represent the selected application target
    enum class RecoveryTargetApp {
        WHATSAPP, BUSINESS
    }

    // 2. State variable storing the currently active platform selection
    private var currentSelectedApp = RecoveryTargetApp.WHATSAPP
    private val notificationPermissionHelper = NotificationPermissionHelper(this)
    // =========================================================================
    // ✅ MASTER FIX: Keep state instance alive to detect screen navigation returns
    // =========================================================================
    private var isScreenRefreshed = false

    private lateinit var nativeAdsHelper: NativeAdsHelper
    lateinit var fullScreenAdsHelper: FullScreenAdsHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nativeAdsHelper= NativeAdsHelper(requireActivity())
        fullScreenAdsHelper= FullScreenAdsHelper(requireActivity())

        setupCards()


        val packageName: String = requireContext().packageName
        Log.d("WARecoveryFragment", "onViewCreated initialization context targeting package: $packageName")

        // Pass the state check flag: true if returning from another screen, false if first app initialization
        notificationPermissionHelper.checkAndRequestPermission(isRefresh = isScreenRefreshed)

        // Lock the flag to true so any subsequent back navigation triggers don't fire duplicate notifications
        isScreenRefreshed = true
        loadNativeAds()

    }

    private fun setupCards() {
        // Message Card
        binding.cardMessages.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            // Retrieve the string and handle empty/null cases safely

            Log.d("DestinationFragment", "Selected app received: $currentSelectedApp")
            val bundle = Bundle().apply {
                putString("isBusinessMode", currentSelectedApp.toString())
            }
            findNavController().navigate(R.id.action_WARecoveryFragment_to_WAMassageFragment,bundle)

        }

        binding.ivCrownIcon.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            val bundle = Bundle().apply {
                putString("isBusinessMode", currentSelectedApp.toString())
            }
            findNavController().navigate(R.id.action_WARecoveryFragment_to_premiumFragment,bundle)
        }

        // Photo Card
        binding.cardPhotos.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            val bundle = Bundle().apply {
                putString("isBusinessMode", currentSelectedApp.toString())
            }
             findNavController().navigate(R.id.action_WARecoveryFragment_to_WAImageFragment,bundle)
        }

        // Video Card
        binding.cardVideos.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            val bundle = Bundle().apply {
                putString("isBusinessMode", currentSelectedApp.toString())
            }
            findNavController().navigate(R.id.action_WARecoveryFragment_to_WAVideoFragment,bundle)
        }

        // GIF Card
        binding.cardGifs.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            val bundle = Bundle().apply {
                putString("isBusinessMode", currentSelectedApp.toString())
            }
            findNavController().navigate(R.id.action_WARecoveryFragment_to_WAGIFFragment,bundle)
        }

        // Sticker Card
        binding.cardStickers.setAdClickListener(requireActivity(), fullScreenAdsHelper) {

            val bundle = Bundle().apply {
                putString("isBusinessMode", currentSelectedApp.toString())
            }
            findNavController().navigate(R.id.action_WARecoveryFragment_to_WAStickerFragment,bundle)
        }

        // Audio Card
        binding.cardAudio.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            val bundle = Bundle().apply {
                putString("isBusinessMode", currentSelectedApp.toString())
            }
            findNavController().navigate(R.id.action_WARecoveryFragment_to_WAAudioFragment,bundle)

        }

        // Voice Card
        binding.cardVoice.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            val bundle = Bundle().apply {
                putString("isBusinessMode", currentSelectedApp.toString())
            }
         findNavController().navigate(R.id.action_WARecoveryFragment_to_WAAudioFragment,bundle)
        }

        // Documents Card
        binding.cardDocuments.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            val bundle = Bundle().apply {
                putString("isBusinessMode", currentSelectedApp.toString())
            }
            findNavController().navigate(R.id.action_WARecoveryFragment_to_WADocumentFragment,bundle)
        }

        binding.btnSettings.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            findNavController().navigate(R.id.action_WARecoveryFragment_to_settingFragment)
        }

        binding.btnLanguageSelector.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            val bundle = Bundle().apply {
                putString("isBusinessMode", currentSelectedApp.toString())
            }
            findNavController().navigate(R.id.action_WARecoveryFragment_to_guideFragment,bundle)

        }

        // WhatsApp Recovery Tab Click Listener
        binding.btnTabWa.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            if (currentSelectedApp != RecoveryTargetApp.WHATSAPP) {
                currentSelectedApp = RecoveryTargetApp.WHATSAPP
                updateNavigationUIState()
                onRecoveryTargetChanged(currentSelectedApp)
            }
        }

        // WhatsApp Business Tab Click Listener
        binding.btnTabBusiness.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            if (currentSelectedApp != RecoveryTargetApp.BUSINESS) {
                currentSelectedApp = RecoveryTargetApp.BUSINESS
                updateNavigationUIState()
                onRecoveryTargetChanged(currentSelectedApp)
            }
        }
    }

    private fun updateNavigationUIState() {
        val context = requireContext()

        // Fetch custom Inter fonts smoothly to manipulate text weight programmatically
        val interBold = ResourcesCompat.getFont(context, R.font.inter_bold)
        val interMedium = ResourcesCompat.getFont(context, R.font.inter_medium)

        if (currentSelectedApp == RecoveryTargetApp.WHATSAPP) {
            // --- SELECT WHATSAPP / UNSELECT BUSINESS ---

            // Set WhatsApp Tab Active Visuals
            binding.btnTabWa.setBackgroundResource(R.drawable.bg_bottom_nav_active_pill)
            binding.ivNavWa.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.card_message_accent
                )
            )
            binding.tvNavWa.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.card_message_accent
                )
            )
            binding.tvNavWa.typeface = interBold

            // Set Business Tab Inactive Visuals
            binding.btnTabBusiness.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    android.R.color.transparent
                )
            )
            binding.ivNavBusiness.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.dash_nav_inactive_text
                )
            )
            binding.tvNavBusiness.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.dash_nav_inactive_text
                )
            )
            binding.tvNavBusiness.typeface = interMedium

        } else {
            // --- SELECT BUSINESS / UNSELECT WHATSAPP ---

            // Set Business Tab Active Visuals
            binding.btnTabBusiness.setBackgroundResource(R.drawable.bg_bottom_nav_active_pill)
            binding.ivNavBusiness.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.card_message_accent
                )
            )
            binding.tvNavBusiness.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.card_message_accent
                )
            )
            binding.tvNavBusiness.typeface = interBold

            // Set WhatsApp Tab Inactive Visuals
            binding.btnTabWa.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    android.R.color.transparent
                )
            )
            binding.ivNavWa.setColorFilter(
                ContextCompat.getColor(
                    context,
                    R.color.dash_nav_inactive_text
                )
            )
            binding.tvNavWa.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.dash_nav_inactive_text
                )
            )
            binding.tvNavWa.typeface = interMedium
        }
    }

    private fun onRecoveryTargetChanged(newTarget: RecoveryTargetApp) {
        when (newTarget) {
            RecoveryTargetApp.WHATSAPP -> {
                // TODO: Load standard WhatsApp local message files/notifications from database
            }

            RecoveryTargetApp.BUSINESS -> {
                // TODO: Load WhatsApp Business local message files/notifications from database
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
