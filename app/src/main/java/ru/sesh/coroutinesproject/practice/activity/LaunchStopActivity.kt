package ru.sesh.coroutinesproject.practice.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.sesh.coroutinesproject.lore.coroutine.ScopeExt
import ru.sesh.coroutinesproject.databinding.ActivityLaunchStopBinding
import ru.sesh.coroutinesproject.lore.utils.logging
import java.util.concurrent.TimeUnit

class LaunchStopActivity : AppCompatActivity() {

    private val binding: ActivityLaunchStopBinding by lazy {
        ActivityLaunchStopBinding.inflate(layoutInflater)
    }

    private val scope = ScopeExt.scope

    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupViews()
    }

    private fun setupViews() {
        with(binding) {
            launchButton.setOnClickListener {
                launch()
            }
            cancelButton.setOnClickListener {
                cancel()
            }
        }
    }

    private fun launch() {
        logging("launch, start")
        job = scope.launch {
            logging("coroutine, start")
            repeat(5) {
                TimeUnit.SECONDS.sleep(1)
                logging("coroutine, $it")
            }
            logging("coroutine, end")
        }
        logging("launch, end")
    }

    private fun cancel() {
        logging("cancel")
        job.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        logging("onDestroy")
        scope.cancel()
    }
}