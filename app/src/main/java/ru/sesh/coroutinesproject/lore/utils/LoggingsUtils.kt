package ru.sesh.coroutinesproject.lore.utils

import android.util.Log
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

const val TAG = "COROUTINES_TAG"

private var formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())


fun logging(message: String) {
    Log.d(TAG, "${formatter.format(Date())} $message [${Thread.currentThread().name}]")
}

fun coroutineContextParsing(context: CoroutineContext): String =
    "Job = ${context[Job]}, Dispatcher = ${context[ContinuationInterceptor]}"