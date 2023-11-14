package com.example.stt

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var textView: TextView

    private var printRecognisedText = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
        )

        initializeSpeechRecognizer()
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
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
                    if (it.lowercase() == "google") {
                        printRecognisedText = true
                        speechRecognizer.startListening(intent)
                        return
                    }

                    if (printRecognisedText) {
                        printRecognisedText = false
                        printRecognisedTextOnScreen(it)
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                TODO("Not yet implemented")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                TODO("Not yet implemented")
            }
        })

        if (isRecordAudioPermissionGranted()) {
            speechRecognizer.startListening(intent)
        } else {
            requestRecordAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private val requestRecordAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                initializeSpeechRecognizer()
            } else {
                Toast.makeText(
                    this,
                    "Permission denied. App cannot function properly.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

    private fun isRecordAudioPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun printRecognisedTextOnScreen(text: String) {
        textView.text = text
    }

    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}
