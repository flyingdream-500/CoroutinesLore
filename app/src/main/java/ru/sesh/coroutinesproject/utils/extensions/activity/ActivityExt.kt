package ru.sesh.coroutinesproject.utils.extensions.activity

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.sesh.coroutinesproject.lore.utils.logging

/**
 * lifecycleScope = CloseableCoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
 */
object ActivityExt {

    /**
     * Можно в любом месте Activity вызвать корутину, если это необходимо.
     * При закрытии экрана scope будет отменен.
     * При повороте он также будет отменен, но будет создан новый в новом Activity, и новая корутина будет запущена
     */
    fun AppCompatActivity.launchLifecycleScope() {
        lifecycleScope.launch {
            repeat(3) {
                delay(1_000)
                logging("data: $it")
            }
        }
    }
}