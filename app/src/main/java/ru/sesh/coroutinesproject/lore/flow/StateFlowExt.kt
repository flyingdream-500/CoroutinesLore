package ru.sesh.coroutinesproject.lore.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

object StateFlowExt {

    /**
     * StateFlow это SharedFlow, который удобен для того, чтобы хранить в нем состояние чего-либо.
     * Он всегда хранит в кэше одно (последнее полученное) значение, которое будет получать каждый новый подписчик.
     * А вновь пришедшее значение всегда будет в буфере заменять старое
     */
    fun stateFlowBehaviour(): Flow<Int> {
        val shared = MutableSharedFlow<Int>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
        shared.tryEmit(0) // emit the initial value
        return shared.distinctUntilChanged() // get StateFlow-like behavior
    }

    /**
     * Из обычного Flow можно сделать StateFlow с помощью оператора stateIn
     */
    fun stateInFlow(
        scope: CoroutineScope,
        sharingStarted: SharingStarted = SharingStarted.Lazily,
        initialValue: Int = 0
    ) =
        flow<Int> {
            emit(2)
        }.stateIn(
            scope = scope,
            started = sharingStarted,
            initialValue = initialValue
        )


}