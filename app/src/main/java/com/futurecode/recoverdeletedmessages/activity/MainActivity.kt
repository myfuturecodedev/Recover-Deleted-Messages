package com.futurecode.recoverdeletedmessages.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {
    private var navController: NavController? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        navController = navHostFragment!!.navController

        navController?.addOnDestinationChangedListener { controller, destination, bundle ->
            Log.e("TAG", "${destination.displayName}")
            if (destination.id != R.id.splashFragment) {
                //binding.flBanner.reload()
            }
        }
    }

    fun goToMain() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}

