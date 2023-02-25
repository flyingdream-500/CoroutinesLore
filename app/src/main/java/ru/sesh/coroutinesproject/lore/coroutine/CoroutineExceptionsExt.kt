package ru.sesh.coroutinesproject.lore.coroutine

import kotlinx.coroutines.*
import ru.sesh.coroutinesproject.lore.utils.logging
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
     * * CoroutineScope - это suspend функция, внутри которой запускается специальная корутина ScopeCoroutine.
     * Код, который мы пишем в блоке coroutineScope, становится кодом этой корутины и выполняется именно в ней
     * * Основная особенность ScopeCoroutine в том, что она не передает ошибку родителю.
     * Т.е. если в корутине 1_2 произойдет ошибка, то она пойдет в ScopeCoroutine, которая отменит себя и своих детей (т.е. корутину 1_1),
     * но своему родителю (корутине 1) она ошибку передавать не будет
     *
     * * Ошибка из корутины 1_2 не пропадает просто так. ScopeCoroutine, хоть и не передаст ее своему родителю, но она передаст ее в свою обертку - suspend функцию coroutineScope.
     * И suspend функция выбросит эту ошибку в наш код в месте своего вызова. И если не поймать ее там, то мы получим стандартное поведение при ошибке в корутине 1
     *
     * * Код coroutineScope выполняется в том же потоке, что и вызвавшая его корутина.
     * А вот продолжить вызвавшую корутину он может в другом потоке (но в том же диспетчере)
     */
    fun coroutineScopeException(scope: CoroutineScope) {
        scope.launch(CoroutineName("1")) {

            coroutineScope {
                launch(CoroutineName("1_1")) {
                    logging("coroutine: 1_1")
                }

                launch(CoroutineName("1_2")) {
                    logging("coroutine: 1_2")
                    exceptionFunction()
                }
            }

            launch(CoroutineName("1_3")) {
                logging("coroutine: 1_3")
            }

            launch(CoroutineName("1_4")) {
                logging("coroutine: 1_4")
            }

        }
    }

    /**
     * * Для обработки ошибки корутин 1_1 и 1_2 необходимо обернуть coroutineScope в try/catch
     */
    fun safeCoroutineScopeException(scope: CoroutineScope) {
        scope.launch(CoroutineName("1")) {
            try {
                coroutineScope {
                    launch(CoroutineName("1_1")) {
                        logging("coroutine: 1_1")
                    }

                    launch(CoroutineName("1_2")) {
                        logging("coroutine: 1_2")
                        exceptionFunction()
                    }
                }
            } catch (e: Exception) {
                logging("exception in coroutineScope^ $e")
            }


            launch(CoroutineName("1_3")) {
                logging("coroutine: 1_3")
            }

            launch(CoroutineName("1_4")) {
                logging("coroutine: 1_4")
            }

        }
    }

    /**
     * * Если coroutineScope принимает ошибки от своих дочерних корутин и просто не шлет их дальше в родительскую корутину,
     * то supervisorScope даже не принимает ошибку от дочерних
     * * Корутина 1_2, которая пытается передать ошибку наверх в ScopeCoroutine, получает отрицательный ответ и пытается обработать ошибку сама.
     * Для этого она использует предоставленный ей CoroutineExceptionHandler. Если же его нет, то будет крэш
     * * Поэтому имеет смысл использовать CoroutineExceptionHandler внутри supervisorScope. В этом случае ошибка попадет в этот обработчик и на этом все закончится.
     * Функция supervisorScope не выбросит исключение и не отменит остальные корутины внутри себя, т.е. корутину 1_1
     */
    fun supervisorScopeException(scope: CoroutineScope) {
        scope.launch(handler + CoroutineName("1")) {

            supervisorScope {
                launch(CoroutineName("1_1")) {
                    logging("coroutine: 1_1")
                }

                launch(CoroutineName("1_2")) {
                    logging("coroutine: 1_2")
                    exceptionFunction()
                }
            }

            launch(CoroutineName("1_3")) {
                logging("coroutine: 1_3")
            }

            launch(CoroutineName("1_4")) {
                logging("coroutine: 1_4")
            }

        }
    }

    /**
     * launch запускает async корутину и методом await подписывается на ожидание результата.
     * Когда в async произойдет исключение, оно будет поймано и отправлено в родительский launch и далее вверх.
     * Т.е. родительский launch будет отменен. А метод await вместо результата выбросит то самое исключение,
     * которое произошло в async
     */
    fun exceptionWithAsync(scope: CoroutineScope) {
        scope.launch(handler) {
            val deferred = async {
                exceptionFunction()
            }
            logging("before await")
            // Исключение сработает при вызове функции await()
            // Если обернуть в блок try/catch, то корутина продолжит выполняться
            val result = deferred.await()
            logging("after await")
        }
    }

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