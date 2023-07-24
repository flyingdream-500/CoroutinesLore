package ru.sesh.coroutinesproject.lore.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.shareIn

/**
 * Flow начинает работу, когда получатель вызывает его метод collect.
 * И для каждого нового получателя, который вызывает метод collect, создается и начинает работать новый Flow.
 * Такое поведение называется cold
 *
 * SharedFlow отличается от обычного Flow тем, что он не cold, а hot.
 * Он работает в одном экземпляре, его старт не првязан к получателям, и он может иметь несколько получателей
 *
 * Все это потокобезопасно и не требует от нас отдельных действий по синхронизации.
 *
 * Если со стороны получателя необходимо отменить подписку и перестать получать данные,
 * то мы просто отменяем корутину, в которой вызван collect получателя.
 */
object SharedFlowExt {

    // Параметр replay включает буфер указанного размера.
    // Он будет хранить элементы для медленных получателей, чтобы не задерживать всех остальных
    private val mutableSharedFlow = MutableSharedFlow<Int>(replay = 3)
    private val sharedFlow = mutableSharedFlow.asSharedFlow()

    /**
     * С помощью shareIn можно сделать так, чтобы был только один Flow на всех подписчиков
     *
     * Eagerly - работа в Flow стартует сразу при создании SharedFlow, даже если еще нет подписчиков.
     * В этом случае данные пойдут в никуда (и в кэш). Flow будет работать, пока не отменим scope.
     * Lazily - стартует при появлении первого подписчика. Flow будет работать, пока не отменим scope.
     * WhileSubscribed - стартует при появлении первого подписчика. При уходе последнего подписчика - останавливается.
     * Т.е. отменяется подкапотная корутина, в которой работал оригинальный Flow.
     */
    fun shareInFlow(
        scope: CoroutineScope,
        sharingStarted: SharingStarted = SharingStarted.Lazily,
        replay: Int = 3
    ) =
        sharedFlow.shareIn(
            scope = scope,
            started = sharingStarted,
            replay = replay
        )
}