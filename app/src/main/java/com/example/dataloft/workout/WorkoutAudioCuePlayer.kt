package com.example.dataloft.workout

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WorkoutAudioCuePlayer {
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 80)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun pulse(times: Int = 1) {
        scope.launch {
            repeat(times) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 160)
                delay(220L)
            }
        }
    }

    fun release() {
        toneGenerator.release()
    }
}
