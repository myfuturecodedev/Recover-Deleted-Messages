package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentPremiumBinding

/**
 * Fragment responsible for handling the Premium Subscription Paywall interface.
 * Extends BaseFragment to inherit core architecture, view bindings, and preferences.
 */
class PremiumFragment : BaseFragment<FragmentPremiumBinding>(FragmentPremiumBinding::inflate) {

    // Plan selection enumeration for state safety mapping
    private enum class SubscriptionTier {
        LIFETIME, MONTHLY
    }

    // Default configuration track matched identically to the design layout profile
    private var activePlanSelection = SubscriptionTier.LIFETIME

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // super.onViewCreated automatically tracks checkAndShowInAppBanner() via BaseFragment architecture

        initializePaywallViews()
        setupClickListeners()
    }

    /**
     * Set up default views and ensures visibility flags match the asset specification.
     */
    private fun initializePaywallViews() {
        // Explicitly ensuring the "Save 99%" badge is visible on the default selected Lifetime container
        binding.badgeSaveDiscount.visibility = View.VISIBLE

        // Refresh structural visual styles instantly
        updatePlanSelectionVisuals()
    }

    /**
     * Configures clean interactive click channel triggers across the paywall interface.
     */
    private fun setupClickListeners() {
        // Close / Dismiss Paywall Action
        binding.btnClosePaywall.setOnClickListener {
            findNavController().navigateUp()
        }

        // Selection Toggle: Lifetime Plan Card
        binding.cardPlanLifetime.setOnClickListener {
            if (activePlanSelection != SubscriptionTier.LIFETIME) {
                activePlanSelection = SubscriptionTier.LIFETIME
                updatePlanSelectionVisuals()
            }
        }

        // Selection Toggle: Monthly Plan Card
        binding.cardPlanMonthly.setOnClickListener {
            if (activePlanSelection != SubscriptionTier.MONTHLY) {
                activePlanSelection = SubscriptionTier.MONTHLY
                updatePlanSelectionVisuals()
            }
        }

        // Primary Continue Subscription CTA Trigger Action
        binding.btnContinueSubscription.setOnClickListener {
            when (activePlanSelection) {
                SubscriptionTier.LIFETIME -> processSubscriptionPurchasePipeline("sku_lifetime_premium")
                SubscriptionTier.MONTHLY -> processSubscriptionPurchasePipeline("sku_monthly_premium_trial")
            }
        }

        // --- LEGAL WEB REDIRECT CHANNELS ---
        binding.btnLegalTerms.setOnClickListener {
            launchExternalLegalDocumentViewer("https://futurecode.com/terms")
        }

        binding.btnLegalPrivacy.setOnClickListener {
            launchExternalLegalDocumentViewer("https://futurecode.com/privacy")
        }

        binding.btnPurchaseRestore.setOnClickListener {
            triggerBillingRestoreSequence()
        }
    }

    /**
     * Mutates structural container background layout shapes and vector assets
     * in real-time to match the active selected state criteria cleanly.
     */
    private fun updatePlanSelectionVisuals() {
        val context = requireContext()

        if (activePlanSelection == SubscriptionTier.LIFETIME) {
            // --- FOCUS LIFETIME / DE-EMPHASIZE MONTHLY ---
            binding.cardPlanLifetime.setBackgroundResource(R.drawable.bg_paywall_card_selected)
            binding.ivLifetimeIndicator.setImageResource(R.drawable.ic_check_circle_filled)
            binding.tvLifetimePrice.setTextColor(ContextCompat.getColor(context, R.color.paywall_accent_green))

            binding.cardPlanMonthly.setBackgroundResource(R.drawable.bg_paywall_card_unselected)
            binding.ivMonthlyIndicator.setImageResource(R.drawable.ic_radio_unselected)
            binding.tvMonthlyPrice.setTextColor(ContextCompat.getColor(context, R.color.paywall_text_primary))
        } else {
            // --- FOCUS MONTHLY / DE-EMPHASIZE LIFETIME ---
            binding.cardPlanMonthly.setBackgroundResource(R.drawable.bg_paywall_card_selected)
            binding.ivMonthlyIndicator.setImageResource(R.drawable.ic_check_circle_filled)
            binding.tvMonthlyPrice.setTextColor(ContextCompat.getColor(context, R.color.paywall_accent_green))

            binding.cardPlanLifetime.setBackgroundResource(R.drawable.bg_paywall_card_unselected)
            binding.ivLifetimeIndicator.setImageResource(R.drawable.ic_radio_unselected)
            binding.tvLifetimePrice.setTextColor(ContextCompat.getColor(context, R.color.paywall_text_primary))
        }
    }

    private fun processSubscriptionPurchasePipeline(productSkuKey: String) {
        // Integrate your standard BillingClient library initialization launch loop hooks securely here
    }

    private fun launchExternalLegalDocumentViewer(targetUrl: String) {
        // Handle web document execution links cleanly here
    }

    private fun triggerBillingRestoreSequence() {
        // Query Google Play inventory caches natively to refresh transactional states inside your prefManager
    }
}