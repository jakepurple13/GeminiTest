package com.programmersbox.geminitest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.programmersbox.geminitest.chat.ChatScreen
import com.programmersbox.geminitest.generate.Summarize
import com.programmersbox.geminitest.generate.SummarizeUiState
import com.programmersbox.geminitest.generate.SummarizeViewModel
import com.programmersbox.geminitest.image.ImageScreen
import com.programmersbox.geminitest.ui.theme.GeminiTestTheme

typealias NavClick = () -> Unit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeminiTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                onSummarizeClick = { navController.navigate("summarize") },
                                onChatClick = { navController.navigate("chat") },
                                onImageClick = { navController.navigate("image") }
                            )
                        }
                        composable("summarize") {
                            Summarize(
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("chat") {
                            ChatScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("image") {
                            ImageScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    onSummarizeClick: NavClick,
    onChatClick: NavClick,
    onImageClick: NavClick,
) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Gemini Playground") }) }
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Button(onClick = onSummarizeClick) { Text("Summarize") }
            Button(onClick = onChatClick) { Text("Chat") }
            Button(onClick = onImageClick) { Text("Image") }
        }
    }
}