package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.adapter.ChatMessageAdapter
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentChatDetailBinding
import com.futurecode.recoverdeletedmessages.utils.Constants
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import com.futurecode.recoverdeletedmessages.ads.interstitial_ad.FullScreenAdsHelper
import com.futurecode.recoverdeletedmessages.viewModel.MessageViewModel
import com.futurecode.recoverdeletedmessages.viewModel.ViewModelFactory

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatDetailFragment : BaseFragment<FragmentChatDetailBinding>(FragmentChatDetailBinding::inflate) {

    private val viewModel: MessageViewModel by viewModels { ViewModelFactory(MyApplication.app.repository) }
    private lateinit var adapter: ChatMessageAdapter
    private lateinit var fullScreenAdsHelper: FullScreenAdsHelper


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contactName = arguments?.getString(Constants.ARG_CONTACT_NAME) ?: "Unknown"
        fullScreenAdsHelper= FullScreenAdsHelper(requireActivity())

        binding.tvContactName.text = contactName
        binding.ivBack.setOnClickListener { findNavController().popBackStack() }

        adapter = ChatMessageAdapter { item ->
            if (item.mediaPath?.isNotEmpty() == true) {
                val bundle = Bundle().apply {
                    putString(Constants.ARG_MEDIA_PATH, item.mediaPath)
                    putString(Constants.ARG_MEDIA_TYPE, item.messageType)
                }
                findNavController().navigate(R.id.action_chatDetailFragment_to_mediaViewerFragment, bundle)
            }
        }

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        binding.rvMessages.layoutManager = layoutManager
        binding.rvMessages.adapter = adapter

        val messagesFlow = viewModel.getMessagesByContact(contactName)
        lifecycleScope.launch {
            messagesFlow.collectLatest { messages ->
                adapter.submitList(messages)
                if (messages.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.rvMessages.smoothScrollToPosition(messages.size - 1)
                }
            }
        }
    }
}


