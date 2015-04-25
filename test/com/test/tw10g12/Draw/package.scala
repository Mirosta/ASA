package com.test.tw10g12

import com.test.tw10g12.Maths.inTolerance
import com.test.tw10g12.Test._
import com.tw10g12.Draw.Engine.Colour
import org.scalatest.matchers.{MatchResult, Matcher}
/**
 * Created by Tom on 25/04/2015.
 */
package object Draw
{
    case class MatchColourMatcher(r: Double, g: Double, b:Double, a: Double) extends Matcher[Colour] {

        def apply(colour: Colour): MatchResult = {
            val tolerance = 0.000001
            val resR: Boolean = inTolerance(colour.getR, r, tolerance)
            val resG = inTolerance(colour.getG, g, tolerance)
            val resB = inTolerance(colour.getB, b, tolerance)
            val resA = inTolerance(colour.getA, a, tolerance)

            val matchResults = List[MatchResult](MatchResult(resR,
                "Red value " + colour.getR + " should match " + r + " (+- " + tolerance + ")",
                "Red value " + colour.getR + " matched " + r + " (+- " + tolerance + ")" + ", but it shouldn't have"),
                MatchResult(resG,
                    "Green value " + colour.getG + " should match " + g + " (+- " + tolerance + ")",
                    "Green value " + colour.getG + " matched " + g + " (+- " + tolerance + ")" + ", but it shouldn't have"),
                MatchResult(resB,
                    "Blue value " + colour.getB + " should match " + b + " (+- " + tolerance + ")",
                    "Blue value " + colour.getB + " matched " + b + " (+- " + tolerance + ")" + ", but it shouldn't have"),
                MatchResult(resA,
                    "Alpha value " + colour.getA + " should match " + a + " (+- " + tolerance + ")",
                    "Alpha value " + colour.getA + " matched " + a + " (+- " + tolerance + ")" + ", but it shouldn't have"))
            combineResults(matchResults)
        }
    }

    def matchColour(r: Double, g: Double, b:Double) = matchColour(r, g, b, 1)
    def matchColour(r: Double, g: Double, b:Double, a: Double) = MatchColourMatcher(r, g, b, a)
    def matchColour(colour: Colour) = matchColour(colour.getR, colour.getG, colour.getB, colour.getA)
}
