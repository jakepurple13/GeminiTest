package com.programmersbox.geminitest.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.programmersbox.geminitest.BuildConfig
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChatViewModel(
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.apiKey
    ),
    val chatHistoryDb: ChatHistoryDb = ChatHistoryDb()
) : ViewModel() {

    private var chat = generativeModel.startChat()

    val messageList = mutableStateListOf<Message>()

    var isLoading by mutableStateOf(false)

    var prompt by mutableStateOf("")

    fun onPromptChange(value: String) {
        prompt = value
    }

    var chatStream by mutableStateOf("")

    fun send(input: String) {
        viewModelScope.launch {
            runCatching {
                messageList.add(Message.User(input))
                isLoading = true
                //val message = input
                //prompt = ""
                chat.sendMessageStream(input)
            }
                .onSuccess {
                    it
                        .onEach { chatStream += it.text.orEmpty() }
                        .onCompletion {
                            messageList.add(Message.Gemini(chatStream))
                            chatStream = ""
                            isLoading = false
                        }
                        .launchIn(this)
                }
                .onFailure {
                    it.printStackTrace()
                    messageList.add(Message.Error(it.localizedMessage.orEmpty()))
                    isLoading = false
                }
        }
    }

    fun saveMessageHistory() {
        viewModelScope.launch {
            chatHistoryDb.saveMessages(chat.history)
        }
    }

    fun loadChatHistory(history: History) {
        chat = generativeModel.startChat(
            history = history.messageList.map {
                content(it.role) { text(it.message) }
            }
        )
        messageList.clear()
        messageList.addAll(
            history.messageList.map {
                when (it.role) {
                    "user" -> Message.User(it.message)
                    "model" -> Message.Gemini(it.message)
                    else -> Message.Error(it.message)
                }
            }
        )
    }

    fun newChat() {
        chat = generativeModel.startChat()
        messageList.clear()
        messageList.add(Message.Gemini("Hello! I am Gemini!"))
    }
}

sealed class Message(val text: String, val timestamp: Long = System.currentTimeMillis()) {
    class Gemini(text: String) : Message(text)
    class User(text: String) : Message(text)
    class Error(text: String) : Message(text)
}