package com.dox.ara.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.dox.ara.dao.AlarmDao
import com.dox.ara.listener.AlarmListener
import com.dox.ara.model.Alarm
import com.dox.ara.utility.Constants.ALARM_ID_EXTRA
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class AlarmRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmDao: AlarmDao
) {
    suspend fun save(alarm: Alarm): Long = alarmDao.insert(alarm)
    suspend fun update(alarm: Alarm) = alarmDao.update(alarm)
    suspend fun delete(alarm: Alarm) = alarmDao.delete(alarm)
    suspend fun getAlarm(id: Long): Alarm? = alarmDao.getAlarm(id)
    fun getAllAlarms(): Flow<List<Alarm>> = alarmDao.getAllAlarms()

    fun scheduleAlarm(alarm: Alarm): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmListener::class.java).apply {
            putExtra(ALARM_ID_EXTRA, alarm.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        var canSchedule = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            canSchedule = alarmManager.canScheduleExactAlarms()
        }

        return if(canSchedule) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarm.time,
                pendingIntent
            )
            Timber.d("[${::scheduleAlarm.name}] Alarm scheduled")
            true
        } else {
            Timber.e("[${::scheduleAlarm.name}] Cannot schedule alarm")
            false
        }
    }

    fun cancelAlarm(alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmListener::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Timber.d("[${::cancelAlarm.name}] Alarm cancelled")
    }

}
