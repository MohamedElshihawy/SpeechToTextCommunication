package com.example.stt

import ai.picovoice.porcupine.Porcupine
import ai.picovoice.porcupine.PorcupineException
import ai.picovoice.porcupine.PorcupineManager
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class WakeUpWordDetector : Service() {
    private val accessKey =
        "qw6LFHTvzRfJ2GM6/1HK/UTFe9mXj1U/+Y7WalPemGulDJtx/uYSaw=="
    private lateinit var porcupineManager: PorcupineManager
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var onRecognitionListener: SpeechRecognitionCallbacks

    private val speechRecognizerListener = object : RecognitionListener {
        override fun onReadyForSpeech(p0: Bundle?) {
            Log.e("SPEECH", "onReadyForSpeech")
        }

        override fun onBeginningOfSpeech() {
            Log.e("SPEECH", "onBeginningOfSpeech")
        }

        override fun onRmsChanged(p0: Float) {
            Log.e("SPEECH", "onRmsChanged")
        }

        override fun onBufferReceived(p0: ByteArray?) {
            Log.e("SPEECH", "onBufferReceived")
        }

        override fun onEndOfSpeech() {
            Log.e("SPEECH", "onEndOfSpeech")
        }

        override fun onError(errorCode: Int) {
            Log.e("SPEECH", "onError code is $errorCode")
        }

        override fun onResults(results: Bundle?) {
            val matches =
                results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.get(0)?.let {
                Log.e("TAG", "onResults: $it")
                // onRecognitionListener.onSpeechRecognised(it)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            TODO("Not yet implemented")
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            TODO("Not yet implemented")
        }
    }

    override fun onCreate() {
        super.onCreate()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(speechRecognizerListener)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            porcupineManager = PorcupineManager.Builder()
                .setAccessKey(accessKey)
                .setKeyword(Porcupine.BuiltInKeyword.HEY_GOOGLE)
                .setSensitivity(0.7f)
                .build(
                    applicationContext,
                ) { _ ->
                    Handler(Looper.getMainLooper()).postDelayed({
                        startListeningToSpeech()
                    }, 2000)
                }
            porcupineManager.start()
        } catch (e: PorcupineException) {
            Log.e("Couldn't init wake up word", e.toString())
        }

        return START_STICKY
    }

    private fun startListeningToSpeech() {
        speechRecognizer.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "US-en")
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
            },
        )
    }

    override fun onDestroy() {
        try {
            porcupineManager.stop()
            porcupineManager.delete()
        } catch (e: PorcupineException) {
            Log.e("Couldn't destroy porcupine", e.toString())
        }
        super.onDestroy()
    }
}
