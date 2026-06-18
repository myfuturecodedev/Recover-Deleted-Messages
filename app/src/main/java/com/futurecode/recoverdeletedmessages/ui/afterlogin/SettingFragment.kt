package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.ads.interstitial_ad.FullScreenAdsHelper
import com.futurecode.recoverdeletedmessages.ads.native_ad.NativeAdsHelper
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentSettingBinding
import com.futurecode.recoverdeletedmessages.utils.Utils.setAdClickListener

/**
 * Fragment responsible for displaying the user settings panel.
 * Extends BaseFragment to inherit core architecture, view bindings, and preference frameworks.
 */
class SettingFragment : BaseFragment<FragmentSettingBinding>(FragmentSettingBinding::inflate) {

    private var nativeAdsHelper: NativeAdsHelper? = null

    private lateinit var fullScreenAdsHelper: FullScreenAdsHelper


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val packageName: String = requireContext().packageName
        Log.d(TAG, "onViewCreated: $packageName")

        // Single initialization channel helper objects
        nativeAdsHelper = NativeAdsHelper(requireActivity())
        fullScreenAdsHelper = FullScreenAdsHelper(requireActivity())

        initViews()
        setupClickListeners()
        loadNativeAds()
    }

    /**
     * Initializes UI component states and displays configurations dynamically.
     */
    private fun initViews() {
        // App Version Name ko dynamically UI text view par set karne ke liye check (Optional)
        try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val version = pInfo.versionName
            // binding.tvVersion.text = "Version $version" (Agar aapke layout me ID ho toh)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Configures the user click channel triggers for your settings row items.
     */
    private fun setupClickListeners() {

        // 1. BACK NAVIGATION: Safely dismisses the screen profile controller
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // 2. SHARE APP ACTION: Generates an implicit native system text intent chooser
        binding.btnRowShare.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            try {
                val appPackageName = requireContext().packageName
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Hey, check out this amazing application to recover deleted messages instantly! Download Now: https://play.google.com/store/apps/details?id=$appPackageName"
                    )
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Share App Via")
                startActivity(shareIntent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unable to share at this moment", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. CONTACT US / FEEDBACK ACTION: Launches native email clients with auto metadata
        binding.btnRowContact.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            try {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:") // Only deep-linked email apps handle this processing pipeline
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("futurecode.support@gmail.com")) // Apni support email se replace karein bhai
                    putExtra(Intent.EXTRA_SUBJECT, "Feedback & Bug Report - Recover Deleted Messages")
                    putExtra(Intent.EXTRA_TEXT, "\n\n\n--- App Metadata Info ---\nDevice: ${android.os.Build.MODEL}\nOS Version: Android ${android.os.Build.VERSION.RELEASE}")
                }
                startActivity(Intent.createChooser(emailIntent, "Send Email via..."))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "No email application found on this device", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. TERMS & CONDITIONS ACTION: Opens the legal document link via the system web browser
        binding.btnRowTerms.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            openWebPageUrl("https://futurecode-studios.blogspot.com/p/terms-conditions.html") // Apne web portal URLs add karein bhai
        }

        // 5. PRIVACY POLICY ACTION: Opens the designated cloud dashboard policy node
        binding.btnRowPrivacy.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            openWebPageUrl("https://futurecode-studios.blogspot.com/p/privacy-policy.html")
        }

        // 6. PREMIUM / HELP SUPPORT BANNER: Navigates straight into guide helper or website support
        binding.btnBannerSupport.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            try {
                // Agar aapke paas dynamic Guide Screen Fragment hai, toh navigate bhi kar sakte hain:
                // findNavController().navigate(R.id.action_global_guideFragment)

                Toast.makeText(requireContext(), "Connecting with Premium Support Desk...", Toast.LENGTH_SHORT).show()
                openWebPageUrl("https://futurecode-studios.blogspot.com/p/support.html")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Helper pipeline to cleanly redirect legal actions and web routing safely to system packages
     */
    private fun openWebPageUrl(url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(browserIntent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Invalid Web Address or Link cannot be parsed", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Fetches and renders the visual bottom dynamic ad banner layout setup safely
     */
    fun loadNativeAds() {
        try {
            nativeAdsHelper?.showNativeAd(
                nativeBannerAdView = binding.nativeAds3.frame,
                mainLayout = binding.nativeAds3.mainLayout,
                placeholder = binding.nativeAds3.placeholder
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        // ✅ FIXED: Nullify helper instances to prevent memory/view leak context retainers
        nativeAdsHelper = null
        super.onDestroyView()
    }
}