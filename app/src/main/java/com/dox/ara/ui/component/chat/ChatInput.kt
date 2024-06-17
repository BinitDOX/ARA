package com.dox.ara.ui.component.chat

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ContentAlpha
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.LocalContentAlpha
import com.dox.ara.R
import com.dox.ara.ui.component.IndicatingIconButton
import com.dox.ara.ui.theme.ARATheme

@SuppressLint("UnrememberedMutableState")
@Composable
fun ChatInput(
    isListening: Boolean,
    testCommands: List<String>,
    chatInput: MutableState<TextFieldValue>,
    onMicToggle: () -> Unit,
    onMessageSent: (String) -> Unit,
) {
    val textEmpty: Boolean by derivedStateOf { chatInput.value.text.isEmpty() }

    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {

        ChatTextField(
            modifier = Modifier.weight(1f),
            input = chatInput.value,
            testCommands = testCommands,
            empty = textEmpty,
            onValueChange = {
                chatInput.value = it
            }
        )

        Spacer(modifier = Modifier.width(6.dp))

        FloatingActionButton(
            shape = CircleShape,
            modifier = Modifier
                .size(48.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            onClick = {
                if (!textEmpty && !isListening) {
                    onMessageSent(chatInput.value.text)
                    chatInput.value = TextFieldValue("")
                } else {
                    onMicToggle()
                }
            }
        ) {
            Icon(
                imageVector = if (isListening) Icons.Filled.Stop
                            else if (textEmpty) Icons.Filled.Mic
                            else Icons.AutoMirrored.Filled.Send,
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription = stringResource(id = R.string.cd_btn_send_message)
            )
        }
    }
}

private val circleButtonSize = 44.dp

@Composable
private fun ChatTextField(
    modifier: Modifier = Modifier,
    testCommands: List<String>,
    input: TextFieldValue,
    empty: Boolean,
    onValueChange: (TextFieldValue) -> Unit
) {

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {

                IndicatingIconButton(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.then(Modifier.size(circleButtonSize)),
                    indication = rememberRipple(bounded = false, radius = circleButtonSize / 2)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mood,
                        contentDescription = stringResource(id = R.string.cd_btn_emoji_picker),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = circleButtonSize),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        textStyle = TextStyle(
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        value = input,
                        onValueChange = onValueChange,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            if (empty) {
                                Text(stringResource(id = R.string.placeholder_message), fontSize = 18.sp)
                            }
                            innerTextField()
                        }
                    )
                }

                QuickCommandButton (testCommands) { command -> onValueChange(TextFieldValue("${input.text} $command"))}

                AnimatedVisibility(visible = empty) {
                    IndicatingIconButton(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.then(Modifier.size(circleButtonSize)),
                        indication = rememberRipple(bounded = false, radius = circleButtonSize / 2)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = stringResource(id = R.string.cd_btn_camera),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickCommandButton(
    testCommands: List<String>,
    onClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        IndicatingIconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(circleButtonSize),
            indication = rememberRipple(bounded = false, radius = circleButtonSize / 2)
        ) {
            Icon(
                imageVector = Icons.Default.Api,
                contentDescription = stringResource(id = R.string.cd_btn_quick_command),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
            )
        }
    }

    Box(modifier = Modifier.offset(x = 1000.dp, y = 100.dp)) {  // Right align
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column {
                QuickCommandMenuItem("<BREAK>", onClick) { expanded = false }
                for (command in testCommands) {
                    QuickCommandMenuItem(command, onClick) { expanded = false }
                }
            }
        }
    }
}

@Composable
fun QuickCommandMenuItem(
    command: String,
    onClick: (String) -> Unit,
    onItemClick: () -> Unit
) {
    DropdownMenuItem(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .background(MaterialTheme.colorScheme.background),
        onClick = {
            onClick(command)
            onItemClick()
        },
        text = { Text(command) }
    )
}

@Preview
@Composable
private fun ChatInputPreview() {
    ARATheme {
        ChatInput(
            true,
            listOf("test"),
            remember { mutableStateOf (TextFieldValue("")) },
            onMicToggle = {},
            onMessageSent = {}
        )
    }
}