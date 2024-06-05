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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ContentAlpha
import androidx.wear.compose.material.LocalContentAlpha
import com.dox.ara.ui.component.chat.chatflexbox.ChatFlexBoxLayout
import com.dox.ara.ui.component.chat.chatflexbox.SubcomposeColumn
import com.dox.ara.ui.data.MessageStatus
import com.dox.ara.ui.theme.ARATheme


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReceivedMessageRow(
    receiverName: String,
    receiverNameColor: Color = MaterialTheme.colorScheme.primary,
    text: String,
    quotedMessage: String? = null,
    quotedFrom: String? = null,
    quotedImage: Int? = null,
    quotedColor: Color = MaterialTheme.colorScheme.primary,
    quotedMessageColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
    quotedBackgroundColor: Color = MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
    messageTime: String,
    showMessageStatus: Boolean = false,
    messageStatus: MessageStatus,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
    onLongPress: () -> Unit = {},
    onPress: () -> Unit = {}
) {
    // Whole column that contains chat bubble and padding on start or end
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 8.dp, end = 60.dp, top = 2.dp, bottom = 2.dp)

    ) {

        // This is chat bubble
        SubcomposeColumn(
            modifier = Modifier
                .shadow(1.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .combinedClickable (
                    onClick = { onPress() },
                    onLongClick = { onLongPress() }
                ),
            content = {
                RecipientName(
                    name =  receiverName,
                    color = receiverNameColor,
                    isName = true,
                )

                if (quotedMessage != null || quotedImage != null) {
                    // Quoted message
                    QuotedMessage(
                        modifier = Modifier
                            .padding(top = 4.dp, start = 4.dp, end = 4.dp)
                            .height(IntrinsicSize.Min)
                            .background(quotedBackgroundColor, shape = RoundedCornerShape(8.dp))
                            .clip(shape = RoundedCornerShape(8.dp))
                            .clickable {

                            },
                        quotedMessage = quotedMessage,
                        quotedFrom = quotedFrom,
                        quotedImage = quotedImage,
                        quotedColor = quotedColor,
                        quotedMessageColor = quotedMessageColor,
                    )
                }

                ChatFlexBoxLayout(
                    modifier = Modifier
                        .padding(start = 2.dp, top = 2.dp, end = 4.dp, bottom = 2.dp),
                    text = text,
                    color = MaterialTheme.colorScheme.onTertiary,
                    messageStat = {
                        if(showMessageStatus) {
                            MessageTimeText(
                                modifier = Modifier.wrapContentSize(),
                                messageTime = messageTime,
                                messageStatus = messageStatus
                            )
                        } else {
                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                Text(
                                    modifier = Modifier.padding(
                                        top = 1.dp,
                                        bottom = 1.dp,
                                        end = 4.dp
                                    ),
                                    text = messageTime,
                                    fontSize = 12.sp
                                )
                            }
                        }
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
        ReceivedMessageRow(
            receiverName = "James",
            receiverNameColor = MaterialTheme.colorScheme.primary,
            text = "Hello, how are you?",
            quotedFrom = "John",
            messageTime = "12:00",
            messageStatus = MessageStatus.READ
        )
    }
}


@Preview(name = "QuotedMessage")
@Composable
private fun SentMessageQuotedRowPreview(){
    ARATheme {
        ReceivedMessageRow(
            receiverName = "John",
            receiverNameColor = MaterialTheme.colorScheme.primary,
            text = "Hello, how are you?",
            messageTime = "12:00",
            quotedFrom = "James",
            messageStatus = MessageStatus.SENT,
            quotedMessage = "Hello, how are you?",
        )
    }
}