package com.futurecode.recoverdeletedmessages.ui.prelogin
import com.futurecode.recoverdeletedmessages.adapter.LanguageAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.futurecode.recoverdeletedmessages.activity.MainActivity
import com.futurecode.recoverdeletedmessages.activity.MyApplication
import com.futurecode.recoverdeletedmessages.base.BaseFragment
import com.futurecode.recoverdeletedmessages.databinding.FragmentLanguageBinding
import com.futurecode.recoverdeletedmessages.viewModel.LanguageViewModel
import android.Manifest
import android.os.Build

import androidx.activity.result.contract.ActivityResultContracts
import com.futurecode.recoverdeletedmessages.ads.interstitial_ad.FullScreenAdsHelper
import com.futurecode.recoverdeletedmessages.ads.native_ad.NativeAdsHelper
import com.futurecode.recoverdeletedmessages.utils.Utils.setAdClickListener


//class LanguageFragment : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {
//
//    private lateinit var languageAdapter: LanguageAdapter
//    private var currentlySelectedLanguageCode: String = "en"
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        // super.onViewCreated already evaluates checkAndShowInAppBanner() safely.
//
//        initLanguageSelectionList()
//        setupActionClickListeners()
//    }
//
//    private fun initLanguageSelectionList() {
//        // Fetch saved profile string configurations, defaulting safely to "en"
//        val savedLang = prefManager.getString("selected_lang", "en") ?: "en"
//        currentlySelectedLanguageCode = savedLang
//
//        val rawLanguages = listOf(
//            LanguageModel("en_default", "English (default)", savedLang == "en_default" || savedLang == "en"),
//            LanguageModel("en", "English", savedLang == "en"),
//            LanguageModel("ar", "العربية", savedLang == "ar"),
//            LanguageModel("de", "Deutsch", savedLang == "de"),
//            LanguageModel("es", "Español", savedLang == "es"),
//            LanguageModel("fr", "Français", savedLang == "fr"),
//            LanguageModel("id", "Bahasa Indonesia", savedLang == "id"),
//            LanguageModel("it", "Italiano", savedLang == "it"),
//            LanguageModel("ja", "日本語", savedLang == "ja"),
//            LanguageModel("ko", "한국어", savedLang == "ko")
//        )
//
//        languageAdapter = LanguageAdapter(rawLanguages) { selection ->
//            currentlySelectedLanguageCode = selection.languageCode
//        }
//
//        binding.rvLanguages.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = languageAdapter
//            setHasFixedSize(true)
//        }
//    }
//
//    private fun setupActionClickListeners() {
//        binding.btnBack.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
//            findNavController().navigateUp()
//        }
//
//        binding.btnDone.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
//            // Persist the user's choice inside your BaseFragment's prefManager
//           // prefManager.putString("selected_lang", currentlySelectedLanguageCode)
//
//            // Advance navigation stack onto targeted structural flows
//            //findNavController().navigate(R.id.action_languageFragment_to_onboardingFragment)
//        }
//
//        binding.btnUpgrade.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
//            // Triggers premium logic
//            //findNavController().navigate(R.id.action_languageFragment_to_premiumPaywallFragment)
//        }
//    }
//}


//res/
//├── values/                    <-- Default/Fallback (English)
//│   └── strings.xml
//├── values-ar/                 <-- Arabic
//│   └── strings.xml
//├── values-de/                 <-- German
//│   └── strings.xml
//├── values-es/                 <-- Spanish
//│   └── strings.xml
//├── values-fr/                 <-- French
//│   └── strings.xml
//├── values-id/                 <-- Indonesian
//│   └── strings.xml
//├── values-it/                 <-- Italian
//│   └── strings.xml
//├── values-ja/                 <-- Japanese
//│   └── strings.xml
//└── values-ko/                 <-- Korean



