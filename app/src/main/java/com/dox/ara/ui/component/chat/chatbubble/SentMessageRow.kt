package com.dox.ara.ui.component.chat.chatbubble


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dox.ara.ui.component.chat.chatflexbox.ChatFlexBoxLayout
import com.dox.ara.ui.component.chat.chatflexbox.SubcomposeColumn
import com.dox.ara.ui.data.MessageStatus
import com.dox.ara.ui.theme.ARATheme


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SentMessageRow(
    text: String,
    quotedMessage: String? = null,
    quotedFrom: String,
    quotedImage: Int? = null,
    quotedColor: Color = MaterialTheme.colorScheme.primary,
    quotedMessageColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
    quotedBackgroundColor: Color = MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
    messageTime: String,
    messageStatus: MessageStatus,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary,
    onLongPress: () -> Unit = {}
) {

    // Whole column that contains chat bubble and padding on start or end
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 60.dp, end = 8.dp, top = 2.dp, bottom = 2.dp)
    ) {

        // This is chat bubble
        SubcomposeColumn(
            modifier = Modifier
                .shadow(1.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .combinedClickable (
                    onClick = {},
                    onLongClick = {
                        onLongPress()
                    }
                ),

            content = {

                // Quoted message
                if (quotedMessage != null || quotedImage != null) {
                    QuotedMessage(
                        modifier = Modifier
                            .padding(top = 4.dp, start = 4.dp, end = 4.dp)
                            .height(IntrinsicSize.Min)
                            .background(quotedBackgroundColor, shape = RoundedCornerShape(8.dp))
                            .clip(shape = RoundedCornerShape(8.dp))
                            .clickable {

                            },
                        quotedColor = quotedColor,
                        quotedMessageColor = quotedMessageColor,
                        quotedMessage = quotedMessage,
                        quotedFrom = quotedFrom,
                        quotedImage = quotedImage
                    )
                }

                ChatFlexBoxLayout(
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(
                        start = 2.dp,
                        top = 2.dp,
                        end = 8.dp,
                        bottom = 2.dp
                    ),
                    text = text,
                    messageStat = {
                        MessageTimeText(
                            modifier = Modifier.wrapContentSize(),
                            messageTime = messageTime,
                            messageStatus = messageStatus
                        )
                    }
                )
            }
        )
    }
}

@Preview(name = "Message")
@Composable
private fun SentMessageRowPreview(){
    ARATheme {
        SentMessageRow(
            text = "Hello, how are you?",
            messageTime = "12:00",
            messageStatus = MessageStatus.READ,
            quotedFrom = "A"
        )
    }
}


@Preview(name = "QuotedMessage")
@Composable
private fun SentMessageQuotedRowPreview(){
    ARATheme {
        SentMessageRow(
            text = "Hello, how are you?",
            messageTime = "12:00",
            messageStatus = MessageStatus.READ,
            quotedMessage = "Hello, how are you?",
            quotedFrom = "B",
        )
    }
}