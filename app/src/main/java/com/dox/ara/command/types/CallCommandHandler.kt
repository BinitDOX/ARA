package com.dox.ara.command.types

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import com.dox.ara.command.CommandHandler
import com.dox.ara.command.CommandHandlerFactory.CommandType.CALL
import com.dox.ara.command.CommandResponse
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber


class CallCommandHandler @AssistedInject constructor(
    @Assisted private val args : List<String>,
    @ApplicationContext private val context: Context,
    ) : CommandHandler(args) {
    override val numArgs = 1
    private lateinit var contactName: String
    private lateinit var phoneNumber: String

    override fun help(): String {
        return "[${CALL.name.lowercase()}('contactName')]"
    }

    override fun parseArguments() {
        val contactName = args[0].replace("'","").replace("\"", "")
        this.contactName = contactName

        phoneNumber = getPhoneNumberFromContactName(context, contactName)
            ?: throw IllegalArgumentException("Contact $contactName not found.")
    }

    override suspend fun execute(chatId: Long): CommandResponse {
        return try {
            val intent = Intent(Intent.ACTION_CALL);
            intent.data = Uri.parse("tel:$phoneNumber")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)

            CommandResponse(
                message = "Calling $contactName...",
                isSuccess = true,
                getResponse = false
            )
        } catch (e: Exception) {
            Timber.e("[${::execute.name}] Error: $e")
            CommandResponse(
                message = "Failed to call: $contactName",
                isSuccess = false,
                getResponse = true
            )
        }
    }

    private fun getPhoneNumberFromContactName(context: Context, contactName: String): String? {
        val contentResolver = context.contentResolver
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(contactName)
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (numberIndex != -1) {
                    return it.getString(numberIndex)
                }
            }
        }

        return null
    }
}