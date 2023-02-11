package ru.sesh.coroutinesproject.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

const val TAG = "COROUTINES_TAG"

private var formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())


fun logging(message: String) {
    Log.d(TAG, "${formatter.format(Date())} $message [${Thread.currentThread().name}]")
}