package com.moneybol.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.moneybol.app.core.model.AnnouncementFormat
import com.moneybol.app.core.model.PaymentEvent
import com.moneybol.app.settings.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Manages payment voice announcements via Text-to-Speech.
 *
 * Responsibilities:
 * - TTS engine initialization and lifecycle
 * - Sequential announcement queue (never overlap speech)
 * - Audio focus management
 * - Speech rate and language configuration
 * - Repeat last payment
 * - Test announcement
 *
 * Thread-safe: announcements are queued and processed sequentially.
 */
@Singleton
class AnnouncementManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val announcementQueue = Channel<String>(Channel.UNLIMITED)
    private val utteranceCounter = AtomicInteger(0)
    private val isInitialized = AtomicBoolean(false)

    private var tts: TextToSpeech? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null
    private var lastAnnouncementText: String? = null

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        initializeTts()
        startQueueProcessor()
    }

    private fun initializeTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Log.w(TAG, "English TTS language not available, using default")
                }
                isInitialized.set(true)
            } else {
                Log.e(TAG, "TTS initialization failed with status: $status")
            }
        }
    }

    /**
     * Process announcements sequentially from the queue.
     */
    private fun startQueueProcessor() {
        scope.launch(Dispatchers.Main) {
            for (text in announcementQueue) {
                if (isInitialized.get()) {
                    speakInternal(text)
                }
            }
        }
    }

    /**
     * Announce a payment event.
     */
    fun announce(event: PaymentEvent) {
        scope.launch {
            val format = userPreferencesRepository.announcementFormat.first()
            val text = AnnouncementFormatter.format(event, format)
            lastAnnouncementText = text
            announcementQueue.send(text)
        }
    }

    /**
     * Play a test announcement.
     */
    fun playTestAnnouncement() {
        scope.launch {
            val format = userPreferencesRepository.announcementFormat.first()
            val text = AnnouncementFormatter.formatTest(format)
            announcementQueue.send(text)
        }
    }

    /**
     * Repeat the last payment announcement.
     */
    fun repeatLastAnnouncement() {
        val text = lastAnnouncementText ?: return
        scope.launch {
            announcementQueue.send(text)
        }
    }

    /**
     * Speak text using TTS with audio focus management.
     * Suspends until speech is complete.
     */
    private suspend fun speakInternal(text: String) {
        val ttsEngine = tts ?: return

        // Request audio focus
        requestAudioFocus()

        // Configure speech rate
        val rate = userPreferencesRepository.speechRate.first()
        ttsEngine.setSpeechRate(rate)

        // Generate unique utterance ID
        val utteranceId = "moneybol_${utteranceCounter.incrementAndGet()}"

        // Speak and wait for completion
        try {
            suspendCancellableCoroutine { continuation ->
                ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {}

                    override fun onDone(id: String?) {
                        if (id == utteranceId) {
                            if (continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }
                    }

                    @Deprecated("Deprecated in API")
                    override fun onError(id: String?) {
                        if (id == utteranceId) {
                            if (continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }
                    }

                    override fun onError(id: String?, errorCode: Int) {
                        if (id == utteranceId) {
                            Log.w(TAG, "TTS error for utterance $id: code $errorCode")
                            if (continuation.isActive) {
                                continuation.resume(Unit)
                            }
                        }
                    }
                })

                ttsEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

                continuation.invokeOnCancellation {
                    ttsEngine.stop()
                }
            }
        } finally {
            abandonAudioFocus()
        }
    }

    private fun requestAudioFocus() {
        val am = audioManager ?: return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(audioAttributes)
            .setWillPauseWhenDucked(false)
            .build()

        am.requestAudioFocus(focusRequest!!)
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        focusRequest?.let { am.abandonAudioFocusRequest(it) }
        focusRequest = null
    }

    /**
     * Shutdown TTS engine. Call when app is destroyed.
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized.set(false)
        abandonAudioFocus()
    }

    companion object {
        private const val TAG = "AnnouncementManager"
    }
}
