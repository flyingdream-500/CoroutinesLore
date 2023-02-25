package ru.sesh.coroutinesproject.lore.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.sesh.coroutinesproject.lore.utils.logging

/**
 * * Канал используется, как средство передачи данных между корутинами.
 * Т.е. он является ячейкой, куда одна корутина может поместить данные, а другая корутина - взять их оттуда
 *
 * * Канал - потокобезопасен и нам не надо самим возиться с блокировками и синхронностью
 *
 */
object CoroutineChannelsExt {
    private val channel = Channel<Int>()

    private val channelWithCapacity = Channel<Int>(2)

    // Метод send не ждет вызовов receive, он просто помещает в канал свое значение, заменяя предыдущее.
    // Метод receive получает значение, которое на момент вызова этого метода находится в канале
    private val channelConflated = Channel<Int>(Channel.Factory.CONFLATED)

    // Размер буфера в таком канале ограничен только количеством доступной памяти
    private val channelUnlimited = Channel<Int>(Channel.Factory.UNLIMITED)

    // Канал с размером буфера = 64 по умолчанию
    private val channelBuffered = Channel<Int>(Channel.Factory.BUFFERED)

    /**
     * * Если первая корутина пытается отправить данные методом send, но вторая корутина еще не вызвала receive,
     * то метод send приостановит первую корутину и будет ждать. Аналогично наоборот.
     * Если вторая корутина уже вызвала receive, чтобы получить данные, но первая корутина еще не отправила их,
     * то метод receive приостановит вторую корутину.
     * * Взаимная блокировка происходит по текущему сценарию:
     * извлекается continuation текущей корутины и пакуется в контейнер внутри канала
     */
    fun invokeChannel(scope: CoroutineScope) {
        scope.launch {
            logging("first coroutine, start")
            delay(1_000)
            channel.send(1)
            logging("first coroutine, end")
        }
        scope.launch {
            logging("second coroutine, start")
            val i = channel.receive()
            logging("second coroutine receive:$i, end")
        }
    }

    /**
     * Вторая корутина приостановиться навсегда, так как переменная i3 не будет получена
     */
    fun blockingInvokeChannel(scope: CoroutineScope) {
        scope.launch {
            channel.send(1)
            channel.send(2)
            channel.send(3)
        }
        scope.launch {
            val i = channel.receive()
            val i1 = channel.receive()
            val i2 = channel.receive()
            val i3 = channel.receive()
        }
    }

    /**
     * * Метод close - это явный сигнал о том, что данные больше передаваться не будут
     *
     * * Чтобы нам самим не возиться с отловом и обработкой этого исключения доступен цикл(for(in)),
     * который будет получать данные из канала, пока отправитель не закроет канал
     *
     * @exception ClosedReceiveChannelException если получатель попробует получить данные после закрытия канала
     */
    @Throws(ClosedReceiveChannelException::class)
    fun closeInvokeChannel(scope: CoroutineScope) {
        scope.launch {
            channel.send(1)
            channel.send(2)
            channel.send(3)
            channel.close()
        }
        scope.launch {
            for (item in channel) {
                logging("channel received items: $item")
            }
        }
    }

    /**
     * Поначалу первая корутина отправляет значения не дожидаясь вызовов receive для каждого значения
     * на стороне получателя.
     * Значения сохраняются в буфере канала. Когда он заполняется, то каждому send снова приходится ждать,
     * пока receive не возьмет одно значение и, тем самым, не освободит одно место в буфере.
     * И в конце получатель забирает данные уже только из буфера
     */
    fun invokeChannelWithCapacity(scope: CoroutineScope) {
        scope.launch {
            repeat(10) {
                delay(300)
                logging("send $it")
                channelWithCapacity.send(it)
            }
            channelWithCapacity.close()
        }
        scope.launch {
            for (item in channelWithCapacity) {
                logging("receive $item")
                delay(1_000)
            }
        }
    }

    /**
     * Отмена канала - то же самое, что и закрытие, только с очисткой буфера.
     * Вторая корутина значения 8 и 9 не получит
     */
    fun cancelInvokeChannel(scope: CoroutineScope) {
        scope.launch {
            repeat(10) {
                delay(300)
                logging("send $it")
                channelWithCapacity.send(it)
            }
            channelWithCapacity.cancel()
        }
        scope.launch {
            for (item in channelWithCapacity) {
                logging("receive $item")
                delay(1_000)
            }
        }
    }

    /**
     * * Билдер produce создает и запускает отдельную корутину. Также он создает канал,
     * доступный для получения данных из канала
     * * Под капотом у consumeEach просто try-catch обертка над for циклом.
     * Если эта обертка поймает ошибку, то она отменит канал
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun produceBuilder(scope: CoroutineScope) {
        val channel = scope.produce {
            repeat(3) {
                send(it)
            }
        }

        scope.launch {
            channel.consumeEach { item ->
                logging("data: $item")
            }
        }
    }

}