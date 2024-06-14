package com.dox.ara.command.types

import android.content.Context
import android.content.Intent
import com.dox.ara.command.CommandHandler
import com.dox.ara.command.CommandHandlerFactory.CommandType.PAY_QR
import com.dox.ara.command.CommandResponse
import com.dox.ara.manager.PermissionManager
import com.dox.ara.service.EventListenerService
import com.dox.ara.service.EventListenerService.Companion.ROUTINE_INPUT_EXTRA
import com.dox.ara.service.event.PaymentCommandEvent
import com.dox.ara.utility.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


class PayQrCommandHandler @AssistedInject constructor(
    @Assisted private val args : List<String>,
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) : CommandHandler(args) {
    override val numArgs = 2
    private lateinit var applicationName: PaymentApplication
    private var amount: Float = 0.0F

    enum class PaymentApplication {
        PAYTM,
        PHONE_PAY
    }

    override fun help(): String {
        val appNames = PaymentApplication.entries.joinToString("|") { it.name.lowercase() }

        return "[${PAY_QR.name.lowercase()}(<${appNames}>,'amount')]"
    }

    override fun parseArguments() {
        val applicationName = args[0].replace(" ", "_").uppercase().replace("'", "")
        val amount = args[1].replace("'", "")

        try {
            this.applicationName = PaymentApplication.valueOf(applicationName)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException(
                "Invalid application: $applicationName, " +
                        "must be one of ${PaymentApplication.entries.joinToString { it.name }}",
            )
        }

        try {
            this.amount = amount.toFloat()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Cannot parse amount: $amount, must be a float")
        }
    }

    private suspend fun handlePaytm(): CommandResponse {
        try {
            if(!permissionManager.isAccessibilityPermissionGranted(context)){
                return CommandResponse(
                    isSuccess = false,
                    message = "Accessibility permission is not granted",
                    getResponse = true
                )
            }

            val intent = Intent(context.packageManager.getLaunchIntentForPackage(Constants.PAYTM_PACKAGE_NAME))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(ROUTINE_INPUT_EXTRA, amount.toString())


            var responseQr: CommandResponse? = null
            CoroutineScope(Dispatchers.IO).launch {
                responseQr = EventListenerService.startRoutine(
                    context,
                    PaymentCommandEvent.PAYMENT_QR_ROUTINE,
                    intent,
                    10000L
                )
            }

            val responseUpi = EventListenerService.startRoutine(
                context,
                PaymentCommandEvent.PAYMENT_UPI_ROUTINE,
                null,
                15000L
            )
            return if(responseUpi.isSuccess && responseQr != null){
                responseQr!!
            } else {
                responseUpi
            }
        } catch (e: Exception) {
            Timber.e("[${::handlePaytm.name}] Error: $e")
            return CommandResponse(false, "Error launching Paytm: ${e.message}", true)
        }
    }

    private fun handlePhonePay(): CommandResponse {
        return CommandResponse(
            true,
            "Not implemented yet",
            true
        )
    }

    override suspend fun execute(): CommandResponse {
       return when(applicationName){
           PaymentApplication.PAYTM -> handlePaytm()
           PaymentApplication.PHONE_PAY -> handlePhonePay()
       }
    }
}