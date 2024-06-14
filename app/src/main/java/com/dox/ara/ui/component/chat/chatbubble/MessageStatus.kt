package com.dox.ara.ui.component.chat.chatbubble

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ContentAlpha
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.LocalContentAlpha
import androidx.wear.compose.material.Text
import com.dox.ara.ui.data.MessageStatus
import com.dox.ara.ui.theme.ARATheme

@Composable
fun MessageTimeText(
    modifier: Modifier = Modifier,
    messageTime: String,
    messageStatus: MessageStatus
) {

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                modifier = Modifier
                    .padding(top = 1.dp, bottom = 1.dp),
                text = messageTime,
                fontSize = 12.sp
            )
        }

        Icon(
            modifier = Modifier
                .size(16.dp, 12.dp)
                .padding(start = 4.dp),
            imageVector = when (messageStatus) {
                MessageStatus.PENDING -> {
                    Icons.Default.AccessTime
                }
                MessageStatus.SENT -> {
                    Icons.Default.Done
                }
                MessageStatus.READ, MessageStatus.DELIVERED -> {
                    Icons.Default.DoneAll
                }
                MessageStatus.FAILED -> {
                    Icons.Default.ErrorOutline
                }
                MessageStatus.BLOCKED -> {
                    Icons.Default.Block
                }
            },
            tint = if (messageStatus == MessageStatus.READ) MaterialTheme.colorScheme.primary
            else if(messageStatus == MessageStatus.FAILED) Color.Red.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            contentDescription = null
        )

    }
}

@Preview
@Composable
fun MessageTimeTextPreview() {
    ARATheme {
        MessageTimeText(
            messageTime = "12:00",
            messageStatus = MessageStatus.READ
        )
    }
}