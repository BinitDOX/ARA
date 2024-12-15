package com.dox.ara.logging

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.dox.ara.utility.Constants.APP_ID
import com.dox.ara.utility.Constants.APP_NAME
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FileLoggingTree(private val context: Context) : Timber.DebugTree(), Thread.UncaughtExceptionHandler {
    private var isLoggingException = false
    private var writer: BufferedWriter? = null
    private val writerLock = Any()
    private var currentLogFileDate: String = getCurrentDate()

    init {
        // Set this class as the default uncaught exception handler
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)

        writer = openLogFile()
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        // Check if the exception is already being logged to prevent recursion
        if (!isLoggingException) {
            isLoggingException = true
            // Log uncaught exception to the file
            logException(e)

            Log.e("FileLoggingTree", "[Global Exception Handler] Uncaught Exception: ${e}")

            val crashMessage = "Something went wrong, $APP_NAME will crash"
            Toast.makeText(context, crashMessage, Toast.LENGTH_LONG).show()

            Handler(Looper.getMainLooper()).postDelayed ({
                isLoggingException = false
                defaultUncaughtExceptionHandler?.uncaughtException(t, e)
            }, 3000)
        }
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val newDate = getCurrentDate()
        if (currentLogFileDate != newDate) {
            currentLogFileDate = newDate
            rotateLogFile()
        }

        try {
            synchronized(writerLock) {
                writer?.apply {
                    append("[${getCurrentDateTime()}] [${logLevelToString(priority)}] [$tag]: $message\n")
                    flush()  // Flush after each log to ensure persistence
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun logException(throwable: Throwable) {
        try {
            synchronized(writerLock) {
                writer?.apply {
                    append("[${getCurrentDateTime()}] [ERROR] [Global Exception Handler] Uncaught Exception: ${throwable.message}\n")
                    throwable.printStackTrace(PrintWriter(this))
                    flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openLogFile(): BufferedWriter {
        val logFile = getLogFile()
        return BufferedWriter(FileWriter(logFile, true))
    }

    private fun rotateLogFile() {
        synchronized(writerLock) {
            try {
                closeLogWriter()
                writer = openLogFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getLogFile(): File {
        val logDir = File(context.filesDir, "${APP_ID}-logs")
        if (!logDir.exists()) logDir.mkdirs()

        val logFile = File(logDir, "log_$currentLogFileDate.txt")

        return logFile
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Calendar.getInstance().time)
    }

    private fun logLevelToString(priority: Int): String {
        return when (priority) {
            Log.DEBUG -> "DEBUG"
            Log.INFO -> "INFO"
            Log.WARN -> "WARN"
            Log.ERROR -> "ERROR"
            Log.ASSERT -> "ASSERT"
            else -> "UNKNOWN"
        }
    }

    fun closeLogWriter() {
        synchronized(writerLock) {
            try {
                writer?.apply {
                    append("[${getCurrentDateTime()}] [DEBUG] [${::FileLoggingTree.name}] [${::closeLogWriter.name}] Closed")
                    flush()
                    close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private var defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null
    }
}
