package com.tw10g12.ASA.Debug

import java.util.Date

import collection.mutable.MutableList
/**
 * Created by Tom on 10/11/2014.
 */
object Profiler
{
    var started: Boolean = false
    var startTime: Long = 0
    val times: MutableList[(Long, String)] = new MutableList[(Long, String)]()

    def start(): Unit =
    {
        startTime = new Date().getTime
        synchronized
        {
            times.clear()
        }
        started = true
    }

    def profile(comment: String): Unit =
    {
        if(!started) return
        synchronized
        {
            times.+=((new Date().getTime, comment));
        }
    }

    def print(): Unit =
    {
        var lastTime = startTime
        synchronized
        {
            times.map(pair => {println(pair._2 + ": " + (pair._1 - lastTime)); lastTime = pair._1})
        }
        started = false
    }
}
