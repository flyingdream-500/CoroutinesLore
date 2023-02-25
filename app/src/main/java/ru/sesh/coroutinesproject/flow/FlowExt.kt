package ru.sesh.coroutinesproject.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.sesh.coroutinesproject.utils.logging
import ru.sesh.coroutinesproject.R.raw as Raw

/**
 * * Flow - это аналог Observable/Flowable
 * * Это некая фабрика, которая умеет производить данные.
 * При создании она ничего не делает. Но как только мы ее запустим,
 * она начнет работу и будет постить результаты в колбэк, который мы ей предоставим
 * * Разница между Channel и Flow:
 * - Канал - это просто потокобезопасный инструмент для передачи данных между корутинами.
 * Он не создает ничего. Только передает.
 * Т.е. должны существовать отправитель и получатель.
 * Они работают в своих корутинах независимо друг от друга, используя канал для обмена данными
 * - Flow - это генератор данных. В этом случае нет явного отправителя. Вместо него есть создатель.
 * Он создает Flow и дает его получателю.
 * Получатель запускает Flow в своей корутине и, можно сказать, сам же становится отправителем
 *
 * * Flow является холодным источником данных.
 * Он для каждого получателя будет генерировать данные заново
 */
object FlowExt {

    // Билдер asFlow() под капотом в блоке flow в цикле эмиттит значения
    val numbersFlow = (1..5).asFlow()

    // То же, что и asFlow()
    val lettersFlow = flowOf("a", "b", "c")

    /**
     * * Оператор flow создает объект Flow. Результаты он будет отправлять в свой стандартный метод emit.
     * Этот метод будет перенаправлять результаты в колбэк
     * * Функция, которая возвращает Flow, не обязана быть suspend
     */
    fun baseFlow(): Flow<Int> =
        flow {
            repeat(5) {
                delay(1_000)
                emit(it)
            }
        }

    private suspend fun getNumbers(): List<Int> {
        delay(1_000)
        return (1..5).toList()
    }

    /**
     * * Запускаем Flow и передаем ему коллбэк
     *
     * * Когда мы запускаем метод collect, Flow берет свой блок flow и запускает его, тем самым стартуя работу.
     * А в методе emit он включает перенаправление в блок collect
     *
     * * Таким образом:
     * 1. Метод collect() запускает блок flow, чтобы начать работу.
     * 2. Блок flow запускает блок collect, чтобы отправлять данные.
     *
     * @see [Raw.flow]
     *
     */
    fun invokeFlow(scope: CoroutineScope, flow: Flow<Int>) {
        scope.launch {
            flow.collect { item ->
                logging("collected item from flow: $item")
            }
        }
    }

    /**
     * Flow расширяет возможности suspend функций, позволяя нам получать последовательность данных в suspend режиме
     */
    fun flowAndSuspendFunction(scope: CoroutineScope, flow: Flow<Int>) {
        scope.launch {
            flow.collect { item ->
                logging("collected item from flow: $item")
            }
        }
        scope.launch {
            val items = getNumbers()
            logging("collected item: $items")
        }
    }

    /**
     * Метод отмены корутины
     * * Вызывая метод cancel() мы отменяем текущую корутину, в которой выполняется Flow
     * * Если flow создан билдером (например asFlow), то необходимо добавлять оператор cancellable()
     */
    fun cancelFlow(scope: CoroutineScope) {
        scope.launch {
            (1..5).toList().asFlow().cancellable()
                .collect {
                    if (it == 3) cancel()
                    logging("collect $it")
                }
        }
    }


}