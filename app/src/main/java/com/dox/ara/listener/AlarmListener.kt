package com.dox.ara.listener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dox.ara.repository.AlarmRepository
import com.dox.ara.repository.EventRepository
import com.dox.ara.utility.Constants.ALARM_ID_EXTRA
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AlarmListener : BroadcastReceiver() {
    @Inject
    lateinit var eventRepository: EventRepository

    @Inject
    lateinit var alarmRepository: AlarmRepository

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("[${::onReceive.name}] Alarm received")

        val alarmId = intent.getLongExtra(ALARM_ID_EXTRA, -1L)
        if(alarmId != -1L) {
            CoroutineScope(Dispatchers.IO).launch {
                val alarm = alarmRepository.getAlarm(alarmId)
                if (alarm != null) {
                    val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

                    val content = "An Alarm has been triggered, inform the user about it.\n" +
                        "Alarm description: ${alarm.description}\n" +
                        "Alarm time: ${sdf.format(Date(alarm.time))}\n" +
                        "Append the following command at the end of your next message" +
                        " with any appropriate alarm volume level between 1-100:\n" +
                        "[volume(<1-100>)]"

                    val eventResponse = EventRepository.EventResponse(
                        message = content,
                        getResponse = true,
                        getUserInput = false
                    )
                    val updatedAlarm = alarm.copy(isActive = false)
                    alarmRepository.update(updatedAlarm)
                    eventRepository.handleEvent(eventResponse)
                } else {
                    Timber.e("[${::onReceive.name}] Alarm not found")
                }
            }
        } else {
            Timber.e("[${::onReceive.name}] Alarm id is null or empty")
        }
    }
}
