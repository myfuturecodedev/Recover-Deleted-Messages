package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.futurecode.recoverdeletedmessages.ads.interstitial_ad.FullScreenAdsHelper
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentManageMediaRecoveryBinding
import com.futurecode.recoverdeletedmessages.utils.Utils.setAdClickListener

class ManageMediaRecoveryFragment : BaseFragment<FragmentManageMediaRecoveryBinding>(FragmentManageMediaRecoveryBinding::inflate) {

    private val TAG = "ManageMediaRecovery_Log"

    private lateinit var fullScreenAdsHelper: FullScreenAdsHelper


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fullScreenAdsHelper= FullScreenAdsHelper(requireActivity())
        initializeClickListeners()
    }

    private fun initializeClickListeners() {
        // Top app bar back button navigation routine
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Green Core Action: Refresh Service Task Button
        binding.btnRefreshService.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            Log.d(TAG, "Refresh action triggered. Re-scanning workspace directories.")
            Toast.makeText(requireContext(), "Recovery Service Refreshed", Toast.LENGTH_SHORT).show()
            binding.tvSyncTimeStamp.text = "Last synced: Just now"
        }

        // Red Core Action Alert: Stop Service Button
        binding.btnStopRecoveryService.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            Log.w(TAG, "Warning: User requested system background scanner termination.")
            Toast.makeText(requireContext(), "Service Stopped", Toast.LENGTH_LONG).show()
        }
    }
}