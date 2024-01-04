package com.programmersbox.geminitest.question

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.programmersbox.geminitest.BuildConfig
import com.programmersbox.geminitest.safetySettings
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class QuestionViewModel(
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.apiKey,
        safetySettings = safetySettings
    ),
) : ViewModel() {

    var isLoading by mutableStateOf(false)

    var answer by mutableStateOf("")

    fun askQuestion(inputText: String) {
        viewModelScope.launch {
            isLoading = true
            answer = ""
            runCatching {
                generativeModel.generateContentStream(inputText)
                    .onEach { response -> response.text?.let { outputContent -> answer += outputContent } }
                    .onCompletion { isLoading = false }
                    .launchIn(this)
            }
                .onFailure { isLoading = false }
        }
    }
}