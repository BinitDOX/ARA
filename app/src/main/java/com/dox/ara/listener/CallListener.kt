package com.dox.ara.listener

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import com.dox.ara.command.CommandHandlerFactory.CommandType
import com.dox.ara.command.types.IncomingCallCommandHandler.CallValue
import com.dox.ara.repository.EventRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CallListener: BroadcastReceiver() {
    @Inject
    lateinit var eventRepository: EventRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    // Handle incoming call
                    if(intent.hasExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)) {
                        Timber.d("[onReceive] Incoming call: $phoneNumber")
                        if(!phoneNumber.isNullOrBlank() && context != null) {
                            val contactName = getContactNameByPhoneNumber(context, phoneNumber)
                            Timber.d("[onReceive] Contact name: $contactName")

                            val content = "Incoming call from contact: '${contactName ?: "unknown"}" +
                                "'\nInform the user about this and " +
                                "ask if he wants you to accept or reject the call." +
                                "\nThen append one of these command at the end of your next message" +
                                " ONLY after receiving acknowledgement from the user:\n" +
                                "For Accept: [${CommandType.INCOMING_CALL}(${CallValue.ACCEPT})]\n" +
                                "For Reject: [${CommandType.INCOMING_CALL}(${CallValue.REJECT})]"
                            val eventResponse = EventRepository.EventResponse(
                                message = content,
                                getResponse = true,
                                getUserInput = true
                            )
                            CoroutineScope(Dispatchers.IO).launch {
                                eventRepository.handleEvent(eventResponse)
                            }
                        }
                    }
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // Handle call answered
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // Handle call ended or idle
                }
            }
        }
    }

    @SuppressLint("Range")
    fun getContactNameByPhoneNumber(context: Context, phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)

        var contactName: String? = null
        cursor?.use { c ->
            if (c.moveToFirst()) {
                contactName = c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
            }
        }
        cursor?.close()
        return contactName
    }

}