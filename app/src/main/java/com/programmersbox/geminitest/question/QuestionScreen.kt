package com.programmersbox.geminitest.question

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.programmersbox.geminitest.chat.MessageInput
import com.programmersbox.geminitest.chat.surfaceColorAtElevation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionScreen(
    onBackClick: () -> Unit,
    viewModel: QuestionViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Question") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            MessageInput(
                onSendMessage = viewModel::askQuestion,
                modifier = Modifier
                    .imePadding()
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                    .navigationBarsPadding()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedVisibility(visible = viewModel.answer.isNotEmpty()) {
                OutlinedCard(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .animateContentSize()
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Row(
                        modifier = Modifier
                            .padding(all = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = "Person Icon",
                            modifier = Modifier
                                .requiredSize(36.dp)
                                .drawBehind { drawCircle(color = Color.White) }
                        )
                        SelectionContainer {
                            Text(
                                text = viewModel.answer,
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = viewModel.isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
