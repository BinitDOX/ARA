package com.dox.ara

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dox.ara.listener.AppLifecycleListener
import com.dox.ara.logging.FileLoggingTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class ARAApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    private lateinit var fileLoggingTree: FileLoggingTree

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        fileLoggingTree = FileLoggingTree(this)
        Timber.plant(fileLoggingTree)

        AppLifecycleListener.init(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .build()

    override fun onTerminate() {
        super.onTerminate()
        fileLoggingTree.closeLogWriter()
    }
}