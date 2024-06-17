package com.dox.ara.service.event

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import com.dox.ara.manager.SharedPreferencesManager
import com.dox.ara.utility.Constants.GOOGLE_ASSISTANT_PACKAGE_NAME
import com.dox.ara.utility.Constants.OVERRIDE_GOOGLE_ASSISTANT_KEY
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class GoogleAssistantOverrideEvent @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val assistantTriggerEvent: AssistantTriggerEvent
) {
    companion object {
        var OVERRIDE_GOOGLE_ASSISTANT: String? = null
    }

    init {
        OVERRIDE_GOOGLE_ASSISTANT = sharedPreferencesManager.get(OVERRIDE_GOOGLE_ASSISTANT_KEY)
    }

    fun handleGoogleAssistantOverride(event: AccessibilityEvent, performGlobalAction: (Int) -> Unit) {
        val packageName = event.packageName?.toString() ?: return

        if (packageName == GOOGLE_ASSISTANT_PACKAGE_NAME) {
            try {
                val parentNodeInfo = event.source ?: return

                val proceedButtonNode = parentNodeInfo.findAccessibilityNodeInfosByViewId(
                    "${GOOGLE_ASSISTANT_PACKAGE_NAME}:id/assistant_fragment_host_container")
                if(proceedButtonNode.isNotEmpty()) {
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                    assistantTriggerEvent.assistantListen()
                }
            } catch (e: Exception) {
                Timber.e("[${::handleGoogleAssistantOverride.name}] Error: $e")
            }
        }
    }
}