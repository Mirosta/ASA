package com.test.tw10g12.Maths

import com.test.tw10g12.Test.UnitSpec
import com.tw10g12.Maths.{Vector3, BezierCurve}

/**
 * Created by Tom on 25/04/2015.
 */
class BezierTest extends UnitSpec
{
    "A BezierCurve" should "correctly calculate various positions" in
    {
        val curve = new BezierCurve(new Vector3(1,2,3), new Vector3(2, 2, 3), new Vector3(4, 6, 5), new Vector3(2, 6, 5))

        //x => vars = 1, 2, 2, 4
        //y => vars = 2, 2, 6, 6
        //z => vars = 3, 3, 5, 5
        //t = 0.25
            //x = 1.60938
            //y = 2.625
            //z = 3.31251
        //t = 0.5
            //x = 2.125
            //y = 4
            //z = 4
        //t = 0.75
            //x = 2.82813
            //y = 5.375
            //z = 4.68751
        curve.getPointAlongCurve(0) should be (curve.getStart)
        curve.getPointAlongCurve(1) should be (curve.getEnd)

        curve.getPointAlongCurve(0.25) should matchVector (1.60938,2.625,3.31251, 0.0005)
        curve.getPointAlongCurve(0.5) should matchVector (2.125,4,4, 0.0005)
        curve.getPointAlongCurve(0.75) should matchVector (2.82813,5.375,4.68751, 0.0005)
    }
}
