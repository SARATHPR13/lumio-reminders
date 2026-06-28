package com.lumio.app.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class SpeechState {
    object Idle       : SpeechState()
    object Listening  : SpeechState()
    object Processing : SpeechState()
    data class Result(val text: String)  : SpeechState()
    data class Error(val message: String): SpeechState()
}

class SpeechRecognitionManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _state = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val state: StateFlow<SpeechState> = _state.asStateFlow()

    fun isAvailable(): Boolean =
        SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening() {
        if (!isAvailable()) {
            _state.value = SpeechState.Error("Speech recognition not available on this device")
            return
        }

        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    _state.value = SpeechState.Listening
                }

                override fun onBeginningOfSpeech() {
                    _state.value = SpeechState.Listening
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Used for volume animation — handled in UI
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _state.value = SpeechState.Processing
                }

                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO            -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT           -> "Client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                            "Microphone permission required"
                        SpeechRecognizer.ERROR_NETWORK          -> "Network error — check internet"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT  -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH         -> "Could not understand — try again"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY  -> "Recognizer busy — please wait"
                        SpeechRecognizer.ERROR_SERVER           -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT   -> "No speech detected — try again"
                        else                                    -> "Unknown error ($error)"
                    }
                    _state.value = SpeechState.Error(message)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )
                    val text = matches?.firstOrNull()
                    if (text.isNullOrBlank()) {
                        _state.value = SpeechState.Error("Could not understand — please try again")
                    } else {
                        _state.value = SpeechState.Result(text)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )
                    val partial = matches?.firstOrNull()
                    if (!partial.isNullOrBlank()) {
                        _state.value = SpeechState.Result(partial)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault().toString())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                Locale.getDefault().toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
        }

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.apply {
            stopListening()
            cancel()
            destroy()
        }
        speechRecognizer = null
        if (_state.value is SpeechState.Listening) {
            _state.value = SpeechState.Idle
        }
    }

    fun reset() {
        stopListening()
        _state.value = SpeechState.Idle
    }

    fun destroy() {
        stopListening()
    }
}
