package com.programmersbox.geminitest.summarize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.programmersbox.geminitest.BuildConfig
import com.programmersbox.geminitest.safetySettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SummarizeViewModel(
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.apiKey,
        safetySettings = safetySettings
    )
) : ViewModel() {

    private val _uiState: MutableStateFlow<SummarizeUiState> =
        MutableStateFlow(SummarizeUiState.Initial)
    val uiState: StateFlow<SummarizeUiState> =
        _uiState.asStateFlow()

    fun summarize(inputText: String) {
        _uiState.value = SummarizeUiState.Loading

        val prompt = "Summarize the following text for me: $inputText"

        viewModelScope.launch {
            try {
                generativeModel.generateContentStream(prompt)
                    .onEach { response ->
                        response.text?.let { outputContent ->
                            val state = _uiState.value as? SummarizeUiState.Success
                            if(state != null) {
                                _uiState.value = SummarizeUiState.Success(state.outputText + outputContent)
                            } else {
                                _uiState.value = SummarizeUiState.Success(outputContent)
                            }
                        }
                    }
                    .launchIn(this)

            } catch (e: Exception) {
                _uiState.value = SummarizeUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}