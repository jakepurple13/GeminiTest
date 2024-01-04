package com.programmersbox.geminitest.image

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
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

class ImageViewModel(
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-pro-vision",
        apiKey = BuildConfig.apiKey
    )
) : ViewModel() {

    var description by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    fun describeImage(question: String, images: List<Bitmap>) {
        viewModelScope.launch {
            isLoading = true
            description = ""
            val prompt = "Look at the image(s), and then answer the following question: $question"
            runCatching {
                generativeModel.generateContentStream(
                    content {
                        for (bitmap in images) {
                            image(bitmap)
                        }
                        text(prompt)
                    }
                )
            }
                .onSuccess { response ->
                    response
                        .onEach { description += it.text.orEmpty() }
                        .onCompletion { isLoading = false }
                        .launchIn(this)
                }
                .onFailure {
                    description = it.localizedMessage.orEmpty()
                    isLoading = false
                }
        }
    }
}