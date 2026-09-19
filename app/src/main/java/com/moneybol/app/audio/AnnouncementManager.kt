package com.moneybol.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import com.moneybol.app.core.model.PaymentEvent
import com.moneybol.app.settings.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Manages payment voice announcements via Text-to-Speech in English and Nepali.
 *
 * Responsibilities:
 * - TTS engine initialization and lifecycle
 * - Sequential announcement queue (never overlap speech)
 * - Audio focus management
 * - Language selection (English / Nepali) and TTS availability verification
 * - Speech rate configuration
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
    data class QueueItem(
        val text: String,
        val language: String,
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val announcementQueue = Channel<QueueItem>(Channel.UNLIMITED)
    private val utteranceCounter = AtomicInteger(0)
    private val isInitialized = AtomicBoolean(false)
    private val initDeferred = CompletableDeferred<Boolean>()

    private var tts: TextToSpeech? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null
    private var lastAnnouncementItem: QueueItem? = null

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        initializeTts()
        startQueueProcessor()
    }

    private fun initializeTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val ttsEngine = tts
                if (ttsEngine != null) {
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    ttsEngine.setAudioAttributes(audioAttributes)
                }
                isInitialized.set(true)
                initDeferred.complete(true)
                Log.i(TAG, "TTS initialized successfully")
            } else {
                Log.e(TAG, "TTS initialization failed with status: $status")
                isInitialized.set(false)
                initDeferred.complete(false)
            }
        }
    }

    /**
     * Process announcements sequentially from the queue.
     * Waits for TTS engine initialization instead of dropping messages.
     */
    private fun startQueueProcessor() {
        scope.launch(Dispatchers.Main) {
            for (item in announcementQueue) {
                if (!isInitialized.get()) {
                    withTimeoutOrNull(5000) {
                        initDeferred.await()
                    }
                }
                if (isInitialized.get()) {
                    speakInternal(item)
                } else {
                    Log.e(TAG, "Cannot speak '${item.text}': TTS engine failed to initialize")
                }
            }
        }
    }

    /**
     * Announce a payment event.
     */
    fun announce(event: PaymentEvent) {
        scope.launch {
            val language = userPreferencesRepository.ttsLanguage.first()
            val format = userPreferencesRepository.announcementFormat.first()
            val text = AnnouncementFormatter.format(event, format, language)
            val item = QueueItem(text = text, language = language)
            lastAnnouncementItem = item
            announcementQueue.send(item)
        }
    }

    /**
     * Play a test announcement.
     */
    fun playTestAnnouncement() {
        scope.launch {
            val language = userPreferencesRepository.ttsLanguage.first()
            val format = userPreferencesRepository.announcementFormat.first()
            val text = AnnouncementFormatter.formatTest(format, language)
            val item = QueueItem(text = text, language = language)
            announcementQueue.send(item)
        }
    }

    /**
     * Repeat the last payment announcement.
     */
    fun repeatLastAnnouncement() {
        val item = lastAnnouncementItem ?: return
        scope.launch {
            announcementQueue.send(item)
        }
    }

    /**
     * Check if Nepali TTS voice is available on this device.
     */
    fun isNepaliTtsAvailable(): Boolean {
        val ttsEngine = tts ?: return false
        val nepaliLocale = Locale("ne", "NP")
        val avail = ttsEngine.isLanguageAvailable(nepaliLocale)
        return avail != TextToSpeech.LANG_NOT_SUPPORTED && avail != TextToSpeech.LANG_MISSING_DATA
    }

    /**
     * Speak text using TTS with language configuration and audio focus management.
     * Suspends until speech is complete.
     */
    private suspend fun speakInternal(item: QueueItem) {
        val ttsEngine = tts ?: run {
            Log.e(TAG, "speakInternal called but ttsEngine is null")
            return
        }

        // Configure language and verify availability
        configureLanguage(ttsEngine, item.language)

        requestAudioFocus()

        try {
            val rate = userPreferencesRepository.speechRate.first()
            ttsEngine.setSpeechRate(rate)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set speech rate: ${e.message}")
        }

        val utteranceId = "moneybol_${utteranceCounter.incrementAndGet()}"

        try {
            suspendCancellableCoroutine { continuation ->
                ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {
                        Log.d(TAG, "TTS utterance started: $id")
                    }

                    override fun onDone(id: String?) {
                        if (id == utteranceId && continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }

                    @Deprecated("Deprecated in API")
                    override fun onError(id: String?) {
                        if (id == utteranceId) {
                            Log.w(TAG, "TTS error for utterance: $id")
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

                val speakResult = ttsEngine.speak(item.text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                if (speakResult != TextToSpeech.SUCCESS) {
                    Log.e(TAG, "ttsEngine.speak failed immediately with error code: $speakResult")
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
                continuation.invokeOnCancellation {
                    ttsEngine.stop()
                }
            }
        } finally {
            abandonAudioFocus()
        }
    }

    /**
     * Configure the TTS language.
     * If Nepali is requested but not supported/installed on the device, shows a Toast message
     * and logs a clear status rather than failing silently.
     */
    private fun configureLanguage(ttsEngine: TextToSpeech, language: String) {
        if (language == "ne") {
            val nepaliLocale = Locale("ne", "NP")
            val avail = ttsEngine.isLanguageAvailable(nepaliLocale)

            if (avail == TextToSpeech.LANG_NOT_SUPPORTED || avail == TextToSpeech.LANG_MISSING_DATA) {
                Log.w(TAG, "Nepali TTS voice not installed or supported (availability code: $avail)")
                showTtsUnavailableNotice("Nepali voice data is not installed on this phone. Please install Nepali in Google Speech Services or select English in Settings.")
                // Attempt fallback to general Nepali or engine default
                val generalNepali = Locale("ne")
                val generalAvail = ttsEngine.isLanguageAvailable(generalNepali)
                if (generalAvail != TextToSpeech.LANG_NOT_SUPPORTED && generalAvail != TextToSpeech.LANG_MISSING_DATA) {
                    ttsEngine.setLanguage(generalNepali)
                }
            } else {
                val res = ttsEngine.setLanguage(nepaliLocale)
                Log.i(TAG, "Configured Nepali TTS locale: $res")
            }
        } else {
            val localesToTry = listOf(
                Locale.US,
                Locale("en", "IN"),
                Locale.ENGLISH,
                Locale.getDefault()
            )
            for (loc in localesToTry) {
                val res = ttsEngine.setLanguage(loc)
                if (res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED) {
                    break
                }
            }
        }
    }

    private fun showTtsUnavailableNotice(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun requestAudioFocus() {
        val am = audioManager ?: return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(audioAttributes)
            .setWillPauseWhenDucked(false)
            .build()

        focusRequest?.let { am.requestAudioFocus(it) }
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
