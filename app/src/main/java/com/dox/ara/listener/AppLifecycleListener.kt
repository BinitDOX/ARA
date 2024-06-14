package com.dox.ara.listener

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dox.ara.worker.AutoResponseWorker
import com.truecrm.rat.utility.getWorkerName
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


object AppLifecycleListener : Application.ActivityLifecycleCallbacks {

    private val isInForeground = AtomicBoolean(false)

    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
        scheduleAutoResponseWorker(application.applicationContext)
    }

    private fun scheduleAutoResponseWorker(context: Context){
        val repeatInterval = 1L
        val name = "AutoResponse"

        val workRequest = PeriodicWorkRequestBuilder<AutoResponseWorker>(repeatInterval, TimeUnit.HOURS)
            .setInitialDelay(1L, TimeUnit.MINUTES)
            .addTag(getWorkerName(name))
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            getWorkerName(name),
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )

        Timber.d("[${::scheduleAutoResponseWorker.name}] [${getWorkerName(name)}] Scheduled")
    }

    fun isAppInForeground(): Boolean {
        return isInForeground.get()
    }

    override fun onActivityResumed(activity: Activity) {
        isInForeground.set(true)
    }

    override fun onActivityPaused(activity: Activity) {
        isInForeground.set(false)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
