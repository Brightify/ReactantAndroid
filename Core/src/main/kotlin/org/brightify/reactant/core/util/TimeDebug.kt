package org.brightify.reactant.core.util

import android.util.Log
import java.lang.Math.pow
import java.util.Stack

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
private data class Measurement(var called: Int = 0, var time: Long = 0)

private val timeData = HashMap<String, Measurement>()

private var currentMeasures = Stack<String>()

fun <T> measure(name: String, code: () -> T): T {
    val begin = System.nanoTime()
    currentMeasures.push(name)
    val returnValue = code()
    currentMeasures.pop()

    val time = System.nanoTime() - begin
    if (timeData[name] == null) {
        timeData[name] = Measurement()
    }
    timeData[name]?.called = (timeData[name]?.called ?: 0) + 1
    timeData[name]?.time = (timeData[name]?.time ?: 0) + time
    currentMeasures.lastOrNull()?.let {
        if (timeData[it] == null) {
            timeData[it] = Measurement()
        }
//        timeData[it]?.time = (timeData[it]?.time ?: 0) - time
    }

    return returnValue
//    return code()
}

fun printTimes() {
    timeData.entries.sortedByDescending { it.value.time }.forEach {
        Log.d("Time measure - ${it.key}", "Called ${it.value.called} times, took: " + String.format("%.4f", it.value.time / pow(10.0, 9.0)))
    }
    timeData.clear()
}