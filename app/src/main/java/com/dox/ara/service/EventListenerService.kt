package com.dox.ara.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.dox.ara.command.CommandResponse
import com.dox.ara.service.event.AssistantTriggerEvent
import com.dox.ara.service.event.GoogleAssistantOverrideEvent
import com.dox.ara.service.event.PaymentCommandEvent
import com.dox.ara.service.event.PaymentCommandEvent.Companion.PAYMENT_QR_ROUTINE
import com.dox.ara.service.event.PaymentCommandEvent.Companion.PAYMENT_UPI_ROUTINE
import com.dox.ara.service.event.SettingCommandEvent
import com.dox.ara.service.event.SettingCommandEvent.Companion.QUICK_SETTINGS_ROUTINE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class EventListenerService : AccessibilityService() {
    companion object {
        val ROUTINE_INPUT_EXTRA = "routine_input"

        data class Routine(
            var name: String,
            var active: Boolean,
            var detected: Boolean,
            var inputExtra: String? = null
        )

        suspend fun startRoutine(context: Context, routine: Routine,
                                         intent: Intent?, timeout: Long = 5000): CommandResponse {
            routine.active = true

            if(intent != null) {
                context.startActivity(intent)
            }

            val extra = intent?.getStringExtra(ROUTINE_INPUT_EXTRA)
            routine.inputExtra = extra

            delay(timeout)

            if(!routine.detected){
                routine.active = false
                Timber.e("[${::startRoutine.name}] [${routine.name}] Could not be launched")
                return CommandResponse(
                    false,
                    "${routine.name} could not be launched",
                    true
                )
            }

            if(routine.active) {
                delay(timeout)
                if(routine.active) {
                    routine.active = false
                    routine.detected = false
                    Timber.e("[${::startRoutine.name}] [${routine.name}] Took longer than expected, quitting routine")
                    return CommandResponse(
                        false,
                        "${routine.name} command took longer than expected, halting executing",
                        true
                    )
                }
            }
            routine.detected = false
            return CommandResponse(
                true,
                "${routine.name} command executed successfully",
                false
            )
        }
    }


    @Inject
    lateinit var settingCommandEvent: SettingCommandEvent
    @Inject
    lateinit var assistantTriggerEvent: AssistantTriggerEvent
    @Inject
    lateinit var paymentCommandEvent: PaymentCommandEvent
    @Inject
    lateinit var googleAssistantOverrideEvent: GoogleAssistantOverrideEvent

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                // Key Logger
            }

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                /*val packageName = event.packageName?.toString() ?: return
                if(packageName == ""){
                    Timber.d("Package name: $packageName")
                    printEvent(event)
                }*/

                if(GoogleAssistantOverrideEvent.OVERRIDE_GOOGLE_ASSISTANT.toBoolean()){
                    googleAssistantOverrideEvent.handleGoogleAssistantOverride(event, ::performGlobalAction)
                }

                if(QUICK_SETTINGS_ROUTINE.active){
                    settingCommandEvent.handleQuickSettingsEvent(event, ::performGlobalAction)
                }

                if(PAYMENT_UPI_ROUTINE.active){
                    paymentCommandEvent.handlePaymentUpiEvent(event, ::performGlobalAction)
                }

                if(PAYMENT_QR_ROUTINE.active){
                    paymentCommandEvent.handlePaymentQrEvent(event, ::performGlobalAction)
                }
            }

            AccessibilityEvent.TYPE_VIEW_CLICKED -> {}

            else -> Timber.w("[${::onAccessibilityEvent.name}] Event type not handled: ${event.eventType}")
        }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event != null) {
            assistantTriggerEvent.handleAssistantTriggerEvent(event)
        }
        return super.onKeyEvent(event)
    }


    private fun printEvent(event: AccessibilityEvent) {
        val parentNodeInfo = event.source
        if(parentNodeInfo != null) {
            Timber.d("[G] -------------------------------")
            printNodeInfoWithPath(parentNodeInfo)
        }
    }

    private fun printNodeInfoWithPath(nodeInfo: AccessibilityNodeInfo, path: String = "") {
        for (i in 0 until nodeInfo.childCount) {
            val childNode = nodeInfo.getChild(i)
            if (childNode != null) {
                val childPath = "$path.$i"
                val isInputField = childNode.isEditable // || childNode.isTextEntryKey
                Timber.d("[V] Path:[$childPath] Info:[${childNode.viewIdResourceName} ${childNode.text}" +
                        " ${childNode.contentDescription}] IsClickable:${childNode.isCheckable||childNode.isClickable}" +
                        " IsInput:${isInputField}")
                printNodeInfoWithPath(childNode, childPath)
            }
        }
    }

    override fun onInterrupt() {}
}
