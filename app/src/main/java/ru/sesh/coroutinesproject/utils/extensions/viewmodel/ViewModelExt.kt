package ru.sesh.coroutinesproject.utils.extensions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.sesh.coroutinesproject.lore.utils.logging

/**
 * viewModelScope = CloseableCoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
 * * Dispatchers.Main.immediate - выполняет код последовательно,
 * если корутина запускается в том же потоке [checkImmediateParam]
 *
 * * Без использования корутин весь наш код в ViewModel обычно выполняется в Main потоке.
 * Это удобно и потокобезопасно.
 * Не нужна никакая синхронизация.
 * Если есть потребность выполнить код в другом потоке (сеть, БД), то мы обычно выносим это в репозитории, usecase и т.п.
 * А результат нам приходит в Main потоке
 *  Именно suspend функции и будут тем, кто пойдет выполнять свою работу в фоновый поток, не блокируя при этом Main поток,
 *  а лишь приостанавливая код корутины. А когда результат будет готов, выполнение корутины в Main потоке возобновится
 */
object ViewModelExt {

    /**
     * Проверяем Dispatchers.Main.immediate
     * Вывод будет последовательным: before launch after
     */
    private fun ViewModel.checkImmediateParam() {
        viewModelScope.launch {
            logging("before")
            launch {
                logging("launch")
            }
            logging("after")
        }
    }
}