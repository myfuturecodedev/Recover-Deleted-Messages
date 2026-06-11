package com.futurecode.recoverdeletedmessages.ui.review

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.futurecode.recoverdeletedmessages.adapter.DetailChatFeedAdapter
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.data.MessageEntity
import com.futurecode.recoverdeletedmessages.databinding.FragmentMessagePreviewBinding
import com.futurecode.recoverdeletedmessages.utils.UiState
import com.futurecode.recoverdeletedmessages.viewModel.RecoveryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MessagePreviewFragment : BaseFragment<FragmentMessagePreviewBinding>(FragmentMessagePreviewBinding::inflate) {

    private val TAG = "MsgPreviewFragment_Log"
    private val viewModel: RecoveryViewModel by viewModels()
    private lateinit var detailFeedAdapter: DetailChatFeedAdapter

    private var activeChatThreadId: String = ""
    private var isBusinessContext: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        extractNavigationArguments()
        initializeChatRecyclerView()
        setupToolbarListeners()
        observeConversationPipeline()

        // Trigger data fetch matching target thread identification keys
        viewModel.loadStoredTextChatThreads(isBusinessMode = isBusinessContext)
    }

    private fun extractNavigationArguments() {
        activeChatThreadId = arguments?.getString("chatId") ?: ""
        isBusinessContext = arguments?.getBoolean("isBusinessMode") ?: false

        Log.d(TAG, "Opened thread viewport for ChatId: $activeChatThreadId | Business Mode: $isBusinessContext")
    }

    private fun initializeChatRecyclerView() {
        detailFeedAdapter = DetailChatFeedAdapter()

        binding.rvChatHistoryFeed.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true // Forces the list view to stay scrolled to the bottom (latest chats)
            }
            adapter = detailFeedAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupToolbarListeners() {
        binding.btnDetailBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeConversationPipeline() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messagesUiStateFlow.collectLatest { state ->
                    when (state) {
                        is UiState.Loading -> binding.rvChatHistoryFeed.visibility = View.GONE
                        is UiState.Success<*> -> {
                            binding.rvChatHistoryFeed.visibility = View.VISIBLE

                            @Suppress("UNCHECKED_CAST")
                            val rawMessages = state.data as? List<MessageEntity> ?: emptyList()

                            // Filter data to display only conversations belonging to this specific chatId thread
                            // In a full production app, you would fetch this sub-list directly via a query in your ViewModel/DB
                            val filteredConversationThread = rawMessages.filter { it.chatId == activeChatThreadId }

                            if (filteredConversationThread.isNotEmpty()) {
                                // Set toolbar text dynamically based on sender identity strings
                                binding.tvDetailUserName.text = filteredConversationThread.first().senderName
                            }

                            detailFeedAdapter.submitList(filteredConversationThread)
                        }
                        is UiState.Error -> binding.rvChatHistoryFeed.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}