package com.programmersbox.geminitest.image

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.programmersbox.geminitest.BuildConfig
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ImageScreen(
    onBackClick: () -> Unit,
    viewModel: ImageViewModel = viewModel()
) {
    val context = LocalContext.current

    val pickDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { document -> viewModel.imageUri = document }

    var tempUri by remember { mutableStateOf(Uri.EMPTY) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { document ->
        if (document) {
            viewModel.imageUri = tempUri
        }
    }

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA,
        onPermissionResult = { granted ->
            if (granted) {
                tempUri = context.createTempPictureUri()
                cameraLauncher.launch(tempUri)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { pickDocumentLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    enabled = !viewModel.isLoading
                ) {
                    Icon(Icons.Default.Image, null)
                }

                IconButton(
                    onClick = cameraPermissionState::launchPermissionRequest,
                    enabled = !viewModel.isLoading
                ) {
                    Icon(Icons.Default.CameraAlt, null)
                }
            }

            GlideImage(
                imageModel = { viewModel.imageUri },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.FillWidth
                ),
            )

            IconButton(
                onClick = { viewModel.describeImage(context.contentResolver) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null)
            }

            if (viewModel.isLoading) {
                CircularProgressIndicator()
            }

            Text(viewModel.description)
        }
    }
}

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
                generativeModel.generateContent(
                    content {
                        image(getBitmap(contentResolver, imageUri!!)!!)
                        text("Describe this image")
                    }
                ).text!!
            }
                .onSuccess { description = it }
                .onFailure { description = it.localizedMessage.orEmpty() }
            isLoading = false
        }
    }

    fun getBitmap(contentResolver: ContentResolver, fileUri: Uri?): Bitmap? {
        return try {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, fileUri!!))
        } catch (e: Exception) {
            null
        }
    }
}

fun Context.createTempPictureUri(
    provider: String = "${BuildConfig.APPLICATION_ID}.provider",
    fileName: String = "picture_${System.currentTimeMillis()}",
    fileExtension: String = ".png"
): Uri {
    val tempFile = File.createTempFile(
        fileName, fileExtension, cacheDir
    ).apply { createNewFile() }

    return FileProvider.getUriForFile(applicationContext, provider, tempFile)
}