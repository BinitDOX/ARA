package com.dox.ara.command

import timber.log.Timber

abstract class CommandHandler(
    private val args: List<String>
) {
    companion object {
        fun makeResponseFromException(exception: IllegalArgumentException): CommandResponse {
            return CommandResponse(
                isSuccess = false,
                message = exception.message + "\nInform and ask the user before trying to execute the command again",
                sendResponse = !exception.message.isNullOrBlank(),
                getResponse = !exception.message.isNullOrBlank(),
            )
        }

        fun makeResponseFromUnexpectedException(exception: Exception): CommandResponse {
            return CommandResponse(
                isSuccess = false,
                message = exception.message ?: "",
                sendResponse = false,
                getResponse = false,
            )
        }
    }

    private fun checkArguments() {
        if (args.size != numArgs) {
            throw IllegalArgumentException("Invalid number of arguments, required: $numArgs, found: ${args.size}")
        }
    }

    // Protected properties and methods that must be implemented
    protected abstract val numArgs: Int

    @Throws(IllegalArgumentException::class)
    protected abstract fun parseArguments()

    protected abstract fun help(): String

    protected abstract suspend fun execute(chatId: Long): CommandResponse


    // Public final methods that can be called from the factory returned object
    // But the implementation of these methods cannot be overridden
    suspend fun validateAndExecute(chatId: Long): CommandResponse {
        try {
            checkArguments()
            parseArguments()
        } catch (ex: IllegalArgumentException) {
            Timber.e("[${::validateAndExecute.name}] [Expected] Error: ${ex.message}")
            return makeResponseFromException(ex)
        } catch (ex: Exception){
            Timber.e("[${::validateAndExecute.name}] [Unexpected] Error: $ex")
            return makeResponseFromUnexpectedException(ex)
        }

        return execute(chatId)
    }

    fun getUsage(): String {
        return help()
    }

    fun getHelp(): CommandResponse {
        return CommandResponse(
            isSuccess = true,
            message = help(),
            sendResponse = true,
            getResponse = true
        )
    }
}