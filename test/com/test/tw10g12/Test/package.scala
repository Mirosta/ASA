package com.test.tw10g12

import org.scalatest.matchers.MatchResult

/**
 * Created by Tom on 25/04/2015.
 */
package object Test
{
    def combineResults(matchResults: Seq[MatchResult]): MatchResult =
    {
        MatchResult(!matchResults.exists(result => !result.matches),
        matchResults.filter(result => !result.matches).map(result => result.failureMessage).mkString(" and "),
        matchResults.filter(result => result.matches).map(result => result.negatedFailureMessage).mkString(" and "))
    }
}
