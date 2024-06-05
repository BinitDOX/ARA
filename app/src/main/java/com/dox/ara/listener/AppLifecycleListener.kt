package com.dox.ara.listener

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.concurrent.atomic.AtomicBoolean


object AppLifecycleListener : Application.ActivityLifecycleCallbacks {

    private val isInForeground = AtomicBoolean(false)

    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
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
