package com.dox.ara.ui.component.chat.chatflexbox

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Layout that contains message and message status. [messageStat] is positioned based on
 * how many lines [text] has and with of message composable and [messageStat] or parent of
 * this composable. [messageStat] can be position right side or bottom or top of last line
 * of the message.
 *
 * Since [TextLayoutResult] is required for text properties composable contains message but
 * [messageStat] is a parameter of this function which can be created in lambda block.
 *
 * @param modifier [Modifier] of Chat layout that contains message context, time and status
 * @param messageModifier [Modifier] of layout that contains only the message
 * @param text This is the context of the message.
 * @param fontSize of message [Text].
 * @param fontStyle of message [Text].
 * @param fontWeight of message [Text].
 * @param fontFamily of message [Text].
 * @param letterSpacing of message [Text].
 * @param textDecoration of message [Text].
 * @param textAlign of message [Text].
 * @param lineHeight of message [Text].
 * @param overflow of message [Text].
 * @param softWrap of message [Text].
 * @param maxLines of message [Text].
 * @param messageStat composable that might contain message date and message receive status.
 * @param onMeasure returns results from measuring and positioning chat components.
 */
@Composable
fun ChatFlexBoxLayout(
    modifier: Modifier = Modifier,
    messageModifier: Modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
    text: String,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 16.sp,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    messageStat: @Composable () -> Unit = {},
    onMeasure: ((ChatRowData) -> Unit)? = null
) {
    val chatRowData = remember { ChatRowData() }
    val content = @Composable {

        Message(
            modifier = messageModifier,
            text = text,
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            onTextLayout = { textLayoutResult: TextLayoutResult ->
                chatRowData.text = text
                chatRowData.lineCount = textLayoutResult.lineCount
                chatRowData.lastLineWidth =
                    textLayoutResult.getLineRight(chatRowData.lineCount - 1)
                chatRowData.textWidth = textLayoutResult.size.width
            }
        )

        messageStat()
    }

    ChatLayout(modifier, chatRowData, content, onMeasure)
}

/**
 * Chat Composable that positions it's contents(message only or message and message status)
 * based on [chatRowData] calculation.
 *
 * @param modifier Modifier for entire layout
 * @param chatRowData contains text and dimension related data about this row
 * @param content contains message or message + message status(time and received status if specified)
 * @param onMeasure lambda to return [chatRowData] contains info about chat message text,
 * textWidth, if line count, width of last line and other attributes.
 */
@Composable
internal fun ChatLayout(
    modifier: Modifier = Modifier,
    chatRowData: ChatRowData,
    content: @Composable () -> Unit,
    onMeasure: ((ChatRowData) -> Unit)? = null
) {

    Layout(
        modifier = modifier,
        content = content
    ) { measurables: List<Measurable>, constraints: Constraints ->

        val placeables: List<Placeable> = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0))
        }

        require(placeables.size in 1..2)

        val message = placeables.first()
        val status = if (placeables.size > 1) {
            placeables.last()
        } else {
            null
        }

        chatRowData.parentWidth = constraints.maxWidth
        calculateChatWidthAndHeight(chatRowData, message, status)
        chatRowData.parentWidth =
            chatRowData.rowWidth.coerceAtLeast(minimumValue = constraints.minWidth)

        onMeasure?.invoke(chatRowData)

        layout(width = chatRowData.parentWidth, height = chatRowData.rowHeight) {
            message.placeRelative(0, 0)
            status?.placeRelative(
                chatRowData.parentWidth - status.width,
                chatRowData.rowHeight - status.height
            )
        }
    }
}

/**
 * Calculate [message] and [status] positions based on [chatRowData].
 * Number of message text lines, last line width, width of the parent based on quote,
 * user name, images, videos,etc.
 * effect the position of [status] if it's not null
 */
internal fun calculateChatWidthAndHeight(
    chatRowData: ChatRowData,
    message: Placeable,
    status: Placeable?,
) {

    if (status != null) {

        val lineCount = chatRowData.lineCount
        val lastLineWidth = chatRowData.lastLineWidth
        val parentWidth = chatRowData.parentWidth

        val padding = (message.measuredWidth - chatRowData.textWidth) / 2

        if (lineCount > 1 && lastLineWidth + status.measuredWidth >= chatRowData.textWidth + padding) {
            chatRowData.rowWidth = message.measuredWidth
            chatRowData.rowHeight = message.measuredHeight + status.measuredHeight
            chatRowData.measuredType = 0
        } else if (lineCount > 1 && lastLineWidth + status.measuredWidth < chatRowData.textWidth + padding) {
            chatRowData.rowWidth = message.measuredWidth
            chatRowData.rowHeight = message.measuredHeight
            chatRowData.measuredType = 1
        } else if (lineCount == 1 && message.width + status.measuredWidth >= parentWidth) {
            chatRowData.rowWidth = message.measuredWidth
            chatRowData.rowHeight = message.measuredHeight + status.measuredHeight
            chatRowData.measuredType = 2
        } else {
            chatRowData.rowWidth = message.measuredWidth + status.measuredWidth
            chatRowData.rowHeight = message.measuredHeight
            chatRowData.measuredType = 3
        }
    } else {
        chatRowData.rowWidth = message.width
        chatRowData.rowHeight = message.height
    }
}


