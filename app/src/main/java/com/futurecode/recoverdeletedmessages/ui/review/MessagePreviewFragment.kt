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

    private var activeChatSenderName: String = "" // FIXED: Changed from activeChatThreadId to track sender profile
    private var isBusinessContext: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        extractNavigationArguments()
        initializeChatRecyclerView()
        setupToolbarListeners()
        observeConversationPipeline()

        // FIXED: Trigger the core live reactive database observation stream pipe
        //viewModel.startLiveDatabaseObservation()

        // Inside your MessagePreviewFragment.kt onviewCreated section:
// Replace old loadStoredTextChatThreads with this precise method:
//        viewModel.loadStoredCategoryMedia(
//            targetSenderName = activeChatSenderName,
//            isBusinessMode = isBusinessContext
//        )
    }

    private fun extractNavigationArguments() {
        // FIXED: Pull out unique identity strings via the forwarded bundle arguments pass
        activeChatSenderName = arguments?.getString("chatId") ?: ""
        isBusinessContext = arguments?.getBoolean("isBusinessMode") ?: false

        Log.d(TAG, "Opened thread viewport for Sender: $activeChatSenderName | Business Mode: $isBusinessContext")
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
                viewModel.mediaUiStateFlow.collectLatest { state -> // FIXED: Pointed directly onto the core flow pipeline container
                    when (state) {
                        is UiState.Loading -> binding.rvChatHistoryFeed.visibility = View.GONE
                        is UiState.Success<*> -> {
                            binding.rvChatHistoryFeed.visibility = View.VISIBLE

                            @Suppress("UNCHECKED_CAST")
                            val rawMessages = state.data as? List<MessageEntity> ?: emptyList()

                            // FIXED: Filtering data thread entries using senderName mapping matches instead of missing chatId
                            val filteredConversationThread = rawMessages.filter {
                                it.senderName == activeChatSenderName && it.isBusiness == isBusinessContext
                            }

                            if (filteredConversationThread.isNotEmpty()) {
                                // Set toolbar text dynamically based on sender identity strings
                                binding.tvDetailUserName.text = activeChatSenderName
                            } else {
                                binding.tvDetailUserName.text = activeChatSenderName
                            }

                            // Submits the cleanly filtered user stream array.
                            // Note: We reverse the collection mapping list back if your database query sorts logs DESC,
                            // ensuring standard natural chat behavior inside stackFromEnd Recyclerview frameworks.
                            detailFeedAdapter.submitList(filteredConversationThread.reversed())
                        }
                        is UiState.Error -> binding.rvChatHistoryFeed.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}