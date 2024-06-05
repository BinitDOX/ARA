package com.dox.ara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dox.ara.model.Alarm
import com.dox.ara.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository
): ViewModel() {

    private val _alarmItems = MutableStateFlow(emptyList<Alarm>())
    val alarmItems = _alarmItems.asStateFlow()

    init {
        getAlarms()
    }

    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val id = alarmRepository.save(alarm)
            val updatedAlarm = alarm.copy(id = id)
            if(!alarmRepository.scheduleAlarm(updatedAlarm)){
                alarmRepository.delete(updatedAlarm)
            }
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmRepository.cancelAlarm(alarm)
            val updatedAlarm = alarm.copy(isActive = true)
            alarmRepository.update(updatedAlarm)
            delay(1000L)
            if(!alarmRepository.scheduleAlarm(updatedAlarm)){
                alarmRepository.delete(updatedAlarm)
            }
        }
    }

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            if(alarm.isActive) {
                alarmRepository.scheduleAlarm(alarm)
            } else {
                alarmRepository.cancelAlarm(alarm)
            }
            alarmRepository.update(alarm)
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmRepository.cancelAlarm(alarm)
            alarmRepository.delete(alarm)
        }
    }

    private fun getAlarms() {
        viewModelScope.launch {
            alarmRepository.getAllAlarms().flowOn(Dispatchers.IO).collect { alarmItem: List<Alarm> ->
                _alarmItems.update { alarmItem }
            }
        }
    }
}
