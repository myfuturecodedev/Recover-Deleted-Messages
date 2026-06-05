package com.futurecode.recoverdeletedmessages.viewModel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.futurecode.recoverdeletedmessages.model.LanguageModel
class LanguageViewModel(application: Application) : AndroidViewModel(application) {

    // Keep this reference master list private
    private var masterLanguageList = listOf(
        LanguageModel("en_default", "English (default)", false),
        LanguageModel("en", "English", false),
        LanguageModel("ar", "العربية", false),
        LanguageModel("de", "Deutsch", false),
        LanguageModel("es", "Español", false),
        LanguageModel("fr", "Français", false),
        LanguageModel("id", "Bahasa Indonesia", false),
        LanguageModel("it", "Italiano", false),
        LanguageModel("ja", "日本語", false),
        LanguageModel("ko", "한국어", false)
    )

    private var currentSearchQuery: String = ""

    private val _uiLanguageList = MutableLiveData<List<LanguageModel>>()
    val uiLanguageList: LiveData<List<LanguageModel>> get() = _uiLanguageList

    private val _selectedLanguage = MutableLiveData<LanguageModel?>()
    val selectedLanguage: LiveData<LanguageModel?> get() = _selectedLanguage

    init {
        _uiLanguageList.value = masterLanguageList
    }

    fun filterLanguages(query: String) {
        currentSearchQuery = query.trim()
        dispatchCurrentState()
    }

    /**
     * FIXES SELECTION BUG: Generates deep copies using .copy() to alter structural reference values
     */
    fun selectLanguage(targetLanguage: LanguageModel) {
        // Map elements into an entirely new list pointer structure using clean object instances
        masterLanguageList = masterLanguageList.map { model ->
            model.copy(isSelected = (model.languageCode == targetLanguage.languageCode))
        }

        // Cache selected item tracking state reference directly
        val activeSelection = masterLanguageList.find { it.isSelected }
        _selectedLanguage.value = activeSelection

        // Dispatch states immediately through active pipeline filters
        dispatchCurrentState()
    }

    private fun dispatchCurrentState() {
        if (currentSearchQuery.isEmpty()) {
            _uiLanguageList.value = masterLanguageList
        } else {
            _uiLanguageList.value = masterLanguageList.filter {
                it.displayLanguage.contains(currentSearchQuery, ignoreCase = true)
            }
        }
    }
}