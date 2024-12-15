package com.dox.ara.command.types

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.dox.ara.command.CommandHandler
import com.dox.ara.command.CommandHandlerFactory.CommandType.PAY_UPI
import com.dox.ara.command.CommandResponse
import com.dox.ara.manager.PermissionManager
import com.dox.ara.service.EventListenerService
import com.dox.ara.service.event.PaymentCommandEvent
import com.dox.ara.utility.AppTools.Companion.isAppUpiReady
import com.dox.ara.utility.Constants.PAYTM_PACKAGE_NAME
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber


class PayUpiCommandHandler @AssistedInject constructor(
    @Assisted private val args : List<String>,
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) : CommandHandler(args) {
    override val numArgs = 3
    private lateinit var applicationName: PaymentApplication
    private lateinit var upiId: String
    private var amount: Float = 0.0F

    enum class PaymentApplication {
        PAYTM,
        PHONE_PAY
    }

    override fun help(): String {
        val appNames = PaymentApplication.entries.joinToString("|") { it.name.lowercase() }

        return "[${PAY_UPI.name.lowercase()}(<${appNames}>,'upi-id','amount')]"
    }

    override fun parseArguments() {
        val applicationName = args[0].replace(" ", "_").uppercase().replace("'", "")
        val upiId = args[1].replace("'", "")
        val amount = args[2].replace("'", "")

        // TODO: Make UPI validator
        this.upiId = upiId

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
        return try {
            val uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)  // Payee address
                .appendQueryParameter("pn", "Payee Name")  // Payee name
                //.appendQueryParameter("mc", "")  // Merchant code (optional)
                //.appendQueryParameter("tid", "UNIQUE_TRANSACTION_ID")  // Transaction ID (optional)
                //.appendQueryParameter("tr", "UNIQUE_REF_ID")  // Transaction reference ID (optional)
                .appendQueryParameter("tn", "Automated payment through ARA")  // Transaction note
                .appendQueryParameter("am", amount.toString())  // Amount
                .appendQueryParameter("cu", "INR")  // Currency
                .build()

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = uri
                setPackage(PAYTM_PACKAGE_NAME)
            }

            return if (intent.resolveActivity(context.packageManager) != null) {
                if(!isAppUpiReady(context, PAYTM_PACKAGE_NAME)){
                    return CommandResponse(false, "Paytm app is not UPI ready yet", true)
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                val response = EventListenerService.startRoutine(
                    context,
                    PaymentCommandEvent.PAYMENT_UPI_ROUTINE,
                    intent
                )

                response
            } else {
                CommandResponse(false, "Paytm app is not installed", true)
            }
        } catch (e: Exception) {
            Timber.e("[${::handlePaytm.name}] Error: $e")
            CommandResponse(false, "Error launching Paytm: ${e.message}", true)
        }
    }

    private fun handlePhonePay(): CommandResponse {
        return CommandResponse(
            true,
            "Not implemented yet",
            true
        )
    }

    override suspend fun execute(chatId: Long): CommandResponse {
       return when(applicationName){
           PaymentApplication.PAYTM -> handlePaytm()
           PaymentApplication.PHONE_PAY -> handlePhonePay()
       }
    }
}