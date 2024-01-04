package com.programmersbox.geminitest.image

import android.Manifest
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.programmersbox.geminitest.BuildConfig
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageScreen(
    onBackClick: () -> Unit,
    viewModel: ImageViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val imageRequestBuilder = ImageRequest.Builder(LocalContext.current)
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

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

            UserInput { question, items ->
                scope.launch {
                    val bitmaps = items.mapNotNull {
                        val imageRequest = imageRequestBuilder
                            .data(it)
                            // Scale the image down to 768px for faster uploads
                            .size(size = 768)
                            .precision(Precision.EXACT)
                            .build()
                        try {
                            val result = imageLoader.execute(imageRequest)
                            if (result is SuccessResult) {
                                return@mapNotNull (result.drawable as BitmapDrawable).bitmap
                            } else {
                                return@mapNotNull null
                            }
                        } catch (e: Exception) {
                            return@mapNotNull null
                        }
                    }
                    viewModel.describeImage(question, bitmaps)
                }
            }

            AnimatedVisibility(visible = viewModel.description.isNotEmpty() || viewModel.isLoading) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .animateContentSize()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            viewModel.description,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        if (viewModel.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun UserInput(
    onReasonClicked: (String, List<Uri>) -> Unit
) {
    val context = LocalContext.current
    var userQuestion by rememberSaveable { mutableStateOf("") }
    val imageUris = rememberSaveable(saver = UriSaver()) { mutableStateListOf() }

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { document -> document?.let { imageUris.add(it) } }

    var tempUri by remember { mutableStateOf(Uri.EMPTY) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { document ->
        if (document) {
            imageUris.add(tempUri)
            tempUri = Uri.EMPTY
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

    Card(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(all = 4.dp)
                    .align(Alignment.CenterVertically)
            ) {
                IconButton(
                    onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "Add",
                    )
                }

                IconButton(
                    onClick = cameraPermissionState::launchPermissionRequest,
                ) {
                    Icon(Icons.Default.CameraAlt, null)
                }
            }
            OutlinedTextField(
                value = userQuestion,
                label = { Text("Question") },
                placeholder = { Text("Ask a question about the images") },
                onValueChange = { userQuestion = it },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
            )
            TextButton(
                onClick = {
                    if (userQuestion.isNotBlank()) {
                        onReasonClicked(userQuestion, imageUris.toList())
                    }
                },
                modifier = Modifier
                    .padding(all = 4.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text("Go")
            }
        }

        LazyRow(
            modifier = Modifier.padding(all = 8.dp)
        ) {
            items(imageUris) { imageUri ->
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(4.dp)
                        .requiredSize(72.dp)
                        .clickable { imageUris.remove(imageUri) }
                )
            }
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

class UriSaver : Saver<MutableList<Uri>, List<String>> {
    override fun restore(value: List<String>): MutableList<Uri> = value.map {
        Uri.parse(it)
    }.toMutableList()

    override fun SaverScope.save(value: MutableList<Uri>): List<String> = value.map { it.toString() }
}