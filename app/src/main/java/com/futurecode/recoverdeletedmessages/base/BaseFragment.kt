package com.futurecode.recoverdeletedmessages.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import com.futurecode.recoverdeletedmessages.ui.afterlogin.BusinessWARecoveryFragment
import com.futurecode.recoverdeletedmessages.ui.prelogin.SplashFragment
import com.futurecode.recoverdeletedmessages.utils.PrefManager


typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<VB : ViewBinding>(private val inflate: Inflate<VB>) : Fragment() {

    private var _binding: VB? = null
    protected val binding: VB get() = _binding ?: error("Binding is only valid between onCreateView and onDestroyView")

    protected val bindingOrNull: VB? get() = _binding

    protected val prefManager: PrefManager by lazy { MyApplication.app.prefManager }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Triggers the automated evaluation sequence as soon as the screen renders safely
        checkAndShowInAppBanner()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected fun showAds(flNative: FrameLayout) {
        if (!isAdded) return
    }


    private fun checkAndShowInAppBanner(){
        activity?.let { currentActivity ->
            val navHostFragment = currentActivity.supportFragmentManager
                .fragments
                .firstOrNull { it is NavHostFragment } as? NavHostFragment

            val currentFragment = navHostFragment
                ?.childFragmentManager
                ?.primaryNavigationFragment

            Log.d(
                "CurrentFragment",
                currentFragment?.javaClass?.simpleName ?: "No Fragment"
            )

            // Dynamic view evaluations mapping matching conditions seamlessly
            when (currentFragment) {
                // Check if you are on IntroEarnFragment or HomeFragment
                is BusinessWARecoveryFragment -> {
                    if (prefManager.clickCount == 0) {
                       // InAppUtils.showInAppBanner(currentActivity, true)
                    }
                }
                // Fallback catch-all logic block: serves full variant if the target is NOT SplashFragment
                else -> {
                    if (currentFragment !is SplashFragment) {
                       // InAppUtils.showInAppBanner(currentActivity, false)
                    }
                }
            }
        }
    }

}