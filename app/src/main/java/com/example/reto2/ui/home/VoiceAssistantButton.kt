
package com.example.reto2.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun VoiceAssistantButton(
    onCommandRecognized: (String) -> Unit
) {
    val context = LocalContext.current

    var message by remember {
        mutableStateOf("Pulsa el micrófono y habla")
    }
    var listening by remember { mutableStateOf(false) }

    val latestCommand by rememberUpdatedState(onCommandRecognized)

    val recognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    DisposableEffect(recognizer) {
        recognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {
                message = "Te escucho. Habla ahora..."
            }

            override fun onBeginningOfSpeech() {
                message = "Detecté tu voz..."
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                message = "Procesando tu voz..."
            }

            override fun onError(error: Int) {
                listening = false

                message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO ->
                        "Error de audio. Revisa el micrófono del emulador."

                    SpeechRecognizer.ERROR_CLIENT ->
                        "Error del servicio de voz. Intenta de nuevo."

                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        "Falta el permiso del micrófono."

                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        "Error de conexión. Revisa internet."

                    SpeechRecognizer.ERROR_NO_MATCH ->
                        "No entendí la frase. Habla más cerca del micrófono."

                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                        "El reconocimiento está ocupado. Intenta de nuevo."

                    SpeechRecognizer.ERROR_SERVER ->
                        "El servicio de voz tuvo un error."

                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        "No detecté voz. Pulsa y habla inmediatamente."

                    else ->
                        "Error de reconocimiento: $error"
                }
            }

            override fun onResults(results: Bundle?) {
                listening = false

                val phrase = results
                    ?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )
                    ?.firstOrNull()

                if (!phrase.isNullOrBlank()) {
                    message = "Escuché: $phrase"
                    android.util.Log.d("VOZ_DEBUG", "Frase reconocida: [$phrase]")
                    latestCommand(phrase)
                } else {
                    message = "Android devolvió una frase vacía."
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val phrase = partialResults
                    ?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )
                    ?.firstOrNull()

                if (!phrase.isNullOrBlank()) {
                    message = "Te escucho: $phrase"
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        onDispose {
            recognizer?.destroy()
        }
    }

    fun startListening() {
        if (recognizer == null) {
            message = "No hay servicio de reconocimiento de voz disponible."
            return
        }

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CO")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-CO")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Di: quiero comprar 100 dólares"
            )
        }

        listening = true
        message = "Iniciando reconocimiento..."

        try {
            recognizer.startListening(intent)
        } catch (e: Exception) {
            listening = false
            message = "No se pudo iniciar: ${e.localizedMessage ?: "error desconocido"}"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startListening()
        } else {
            message = "Debes permitir el acceso al micrófono."
        }
    }

    Button(
        onClick = {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                startListening()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        enabled = !listening
    ) {
        Text(if (listening) "Escuchando..." else "🎤 Hablar")
    }

    Text(text = message)
}
