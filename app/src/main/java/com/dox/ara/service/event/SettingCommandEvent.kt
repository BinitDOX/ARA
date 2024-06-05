package com.dox.ara.service.event

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.dox.ara.command.CommandHandlerFactory.CommandType.SETTING
import com.dox.ara.command.types.SettingCommandHandler.SettingType.MOBILE_DATA
import com.dox.ara.command.types.SettingCommandHandler.SettingType.WIFI
import com.dox.ara.service.EventListenerService.Companion.Routine
import com.dox.ara.utility.Constants.SETTINGS_PACKAGE_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


class SettingCommandEvent @Inject constructor() {
    companion object {
        val QUICK_SETTINGS_ROUTINE = Routine(SETTING.name, false, false)
        var WIFI_SUB_ROUTINE = Routine(WIFI.name, false, false)
        var MOBILE_DATA_SUB_ROUTINE = Routine(MOBILE_DATA.name, false, false)

        fun cancelAllSubRoutines() {
            WIFI_SUB_ROUTINE.active = false
            MOBILE_DATA_SUB_ROUTINE.active = false
        }
    }

    fun handleQuickSettingsEvent(event: AccessibilityEvent, performGlobalAction: (Int) -> Unit) {
        val packageName = event.packageName?.toString() ?: return
        Timber.d("Package name: $packageName")

        if (packageName == SETTINGS_PACKAGE_NAME) {
            try {
                QUICK_SETTINGS_ROUTINE.detected = true
                val parentNodeInfo = event.source ?: return

                if(WIFI_SUB_ROUTINE.active) {
                    QUICK_SETTINGS_ROUTINE.active = false
                    WIFI_SUB_ROUTINE.active = false
                    val wifiSetting =
                        parentNodeInfo.findAccessibilityNodeInfosByViewId("android:id/title")
                    for (setting: AccessibilityNodeInfo in wifiSetting) {
                        if (setting.text.contains("Wi-Fi")) {
                            setting.parent.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            Timber.d("[${::handleQuickSettingsEvent.name}] Toggled Wi-Fi")
                            break
                        }
                    }
                    startBackCoroutine(performGlobalAction)
                }

                if(MOBILE_DATA_SUB_ROUTINE.active) {
                    QUICK_SETTINGS_ROUTINE.active = false
                    MOBILE_DATA_SUB_ROUTINE.active = false
                    val mobileDataSetting =
                        parentNodeInfo.findAccessibilityNodeInfosByViewId("android:id/title")
                    for (setting: AccessibilityNodeInfo in mobileDataSetting) {
                        if (setting.text.contains("Mobile data")) {
                            setting.parent.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            Timber.d("[${::handleQuickSettingsEvent.name}] Toggled Mobile Data")
                            break
                        }
                    }
                    startBackCoroutine(performGlobalAction)
                }

            } catch (e: Exception) {
                Timber.e("[${::handleQuickSettingsEvent.name}] Error: $e")
            }
        }
    }

    private fun startBackCoroutine(performGlobalAction: (Int) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }
    }
}