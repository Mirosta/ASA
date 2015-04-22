package com.tw10g12.ASA.GUI

import java.awt.{GridBagLayout, Insets}
import javax.swing.{JLabel, JPanel}

import com.tw10g12.ASA.Model.{Tile, SimulationStatistics}
import com.tw10g12.ASA.Util
import scala.collection.mutable

/**
 * Created by Tom on 21/04/2015.
 */

class StatisticsPanel(extraStats: Vector[String], extraStatsNames: Map[String, String]) extends JPanel(new GridBagLayout())
{
    var totalTilesLabel: JLabel = null
    var tileStatsPanel: StatsPanel[Tile] = null
    var extraStatsPanel: StatsPanel[String] = null

    def setup(): Unit =
    {
        totalTilesLabel = new JLabel("Total Tiles: ")
        tileStatsPanel = new StatsPanel[Tile](tileToMetricAndName, populateLabel)
        extraStatsPanel = new StatsPanel[String]((str: String) => (str, extraStatsNames(str)), populateLabel)

        Util.addToGridBag(totalTilesLabel, this, 1, 1, 0, 0)
        Util.addToGridBag(tileStatsPanel, this, 2, 1, 0, 0)
        Util.addToGridBag(extraStatsPanel, this, 1, 2, 2, 1, 0, 0)
    }

    def tileToMetricAndName(tile: Tile): (String, String) = ("tile_" + tile.typeID, if(tile.typeID == -1) "Seed Tile" else "Tile " + (tile.typeID + 1))

    def update(stats: SimulationStatistics, tileTypes: (Tile, List[Tile])): Unit =
    {
        val statsInUse: Vector[(String, Int)] = (0 until extraStats.size).filter(index => stats.hasMetric(extraStats(index))).map(index => (extraStats(index), index)).toVector

        populateLabel(stats, totalTilesLabel, "totalTiles", "Total Tiles")
        extraStatsPanel.update(stats, statsInUse.map(pair => pair._1))
        tileStatsPanel.update(stats, (tileTypes._1 :: tileTypes._2).toVector)
    }

    def populateLabel(stats: SimulationStatistics, label: JLabel, metric: String, niceName: String): Unit =
    {
        label.setText(niceName + ": " + stats.getMetric(metric).toInt)
    }
}

class StatsPanel[T] (getStat: T => (String, String), populateLabel: (SimulationStatistics, JLabel, String, String) => Unit) extends JPanel(new GridBagLayout())
{
    def this(getStat: Function[T, (String, String)]) = this(getStat, (stats: SimulationStatistics, label: JLabel, metric: String, niceName: String) => label.setText(niceName + ": " + stats.getMetric(metric)))
    def this() = this((d: T) => (d.toString(), d.toString()))

    val labels: mutable.ArrayBuffer[JLabel] = mutable.ArrayBuffer()
    var labelsX = 1

    def createNewLabel(): JLabel =
    {
        val newLabel = new JLabel("")

        Util.addToGridBag(newLabel, this, labelsX, 1, 0, 1, new Insets(5, 5, 5, 5))
        labelsX += 1

        return newLabel
    }

    def update(stats: SimulationStatistics, data: Vector[T]): Unit =
    {
        if(labels.size > data.size)
        {
            labels.remove(data.size, labels.size - data.size)
            labelsX = data.size + 1
        }
        else if(labels.size < data.size) (0 until data.size - labels.size).map(_ => labels.append(createNewLabel()))

        (0 until data.size).map(index => { val metricAndName = getStat(data(index)); populateLabel(stats, labels(index), metricAndName._1, metricAndName._2) })
    }
}
