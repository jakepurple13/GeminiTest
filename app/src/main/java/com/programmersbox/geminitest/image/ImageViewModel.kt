package com.programmersbox.geminitest.image

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.programmersbox.geminitest.BuildConfig
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ImageViewModel(
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-pro-vision",
        apiKey = BuildConfig.apiKey
    )
) : ViewModel() {

    var description by mutableStateOf("")
    var imageUri by mutableStateOf<Uri?>(null)
    var isLoading by mutableStateOf(false)

    fun describeImage(contentResolver: ContentResolver) {
        viewModelScope.launch {
            isLoading = true
            runCatching {
                generativeModel.generateContentStream(
                    content {
                        image(getBitmap(contentResolver, imageUri!!)!!)
                        text("Describe this image")
                    }
                )
            }
                .onSuccess { response ->
                    response
                        .onEach { description += it.text.orEmpty() }
                        .launchIn(this)
                }
                .onFailure { description = it.localizedMessage.orEmpty() }
            isLoading = false
        }
    }

    private fun getBitmap(contentResolver: ContentResolver, fileUri: Uri?): Bitmap? {
        return try {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, fileUri!!))
        } catch (e: Exception) {
            null
        }
    }
}