//class LanguageFragment : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {
//
//    private val viewModel: LanguageViewModel by viewModels()
//    private lateinit var languageAdapter: LanguageAdapter
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        // super.onViewCreated already executes checkAndShowInAppBanner() via BaseFragment configuration setup
//
//        setupRecyclerView()
//        setupSearchPipeline()
//        observeViewModelData()
//        setupClickListeners()
//    }
//
//    private fun setupRecyclerView() {
//        languageAdapter = LanguageAdapter(requireActivity()) { selectedLanguage ->
//            viewModel.selectLanguage(selectedLanguage)
//        }
//
//        binding.rvLanguageList.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = languageAdapter
//            setHasFixedSize(true)
//        }
//    }
//
//    private fun setupSearchPipeline() {
//        binding.etSearchLanguage.doAfterTextChanged { text ->
//            viewModel.filterLanguages(text?.toString() ?: "")
//        }
//    }
//
//    private fun observeViewModelData() {
//        viewModel.uiLanguageList.observe(viewLifecycleOwner) { rawLanguages ->
//            val mixedListWithAds = mutableListOf<Any>()
//
//            rawLanguages.forEachIndexed { index, languageModel ->
//                mixedListWithAds.add(languageModel)
//                // Intercept data processing index 2 to dynamically slide real-time native ad units in place
//                if (index == 2) {
//                    mixedListWithAds.add("AD_UNIT")
//                }
//            }
//            languageAdapter.submitList(mixedListWithAds)
//        }
//
//        binding.btnConfirmSelection.isEnabled = false
//        viewModel.selectedLanguage.observe(viewLifecycleOwner) { selected ->
//            binding.btnConfirmSelection.isEnabled = (selected != null)
//        }
//    }
//
//    private fun setupClickListeners() {
//        binding.btnBack.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
//            requireActivity().onBackPressedDispatcher.onBackPressed()
//        }
//
//        binding.btnConfirmSelection.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
//            val confirmedLanguage = viewModel.selectedLanguage.value
//            confirmedLanguage?.let {
//
//                prefManager.selectedLanguage=it.languageCode
//                prefManager.isLanguageSelectedFirstTime=true
//
//
//                // 2. Refresh application configuration locale context strings
//                 MyApplication.setLocale(requireContext())
//                // 3. Force completely clean restart target to apply language mutations globally
//                val intent = Intent(requireActivity(), MainActivity::class.java).apply {
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                }
//                startActivity(intent)
//
//                // Kill current hosting activity container cleanly
//                requireActivity().finish()
//            }
//        }
//    }
//}





class LanguageFragment : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {

    private val viewModel: LanguageViewModel by viewModels()
    private lateinit var languageAdapter: LanguageAdapter

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("LanguageFragment_Log", "POST_NOTIFICATIONS permission granted successfully.")
        } else {
            android.util.Log.w("LanguageFragment_Log", "POST_NOTIFICATIONS permission was denied by user.")
        }
    }

    private lateinit var nativeAdsHelper: NativeAdsHelper
    lateinit var fullScreenAdsHelper: FullScreenAdsHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchPipeline()
        observeViewModelData()
        setupClickListeners()

        // =========================================================================
        // CODE PATCH START: TRIGGER VERSION-WISE PERMISSION GATE ON ENTRY
        // =========================================================================
        checkAndPromptRuntimeNotificationPermission()
        // =========================================================================
    }

    // =========================================================================
    // CODE PATCH START: VERSION CONTROLLER METHOD
    // =========================================================================
    private fun checkAndPromptRuntimeNotificationPermission() {
        // POST_NOTIFICATIONS permission strictly required only on Android 13 (API 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Android 12 and below handle notifications automatically without runtime popups
            android.util.Log.d("LanguageFragment_Log", "Pre-Android 13 device detected. Auto-granted context.")
        }
    }
    // =========================================================================

    private fun setupRecyclerView() {
        languageAdapter = LanguageAdapter(requireActivity()) { selectedLanguage ->
            viewModel.selectLanguage(selectedLanguage)
        }

        binding.rvLanguageList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = languageAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearchPipeline() {
        binding.etSearchLanguage.doAfterTextChanged { text ->
            viewModel.filterLanguages(text?.toString() ?: "")
        }
    }

    private fun observeViewModelData() {
        viewModel.uiLanguageList.observe(viewLifecycleOwner) { rawLanguages ->
            val mixedListWithAds = mutableListOf<Any>()

            rawLanguages.forEachIndexed { index, languageModel ->
                mixedListWithAds.add(languageModel)
                if (index == 2) {
                    mixedListWithAds.add("AD_UNIT")
                }
            }
            languageAdapter.submitList(mixedListWithAds)
        }

        binding.btnConfirmSelection.isEnabled = false
        viewModel.selectedLanguage.observe(viewLifecycleOwner) { selected ->
            binding.btnConfirmSelection.isEnabled = (selected != null)
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnConfirmSelection.setAdClickListener(requireActivity(), fullScreenAdsHelper) {
            val confirmedLanguage = viewModel.selectedLanguage.value
            confirmedLanguage?.let {

                prefManager.selectedLanguage = it.languageCode
                prefManager.isLanguageSelectedFirstTime = true

                // Refresh application configuration locale context strings
                MyApplication.setLocale(requireContext())

                // Force completely clean restart target to apply language mutations globally
                val intent = Intent(requireActivity(), MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)

                // Kill current hosting activity container cleanly
                requireActivity().finish()
            }
        }
    }



}