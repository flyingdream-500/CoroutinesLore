package ru.sesh.coroutinesproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.sesh.coroutinesproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupViews()
    }

    private fun setupViews() {
        binding.launchCancelScreen.setOnClickListener {
            startActivity(Intent(this, LaunchStopActivity::class.java))
        }
    }
}