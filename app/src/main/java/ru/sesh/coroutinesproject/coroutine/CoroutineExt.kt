package ru.sesh.coroutinesproject.coroutine

import android.util.Log
import kotlinx.coroutines.*
import ru.sesh.coroutinesproject.utils.TAG
import ru.sesh.coroutinesproject.utils.coroutineContextParsing
import ru.sesh.coroutinesproject.utils.logging
import java.util.concurrent.TimeUnit
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * - Корутина - это не какой-то конкретный объект. Это набор объектов,
 * которые взаимодействуют друг с другом(Job, Context, Dispatcher и CoroutineScope)
 *
 * - Билдер в зависимости от себя и своих входных параметров выбирает одну из реализаций Job
 * и создает ее объект. Состояние корутины в нем устанавливается в Активна.
 * Из блока кода корутины билдер создает Continuation объект.
 * У Continuation вызывается метод resume
 *
 */
object CoroutineExt {}

/**
 * - Job является ключевым объектом для корутины. Он хранит в себе состояние корутины: активна/отменена/завершена
 * - Job дает нам возможность отменить корутину или стартовать отложенную корутину
 */
object JobExt {

    /**
     * Корутина продолжает выполняться несмотря на отмену.
     * Так произошло потому, что метод cancel меняет статус корутины, но не прерывает ее выполнение
     */
    fun cancelJob(scope: CoroutineScope) {
        logging("launch, start")
        val job = scope.launch {
            logging("coroutine, start")
            repeat(5) {
                TimeUnit.SECONDS.sleep(1)
                logging("coroutine, $it")
            }
            logging("coroutine, end")
        }
        TimeUnit.SECONDS.sleep(2)
        logging("launch, cancel")
        job.cancel()
    }

    /**
     * Привязываем выполнение метода к статусу job
     */
    fun cancelJobWithChangeStatus(scope: CoroutineScope) {
        logging("launch, start")
        val job = scope.launch {
            logging("coroutine, start")
            repeat(5) {
                if (!isActive) return@repeat
                TimeUnit.SECONDS.sleep(1)
                logging("coroutine, $it")
            }
            logging("coroutine, end")
        }
        TimeUnit.SECONDS.sleep(2)
        logging("launch, cancel")
        job.cancel()
    }

    /**
     * Suspend функция delay при отмене корутины сразу прерывает выполнение кода корутины
     */
    fun cancelJobWithSuspendFunc(scope: CoroutineScope) {
        logging("launch, start")
        val job = scope.launch {
            logging("coroutine, start")
            repeat(5) {
                delay(1_000)
                logging("coroutine, $it")
            }
            logging("coroutine, end")
        }
        TimeUnit.SECONDS.sleep(2)
        logging("launch, cancel")
        job.cancel()
    }

    /**
     * - Билдер launch создает и запускает корутину и возвращает ее job.
     * Используя метод join мы приостанавливаем выполнение кода, пока корутина не завершит работу
     *
     * Запускаем дочерние корутины, а потом для обеих вызываем join, тем самым дожидаясь окончания их работы.
     * Дочерние корутины отработают параллельно, поэтому общее время работы родительской корутины составит 1500
     */
    fun jobJoiningParallelWork(scope: CoroutineScope) {
        scope.launch {
            logging("parent coroutine, start")

            val job = launch {
                TimeUnit.MILLISECONDS.sleep(1000)
            }

            val job2 = launch {
                TimeUnit.MILLISECONDS.sleep(1500)
            }

            logging("parent coroutine, wait until children complete")
            job.join()
            job2.join()

            logging("parent coroutine, end")
        }
    }

    /**
     * Cоздаем корутину c параметром start = LAZY.
     * Это не даст корутине начать работу сразу после создания.
     */
    fun lazyStartOfCoroutine(scope: CoroutineScope) {
        val job = scope.launch(start = CoroutineStart.LAZY) {
            logging("coroutine, start")
            TimeUnit.SECONDS.sleep(2_000)
            logging("coroutine, end")
        }
        TimeUnit.SECONDS.sleep(1_000)
        job.start()
    }
}

