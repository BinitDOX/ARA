package com.dox.ara.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex

@Composable
fun AnimatedProfileDialog(
    showDialog: MutableState<Boolean>,
    contentInitialOffset: Pair<Float, Float>,
    content: @Composable () -> Unit
) {
    var hasDismissDialogAnimationEnded by remember { mutableIntStateOf(2) }
    var hasShowDialogAnimationEnded by remember { mutableIntStateOf(0) }

    if(showDialog.value){
        hasDismissDialogAnimationEnded = 0
        hasShowDialogAnimationEnded = 0
    }

    val offsetX by animateFloatAsState(
        if (!showDialog.value) contentInitialOffset.first else 0f,
        animationSpec = tween(durationMillis = 500)
    ) {
        if(!showDialog.value){
            hasDismissDialogAnimationEnded++
        } else {
            hasShowDialogAnimationEnded++
        }
    }

    val offsetY by animateFloatAsState(
        if (!showDialog.value) contentInitialOffset.second else 0f,
        animationSpec = tween(durationMillis = 500)
    ) {
        if(!showDialog.value){
            hasDismissDialogAnimationEnded++
        } else {
            hasShowDialogAnimationEnded++
        }
    }

    val contentSize by animateFloatAsState(if(!showDialog.value) 64f else 200f,
        animationSpec = tween(durationMillis = 500))

    if (showDialog.value || hasDismissDialogAnimationEnded < 2) {
        Dialog(onDismissRequest = {
            showDialog.value = false
        }) {
            ProfileDialog(
                showDialog = showDialog.value,
                hasAnimationEnded = hasShowDialogAnimationEnded,
                contentSize,
                Pair(offsetX, offsetY),
                content
            )
        }
    }
}

@Composable
fun ProfileDialog(
    showDialog: Boolean,
    hasAnimationEnded: Int,
    contentSize: Float,
    contentOffset: Pair<Float, Float>,
    content: @Composable () -> Unit
) {
    Box (
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset(
                    x = contentOffset.first.dp,
                    y = contentOffset.second.dp
                )
                .size(contentSize.dp)
                .zIndex(10f)
        ) {
            content()
        }
        if(hasAnimationEnded >= 2) {
            Column (
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ){
                Card(
                    modifier = Modifier
                        .height(320.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = "This is a minimal dialog",
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center),
                        textAlign = TextAlign.Center,
                    )
                }

                Card(
                    modifier = Modifier
                        .height(320.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = "This is a minimal dialog",
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun MyApp() {
    val showDialog = remember { mutableStateOf(false) }
    var fabPosition by remember { mutableStateOf(Pair(0f, 0f)) }

    Scaffold(
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .onGloballyPositioned {
                        fabPosition =
                            Pair(it.positionInRoot().x - 630f, it.positionInRoot().y - 1377f)
                    }
            ) {
                FloatingActionButton(
                    imageVector = Icons.Default.Add,
                    onClick = {
                        showDialog.value = true
                    },
                )
            }
        }
    ) {it ->
        AnimatedProfileDialog(
            showDialog = showDialog,
            contentInitialOffset = fabPosition,
            content = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        )
    }
}

@Preview
@Composable
fun MyAppPreview() {
    MyApp()
}