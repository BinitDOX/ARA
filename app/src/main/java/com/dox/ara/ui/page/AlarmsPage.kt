package com.dox.ara.ui.page

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dox.ara.R
import com.dox.ara.model.Alarm
import com.dox.ara.ui.theme.ARATheme
import com.dox.ara.viewmodel.AlarmsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AlarmsPage(
    showAlarmDialog: MutableState<Boolean>,
    alarmsViewModel: AlarmsViewModel = hiltViewModel()
) {
    val alarmItems by alarmsViewModel.alarmItems.collectAsStateWithLifecycle()
    var selectedAlarm by remember { mutableStateOf<Alarm?>(null) }

    if (showAlarmDialog.value || selectedAlarm != null) {
        AlarmDialog(
            alarm = selectedAlarm,
            onDismiss = {
                showAlarmDialog.value = false
                selectedAlarm = null
            },
            onSave = {
                if (it.id == 0L) {
                    alarmsViewModel.addAlarm(it)
                } else {
                    alarmsViewModel.updateAlarm(it)
                }
                selectedAlarm = null
                showAlarmDialog.value = false
            }
        )
    }
    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(alarmItems, key = { it.id }) { alarm ->
            AlarmRow(
                alarm = alarm,
                onClick = { selectedAlarm = it },
                onDelete = { alarmsViewModel.deleteAlarm(it) },
                onToggle = { alarmsViewModel.toggleAlarm(it) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun AlarmRow(
    alarm: Alarm,
    onClick: (Alarm) -> Unit,
    onToggle: (Alarm) -> Unit,
    onDelete: (Alarm) -> Unit
) {
    val isActive = remember { mutableStateOf(alarm.isActive) }
    val dateTime = remember(alarm.time) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sdf.format(Date(alarm.time))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick(alarm) }
            .padding(vertical = 14.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column (
            modifier = Modifier.padding(4.dp)
        ){
            Text(
                text = dateTime.split(" ")[1],
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateTime.split(" ")[0],
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = isActive.value,
                onCheckedChange = {
                    isActive.value = it
                    onToggle(alarm.copy(isActive = it))
                }
            )
            Box (
                modifier = Modifier.padding(start = 12.dp)
            ){
                IconButton(onClick = { onDelete(alarm) }) {
                    Icon(
                        Icons.Default.Close,
                        stringResource(id = R.string.cd_btn_delete_alarm),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmDialog(
    alarm: Alarm?,
    onDismiss: () -> Unit,
    onSave: (Alarm) -> Unit
) {
    var date by remember { mutableStateOf(alarm?.time?.let { Date(it) } ?: Date()) }
    var description by remember { mutableStateOf(alarm?.description ?: "") }
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    val context = LocalContext.current

    // State for DatePickerDialog
    val datePickerDialog = remember {
        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                time = date
                set(year, month, dayOfMonth)
            }
            date = calendar.time
        }, date.year + 1900, date.month, date.date)
    }

    // State for TimePickerDialog
    val timePickerDialog = remember {
        TimePickerDialog(context, { _, hourOfDay, minute ->
            val calendar = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
            }
            date = calendar.time
        }, date.hours, date.minutes, true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Set Alarm", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        dateFormatter.format(date),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        shape = MaterialTheme.shapes.medium,
                        onClick = { datePickerDialog.show() }
                    ) {
                        Text("Pick Date")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(timeFormatter.format(date), style = MaterialTheme.typography.titleMedium)
                    Button(
                        shape = MaterialTheme.shapes.medium,
                        onClick = { timePickerDialog.show() }
                    ) {
                        Text("Pick Time")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        onSave(
                            Alarm(
                                id = alarm?.id ?: 0,
                                time = date.time,
                                description = description,
                                isActive = alarm?.isActive ?: true
                            )
                        )
                        onDismiss()
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AlarmDialogPreview() {
    ARATheme {
        AlarmDialog(
            alarm = null,
            onDismiss = {},
            onSave = {}
        )
    }
}

@Preview
@Composable
private fun AlarmRowPreview() {
    ARATheme {
        AlarmRow(
            alarm = Alarm(
                id = 1,
                description = "Alarm 1",
                time = System.currentTimeMillis(),
                isActive = true
            ),
            onClick = {},
            onToggle = {},
            onDelete = {}
        )
    }
}

@Preview
@Composable
private fun AlarmsPagePreview() {
    ARATheme {
        AlarmsPage(
            showAlarmDialog = remember { mutableStateOf(false) }
        )
    }
}