/**
 *
 * - Scope является обязательным при создании корутин
 *
 * - Scope - это такой родитель для всех корутин. Когда мы отменяем scope,
 * мы отменяем все его дочерние корутины
 *
 * - Если корутин много, то становится неудобно собирать все их джобы и потом отменять их.
 * Scope помогает решить эту проблему
 *
 * - Scope может быть предоставлен некоторыми объектами с жизненным циклом. Например [viewModelScope]
 *
 * - В родительской корутине существует свой scope. И это не тот же самый scope,
 * который мы используем для запуска этой родительской корутины.
 * Каждая корутина создает внутри себя свой scope, чтобы иметь возможность запускать дочерние корутины
 *
 * - У любого scope по контракту должен быть Job,
 * который будет выступать родительским для создаваемых корутин.
 * У scope внутри корутины эту роль берет на себя Job корутины.
 * Когда мы в родительской корутине создаем дочернюю, Job дочерней корутины подписывается на Job родительской.
 * И если отменить родительскую корутину, то отменится и дочерняя
 *
 * - Job наследует интерфейс CoroutineScope и хранит ссылку на Context.
 * Т.к. Context должен содержать Job, то Job просто помещает в Context ссылку на себя
 */
object ScopeExt {
    // простой способ создать scope
    val scope = CoroutineScope(Job())

    fun innerCoroutinesByOneScope() {
        scope.launch {
            // parent coroutine code block
            this.launch {
                // child coroutine code block
            }
        }
    }

    /**
     * Job наследует интерфейс CoroutineScope и хранит ссылку на Context.
     * Т.к. Context должен содержать Job, то Job просто помещает в Context ссылку на себя
     *
     * Output:
     * - job = StandaloneCoroutine{Active}@d78e02d
     * - scope = StandaloneCoroutine{Active}@d78e02d
     */
    fun jobIsScopeCheck() {
        val job = scope.launch {
            Log.d(TAG, "scope = $this")
        }
        Log.d(TAG, "job = $job")
    }

}

object BuilderExt {

    /**
     * - Билдер async возвращает Deferred - наследник Job
     * - Все аналогично примеру с launch+join
     * - async корутину также можно запускать в режиме Lazy. Метод await стартует ее выполнение
     */
    fun asyncBuilder(scope: CoroutineScope) {
        scope.launch {
            logging("start coroutine")
            val deferred = scope.async {
                delay(1_000)
                "result"
            }
            logging("waiting for children coroutine by async")
            val result = deferred.await()
            logging("end coroutine")
        }
    }
}

/**
 * - Когда билдер создает новую корутину, он создает для нее новый пустой контекст и
 * помещает туда элементы из контекста родителя этой корутины.
 * Т.е. элементы контекста scope будут переданы в контекст корутины, созданной в этом scope.
 *
 * - Но есть пара нюансов.

Во-первых, Job не передается. Для создаваемой корутины создается новый Job,
который подписывается на Job родителя и помещается в контекст созданной корутины.

Во-вторых, если при передаче выясняется, что отсутствует диспетчер, то будет взят диспетчер по умолчанию.
Поэтому мы можем нигде явно не указывать диспетчер. В этом случае корутина сама возьмет себе дефолтный.
 */
object CoroutineContextExt {

    // Создание контекста корутин из нескольких слагаемых элементов
    val coroutineContext = Job() + Dispatchers.Default

    data class Person(
        val id: Long,
        val name: String,
        val age: Int
    ) : AbstractCoroutineContextElement(Person) {
        companion object Key : CoroutineContext.Key<Person>
    }

    /**
     * Чтобы сделать какие-либо данные доступными для всех корутин,
     * включая вложенные, добаляем данные в CoroutineContext
     * т.к. данные контекста передаются между корутинами
     */
    fun createScopeWithData() {
        val person = Person(1L, "Kurduk", 21)
        val scope = CoroutineScope(Job() + Dispatchers.Default + person)
        scope.launch {
            val data = coroutineContext[Person]
            logging(data.toString())
        }
    }

    fun innerScopeWithDifferentContext() {
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        logging("scope, ${coroutineContextParsing(scope.coroutineContext)}")

        scope.launch {
            logging("coroutine, level1, ${coroutineContextParsing(coroutineContext)}")

            launch(Dispatchers.Default) {
                logging("coroutine, level2, ${coroutineContextParsing(coroutineContext)}")

                launch {
                    logging("coroutine, level3, ${coroutineContextParsing(coroutineContext)}")
                }
            }
        }
    }
}