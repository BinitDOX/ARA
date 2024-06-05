package com.dox.ara.service.event

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.dox.ara.command.CommandHandlerFactory.CommandType
import com.dox.ara.manager.SharedPreferencesManager
import com.dox.ara.service.EventListenerService.Companion.Routine
import com.dox.ara.utility.Constants.PAYMENT_CODE_KEY
import com.dox.ara.utility.Constants.PAYTM_PACKAGE_NAME
import timber.log.Timber
import javax.inject.Inject


class PaymentCommandEvent @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) {
    companion object {
        val PAYMENT_ROUTINE = Routine(CommandType.PAY.name, false, false)
        var paymentCode: String? = null
    }

    init {
        paymentCode = sharedPreferencesManager.get(PAYMENT_CODE_KEY)
    }

    fun handlePaymentEvent(event: AccessibilityEvent, performGlobalAction: (Int) -> Unit) {
        val packageName = event.packageName?.toString() ?: return

        if (packageName == PAYTM_PACKAGE_NAME) {
            try {
                PAYMENT_ROUTINE.detected = true
                val parentNodeInfo = event.source ?: return

                val proceedButtonNode = parentNodeInfo.findAccessibilityNodeInfosByViewId("$PAYTM_PACKAGE_NAME:id/proceed")
                if(proceedButtonNode.isNotEmpty()) {
                    proceedButtonNode[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }

                if(paymentCode.isNullOrEmpty()) {
                    Timber.w("[${::handlePaymentEvent.name}] Payment code is not set, skipping further execution")
                    return
                }

                val virtualKeyboard = parentNodeInfo.findAccessibilityNodeInfosByViewId("$PAYTM_PACKAGE_NAME:id/fragmentTelKeyboard")
                if(virtualKeyboard.isNotEmpty()) {
                    val buttons = ArrayList<AccessibilityNodeInfo>()
                    for (i in 0 until 10) {
                        val nodes = parentNodeInfo.findAccessibilityNodeInfosByText(i.toString())
                        for(node in nodes) {
                            if (node.isClickable && node.text == i.toString()){
                                buttons.add(node)
                            }
                        }
                    }
                    for(code in paymentCode!!) {
                        buttons[code.digitToInt()].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                    PAYMENT_ROUTINE.active = false
                }
            } catch (e: Exception) {
                Timber.e("[${::handlePaymentEvent.name}] Error: $e")
            }
        }
    }
}