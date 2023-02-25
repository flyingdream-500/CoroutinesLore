package ru.sesh.coroutinesproject.lore.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.sesh.coroutinesproject.lore.utils.logging

/**
 * * Основные операторы для Flow бывают двух типов: Intermediate и Terminal.
 * Первые добавляют в Flow различные преобразования данных, но не запускают его.
 * А вторые - запускают Flow и работают с результатом его работы
 *
 * * Примеры Intermediate операторов - map, filter, take, zip, combine, withIndex, scan, debounce, distinctUntilChanged, drop, sample
 *
 * * Важный момент. Когда вы используете map, filter или создаете свой оператор, не забывайте,
 * что используемый в них код преобразования данных будет выполнен в suspend функции collect, которая начнет всю работу Flow.
 * Это значит что ваш код не должен быть тяжелым или блокировать поток.
 * Не забывайте, что suspend функция может быть запущена и в main потоке.
 *
 * * Примеры Terminal операторов - collect, single, reduce, count, first, toList, toSet, fold
 */
object FlowOperatorsExt {

    private fun sampleOfMapOperatorInFlow(): Flow<Int> =
        flowOf(1, 2, 3).map { it * 10 }

    /**
     * Пример вызова терминального оператора
     *
     * Т.к. Terminal операторам приходится вызывать collect,
     * все они являются suspend функциями и вызываться могут только в корутине (или в другой suspend функции)
     */
    private suspend fun sampleOfCountOperatorInFlow(): Int =
        flowOf("a", "b", "c").count()

    /**
     * Кастомный промежуточный оператор для преобразования значений внутри flow
     */
    private fun Flow<String>.toUpperString(): Flow<String> =
        flow {
            collect {
                emit(it.uppercase())
            }
        }

    /**
     * Кастомный терминальный оператор для flow
     *
     * Обязательно suspend, так как вызывается метод collect
     */
    private suspend fun Flow<String>.join(): String {
        val stringBuilder = StringBuilder()
        collect {
            stringBuilder.append(it).append(",")
        }
        return stringBuilder.toString()
    }

    /**
     * Пример оператора transform, который из Flow<String> делает Flow<Char>
     */
    private fun Flow<String>.transformOperator(): Flow<Char> =
        transform {
            emit(it.first())
        }

    /**
     * ChannelFlow = Flow + Channel(ваш кэп)
     *
     * В итоге мы получили Flow, внутри которого будут созданы корутина и канал.
     * Когда мы запустим его методом collect(), данные будут создаваться в отдельной корутине
     * и с помощью канала вернутся в нашу текущую корутину.
     * * Оператор buffer используется, чтобы перейти на использование канала с указанным режимом/размером буфера (capacity)
     */
    private fun getChannelFlow(): Flow<Int> =
        channelFlow {
            repeat(3) {
                launch {
                    logging("value: $it")
                    delay(1_000)
                    send(it)
                }
            }
        }
            .buffer(1)
            .flowOn(Dispatchers.IO)

    fun startChannelFlow(scope: CoroutineScope) {
        scope.launch(Dispatchers.Main) {
            logging("coroutine, start")
            getChannelFlow().collect {
                logging("value from channelFlow: $it")
            }
            logging("coroutine, end")
        }
    }

    /**
     * * Мы используем flowOn, когда хотим сообщить Flow, какой контекст ему использовать для кода создания данных
     *
     *  * Оператор flowOn влияет на все предыдущие операторы в цепочке, пока не встретит другой flowOn
     */
    private fun flowOnOperator() =
        flow<Int> {
            // ... IO thread
        }
            .map {
                // ... IO thread
            }
            .flowOn(Dispatchers.IO)
            .onEach {
                // ... Main thread
            }
            .flowOn(Dispatchers.Main)

    /**
     * Оператор produceIn конвертирует flow в канал
     *
     * - В produceIn надо передавать scope, который будет использован для запуска produce
     * - Получившийся канал (в отличие от обычного Flow) не является Cold. Т.е. он начнет работу сразу же, как мы его создадим
     * - Если нам надо указать свой контекст или параметры буфера, мы используем операторы flowOn и buffer
     */
    private fun produceInOperator(scope: CoroutineScope): ReceiveChannel<Int> =
        flow {
            repeat(3) {
                delay(1_000)
                emit(it)
            }
        }
            .buffer(3)
            .flowOn(Dispatchers.IO)
            .produceIn(scope)

    fun startChannel(scope: CoroutineScope) {
        val channel = produceInOperator(scope)
        scope.launch {
            delay(3_500)
            channel.consumeEach {
                logging("data: $it")
            }
        }
    }


}