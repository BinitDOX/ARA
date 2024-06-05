package com.dox.ara.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dox.ara.utility.Constants.APP_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LogsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _selectedDate = MutableStateFlow(getTodayDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    init {
        fetchLogsForDate(getTodayDate())
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        fetchLogsForDate(date)
    }

    private fun fetchLogsForDate(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val logLines = readLogs(date).toList()
            _logs.value = logLines
        }
    }

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
    }

    private fun readLogs(date: String): Flow<String> = flow {
        val logDir = File(context.filesDir, "${APP_ID}-logs")
        val logFile = File(logDir, "log_$date.txt")

        if(!logFile.exists()){
            _logs.value = emptyList()
            return@flow
        }

        logFile.bufferedReader().use { reader ->
            var line: String? = reader.readLine()
            while (line != null) {
                emit(line)
                line = reader.readLine()
            }
        }
    }
}

