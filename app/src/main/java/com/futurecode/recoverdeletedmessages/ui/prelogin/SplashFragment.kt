package com.futurecode.recoverdeletedmessages.ui.prelogin

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.navigation.fragment.findNavController
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.activity.MainActivity
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentSplashBinding

/**
 * Fragment responsible for displaying the splash screen upon app launch.
 * Extends BaseFragment to inherit core architecture and binding logic.
 * Dynamically tracks custom progress states before advancing layouts.
 */
class SplashFragment : BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {

    private val splashDurationMs: Long = 3000 // Total execution window (3 seconds)
    private var progressAnimator: ValueAnimator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // super.onViewCreated already handles checkAndShowInAppBanner() via BaseFragment architecture.

        startProgressEvaluation()
    }

    /**
     * Initializes and executes a precise progress animation cycle.
     * Transitions navigation structures only upon successful 100% completion metrics.
     */
    private fun startProgressEvaluation() {
        // Enforce safe clean start configurations
        binding.pbSplash.progress = 0

        // Explicit progress updates mapped smoothly across structural limits
        progressAnimator = ValueAnimator.ofInt(0, 100).apply {
            duration = splashDurationMs
            interpolator = LinearInterpolator() // Guarantees smooth, steady loading visual velocities

            addUpdateListener { animator ->
                if (bindingOrNull != null) {
                    val currentProgress = animator.animatedValue as Int
                    binding.pbSplash.progress = currentProgress
                }
            }

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator) {
                    // Safe guard condition check: Verification loop ensuring true completion targets
                    if (isAdded && bindingOrNull != null && binding.pbSplash.progress == 100) {
                        navigateToNextScreen()
                    }
                }

                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })

            start()
        }
    }

    /**
     * Safe execution mechanism translating architecture navigation destinations cleanly.
     */
    private fun navigateToNextScreen() {
        if (prefManager.isLanguageSelectedFirstTime){
            (activity as? MainActivity)?.goToMain()
        }else{
            findNavController().navigate(R.id.action_splashFragment_to_languageFragment)
        }

    }

    override fun onDestroyView() {
        // Stop tracking instances completely to prevent lifecycle memory leak exceptions
        progressAnimator?.removeAllUpdateListeners()
        progressAnimator?.cancel()
        progressAnimator = null
        super.onDestroyView()
    }
}