package com.programmersbox.geminitest.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.programmersbox.geminitest.ui.theme.GeminiTestTheme
import kotlinx.coroutines.launch
import kotlin.math.ln

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val lazyState = rememberLazyListState()
    LaunchedEffect(viewModel.messageList.lastIndex, viewModel.chatStream) {
        lazyState.animateScrollToItem(0)
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("Saved Chats") },
                            actions = {
                                IconButton(onClick = viewModel::saveMessageHistory) {
                                    Icon(Icons.Default.SaveAlt, null)
                                }
                            }
                        )
                    }
                ) { padding ->
                    val chatHistory by viewModel.chatHistoryDb.getHistory().collectAsState(initial = emptyList())
                    LazyColumn(
                        contentPadding = padding,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(chatHistory) { history ->
                            HistoryItem(
                                history = history,
                                onClick = { viewModel.loadChatHistory(history) }
                            )
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Chat") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::newChat) {
                            Icon(Icons.Default.AddCircleOutline, null)
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
                MessageInput(
                    onSendMessage = { viewModel.send(it) },
                    resetScroll = {
                        scope.launch { lazyState.animateScrollToItem(0) }
                    },
                    modifier = Modifier
                        .imePadding()
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                        .navigationBarsPadding()
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { padding ->
            LazyColumn(
                state = lazyState,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = padding,
                reverseLayout = true,
                modifier = Modifier
                    .fillMaxSize() // fill the entire window
                    //.imePadding() // padding for the bottom for the IME
                //.imeNestedScroll(), // scroll IME at the bottom
            ) {

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

                if (viewModel.chatStream.isNotEmpty()) {
                    item {
                        GeminiMessage(message = Message.Gemini(viewModel.chatStream))
                    }
                }

                items(viewModel.messageList.reversed()) { message ->
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

                item { GeminiMessage(message = Message.Gemini("Hello! I am Gemini!")) }

            }
        }
    }
}

@Composable
private fun GeminiMessage(
    message: Message.Gemini,
    modifier: Modifier = Modifier
) {
    ChatBubbleItem(
        chatMessage = message,
        modifier = modifier
    )
}

@Composable
private fun UserMessage(
    message: Message.User,
    modifier: Modifier = Modifier
) {
    ChatBubbleItem(
        chatMessage = message,
        modifier = modifier
    )
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

@Composable
fun ChatBubbleItem(
    chatMessage: Message,
    modifier: Modifier = Modifier
) {
    val isModelMessage = chatMessage is Message.Gemini

    val backgroundColor = when (chatMessage) {
        is Message.Gemini -> MaterialTheme.colorScheme.primaryContainer
        is Message.User -> MaterialTheme.colorScheme.secondaryContainer
        is Message.Error -> MaterialTheme.colorScheme.errorContainer
    }

    val bubbleShape = if (isModelMessage) {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    }

    val horizontalAlignment = if (isModelMessage) {
        Alignment.Start
    } else {
        Alignment.End
    }

    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = when (chatMessage) {
                is Message.Error -> "Error"
                is Message.Gemini -> "Gemini"
                is Message.User -> "User"
            },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        BoxWithConstraints {
            Card(
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                shape = bubbleShape,
                modifier = Modifier.widthIn(0.dp, maxWidth * 0.9f)
            ) {
                SelectionContainer {
                    Text(
                        text = chatMessage.text,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    history: History,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        ListItem(headlineContent = { Text(history.messageList.firstOrNull()?.message.orEmpty()) })
    }
}

@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    resetScroll: () -> Unit = {},
) {
    var userMessage by rememberSaveable { mutableStateOf("") }

    ElevatedCard(
        shape = RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp,
            bottomEnd = 0.dp,
            bottomStart = 0.dp
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = userMessage,
                label = { Text("Message") },
                onValueChange = { userMessage = it },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .weight(0.85f)
            )
            IconButton(
                onClick = {
                    if (userMessage.isNotBlank()) {
                        onSendMessage(userMessage)
                        userMessage = ""
                        resetScroll()
                    }
                },
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .weight(0.15f)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "send",
                    modifier = Modifier
                )
            }
        }
    }
}

internal fun ColorScheme.surfaceColorAtElevation(
    elevation: Dp,
): Color {
    if (elevation == 0.dp) return surface
    val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
    return primary.copy(alpha = alpha).compositeOver(surface)
}

@PreviewDynamicColors
@PreviewLightDark
@Composable
private fun ChatScreenPreview() {
    GeminiTestTheme {
        ChatScreen(onBackClick = {})
    }
}
