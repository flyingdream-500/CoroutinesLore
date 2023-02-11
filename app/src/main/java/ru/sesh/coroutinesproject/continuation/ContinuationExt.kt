package ru.sesh.coroutinesproject.continuation

import android.content.Context
import android.widget.Toast
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.sesh.coroutinesproject.utils.AsyncWork.AsyncWorkListener
import ru.sesh.coroutinesproject.utils.AsyncWork.asyncWork
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * - Continuation - коллбэк для suspend функции
 *
 * - Suspend - маркер того, что данная функция умеет (и должна) работать с Continuation,
 *  чтобы приостановить выполнение корутины не блокируя поток
 *
 * - Функция, в которой мы хотим получить доступ к Continuation, должна запускаться только в корутине.
 * В обычном коде ее запускать нельзя. И вот именно для реализации этого ограничения и используется слово suspend
 */
object ContinuationExt {

    /**
     * Suspend метод асинхронной работы с возвращаемым результатом
     *
     * - [suspendCoroutine] предоставляет нам continuation.
     * - В continuation.resume передаем результат выполнения асинхронной работы.
     * - В методе continuation.resumeWithException,  continuation не продолжит свое выполнение и код,
     * который в корутине находится после suspend функции не будет выполнен
     *
     * - Если не вызвать continuation.resume*, то корутина потеряется и не выполниться
     *
     * @return результат выполнения асинхронного метода
     */
    @WorkerThread
    private suspend fun callMe(): String? {
        return suspendCoroutine { continuation ->
            asyncWork(callback = object : AsyncWorkListener<Result<String?>> {
                override fun event(value: Result<String?>) {
                    continuation.resume(value.getOrNull())
                }

                override fun error(error: Exception) {
                    continuation.resumeWithException(error)
                }
            })
        }
    }

    private fun continuationStartFun(context: Context) {
        GlobalScope.launch {
            val result = callMe()
            withContext(Dispatchers.Main) {
                result?.let {
                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}