package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentPremiumBinding
import com.futurecode.recoverdeletedmessages.googleBilling.BillingManager

/**
 * Fragment responsible for handling the Premium Subscription Paywall interface.
 * Extends BaseFragment to inherit core architecture, view bindings, and preferences.
 */
class PremiumFragment : BaseFragment<FragmentPremiumBinding>(FragmentPremiumBinding::inflate) {

    // ✅ FIXED: Plan selection enumeration aligned perfectly with XML cards layout
    private enum class Plan {
        WEEKLY, MONTHLY, QUARTERLY
    }

    // Default configuration track matched identically to the design layout profile
    private var activePlanSelection = Plan.QUARTERLY
    private lateinit var billingManager: BillingManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializePaywallViews()
        setupClickListeners()
        initBillingManager()

    }

    /**
     * Set up default views and ensures visibility flags match the asset specification.
     */
    private fun initializePaywallViews() {
        // Explicitly ensuring the "Save 99%" badge is visible on the default selected container
       // binding.badgeSaveDiscount.visibility = View.VISIBLE

        // Refresh structural visual styles instantly
        updatePlanSelectionVisuals()
    }


    private fun initBillingManager() {
        billingManager = BillingManager(requireActivity(), onPurchaseFailure = { reason ->
            MyApplication.app.prefManager.isUserHasPremium = false
            Toast.makeText(requireContext(), "Purchase Failed: $reason", Toast.LENGTH_SHORT).show()
        }, onPurchaseSuccess = {
            MyApplication.app.prefManager.isUserHasPremium = true
            Toast.makeText(requireContext(), "Purchase Successful", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        })

        billingManager.startConnection()
    }
    /**
     * Configures clean interactive click channel triggers across the paywall interface.
     */
    private fun setupClickListeners() {
        // Close / Dismiss Paywall Action
        binding.btnClosePaywall.setOnClickListener {
            findNavController().navigateUp()
        }

        // ✅ FIXED: Selection Toggle for Weekly Card
        binding.cardPlanWeekly.setOnClickListener {
            if (activePlanSelection != Plan.WEEKLY) {
                activePlanSelection = Plan.WEEKLY
                updatePlanSelectionVisuals()
            }
        }

        // ✅ FIXED: Selection Toggle for Monthly Card
        binding.cardPlanMonthly.setOnClickListener {
            if (activePlanSelection != Plan.MONTHLY) {
                activePlanSelection = Plan.MONTHLY
                updatePlanSelectionVisuals()
            }
        }

        // ✅ FIXED: Selection Toggle for Quarterly Card
        binding.cardPlanQuartly.setOnClickListener {
            if (activePlanSelection != Plan.QUARTERLY) {
                activePlanSelection = Plan.QUARTERLY
                updatePlanSelectionVisuals()
            }
        }

        // Primary Continue Subscription CTA Trigger Action
        binding.btnContinueSubscription.setOnClickListener {
            // ✅ FIXED: Processed according to the correct Plan enum classes

           // Log.d("ndbfabcvhjegfbdhjewfb", "setupClickListeners: $activePlanSelection")

            launchPurchase(activePlanSelection)

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
     * ✅ FIXED: Mutates all 3 structural containers backgrounds and radio icon targets
     * in real-time to match the active selected state criteria cleanly without type errors.
     */
    private fun updatePlanSelectionVisuals() {
        val context = requireContext()

        // 1. Reset all layouts to unselected state first
        binding.cardPlanWeekly.setBackgroundResource(R.drawable.bg_paywall_card_unselected)
        binding.ivWeeklyIndicator.setImageResource(R.drawable.ic_radio_unselected)
        binding.tvWeeklyPrice.setTextColor(ContextCompat.getColor(context, R.color.paywall_text_primary))

        binding.cardPlanMonthly.setBackgroundResource(R.drawable.bg_paywall_card_unselected)
        binding.ivMonthlyIndicator.setImageResource(R.drawable.ic_radio_unselected)
        binding.tvMonthlyPrice.setTextColor(ContextCompat.getColor(context, R.color.paywall_text_primary))

        binding.cardPlanQuartly.setBackgroundResource(R.drawable.bg_paywall_card_unselected)
        binding.ivQuarterlyIndicator.setImageResource(R.drawable.ic_radio_unselected)
        binding.tvQuarterlyPrice.setTextColor(ContextCompat.getColor(context, R.color.paywall_text_primary))

        // 2. Highlight only the active selected plan container
        when (activePlanSelection) {
            Plan.WEEKLY -> {
                binding.cardPlanWeekly.setBackgroundResource(R.drawable.bg_paywall_card_selected)
                binding.ivWeeklyIndicator.setImageResource(R.drawable.ic_check_circle_filled)
                binding.tvWeeklyPrice.setTextColor(ContextCompat.getColor(context, R.color.paywall_accent_green))
            }
            Plan.MONTHLY -> {
                binding.cardPlanMonthly.setBackgroundResource(R.drawable.bg_paywall_card_selected)
                binding.ivMonthlyIndicator.setImageResource(R.drawable.ic_check_circle_filled)
                binding.tvMonthlyPrice.setTextColor(ContextCompat.getColor(context, R.color.paywall_accent_green))
            }
            Plan.QUARTERLY -> {
                binding.cardPlanQuartly.setBackgroundResource(R.drawable.bg_paywall_card_selected)
                binding.ivQuarterlyIndicator.setImageResource(R.drawable.ic_check_circle_filled)
                binding.tvQuarterlyPrice.setTextColor(ContextCompat.getColor(context, R.color.paywall_accent_green))
            }
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



    private fun launchPurchase(plan: Plan) {
        val productId = when (plan) {
            Plan.WEEKLY -> BillingManager.PRODUCT_WEEKLY
            Plan.MONTHLY -> BillingManager.PRODUCT_MONTHLY
            Plan.QUARTERLY -> BillingManager.PRODUCT_QUARTERLY
        }
        billingManager.launchPurchaseFlow(productId)
    }
}