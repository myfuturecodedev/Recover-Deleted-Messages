package com.futurecode.recoverdeletedmessages.ui.afterlogin

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import com.futurecode.recoverdeletedmessages.adapter.ChatContact
import com.futurecode.recoverdeletedmessages.adapter.ChatListAdapter
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.data.MessageEntity
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAMassageBinding
import com.futurecode.recoverdeletedmessages.databinding.LayoutPermissionBottomSheetBinding
import com.futurecode.recoverdeletedmessages.service.WANotificationListenerService
import com.futurecode.recoverdeletedmessages.ui.dialogs.NotificationAccessDialog
import com.futurecode.recoverdeletedmessages.utils.Constants
import com.futurecode.recoverdeletedmessages.utils.UiState
import com.futurecode.recoverdeletedmessages.viewModel.MessageViewModel
import com.futurecode.recoverdeletedmessages.viewModel.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WAMassageFragment : BaseFragment<FragmentWAMassageBinding>(FragmentWAMassageBinding::inflate) {

    private val TAG = "WAMassageFragment_Log"
   // private val viewModel: RecoveryViewModel by viewModels()
    private lateinit var chatAdapter: ChatListAdapter
    private val checkedChatIdsSet = mutableSetOf<String>()

    private var isBusinessModeActive = false
    private var permissionBottomSheetDialog: BottomSheetDialog? = null

    private val viewModel: MessageViewModel by viewModels { ViewModelFactory(MyApplication.app.repository) }
    private lateinit var adapter: ChatListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedApp = arguments?.getString("isBusinessMode") ?: "false"
        isBusinessModeActive = selectedApp.toBoolean() || selectedApp == "BUSINESS"

        Log.d("DestinationFragment", "Selected app converted mode: $isBusinessModeActive")


        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        initializeRecyclerView()

    }

    private fun initializeRecyclerView() {
        if (!WANotificationListenerService.isNotificationAccessGranted(requireContext())) {
            NotificationAccessDialog.show(childFragmentManager)
        }

        adapter = ChatListAdapter { contactName ->
            val bundle = Bundle().apply { putString(Constants.ARG_CONTACT_NAME, contactName) }
            findNavController().navigate(R.id.action_WAMassageFragment_to_ChatDetailFragment, bundle)
        }
        binding.rvChatThreadsRecycleView.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatThreadsRecycleView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.rvChatThreadsRecycleView.adapter = adapter

        lifecycleScope.launch {
            viewModel.allMessages.collectLatest { messages ->
                val grouped = messages
                    .groupBy { it.contactName }
                    .map { (contact, msgs) ->
                        ChatContact(
                            contactName = contact,
                            lastMessage = msgs.maxByOrNull { it.timestamp } ?: msgs.first(),
                            messageCount = msgs.size,
                            hasNew = msgs.any { it.isNew }
                        )
                    }
                    .sortedByDescending { it.lastMessage.timestamp }

                adapter.submitList(grouped)
                //binding.layoutEmptyGuideState.visibility = if (grouped.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-show dialog if permission still not granted (user may have just come back from Settings)
        val granted = WANotificationListenerService.isNotificationAccessGranted(requireContext())
        if (!granted && childFragmentManager.findFragmentByTag("NotificationAccessDialog") == null) {
            NotificationAccessDialog.show(childFragmentManager)
        }
    }
}