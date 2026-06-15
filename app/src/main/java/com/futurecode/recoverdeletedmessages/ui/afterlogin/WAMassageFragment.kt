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
import androidx.recyclerview.widget.LinearLayoutManager
import com.futurecode.recoverdeletedmessages.R
import com.futurecode.recoverdeletedmessages.adapter.ChatListAdapter
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.data.MessageEntity
import com.futurecode.recoverdeletedmessages.databinding.FragmentWAMassageBinding
import com.futurecode.recoverdeletedmessages.databinding.LayoutPermissionBottomSheetBinding
import com.futurecode.recoverdeletedmessages.utils.UiState
import com.futurecode.recoverdeletedmessages.viewModel.RecoveryViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WAMassageFragment : BaseFragment<FragmentWAMassageBinding>(FragmentWAMassageBinding::inflate) {

    private val TAG = "WAMassageFragment_Log"
    private val viewModel: RecoveryViewModel by viewModels()
    private lateinit var chatAdapter: ChatListAdapter
    private val checkedChatIdsSet = mutableSetOf<String>()

    private var isBusinessModeActive = false
    private var permissionBottomSheetDialog: BottomSheetDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedApp = arguments?.getString("isBusinessMode") ?: "false"
        isBusinessModeActive = selectedApp.toBoolean() || selectedApp == "BUSINESS"

        Log.d("DestinationFragment", "Selected app converted mode: $isBusinessModeActive")

        initializeRecyclerView()
        setupActionClickListeners()
        observeTextRecoveryStreamPipeline()
    }

    private fun initializeRecyclerView() {
        chatAdapter = ChatListAdapter(
            onChatClicked = { selectedChat ->
                // FIXED: Changed selectedChat.chatId to selectedChat.senderName
                if (checkedChatIdsSet.isNotEmpty()) {
                    // Agar selection active hai, toh click karne par checkbox toggle hoga
                    handleSelectionRowToggle(selectedChat.senderName)
                } else {
                    // FIXED: Using senderName as safe unique key targeting navigation context
                    Log.d(TAG, "Navigating down into individual chat details feed view pass.")

                    val args = Bundle().apply {
                        putString("chatId", selectedChat.senderName) // Passing senderName as unique chat indicator
                        putBoolean("isBusinessMode", isBusinessModeActive)
                    }

                    findNavController().navigate(R.id.action_WAMassageFragment_to_MessagePreviewFragment, args)
                }
            },
            onChatLongPressed = { selectedChat ->
                // FIXED: Changed selectedChat.chatId to selectedChat.senderName
                handleSelectionRowToggle(selectedChat.senderName)
            }
        )

        binding.rvChatThreadsRecycleView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
            setHasFixedSize(true)
        }
    }

    private fun handleSelectionRowToggle(chatId: String) {
        if (checkedChatIdsSet.contains(chatId)) {
            checkedChatIdsSet.remove(chatId)
        } else {
            checkedChatIdsSet.add(chatId)
        }

        binding.containerBatchActionDeck.visibility =
            if (checkedChatIdsSet.isNotEmpty()) View.VISIBLE else View.GONE
        chatAdapter.submitActiveSelectionsList(checkedChatIdsSet)
    }

    private fun setupActionClickListeners() {
        binding.btnBatchDeleteThreads.setOnClickListener {
           // viewModel.deleteSelectedChatThreads(checkedChatIdsSet.toList(), isBusinessModeActive)
            checkedChatIdsSet.clear()
            binding.containerBatchActionDeck.visibility = View.GONE
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeTextRecoveryStreamPipeline() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messagesUiStateFlow.collectLatest { state ->
                    when (state) {
                        is UiState.Loading -> {
                            Log.d(TAG, "Parsing history configuration file streams...")
                            binding.rvChatThreadsRecycleView.visibility = View.GONE
                            binding.layoutEmptyGuideState.root.visibility = View.GONE
                        }

                        is UiState.Success<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            val dataList = state.data as? List<MessageEntity> ?: emptyList()

                            Log.d(TAG, "Success state arrived. Current items count: ${dataList.size}")

                            if (dataList.isEmpty()) {
                                binding.rvChatThreadsRecycleView.visibility = View.GONE
                                binding.layoutEmptyGuideState.root.visibility = View.VISIBLE

                                binding.layoutEmptyGuideState.layoutBtnViewGuide.setOnClickListener {
                                    Log.d(TAG, "Empty View Action: Checking notification permission access.")
                                    checkAndPromptNotificationPermissionGate()
                                }

                            } else {
                                binding.rvChatThreadsRecycleView.visibility = View.VISIBLE
                                binding.layoutEmptyGuideState.root.visibility = View.GONE
                                chatAdapter.submitList(dataList)
                            }
                        }

                        is UiState.Error -> {
                            Log.e(TAG, "Pipeline operation logged internal processing exceptions", state.exception)
                            binding.rvChatThreadsRecycleView.visibility = View.VISIBLE
                            binding.layoutEmptyGuideState.root.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun isNotificationServiceEnabled(context: Context): Boolean {
        val pkgName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":")
            for (name in names) {
                val componentName = ComponentName.unflattenFromString(name)
                if (componentName != null && TextUtils.equals(pkgName, componentName.packageName)) {
                    return true
                }
            }
        }
        return false
    }

    private fun checkAndPromptNotificationPermissionGate() {
        if (!isNotificationServiceEnabled(requireContext())) {
            Log.d(TAG, "Notification listener access missing. Intent dispatch to System Settings triggered.")
            redirectToSystemNotificationSettings()
        } else {
            Log.d(TAG, "Notification Listener access already verified and granted successfully.")
        }
    }

    override fun onResume() {
        super.onResume()
       // viewModel.loadStoredTextChatThreads(isBusinessModeActive)
        evaluatePermissionAndShowPopupGate()
    }

    private fun evaluatePermissionAndShowPopupGate() {
        val isAccessGranted = isNotificationServiceEnabled(requireContext())

        if (!isAccessGranted) {
            Log.d(TAG, "Notification Access Missing. Presenting permission bottom sheet dialog panel.")
            showPermissionBottomSheet()
        } else {
            Log.d(TAG, "Notification Access Verified Active. Dismissing popup layers safely.")
            permissionBottomSheetDialog?.dismiss()
        }
    }

    private fun showPermissionBottomSheet() {
        if (permissionBottomSheetDialog?.isShowing == true) return

        val context = requireContext()

        permissionBottomSheetDialog = BottomSheetDialog(context).apply {
            val sheetBinding = LayoutPermissionBottomSheetBinding.inflate(layoutInflater)
            setContentView(sheetBinding.root)
            setCancelable(false)

            val bottomSheetContainer = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheetContainer?.setBackgroundColor(android.graphics.Color.TRANSPARENT)

            sheetBinding.ivCloseSheet.setOnClickListener { dismiss() }

            sheetBinding.btnAllowPermission.setOnClickListener {
                dismiss()
                redirectToSystemNotificationSettings()
            }

            show()
        }
    }

    private fun redirectToSystemNotificationSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed launching explicit settings intent node layout link direct", e)
        }
    }
}