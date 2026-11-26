package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class ArcadeSoundEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    @Volatile
    var isMuted: Boolean = false

    fun playSelectDirection(isBull: Boolean) {
        if (isMuted) return
        val freq = if (isBull) 660.0 else 440.0
        playToneSequence(listOf(freq to 55))
    }

    fun playLockIn() {
        if (isMuted) return
        playToneSequence(listOf(330.0 to 50, 523.25 to 60, 783.99 to 90))
    }

    fun playRevealTick(stepIndex: Int) {
        if (isMuted) return
        val freq = 520.0 + (stepIndex.coerceIn(0, 7) * 45.0)
        playToneSequence(listOf(freq to 36))
    }

    fun playVictory() {
        if (isMuted) return
        playToneSequence(
            listOf(
                523.25 to 75,
                659.25 to 75,
                783.99 to 85,
                1046.50 to 170
            )
        )
    }

    fun playLoss() {
        if (isMuted) return
        playToneSequence(
            listOf(
                220.0 to 120,
                164.81 to 220
            ),
            squareWave = true
        )
    }

    private fun playToneSequence(
        notes: List<Pair<Double, Int>>,
        squareWave: Boolean = false
    ) {
        scope.launch {
            runCatching {
                val sampleRate = 22050
                val totalSamples = notes.sumOf { (sampleRate * it.second) / 1000 }
                if (totalSamples <= 0) return@launch

                val pcm = ShortArray(totalSamples)
                var offset = 0
                for ((freq, durationMs) in notes) {
                    val count = (sampleRate * durationMs) / 1000
                    for (i in 0 until count) {
                        val t = i.toDouble() / sampleRate
                        val env = exp(-4.5 * (i.toDouble() / count))
                        val raw = sin(2.0 * PI * freq * t)
                        val wave = if (squareWave) {
                            if (raw >= 0.0) 0.55 else -0.55
                        } else {
                            raw * 0.65
                        }
                        pcm[offset + i] = (wave * env * Short.MAX_VALUE * 0.45).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                            .toShort()
                    }
                    offset += count
                }

                val bufferSize = pcm.size * 2
                val track = AudioTrack.Builder()
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
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
                val totalMs = notes.sumOf { it.second }.toLong() + 60L
                kotlinx.coroutines.delay(totalMs)
                track.stop()
                track.release()
            }
        }
    }
}
