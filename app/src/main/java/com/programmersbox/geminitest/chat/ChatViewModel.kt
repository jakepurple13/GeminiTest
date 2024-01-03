package com.programmersbox.geminitest.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.programmersbox.geminitest.BuildConfig
import kotlinx.coroutines.launch

class ChatViewModel(
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.apiKey
    )
) : ViewModel() {
    val messageList = mutableStateListOf<Message>(
        Message.Gemini("Hello! I am Gemini!")
    )
    var isLoading by mutableStateOf(false)

    var prompt by mutableStateOf("")

    fun onPromptChange(value: String) {
        prompt = value
    }

    fun send() {
        viewModelScope.launch {
            runCatching {
                messageList.add(Message.User(prompt))
                isLoading = true
                val message = prompt
                prompt = ""
                generativeModel.generateContent(message).text!!
            }
                .onSuccess { messageList.add(Message.Gemini(it)) }
                .onFailure { messageList.add(Message.Error(it.localizedMessage.orEmpty())) }
            isLoading = false
        }
    }
}

sealed class Message(val text: String) {
    class Gemini(text: String) : Message(text)
    class User(text: String) : Message(text)
    class Error(text: String) : Message(text)
}