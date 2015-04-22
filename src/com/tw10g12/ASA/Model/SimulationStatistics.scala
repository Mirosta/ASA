package com.tw10g12.ASA.Model

import com.tw10g12.IO.JSONSerializable
import org.json.JSONObject

import scala.collection.mutable

/**
 * Created by Tom on 20/04/2015.
 */
class SimulationStatistics(startingMap: mutable.Map[String, Double]) extends JSONSerializable
{
    def this() = this(mutable.Map())

    val metrics: mutable.Map[String, Double] = startingMap

    def increaseMetric(metric: String, amount: Double): Unit =
    {
        if(!metrics.contains(metric)) metrics.put(metric, 0)
        metrics(metric) = metrics(metric) + amount
    }

    def decreaseMetric(metric: String, amount: Double) = increaseMetric(metric, -amount)

    def incrementMetric(metric: String) = increaseMetric(metric, 1)

    def decrementMetric(metric: String) = increaseMetric(metric, -1)

    def updateMetric(metric: String, value: Double): Unit =
    {
        metrics.put(metric, value)
    }

    def hasMetric(metric: String): Boolean = metrics.contains(metric)

    def getMetric(metric: String): Double = if(metrics.contains(metric)) metrics(metric) else 0

    override def clone(): SimulationStatistics =
    {
        return new SimulationStatistics(metrics.clone())
    }

    override def toJSON(obj: JSONObject): JSONObject =
    {
        val obj = new JSONObject()
        metrics.map(pair => obj.put(pair._1, pair._2))
        return obj
    }
}
