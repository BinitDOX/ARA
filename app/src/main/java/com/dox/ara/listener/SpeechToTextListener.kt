package com.dox.ara.listener

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

class SpeechToTextListener @Inject constructor(
    @ApplicationContext private val context: Context
) : RecognitionListener {

    private val _state = MutableStateFlow(SpeechToTextState())
    val state = _state.asStateFlow()

    private lateinit var recognizer: SpeechRecognizer

    fun initialize() {
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(this@SpeechToTextListener)
        }
    }

    fun startListening(languageCode: String = "en-US") {
        _state.update { SpeechToTextState() }

        if(!SpeechRecognizer.isRecognitionAvailable(context)){
            _state.update {
                it.copy(error = "Speech recognition is not available")
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
        }

        recognizer.startListening(intent)

        _state.update {
            it.copy(isListening = true)
        }
    }

    fun stopListening() {
        recognizer.stopListening()
        _state.update {
            it.copy(isListening = false)
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _state.update {
            it.copy(error = null)
        }
    }

    override fun onBeginningOfSpeech() = Unit

    override fun onRmsChanged(rmsdB: Float) = Unit

    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onEndOfSpeech() {
        _state.update {
            it.copy(isListening = false)
        }
    }

    override fun onError(error: Int) {
        Timber.e("[onError] Error: $error")

        if(error == SpeechRecognizer.ERROR_CLIENT) {
            return
        }

        _state.update {
            it.copy(
                isListening = false,
                error = "Error: $error"
            )
        }
    }

    override fun onResults(results: Bundle?) {
        results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            ?.let { text ->
                _state.update {
                    it.copy(spokenText = text)
                }
                Timber.d("[onResults] Spoken text: $text")
            }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        partialResults
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            ?.let { text ->
                _state.update {
                    it.copy(spokenText = text)
                }
                Timber.d("[onPartialResults] Spoken text: $text")
            }
    }

    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}

data class SpeechToTextState(
    val isListening: Boolean = false,
    val spokenText: String = "",
    val error: String? = null
)