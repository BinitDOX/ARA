package com.dox.ara.ui.component.chat

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.window.DialogProperties


@Composable
fun DeletionConfirmDialog(
    openDialog: MutableState<Boolean>,
    onConfirm: () -> Unit
) {
    if (openDialog.value) {
        AlertDialog(
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            onDismissRequest = {
                openDialog.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm()
                        openDialog.value = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                    }
                ) {
                    Text("Cancel")
                }
            },
            title = {
                Text(text = "Confirm Deletion")
            },
            text = {
                Text("Are you sure you want to delete this chat and assistant?")
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        )
    }
}