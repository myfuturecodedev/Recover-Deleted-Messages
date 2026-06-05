package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentWARecoveryBinding
class WARecoveryFragment : BaseFragment<FragmentWARecoveryBinding>(FragmentWARecoveryBinding::inflate) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCards()
    }

    private fun setupCards() {
        // Message Card
        binding.cardMessages.apply {

        }

        // Photo Card
        binding.cardPhotos.apply {

        }

        // Video Card
        binding.cardVideos.apply {

        }

        // GIF Card
        binding.cardGifs.apply {

        }

        // Sticker Card
        binding.cardStickers.apply {

        }

        // Audio Card
        binding.cardAudio.apply {

        }

        // Voice Card
        binding.cardVoice.apply {

        }

        // Documents Card
        binding.cardDocuments.apply {

        }

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_WARecoveryFragment_to_settingFragment)
        }

        binding.btnLanguageSelector.setOnClickListener {
            findNavController().navigate(R.id.action_WARecoveryFragment_to_guideFragment)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
