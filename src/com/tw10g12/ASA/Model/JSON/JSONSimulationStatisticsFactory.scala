package com.tw10g12.ASA.Model.JSON

import com.tw10g12.ASA.Model.SimulationStatistics
import org.json.JSONObject
import scala.collection.mutable

/**
 * Created by Tom on 21/04/2015.
 */
object JSONSimulationStatisticsFactory
{
    def createSimulationStatistics(serialized: JSONObject): SimulationStatistics =
    {
        val metricsKeys = serialized.keySet().toArray.map(metric => metric.asInstanceOf[String])
        val metrics: mutable.Map[String, Double] = collection.mutable.Map() ++ metricsKeys.map(metric => (metric, serialized.getDouble(metric)))
        return new SimulationStatistics(metrics)
    }
}
