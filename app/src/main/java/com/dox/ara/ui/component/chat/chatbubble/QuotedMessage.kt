package com.dox.ara.ui.component.chat.chatbubble

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ContentAlpha
import androidx.wear.compose.material.LocalContentAlpha
import com.dox.ara.R


@Composable
fun QuotedMessage(
    modifier: Modifier = Modifier,
    quotedFrom: String? = null,
    quotedMessage: String? = null,
    quotedImage: Int? = null,
    quotedColor: Color = Color.Red,
    quotedMessageColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {

    QuoteImageRow(modifier = modifier,
        content = {
            Row {

                Surface(
                    color = quotedColor,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                ) {
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .wrapContentHeight()
                ) {
                    Text(
                        quotedFrom ?: stringResource(id = R.string.placeholder_user),
                        color = quotedColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        letterSpacing = 1.sp,
                        overflow = TextOverflow.Ellipsis
                    )

                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (quotedImage != null) {
                                Icon(
                                    imageVector = Icons.Default.InsertPhoto,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            Text(
                                text = quotedMessage ?: "Photo",
                                fontSize = 12.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                color = quotedMessageColor
                            )
                        }

                    }
                }
            }
        },
        image = {
            if (quotedImage != null) {
                Image(
                    painter = painterResource(id = quotedImage),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .layoutId("image")
                        .size(60.dp)
                        .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                )
            }
        }
    )
}

@Composable
private fun QuoteImageRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    image: @Composable (() -> Unit)? = null
) {

    val finalContent = @Composable {
        if (image != null) {
            content()
            image.invoke()
        } else {
            content()
        }
    }

    Layout(modifier = modifier, content = finalContent) { measurables, constraints ->

        var imageIndex = -1

        val placeables = measurables.mapIndexed { index, measurable ->

            if (measurable.layoutId == "image") {
                imageIndex = index
            }
            measurable.measure(constraints = constraints.copy(minWidth = 0))
        }

        val size =
            placeables.fold(IntSize.Zero) { current: IntSize, placeable: Placeable ->

                IntSize(
                    width = current.width + placeable.width,
                    height = maxOf(current.height, placeable.height)
                )
            }


        val width = size.width.coerceAtLeast(constraints.minWidth)

        layout(width, size.height) {
            var x = 0

            placeables.forEachIndexed { index: Int, placeable: Placeable ->
                if (index != imageIndex) {
                    placeable.placeRelative(x, 0)
                    x += placeable.width
                } else {
                    placeable.placeRelative(width - placeable.width, 0)
                }
            }
        }
    }
}