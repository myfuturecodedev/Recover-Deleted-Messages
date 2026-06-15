package com.futurecode.recoverdeletedmessages.activity

import android.os.Build
import android.os.Bundle
import android.util.Log

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
        //setStatusBarColor(R.color.primary_blue, isLightStatusIcons = true)

        // Example: Red status bar background with light text icons
        //setStatusBarColorWithVersionHandling(R.color.bnv_active, isLightStatusIcon = true)

        // 2. Initialize Jetpack Navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController?.addOnDestinationChangedListener { controller, destination, bundle ->
            Log.e("TAG", "${destination.displayName.toString()}")

        }
    }
}