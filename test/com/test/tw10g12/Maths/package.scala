package com.test.tw10g12.Maths

import com.tw10g12.Maths.Vector3
import com.test.tw10g12.Test.combineResults
import org.scalatest.matchers.{MatchResult, Matcher}

/**
 * Created by Tom on 25/04/2015.
 */
package object Maths
{
     case class MatchVectorMatcher(x: Double, y: Double, z:Double) extends Matcher[Vector3] {
         def apply(vec: Vector3): MatchResult = {
             val resX: Boolean = vec.getX == x
             val resY = vec.getY == y
             val resZ = vec.getZ == z

             val matchResults = List[MatchResult](MatchResult(resX,
                 "X coordinate " + vec.getX + " should match " + x,
                 "X coordinate " + vec.getX + " matched " + x + ", but it shouldn't have"),
             MatchResult(resY,
                 "Y coordinate " + vec.getY + " should match " + y,
                 "Y coordinate " + vec.getY + " matched " + y + ", but it shouldn't have"),
             MatchResult(resZ,
                 "Z coordinate " + vec.getZ + " should match " + z,
                 "Z coordinate " + vec.getZ + " matched " + z + ", but it shouldn't have"))
             combineResults(matchResults)
         }
     }

     def matchVector(x: Double, y: Double, z:Double) = MatchVectorMatcher(x, y, z)
 }
