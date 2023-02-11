package ru.sesh.coroutinesproject.coroutine

import android.util.Log
import kotlinx.coroutines.*
import ru.sesh.coroutinesproject.utils.TAG
import ru.sesh.coroutinesproject.utils.logging
import java.util.concurrent.TimeUnit

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