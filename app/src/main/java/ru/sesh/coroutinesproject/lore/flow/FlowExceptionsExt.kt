package ru.sesh.coroutinesproject.lore.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import ru.sesh.coroutinesproject.lore.utils.logging

/**
 * * Если в Flow возникает ошибка, то мы получим ее там где происходит вызов метода collect()
 */
object FlowExceptionsExt {

    /**
     * Обработка ошибки во Flow с помощью try/catch
     */
    fun handleFlowException(scope: CoroutineScope) {
        scope.launch {
            logging("coroutine, start")
            try {
                flowWithException().collect {
                    logging("emitted value: $it")
                }
            } catch (e: Exception) {
                logging("catch exception: $e")
            }
            logging("coroutine, end")
        }
    }

    /**
     * * Оператор catch является аналогом стандартного try-catch.
     * Он перехватит ошибку, чтобы она не ушла в collect
     * * Оператор catch сможет поймать ошибку только из предшествующих ему операторов.
     * Если ошибка возникла в операторе, который в цепочке находится после catch, то она не будет поймана этим catch
     * Поэтому catch лучше всего ставить перед collect, либо в цепочке использовать несколько операторов catch
     * * Оператор retry перезапустит Flow в случае ошибки. Как и оператор catch, он срабатывает только для тех ошибок,
     * которые произошли в предшествующих ему операторах
     */
    fun handleFlowExceptionWithCatch(scope: CoroutineScope) {
        scope.launch {
            logging("coroutine, start")
            flowWithException()
                .retry(2)
                .catch { logging("catch exception: $it") }
                .collect {
                    logging("emitted value: $it")
                }
            logging("coroutine, end")
        }
    }

    private fun flowWithException() =
        flow {
            delay(1_000)
            emit(1)
            Integer.parseInt("a")
            emit(1)
        }
}