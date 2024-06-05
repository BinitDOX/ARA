package com.dox.ara.ui.component.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dox.ara.ui.data.ChatItem
import com.dox.ara.ui.data.MessageStatus
import com.dox.ara.ui.data.Role
import com.dox.ara.ui.data.dummyChatItems
import com.dox.ara.ui.theme.ARATheme

@Composable
fun ChatDetails(chatItem: ChatItem) {
    Column (
        modifier = Modifier
            .wrapContentHeight()
            .padding(top = 6.dp, bottom = 6.dp, start = 12.dp, end = 4.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        MessageHeader(chatItem)
        Spacer(modifier = Modifier.padding(vertical = 4.dp))
        MessageBody(chatItem)
    }
}

@Composable
fun MessageHeader(chatItem: ChatItem) {
    Row (
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = chatItem.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = chatItem.date ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = if(chatItem.unreadCount != null && chatItem.unreadCount > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageBody(chatItem: ChatItem) {
    Row (
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(chatItem.content != null) {
            if (chatItem.from == Role.USER) {
                val imageVector = when (chatItem.status!!) {
                    MessageStatus.PENDING -> Icons.Filled.AccessTime
                    MessageStatus.SENT -> Icons.Filled.Done
                    MessageStatus.DELIVERED -> Icons.Filled.DoneAll
                    MessageStatus.READ -> Icons.Filled.DoneAll
                    MessageStatus.FAILED -> Icons.Filled.ErrorOutline
                }
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = imageVector,
                    contentDescription = null,
                    tint = if (chatItem.status == MessageStatus.READ) MaterialTheme.colorScheme.primary
                    else if(chatItem.status == MessageStatus.FAILED) Color.Red.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            }

            Text(
                modifier = Modifier.weight(1f),
                text = chatItem.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (chatItem.unreadCount != null && chatItem.unreadCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = chatItem.unreadCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun ChatDetailsPreview() {
    ARATheme {
        ChatDetails(dummyChatItems[0])
    }
}