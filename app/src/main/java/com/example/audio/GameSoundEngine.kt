package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class GameSoundEngine {
    private val sampleRate = 22050
    private val scope = CoroutineScope(Dispatchers.Default)
    private val trackSemaphore = Semaphore(4)
    private var musicJob: Job? = null
    var isMuted = false

    fun playHeroicChime() {
        if (isMuted) return
        scope.launch {
            val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f) // C5, E5, G5, C6
            val durationMs = 260
            val totalSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(totalSamples)

            for (i in 0 until totalSamples) {
                val t = i.toFloat() / sampleRate
                var sample = 0f
                for ((idx, freq) in notes.withIndex()) {
                    val noteStart = idx * 0.04f
                    if (t >= noteStart) {
                        val decay = exp(-((t - noteStart) * 9f))
                        sample += (sin(2.0 * PI * freq * (t - noteStart)) * decay).toFloat() * 0.25f
                    }
                }
                buffer[i] = (sample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
            }
            playRawBuffer(buffer)
        }
    }

    fun playPunchImpact() {
        if (isMuted) return
        scope.launch {
            val durationMs = 150
            val totalSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(totalSamples)
            val random = Random(42)

            for (i in 0 until totalSamples) {
                val t = i.toFloat() / sampleRate
                val freq = 160f * exp(-t * 28f) + 40f
                val sine = sin(2.0 * PI * freq * t).toFloat()
                val noise = (random.nextFloat() * 2f - 1f) * exp(-t * 35f)
                val mixed = (sine * 0.7f + noise * 0.3f) * exp(-t * 18f)
                buffer[i] = (mixed.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
            }
            playRawBuffer(buffer)
        }
    }

    fun playHeroModeActivate() {
        if (isMuted) return
        scope.launch {
            val durationMs = 600
            val totalSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(totalSamples)

            for (i in 0 until totalSamples) {
                val t = i.toFloat() / sampleRate
                val freq = 220f + 440f * (t / (durationMs / 1000f))
                val wave = sin(2.0 * PI * freq * t).toFloat() * 0.6f +
                        sin(2.0 * PI * (freq * 1.5f) * t).toFloat() * 0.3f
                val env = exp(-t * 2.5f)
                buffer[i] = ((wave * env).coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
            }
            playRawBuffer(buffer)
        }
    }

    fun playCivilianThankYou() {
        if (isMuted) return
        scope.launch {
            val notes = floatArrayOf(440f, 554.37f, 659.25f) // A4, C#5, E5
            val durationMs = 300
            val totalSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(totalSamples)

            for (i in 0 until totalSamples) {
                val t = i.toFloat() / sampleRate
                var sample = 0f
                for ((idx, freq) in notes.withIndex()) {
                    val noteStart = idx * 0.06f
                    if (t >= noteStart) {
                        val decay = exp(-((t - noteStart) * 7f))
                        sample += (sin(2.0 * PI * freq * (t - noteStart)) * decay).toFloat() * 0.3f
                    }
                }
                buffer[i] = (sample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
            }
            playRawBuffer(buffer)
        }
    }

    fun playDodgeWhoosh() {
        if (isMuted) return
        scope.launch {
            val durationMs = 120
            val totalSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(totalSamples)
            val random = Random(123)

            for (i in 0 until totalSamples) {
                val t = i.toFloat() / sampleRate
                val noise = (random.nextFloat() * 2f - 1f)
                val envelope = sin((t / (durationMs / 1000f)) * PI).toFloat()
                buffer[i] = ((noise * envelope * 0.4f).coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
            }
            playRawBuffer(buffer)
        }
    }

    fun startAmbientRhythm(isBoss: Boolean = false) {
        musicJob?.cancel()
        musicJob = scope.launch {
            var step = 0
            while (isActive) {
                if (!isMuted) {
                    if (isBoss) {
                        playBossPulse(step)
                    } else {
                        playAfricanDjembePulse(step)
                    }
                }
                step = (step + 1) % 16
                delay(160) // approx 94 BPM
            }
        }
    }

    fun stopAmbientRhythm() {
        musicJob?.cancel()
        musicJob = null
    }

    private fun playAfricanDjembePulse(step: Int) {
        // Rhythmic pattern inspired by Semba/Kuduro syncopation
        val isBass = step == 0 || step == 6 || step == 10
        val isTone = step == 3 || step == 8 || step == 12 || step == 14
        val isSlap = step == 4 || step == 15

        if (!isBass && !isTone && !isSlap) return

        val durationMs = if (isBass) 120 else 60
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)

        val baseFreq = when {
            isBass -> 85f
            isTone -> 180f
            else -> 320f
        }

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            val freq = baseFreq * exp(-t * 22f)
            val amp = if (isBass) 0.4f else 0.25f
            val sample = sin(2.0 * PI * freq * t).toFloat() * exp(-t * 18f) * amp
            buffer[i] = (sample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        }
        playRawBuffer(buffer)
    }

    private fun playBossPulse(step: Int) {
        if (step % 4 != 0 && step != 10) return
        val durationMs = 200
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            val freq = 55f // low dramatic A1
            val sample = (sin(2.0 * PI * freq * t) + 0.5 * sin(2.0 * PI * freq * 2 * t)).toFloat() *
                    exp(-t * 8f) * 0.45f
            buffer[i] = (sample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        }
        playRawBuffer(buffer)
    }

    private fun playRawBuffer(buffer: ShortArray) {
        scope.launch {
            try {
                trackSemaphore.withPermit {
                    var track: AudioTrack? = null
                    try {
                        track = AudioTrack.Builder()
                            .setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_GAME)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .build()
                            )
                            .setAudioFormat(
                                AudioFormat.Builder()
                                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                    .setSampleRate(sampleRate)
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                    .build()
                            )
                            .setBufferSizeInBytes(buffer.size * 2)
                            .setTransferMode(AudioTrack.MODE_STATIC)
                            .build()

                        track.write(buffer, 0, buffer.size)
                        track.play()
                        delay(buffer.size * 1000L / sampleRate + 50)
                    } finally {
                        track?.stop()
                        track?.release()
                    }
                }
            } catch (_: Exception) {
                // Graceful fallback if device audio track is unavailable
            }
        }
    }
}
