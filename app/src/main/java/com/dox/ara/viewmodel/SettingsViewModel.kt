package com.dox.ara.viewmodel

import androidx.lifecycle.ViewModel
import com.dox.ara.manager.SharedPreferencesManager
import com.dox.ara.service.event.AssistantTriggerEvent
import com.dox.ara.utility.Constants.ASSISTANT_LISTEN_TRIGGER_SEQUENCE_KEY
import com.dox.ara.utility.Constants.ASSISTANT_OPEN_TRIGGER_SEQUENCE_KEY
import com.dox.ara.utility.Constants.DEVICE_UNLOCK_CODE_KEY
import com.dox.ara.utility.Constants.DYNAMIC_URL_KEY
import com.dox.ara.utility.Constants.PAYMENT_CODE_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
): ViewModel() {

    private val _baseUrl = MutableStateFlow("")
    val baseUrl = _baseUrl.asStateFlow()

    private val _paymentCode = MutableStateFlow("")
    val paymentCode = _paymentCode.asStateFlow()

    private val _deviceUnlockCode = MutableStateFlow("")
    val deviceUnlockCode = _deviceUnlockCode.asStateFlow()

    private val _assistantOpenTriggerSequence = MutableStateFlow("")
    val assistantOpenTriggerSequence = _assistantOpenTriggerSequence.asStateFlow()

    private val _assistantListenTriggerSequence = MutableStateFlow("")
    val assistantListenTriggerSequence = _assistantListenTriggerSequence.asStateFlow()

    init {
        getSettings()
    }

    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()


    fun setBaseUrl(value: String) {
        _baseUrl.value = value
    }

    fun setPaymentCode(value: String) {
        _paymentCode.value = value
    }

    fun setDeviceUnlockCode(value: String) {
        _deviceUnlockCode.value = value
    }

    fun setAssistantOpenTriggerSequence(value: String) {
        _assistantOpenTriggerSequence.value = value
    }

    fun setAssistantListenTriggerSequence(value: String) {
        _assistantListenTriggerSequence.value = value
    }

    fun setIsSaved(value: Boolean) {
        _isSaved.value = value
    }

    private fun validateInput(): Boolean {
        return true
    }

    fun getSettings() {
        _baseUrl.value = sharedPreferencesManager.get(DYNAMIC_URL_KEY) ?: ""
        _paymentCode.value = sharedPreferencesManager.get(PAYMENT_CODE_KEY) ?: ""
        _deviceUnlockCode.value = sharedPreferencesManager.get(DEVICE_UNLOCK_CODE_KEY) ?: ""
        _assistantOpenTriggerSequence.value = sharedPreferencesManager.get(ASSISTANT_OPEN_TRIGGER_SEQUENCE_KEY) ?: ""
        _assistantListenTriggerSequence.value = sharedPreferencesManager.get(ASSISTANT_LISTEN_TRIGGER_SEQUENCE_KEY) ?: ""
    }

    fun saveSettings() {
        if(!validateInput()){
            return
        }

        sharedPreferencesManager.save(DYNAMIC_URL_KEY, _baseUrl.value)
        sharedPreferencesManager.save(PAYMENT_CODE_KEY, _paymentCode.value)
        sharedPreferencesManager.save(DEVICE_UNLOCK_CODE_KEY, _deviceUnlockCode.value)
        sharedPreferencesManager.save(ASSISTANT_OPEN_TRIGGER_SEQUENCE_KEY, _assistantOpenTriggerSequence.value)
        sharedPreferencesManager.save(ASSISTANT_LISTEN_TRIGGER_SEQUENCE_KEY, _assistantListenTriggerSequence.value)

        AssistantTriggerEvent.assistantOpenTriggerSequence = _assistantOpenTriggerSequence.value
        AssistantTriggerEvent.assistantListenTriggerSequence = _assistantListenTriggerSequence.value
    }
}
