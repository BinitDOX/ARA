package com.dox.ara.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Reply
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.dox.ara.R
import com.dox.ara.model.Message
import com.dox.ara.ui.component.chat.ChatInput
import com.dox.ara.ui.component.chat.TopBarChat
import com.dox.ara.ui.component.chat.chatbubble.QuotedMessage
import com.dox.ara.ui.component.chat.chatbubble.ReceivedMessageRow
import com.dox.ara.ui.component.chat.chatbubble.SentMessageRow
import com.dox.ara.ui.data.MessageStatus
import com.dox.ara.ui.data.Role
import com.dox.ara.ui.data.RouteItem
import com.dox.ara.ui.theme.ARATheme
import com.dox.ara.utility.Constants.TEST
import com.dox.ara.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    navController: NavController,
    chatViewModel: ChatViewModel = hiltViewModel(),
) {
    val messages = chatViewModel.messages.collectAsLazyPagingItems()
    val assistant by chatViewModel.assistant.collectAsStateWithLifecycle()
    val chat by chatViewModel.chat.collectAsStateWithLifecycle()

    val colorAI by chatViewModel.assistantColor.collectAsStateWithLifecycle()

    val speechToTextState by chatViewModel.speechToTextState.collectAsStateWithLifecycle()

    var quotingMessage by remember { mutableStateOf<Message?>(null) }
    val chatInput = remember { mutableStateOf(TextFieldValue("")) }
    val testCommands = remember { chatViewModel.getAllTestCommandUsages() }

    val scrollState = rememberLazyListState()

    LaunchedEffect( messages.itemSnapshotList.items.size) {
        chatViewModel.markAsRead()
        scrollState.scrollToItem(0)
    }

    LaunchedEffect(speechToTextState.spokenText) {
        chatInput.value = TextFieldValue(speechToTextState.spokenText)
    }

    Scaffold(
        topBar = {
            TopBarChat(navController, chatViewModel
            ) { navController.navigate("${RouteItem.Assistant.route}/${assistant!!.id}") }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .paint(
                        painter = painterResource(id = R.drawable.chat_background),
                        contentScale = ContentScale.FillBounds,
                        alpha = 0.1f
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = scrollState,
                    reverseLayout = true,
                    contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
                ) {
                    items(
                        count = messages.itemCount,
                        key = messages.itemKey { m -> m.id },
                        contentType = messages.itemContentType { "Messages" }

                    ) { index: Int ->
                        val message = messages[index]

                        var selectedMessage by remember { mutableStateOf<Message?>(null) }
                        var showEditDialog by remember { mutableStateOf(false) }

                        if (message != null &&
                            !(chat?.showFailedMessages == false && message.status == MessageStatus.FAILED)) {
                            // TODO: Add scroll to on quoted message (findIndexById or map)

                            if(chat?.showTokens == false){
                                message.content = removeAngularBrackets(message.content)
                            }

                            if(chat?.showCommands == false){
                                message.content = removeSquareBrackets(message.content)
                            }

                            val quotedMessage =
                                message.quotedId?.let { findMessageById(messages, it) }
                            val quotedColor = when (quotedMessage?.from) {
                                Role.ASSISTANT -> colorAI
                                Role.USER -> chatViewModel.colorUser
                                Role.SYSTEM -> chatViewModel.colorSystem
                                null -> MaterialTheme.colorScheme.primary
                            }

                            val quotedFrom = when (quotedMessage?.from) {
                                Role.ASSISTANT -> assistant?.name ?: Role.ASSISTANT.name
                                Role.USER -> stringResource(id = R.string.placeholder_user)
                                Role.SYSTEM -> stringResource(id = R.string.placeholder_system)
                                null -> stringResource(id = R.string.placeholder_user)
                            }

                            Box {
                                when (message.from) {
                                    Role.ASSISTANT -> {
                                        ReceivedMessageRow(
                                            receiverName = assistant?.name
                                                ?: stringResource(id = R.string.placeholder_deleted),
                                            receiverNameColor = colorAI,
                                            text = message.content,
                                            quotedMessage = quotedMessage?.content,
                                            quotedFrom = quotedFrom,
                                            messageTime = chatViewModel.simpleDateFormat.format(
                                                message.timestamp
                                            ),
                                            quotedColor = quotedColor,
                                            onLongPress = {
                                                selectedMessage = message
                                            },
                                            messageStatus = message.status,
                                            showMessageStatus = false,
                                            onPress = {
                                                chatViewModel.playMessageAudio(message.id, message.content, message.timestamp)
                                            }
                                        )
                                    }

                                    Role.USER -> {
                                        SentMessageRow(
                                            text = message.content,
                                            quotedMessage = quotedMessage?.content,
                                            quotedFrom = quotedFrom,
                                            quotedColor = quotedColor,
                                            messageTime = chatViewModel.simpleDateFormat.format(
                                                message.timestamp
                                            ),
                                            messageStatus = message.status,
                                            onLongPress = {
                                                selectedMessage = message
                                            }
                                        )
                                    }

                                    Role.SYSTEM -> {
                                        if(chat?.showSystemMessages == true) {
                                            ReceivedMessageRow(
                                                receiverName = Role.SYSTEM.name,
                                                receiverNameColor = chatViewModel.colorSystem,
                                                text = message.content,
                                                quotedMessage = quotedMessage?.content,
                                                quotedFrom = quotedFrom,
                                                messageTime = chatViewModel.simpleDateFormat.format(
                                                    message.timestamp
                                                ),
                                                quotedColor = quotedColor,
                                                messageStatus = message.status,
                                                showMessageStatus = true,
                                                onLongPress = {
                                                    selectedMessage = message
                                                }
                                            )
                                        }
                                    }
                                }

                                if (selectedMessage != null) {
                                    // TODO: Add items by loop
                                    DropdownMenu(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.tertiary),

                                        offset = if(selectedMessage!!.from == Role.ASSISTANT) DpOffset(0.dp, 0.dp)
                                        else DpOffset(2000.dp, 0.dp), // Wont go out of screen
                                        expanded = selectedMessage != null,
                                        onDismissRequest = { selectedMessage = null },
                                    ) {
                                        DropdownMenuItem(
                                            colors = MenuDefaults.itemColors(
                                                textColor = MaterialTheme.colorScheme.onTertiary,
                                                leadingIconColor = MaterialTheme.colorScheme.onTertiary,
                                            ),
                                            text = {
                                                Text(text = stringResource(id = R.string.btn_delete_message))
                                            },
                                            onClick = {
                                                chatViewModel.viewModelScope.launch {
                                                    chatViewModel.deleteMessage(selectedMessage!!.id)
                                                }
                                                selectedMessage = null
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.Delete,
                                                    contentDescription = stringResource(id = R.string.btn_delete_message)
                                                )
                                            }
                                        )

                                        DropdownMenuItem(
                                            colors = MenuDefaults.itemColors(
                                                textColor = MaterialTheme.colorScheme.onTertiary,
                                                leadingIconColor = MaterialTheme.colorScheme.onTertiary,
                                            ),
                                            text = {
                                                Text(text = stringResource(id = R.string.btn_edit_message))
                                            },
                                            onClick = {
                                                showEditDialog = true
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.Edit,
                                                    contentDescription = stringResource(id = R.string.btn_edit_message)
                                                )
                                            }
                                        )

                                        DropdownMenuItem(
                                            colors = MenuDefaults.itemColors(
                                                textColor = MaterialTheme.colorScheme.onTertiary,
                                                leadingIconColor = MaterialTheme.colorScheme.onTertiary,
                                            ),
                                            text = {
                                                Text(text = stringResource(id = R.string.btn_copy_message))
                                            },
                                            onClick = {
                                                chatViewModel.copyToClipboard(selectedMessage!!.content)
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.ContentCopy,
                                                    contentDescription = stringResource(id = R.string.btn_copy_message)
                                                )
                                            }
                                        )

                                        DropdownMenuItem(
                                            colors = MenuDefaults.itemColors(
                                                textColor = MaterialTheme.colorScheme.onTertiary,
                                                leadingIconColor = MaterialTheme.colorScheme.onTertiary,
                                            ),
                                            text = {
                                                Text(text = stringResource(id = R.string.btn_reply_message))
                                            },
                                            onClick = {
                                                quotingMessage = selectedMessage
                                                selectedMessage = null
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Outlined.Reply,
                                                    contentDescription = stringResource(id = R.string.btn_reply_message)
                                                )
                                            }
                                        )
                                    }

                                    if(showEditDialog){
                                        EditMessageDialog(
                                            message = selectedMessage!!.content,
                                            onDismiss = { showEditDialog = false },
                                            onSave = {
                                                chatViewModel.viewModelScope.launch {
                                                    chatViewModel.editMessage(selectedMessage!!.id, it)
                                                }
                                                showEditDialog = false
                                                selectedMessage = null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Column {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (quotingMessage != null) {
                            val quotingColor = when (quotingMessage!!.from) {
                                Role.ASSISTANT -> colorAI
                                Role.USER -> chatViewModel.colorUser
                                Role.SYSTEM -> chatViewModel.colorSystem
                            }

                            val quotingFrom = when (quotingMessage!!.from) {
                                Role.ASSISTANT -> assistant?.name ?: Role.ASSISTANT.name
                                Role.USER -> stringResource(id = R.string.placeholder_user)
                                Role.SYSTEM -> stringResource(id = R.string.placeholder_system)
                            }

                            QuotedMessage(
                                quotedFrom = quotingFrom,
                                quotedMessage = quotingMessage!!.content,
                                quotedColor = quotingColor,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Outlined.Cancel,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(18.dp)
                                    .clickable { quotingMessage = null },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.35f)
                            )
                        }
                    }

                    ChatInput(
                        isListening = speechToTextState.isListening,
                        chatInput = chatInput,
                        testCommands = testCommands,
                        onMessageSent = { messageContent ->
                            chatViewModel.viewModelScope.launch {
                                if(messageContent.contains(TEST)){
                                    chatViewModel.sendTestMessage(messageContent, quotingMessage?.id)
                                } else {
                                    chatViewModel.sendMessage(messageContent, quotingMessage?.id)
                                }
                                quotingMessage = null
                            }
                        },
                        onMicToggle = {
                            if (speechToTextState.isListening) {
                                chatViewModel.stopListening()
                            } else {
                                chatViewModel.startListening()
                            }
                        }
                    )
                }
            }
        }
    }
}

// TODO: Optimize using a map
private fun findMessageById(messages: LazyPagingItems<Message>, id: Long): Message? {
    for (i in 0 until messages.itemCount) {
        val message = messages[i]
        if (message?.id == id) {
            return message
        }
    }
    return null
}


fun removeSquareBrackets(text: String): String {
    val squareBracketsPattern = "\\[.*?\\]"
    return text.replace(Regex(squareBracketsPattern), "")
}

fun removeAngularBrackets(text: String): String {
    val angularBracketsPattern = "<.*?>"
    return text.replace(Regex(angularBracketsPattern), "")
}

@Composable
fun EditMessageDialog(
    message: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var editedMessage by remember { mutableStateOf(message) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onSave(editedMessage)
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Edit Message") },
        text = {
            OutlinedTextField(
                value = editedMessage,
                onValueChange = { editedMessage = it },
                label = { Text("Message") },
                maxLines = 10,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Preview
@Composable
private fun EditMessageDialogPreview() {
    ARATheme {
        EditMessageDialog(
            message = "Hello",
            onDismiss = {},
            onSave = {}
        )
    }
}


@Preview
@Composable
private fun ChatScreenPreview() {
    ARATheme {
        ChatScreen(NavController(LocalContext.current))
    }
}