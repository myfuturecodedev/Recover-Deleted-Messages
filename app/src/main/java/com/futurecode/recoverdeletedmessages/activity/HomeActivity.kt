package com.futurecode.recoverdeletedmessages.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View

import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.databinding.ActivityHomeBinding

class HomeActivity : BaseActivity() {

    // View Binding instance parameter
    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

// Inside your main activity launcher flow setup:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }
        // 1. Initialize View Binding layout node graph tree
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Initialize Jetpack Navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 🔥 MASTER VISIBILITY CONTROLLER GATE
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("Navigation_Log", "Active Destination Changed Context: ${destination.label}")

            // Check if the current visible screen matches the dashboard main landing fragment
            if (destination.id == R.id.WARecoveryFragment) {
                // Hide the banner container tightly to free layout canvas pixels
                binding.flBanner.visibility = View.GONE
                Log.d("Navigation_Log", "Dashboard Detected. Banner ad wrapper collapsed (GONE).")
            } else {
                // Restore visibility for inner secondary preview screens cleanly
                binding.flBanner.visibility = View.VISIBLE
                Log.d("Navigation_Log", "Secondary Viewer Active. Banner ad wrapper visible.")
            }
        }

    }
}