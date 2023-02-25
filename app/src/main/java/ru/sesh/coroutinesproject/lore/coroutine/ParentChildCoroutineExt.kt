package ru.sesh.coroutinesproject.lore.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.sesh.coroutinesproject.lore.utils.logging

/**
 * Процесс создания и запуска дочерней корутины внутри родительской состоит из следующих этапов:

1. создание контекста дочерней корутины
2. создание джоба дочерней корутины
3. создание связи с родительской корутиной
4. запуск дочерней корутины (создание Continuation и его отправка в диспетчер)

Билдер выполняет первые два этапа. Далее он вызывает метод start у созданного джоба и тот уже сам выполняет третий и четвертый этапы.


 */
object ParentChildCoroutineExt {

    /**
     * * Родительская корутина перестает быть активной лишь тогда,
     * когда все ее дочерние корутины закончат свое выполнение
     * * Вызов метода cancel для родительской корутины каскадно отменит и все ее дочерние корутины
     * * Метод join будет ждать, чтобы корутина была именно завершена, а не просто выполнила свой код
     *
     */
    fun startParentChildCoroutine(scope: CoroutineScope) {
        val job = scope.launch(start = CoroutineStart.LAZY) {
            logging("parent coroutine, start")
            launch {
                logging("child coroutine, start")
                delay(1_000)
                logging("child coroutine, end")
            }
            logging("parent coroutine, end")
        }

        start(scope, job)
    }

    /**
     * * Lazy дочерняя корутина, если она так и не будет вызвана, не даст завершиться родительской корутине
     */
    fun startParentChildCoroutineWithLazy(scope: CoroutineScope) {
        val job = scope.launch(start = CoroutineStart.LAZY) {
            logging("parent coroutine, start")
            launch(start = CoroutineStart.LAZY) {
                logging("child coroutine, start")
                delay(1_000)
                logging("child coroutine, end")
            }
            logging("parent coroutine, end")
        }

        start(scope, job)
    }

    private fun start(scope: CoroutineScope, job: Job) {
        scope.launch {
            job.start()
            delay(500)
            logging("parent coroutine isActive: ${job.isActive}")
            delay(1_000)
            logging("parent coroutine isActive: ${job.isActive}")
        }
    }
}