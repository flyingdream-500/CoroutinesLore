package ru.sesh.coroutinesproject.coroutine

import kotlinx.coroutines.*
import ru.sesh.coroutinesproject.utils.logging
import java.lang.Exception
import java.util.concurrent.TimeUnit

/**
 * * Job получает из Continuation ошибку. Он сообщает об этом родителю (scope),
 * который по такому поводу отменяет себя и всех своих детей.
 * А сам Job пытается передать ошибку в CoroutineExceptionHandler.
 * Если такого обработчика ему не предоставили, то ошибка уходит в глобальный обработчик,
 * что приводит к крэшу приложения
 * * Контекст корутин формируется не только из того, что было передано в билдер, но и того, что пришло из контекста родителя.
 * Поэтому, если надо поместить CoroutineExceptionHandler обработчик во все корутины, то просто поместите его в scope,
 * и он будет передан во все корутины, созданные в нем
 *
 */
object CoroutineExceptionsExt {

    private val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        logging("handled: $throwable")
    }

    /**
     * * SupervisorJob отличается от Job тем, что не отменяет всех своих детей при возникновении ошибки в одном из них.
     */
    val supervisorScope = CoroutineScope(SupervisorJob() + Dispatchers.Default + handler)

    /**
     * Обработка ошибки в корутине при помощи CoroutineExceptionHandler
     */
    fun catchWithExceptionHandler(scope: CoroutineScope) {
        scope.launch(handler) {
            exceptionFunction()
        }
    }

    /**
     * * Несмотря на обработчик ошибок, корутина, в которой произошло исключение,
     * сообщит об ошибке в родительский scope, а тот отменит все свои дочерние корутины
     * * scope отменяет не только корутины, но и себя. А это означает,
     * что в этом scope мы больше не сможем запустить корутины
     */
    fun cancelCoroutinesByException(scope: CoroutineScope) {
        scope.launch(handler) {
            TimeUnit.MILLISECONDS.sleep(1_000)
            exceptionFunction()
        }

        scope.launch {
            repeat(5) {
                TimeUnit.MILLISECONDS.sleep(300)
                logging("coroutine is active: $isActive")
            }
        }

    }

    /**
     * * Метод использует билдер launch, чтобы создать и запустить корутину,
     * и сам после этого сразу завершается.
     * А корутина живет своей жизнью в отдельном потоке.
     * Вот именно поэтому try-catch здесь и не срабатывает.
     *
     * * Билдер launch формирует контекст, создает пару Continuation+Job, и отправляет Continuation диспетчеру,
     * который помещает его в очередь. Ни в одном из этих шагов не было никакой ошибки,
     * поэтому try-catch ничего не поймал. Билдер завершил свою работу, и метод успешно завершился
     *
     * * У диспетчера есть свободный поток, который постоянно мониторит очередь
     * Он обнаруживает там Continuation и начинает его выполнение. И вот тут уже возникает NumberFormatException
     * Но наш try-catch до него никак не мог дотянуться. Т.к. он покрывал только создание
     * и запуск корутины, но не выполнение корутины, т.к. выполнение ушло в отдельный поток
     */
    fun wrongTryCatch(scope: CoroutineScope, exceptionBlock: suspend () -> Unit) {
        logging("wrongTryCatch, start")
        try {
            scope.launch() {
                exceptionBlock()
            }
        } catch (e: Exception) {
            logging("error: $e")
        }
        logging("wrongTryCatch, end")
    }

    /**
     * Метод без обработки выбрасываемового исключения
     */
    @Throws(java.lang.NumberFormatException::class)
    suspend fun exceptionFunction() {
        Integer.parseInt("A")
    }

    /**
     * Метод с обработкой выбрасываемового исключения
     */
    @Throws(java.lang.NumberFormatException::class)
    suspend fun safeExceptionFunction() {
        try {
            Integer.parseInt("A")
        } catch (e: Exception) {
            logging("error: $e")
        }
    }

}