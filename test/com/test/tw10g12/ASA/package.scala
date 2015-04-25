package com.test.tw10g12

import com.tw10g12.Maths.Vector3
import org.scalatest.matchers.{MatchResult, Matcher}
import com.test.tw10g12.Test.combineResults
/**
 * Created by Tom on 25/04/2015.
 */
package object ASA
{
    case class MatchAdjacencyMatcher(size: Int, vectorTileTypes: Map[Vector3, Set[Int]]) extends Matcher[Map[Vector3, List[(Int, Double)]]] {

        def apply(adjacencies: Map[Vector3, List[(Int, Double)]]): MatchResult = {
            val matchSize = adjacencies.size == size
            val sizeResult: MatchResult = MatchResult(matchSize,
                "Adjacencies size " + adjacencies.size + " should match " + size,
                "Adjacencies size " + adjacencies.size + " matched " + size + ", but it shouldn't have")

            val results = vectorTileTypes.flatMap(pair =>
            {
                val adjCon = MatchResult(adjacencies.contains(pair._1),
                    "Adjacencies should contain " + pair._1.toString,
                    "Adjacencies contained " + pair._1 + " but it shouldn't have")
                if(adjacencies.contains(pair._1))
                {
                    val sameSize = MatchResult(adjacencies(pair._1).size == pair._2.size,
                        "The adjacencies for " + pair._1.toString + " should be of size " + pair._2.size + " but it was " + adjacencies(pair._1).size,
                        "The adjacencies for " + pair._1.toString + " shouldn't be of size " + pair._2.size + " but it was")

                    val adjChecks = adjacencies(pair._1).map(innerPair =>
                    {
                        val containsTileType = pair._2.contains(innerPair._1)
                        MatchResult(containsTileType,
                            "The adjacencies for " + pair._1.toString + " shouldn't contain the tile type " + innerPair._1,
                            "The adjacencies for " + pair._1.toString + " should contain the tile type " + innerPair._1)
                    })
                    List(adjCon, sameSize) ++ adjChecks
                }
                else List(adjCon)
            }).toList

            combineResults(sizeResult :: results)
        }
    }

    def matchAdjacencies(size: Int, vectorTileTypes: Map[Vector3, Set[Int]]) = MatchAdjacencyMatcher(size, vectorTileTypes)
}
