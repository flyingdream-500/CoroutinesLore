package ru.sesh.coroutinesproject.utils

import androidx.annotation.WorkerThread

object AsyncWork {

    /**
     * Listener для асинхронной работы
     */
    interface AsyncWorkListener<T> {
        fun event(value: T)
        fun error(error: Exception)
    }

    /**
     * Метод выполнения асинхронной работы
     *
     * @param asyncTime асинхронное время выполнения
     *
     * @return строковый результат в виде [Result]
     */
    @WorkerThread
    fun asyncWork(asyncTime: Long = 3_000, callback: AsyncWorkListener<Result<String?>>) {
        Thread.sleep(asyncTime)
        callback.event(Result.success("result value"))
    }
}