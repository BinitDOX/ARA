package com.dox.ara.command.types

import android.annotation.SuppressLint
import android.content.Context
import android.telecom.TelecomManager
import com.dox.ara.command.CommandHandler
import com.dox.ara.command.CommandHandlerFactory.CommandType.INCOMING_CALL
import com.dox.ara.command.CommandResponse
import com.dox.ara.manager.PermissionManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext


class IncomingCallCommandHandler @AssistedInject constructor(
    @Assisted private val args : List<String>,
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) : CommandHandler(args) {
    override val numArgs = 1
    private lateinit var callValue: CallValue

    enum class CallValue {
        ACCEPT,
        REJECT,
    }

    override fun help(): String {
        return "[${INCOMING_CALL.name.lowercase()}(<accept|reject>)]"
    }

    override fun parseArguments() {
        val callValue = args[0].uppercase().replace("'", "")

        try {
            this.callValue = CallValue.valueOf(callValue)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid value: $callValue, " +
                    "must be one of ${CallValue.entries.joinToString { it.name }}")
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleAccept(): CommandResponse{
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        if(!permissionManager.checkAnswerPhoneCalls()){
            return CommandResponse(
                isSuccess = true,
                message = "Answer phone calls permission not granted",
                getResponse = true
            )
        }

        telecomManager.acceptRingingCall()
        return CommandResponse(
            isSuccess = true,
            message = "Incoming call was accepted",
            getResponse = false
        )
    }

    @SuppressLint("MissingPermission")
    private fun handleReject(): CommandResponse{
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        if(!permissionManager.checkAnswerPhoneCalls()){
            return CommandResponse(
                isSuccess = true,
                message = "Answer phone calls permission not granted",
                getResponse = true
            )
        }

        telecomManager.endCall()
        return CommandResponse(
            isSuccess = true,
            message = "Incoming call was rejected",
            getResponse = false
        )
    }

    override suspend fun execute(): CommandResponse {
        return when(callValue) {
            CallValue.ACCEPT -> handleAccept()
            CallValue.REJECT -> handleReject()
        }
    }
}