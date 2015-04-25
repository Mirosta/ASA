package com.test.tw10g12

import com.tw10g12.Maths.Vector3
import com.test.tw10g12.Test.combineResults
import org.scalatest.matchers.{MatchResult, Matcher}

/**
 * Created by Tom on 25/04/2015.
 */
package object Maths
{
     case class MatchVectorMatcher(x: Double, y: Double, z:Double, tolerance: Double) extends Matcher[Vector3] {

         def apply(vec: Vector3): MatchResult = {
             val resX: Boolean = vec.getX - tolerance <= x && vec.getX + tolerance >= x
             val resY = vec.getY - tolerance <= y && vec.getY + tolerance >= y
             val resZ = vec.getZ - tolerance <= z && vec.getZ + tolerance >= z

             val matchResults = List[MatchResult](MatchResult(resX,
                 "X coordinate " + vec.getX + " should match " + x + " (+- " + tolerance + ")",
                 "X coordinate " + vec.getX + " matched " + x + " (+- " + tolerance + ")" + ", but it shouldn't have"),
             MatchResult(resY,
                 "Y coordinate " + vec.getY + " should match " + y + " (+- " + tolerance + ")",
                 "Y coordinate " + vec.getY + " matched " + y + " (+- " + tolerance + ")" + ", but it shouldn't have"),
             MatchResult(resZ,
                 "Z coordinate " + vec.getZ + " should match " + z + " (+- " + tolerance + ")",
                 "Z coordinate " + vec.getZ + " matched " + z + " (+- " + tolerance + ")" + ", but it shouldn't have"))
             combineResults(matchResults)
         }
     }

     def matchVector(x: Double, y: Double, z:Double) = MatchVectorMatcher(x, y, z, 0)
    def matchVector(x: Double, y: Double, z:Double, tolerance: Double) = MatchVectorMatcher(x, y, z, tolerance)
 }
