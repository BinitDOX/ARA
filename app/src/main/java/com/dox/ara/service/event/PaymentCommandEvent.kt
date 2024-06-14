package com.dox.ara.service.event

import android.os.Bundle
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
        val PAYMENT_UPI_ROUTINE = Routine(CommandType.PAY_UPI.name, false, false)
        val PAYMENT_QR_ROUTINE = Routine(CommandType.PAY_QR.name, false, false)
        var paymentCode: String? = null
    }

    init {
        paymentCode = sharedPreferencesManager.get(PAYMENT_CODE_KEY)
    }

    fun handlePaymentUpiEvent(event: AccessibilityEvent, performGlobalAction: (Int) -> Unit) {
        val packageName = event.packageName?.toString() ?: return

        if (packageName == PAYTM_PACKAGE_NAME) {
            try {
                PAYMENT_UPI_ROUTINE.detected = true
                val parentNodeInfo = event.source ?: return

                val proceedButtonNode = parentNodeInfo.findAccessibilityNodeInfosByViewId("$PAYTM_PACKAGE_NAME:id/proceed")
                if(proceedButtonNode.isNotEmpty()) {
                    proceedButtonNode[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }

                if(paymentCode.isNullOrEmpty()) {
                    Timber.w("[${::handlePaymentUpiEvent.name}] Payment code is not set, skipping further execution")
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
                    PAYMENT_UPI_ROUTINE.active = false
                }
            } catch (e: Exception) {
                Timber.e("[${::handlePaymentUpiEvent.name}] Error: $e")
            }
        }
    }

    fun handlePaymentQrEvent(event: AccessibilityEvent, performGlobalAction: (Int) -> Unit) {
        val packageName = event.packageName?.toString() ?: return

        if (packageName == PAYTM_PACKAGE_NAME) {
            try {
                PAYMENT_QR_ROUTINE.detected = true
                val parentNodeInfo = event.source ?: return

                val buttonNodes = parentNodeInfo.findAccessibilityNodeInfosByViewId("$PAYTM_PACKAGE_NAME:id/acc_cl_parent")
                for(node in buttonNodes) {
                    if (node.isClickable && node.getChild(1).text.contains("Scan &", true)) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        break
                    }
                }

                val amountInputNode = parentNodeInfo.findAccessibilityNodeInfosByViewId("$PAYTM_PACKAGE_NAME:id/amount_et")
                if(amountInputNode.isNotEmpty()) {
                    val arguments = Bundle()
                    arguments.putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        PAYMENT_QR_ROUTINE.inputExtra
                    )
                    amountInputNode[0].performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                }

                val proceedButtonNode = parentNodeInfo.findAccessibilityNodeInfosByViewId("$PAYTM_PACKAGE_NAME:id/proceed")
                if(proceedButtonNode.isNotEmpty()) {
                    proceedButtonNode[0].parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    PAYMENT_QR_ROUTINE.active = false
                }
            } catch (e: Exception) {
                Timber.e("[${::handlePaymentUpiEvent.name}] Error: $e")
            }
        }
    }
}