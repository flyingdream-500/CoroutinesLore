package ru.sesh.coroutinesproject.lore.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.sesh.coroutinesproject.lore.utils.logging
import java.util.concurrent.atomic.AtomicInteger

object Concurrency {

    // Volatile - когда один поток меняет значение переменной, об этом сразу же знают все другие потоки
    // Не подойдет для неатомарной операции
    @Volatile
    var volatileValue = 0

    /**
     * Параллельное выполнение с race condition
     */
    fun concurrentExecution(scope: CoroutineScope) {
        scope.launch {
            var i = 0

            val job1 = launch(Dispatchers.Default) {
                repeat(100_000) {
                    i++
                }
            }
            val job2 = launch(Dispatchers.Default) {
                repeat(100_000) {
                    i++
                }
            }

            job1.join()
            job2.join()
            logging("Concurrent value: $i")
        }
    }

    /**
     * Параллельное выполнение с корректным изменением общей переменной
     */
    fun concurrentWithAtomic(scope: CoroutineScope) {
        scope.launch {
            val i = AtomicInteger()

            val job1 = launch(Dispatchers.Default) {
                repeat(100_000) {
                    i.incrementAndGet()
                }
            }

            val job2 = launch(Dispatchers.Default) {
                repeat(100_000) {
                    i.incrementAndGet()
                }
            }

            job1.join()
            job2.join()
            logging("Concurrent value: $i")
        }
    }

}

object ConcurrencySynchronized {

    private var i = 0

    /**
     * Корректный и параллельный инкремент.
     *
     * Если где-то в корутинах вызываются suspend функции, которые приостанавливают выполнение кода,
     * то в этот момент synchronized блокировки снимаются, и вся synchronized логика рушится.
     */
    @Synchronized
    fun synchronizedIncrement() {
        i++
    }

    private val mutex = Mutex()

    // Вместо synchronized рекомендуется в корутинах использовать Mutex
    private suspend fun increment() {
        mutex.withLock {
            i++
        }
    }


    fun concurrencySynchronized(scope: CoroutineScope) {
        scope.launch {
            val job1 = launch(Dispatchers.Default) {
                repeat(100000) {
                    increment()
                }
            }

            val job2 = launch(Dispatchers.Default) {
                repeat(100000) {
                    increment()
                }
            }

            job1.join()
            job2.join()

            logging("Concurrent value: $i")
        }
    }
}