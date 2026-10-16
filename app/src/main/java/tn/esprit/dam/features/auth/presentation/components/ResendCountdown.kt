package tn.esprit.dam.features.auth.presentation.components

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ResendCountdown(
    private val scope: CoroutineScope,
    private val seconds: Int,
    private val onTick: (remainingSeconds: Int, canResend: Boolean) -> Unit
) {
    private var job: Job? = null

    fun start() {
        job?.cancel()
        job = scope.launch {
            var remaining = seconds
            while (remaining > 0) {
                onTick(remaining, false)
                delay(1000)
                remaining--
            }
            onTick(0, true)
        }
    }

    fun cancel() {
        job?.cancel()
    }
}
