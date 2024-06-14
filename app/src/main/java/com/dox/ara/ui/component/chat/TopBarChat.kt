package com.dox.ara.ui.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.dox.ara.R
import com.dox.ara.ui.component.ActionMenu
import com.dox.ara.ui.component.ProfilePicture
import com.dox.ara.ui.data.ActionItem
import com.dox.ara.ui.data.OverflowMode
import com.dox.ara.ui.theme.ARATheme
import com.dox.ara.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun TopBarChat(
    navController: NavController,
    chatViewModel: ChatViewModel = hiltViewModel(),
    onProfileClicked: () -> Unit = {}
) {
    val assistant by chatViewModel.assistant.collectAsStateWithLifecycle()
    val chat by chatViewModel.chat.collectAsStateWithLifecycle()
    val isDefaultAssistant by chatViewModel.isDefaultAssistant.collectAsStateWithLifecycle()
    val openDeleteDialog = remember { mutableStateOf(false) }

    val actionItems = listOf(
        ActionItem(R.string.btn_view_assistant, Icons.Outlined.Person, OverflowMode.ALWAYS_OVERFLOW) {},
        ActionItem(R.string.btn_delete_chat, Icons.Outlined.Delete, OverflowMode.ALWAYS_OVERFLOW) {
            chatViewModel.viewModelScope.launch {
                openDeleteDialog.value = true
            }
        },
        ActionItem(R.string.btn_show_system_messages,
            if(chat?.showSystemMessages == true) Icons.Outlined.CheckCircle
            else Icons.Outlined.Circle,
            OverflowMode.ALWAYS_OVERFLOW) {
            chatViewModel.toggleShowSystemMessages()
        },
        ActionItem(R.string.btn_show_failed_messages,
            if(chat?.autoResponses == true) Icons.Outlined.CheckCircle
            else Icons.Outlined.Circle,
            OverflowMode.ALWAYS_OVERFLOW) {
            chatViewModel.toggleShowFailedMessages()
        },
        ActionItem(R.string.btn_show_commands,
            if(chat?.autoResponses == true) Icons.Outlined.CheckCircle
            else Icons.Outlined.Circle,
            OverflowMode.ALWAYS_OVERFLOW) {
            chatViewModel.toggleShowCommands()
        },
        ActionItem(R.string.btn_show_tokens,
            if(chat?.autoResponses == true) Icons.Outlined.CheckCircle
            else Icons.Outlined.Circle,
            OverflowMode.ALWAYS_OVERFLOW) {
            chatViewModel.toggleShowTokens()
        },
        ActionItem(R.string.btn_auto_playback_audio,
            if(chat?.autoPlaybackAudio == true) Icons.Outlined.CheckCircle
            else Icons.Outlined.Circle,
            OverflowMode.ALWAYS_OVERFLOW) {
            chatViewModel.toggleAutoPlaybackAudio()
        },
        ActionItem(R.string.btn_set_assistant_as_default,
            if(isDefaultAssistant) Icons.Outlined.CheckCircle
            else Icons.Outlined.Circle,
            OverflowMode.ALWAYS_OVERFLOW) {
            chatViewModel.toggleDefaultChat()
        },
        ActionItem(R.string.btn_auto_responses,
            if(chat?.autoResponses == true) Icons.Outlined.CheckCircle
            else Icons.Outlined.Circle,
            OverflowMode.ALWAYS_OVERFLOW) {
            chatViewModel.toggleAutoResponses()
        },
        ActionItem(R.string.btn_get_assistant_response,
            Icons.Outlined.ArrowDownward,
            OverflowMode.ALWAYS_OVERFLOW) {
            assistant?.let { chatViewModel.getAssistantResponse(it.id) }
        }
    )


    if(openDeleteDialog.value && assistant != null) {
        DeletionConfirmDialog(
            openDialog = openDeleteDialog,
            onConfirm = {
                openDeleteDialog.value = false
                navController.popBackStack()
                chatViewModel.deleteChat(assistant!!.id)
            },
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(MaterialTheme.colorScheme.tertiary)
            .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(onClick = { navController.popBackStack() } ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(id = R.string.cd_btn_back),
                tint = MaterialTheme.colorScheme.onTertiary,
            )
        }

        if(assistant == null){
            Text(
                text = stringResource(id = R.string.placeholder_deleted),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onTertiary
            )
        } else {

            ProfilePicture(size = 42.dp, imageUri = assistant!!.imageUri)

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = assistant!!.name,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onTertiary
                )
                if (assistant!!.about.isNotBlank()) {
                    Text(
                        text = assistant!!.about,
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row {
                ActionMenu(
                    items = actionItems,
                    numIcons = 2,
                )
            }
        }
    }
}

@Preview
@Composable
fun TopBarChatPreview() {
    ARATheme {
        TopBarChat(
            NavHostController(LocalContext.current),
            hiltViewModel()
        )
    }
}