package com.programmersbox.geminitest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.programmersbox.geminitest.chat.ChatScreen
import com.programmersbox.geminitest.image.ImageScreen
import com.programmersbox.geminitest.question.QuestionScreen
import com.programmersbox.geminitest.summarize.Summarize
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
                                onQuestionClick = { navController.navigate("question") },
                                onSummarizeClick = { navController.navigate("summarize") },
                                onChatClick = { navController.navigate("chat") },
                                onImageClick = { navController.navigate("image") }
                            )
                        }

                        composable("question") {
                            QuestionScreen(
                                onBackClick = { navController.popBackStack() }
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
    onQuestionClick: NavClick,
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
            Button(onClick = onQuestionClick) { Text("Question") }
            Button(onClick = onSummarizeClick) { Text("Summarize") }
            Button(onClick = onChatClick) { Text("Chat") }
            Button(onClick = onImageClick) { Text("Image") }
        }
    }
}