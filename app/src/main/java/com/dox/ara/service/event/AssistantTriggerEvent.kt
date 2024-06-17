package com.dox.ara.service.event

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import com.dox.ara.activity.MainActivity
import com.dox.ara.manager.SharedPreferencesManager
import com.dox.ara.repository.EventRepository
import com.dox.ara.ui.data.NavItem
import com.dox.ara.ui.data.RouteItem
import com.dox.ara.utility.Constants.ASSISTANT_LISTEN_TRIGGER_SEQUENCE_KEY
import com.dox.ara.utility.Constants.ASSISTANT_OPEN_TRIGGER_SEQUENCE_KEY
import com.dox.ara.utility.Constants.DEFAULT_CHAT_ID_KEY
import com.dox.ara.utility.Constants.NAVIGATE_TO_EXTRA
import com.dox.ara.utility.Constants.START_PAGE_EXTRA
import com.dox.ara.utility.Constants.START_ROUTE_EXTRA
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class AssistantTriggerEvent @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val eventRepository: EventRepository
) {
    companion object {
        var assistantOpenTriggerSequence: String? = null
        var assistantListenTriggerSequence: String? = null
    }

    init {
        assistantOpenTriggerSequence = sharedPreferencesManager.get(ASSISTANT_OPEN_TRIGGER_SEQUENCE_KEY)
        assistantListenTriggerSequence = sharedPreferencesManager.get(ASSISTANT_LISTEN_TRIGGER_SEQUENCE_KEY)
    }

    private var assistantOpenTriggerState = 0
    private var assistantOpenTriggerJob: Job? = null

    private var assistantListenTriggerState = 0
    private var assistantListenTriggerJob: Job? = null

    private var assistantOpenDebouncer = false
    private var assistantListenDebouncer = false


    fun handleAssistantTriggerEvent(event: KeyEvent) {
        if(event.action == KeyEvent.ACTION_UP) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    if(!assistantOpenTriggerSequence.isNullOrBlank()) {
                        handleAssistantOpenTriggerEvent(event.keyCode, assistantOpenTriggerSequence!!)
                    }
                    if(!assistantListenTriggerSequence.isNullOrBlank()) {
                        handleAssistantListenTriggerEvent(event.keyCode, assistantListenTriggerSequence!!)
                    }
                }
            }
        }
    }

    // TODO: Convert to Finite Automata Machine
    private fun handleAssistantOpenTriggerEvent(keyCode: Int, triggerSequence: String) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP && triggerSequence[assistantOpenTriggerState].uppercase() == "U") {
            assistantOpenTriggerState++
            if(assistantOpenTriggerState == triggerSequence.length) {
                assistantOpenTriggerState = 0
                assistantOpen()
            }
        } else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && triggerSequence[assistantOpenTriggerState].uppercase() == "D") {
            assistantOpenTriggerState++
            if(assistantOpenTriggerState == triggerSequence.length) {
                assistantOpenTriggerState = 0
                assistantOpen()
            }
        } else {
            assistantOpenTriggerState = 0
        }

        // Reset the sequence if no key is pressed within 1 second
        assistantOpenTriggerJob?.cancel()
        assistantOpenTriggerJob = CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            assistantOpenTriggerState = 0
        }
    }

    private fun handleAssistantListenTriggerEvent(keyCode: Int, triggerSequence: String) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP && triggerSequence[assistantListenTriggerState].uppercase() == "U") {
            assistantListenTriggerState++
            if(assistantListenTriggerState == triggerSequence.length) {
                assistantListenTriggerState = 0
                assistantListen()
            }
        } else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && triggerSequence[assistantListenTriggerState].uppercase() == "D") {
            assistantListenTriggerState++
            if(assistantListenTriggerState == triggerSequence.length) {
                assistantListenTriggerState = 0
                assistantListen()
            }
        } else {
            assistantListenTriggerState = 0
        }

        // Reset the sequence if no key is pressed within 1 second
        assistantListenTriggerJob?.cancel()
        assistantListenTriggerJob = CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            assistantListenTriggerState = 0
        }
    }

    private fun validateDefaultAssistant(): String? {
        val chatId = sharedPreferencesManager.get(DEFAULT_CHAT_ID_KEY)
        Timber.d("[${::validateDefaultAssistant.name}] Default Chat ID: $chatId")
        if(chatId.isNullOrBlank()){
            Timber.e("[${::validateDefaultAssistant.name}] No default chat ID found, skipping event")
            return null
        }
        return chatId
    }

    private fun assistantOpen(){
        if(!assistantOpenDebouncer) {
            assistantOpenDebouncer = true
            CoroutineScope(Dispatchers.IO).launch {
                delay(3000)
                assistantOpenDebouncer = false
            }
            Timber.d("[${::assistantOpen.name}] Trigger sequence detected, launching assistant")
            val chatId = validateDefaultAssistant() ?: return

            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(START_ROUTE_EXTRA, RouteItem.Home.route)
            intent.putExtra(START_PAGE_EXTRA, NavItem.Chats.page)
            intent.putExtra(NAVIGATE_TO_EXTRA, "${RouteItem.Chat.route}/$chatId")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    fun assistantListen(){
        if(!assistantListenDebouncer) {
            assistantListenDebouncer = true
            CoroutineScope(Dispatchers.IO).launch {
                delay(3000)
                assistantListenDebouncer = false
            }
            Timber.d("[${::assistantListen.name}] Trigger sequence detected, listening for user input")
            val chatId = validateDefaultAssistant() ?: return

            CoroutineScope(Dispatchers.IO).launch {
                eventRepository.startListeningForUserInput(chatId.toLong())
            }
        }
    }
}