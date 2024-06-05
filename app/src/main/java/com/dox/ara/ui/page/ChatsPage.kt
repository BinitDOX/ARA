package com.dox.ara.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dox.ara.ui.component.ProfilePicture
import com.dox.ara.ui.component.chat.ChatDetails
import com.dox.ara.ui.data.ChatItem
import com.dox.ara.ui.data.RouteItem
import com.dox.ara.ui.theme.ARATheme
import com.dox.ara.viewmodel.ChatsViewModel

@Composable
fun ChatsPage(
    navController: NavController,
    chatsViewModel: ChatsViewModel = hiltViewModel()
) {
    val chatItems by chatsViewModel.chatItems.collectAsStateWithLifecycle()


    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(chatItems, key = { it.id }) { chat ->
            ChatRowItem(chat, onClick = {navController.navigate("${RouteItem.Chat.route}/${chat.id}")})
        }
    }
}

@Composable
fun ChatRowItem(chatItem: ChatItem, onClick: () -> Unit) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 8.dp)
    ) {
        ProfilePicture(size = 64.dp, imageUri = chatItem.imageUri)
        ChatDetails(chatItem)
    }
}


@Preview
@Composable
private fun ChatsPagePreview() {
    ARATheme {
        ChatsPage(
            navController = NavController(LocalContext.current)
        )
    }
}