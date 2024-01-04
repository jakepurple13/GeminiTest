package com.programmersbox.geminitest.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.programmersbox.geminitest.ui.theme.GeminiTestTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val focusRequester = remember { FocusRequester() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val lazyState = rememberLazyListState()
    LaunchedEffect(viewModel.messageList.lastIndex) {
        lazyState.animateScrollToItem(viewModel.messageList.lastIndex)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            OutlinedTextField(
                value = viewModel.prompt,
                label = { Text("Chat with Gemini") },
                placeholder = { Text("Enter Text") },
                onValueChange = viewModel::onPromptChange,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (viewModel.prompt.isNotBlank()) {
                                viewModel.send()
                            }
                        },
                    ) { Icon(Icons.AutoMirrored.Filled.Send, null) }
                },
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth()
                    .background(BottomAppBarDefaults.containerColor)
                    .navigationBarsPadding()
            )
        },
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .imePadding()
    ) { padding ->
        LazyColumn(
            state = lazyState,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize() // fill the entire window
                //.imePadding() // padding for the bottom for the IME
                .imeNestedScroll(), // scroll IME at the bottom
        ) {
            items(viewModel.messageList) { message ->
                when (message) {
                    is Message.Error -> ErrorMessage(
                        message = message,
                    )

                    is Message.Gemini -> GeminiMessage(
                        message = message,
                    )

                    is Message.User -> UserMessage(
                        message = message,
                    )
                }
            }

            if (viewModel.isLoading) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun GeminiMessage(
    message: Message.Gemini,
    modifier: Modifier = Modifier
) {
    Box(
        modifier.fillMaxWidth()
    ) {
        OutlinedCard(
            shape = RoundedCornerShape(
                topStart = 48f,
                topEnd = 48f,
                bottomStart = 0f,
                bottomEnd = 48f
            ),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Row(modifier = Modifier.padding(8.dp)) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = "Person Icon"
                )
                SelectionContainer {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UserMessage(
    message: Message.User,
    modifier: Modifier = Modifier
) {
    Box(
        modifier.fillMaxWidth()
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 48f,
                topEnd = 48f,
                bottomStart = 48f,
                bottomEnd = 0f
            ),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            SelectionContainer {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: Message.Error,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Icon(
            Icons.Outlined.Warning,
            contentDescription = "Person Icon",
            tint = MaterialTheme.colorScheme.error
        )
        SelectionContainer {
            Text(
                text = message.text,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@PreviewDynamicColors
@PreviewLightDark
@Composable
private fun ChatScreenPreview() {
    GeminiTestTheme {
        ChatScreen(onBackClick = {})
    }
}
