package ru.sesh.coroutinesproject.practice.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData


class MainViewModel : ViewModel() {

    /**
     * * Билдер liveData создает корутину и возвращает LiveData.
     * Внутри корутины мы используем метод emit, чтобы постить данные в получившуюся LiveData
     *
     * * Корутина стартует не в момент создания, а когда кто-либо подписывается на LiveData
     * * Если корутина нормально (без отмены по таймауту) заканчивает свою работу,
     * то она уже не будет перезапущена при следующей подписке на LiveData
     */
    val liveData = liveData<Int> {
        repeat(3) {
            emit(it)
        }
    }


}