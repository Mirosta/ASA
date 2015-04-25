package com.test.tw10g12.ASA.JSON

import com.test.tw10g12.Test.UnitSpec
import com.tw10g12.ASA.Model.ATAM.{ATAMGlue, ATAMTile}
import com.tw10g12.ASA.Model.JSON.{JSONTileFactory, JSONGlueFactory}
import com.tw10g12.ASA.Model.SMTAM.{SMTAMTile, SMTAMGlue}
import com.tw10g12.Draw.Engine.Colour
import com.tw10g12.Maths.Vector3
import org.json.JSONObject
import com.test.tw10g12.Draw.matchColour
/**
 * Created by Tom on 25/04/2015.
 */
class JSONTest extends UnitSpec
{
    "A Glue" should "correctly convert to and from JSON" in
    {
        val atamGlue = new ATAMGlue("A", 1, 2, null, false)
        val smtamGlue = new SMTAMGlue("B", 2, 0, null, true)

        val outputATAMGlue = JSONGlueFactory.createGlue(atamGlue.toJSON(new JSONObject()))
        val outputSMTAMGlue = JSONGlueFactory.createGlue(smtamGlue.toJSON(new JSONObject()))

        atamGlue.label should be (outputATAMGlue.label)
        atamGlue.strength should be (outputATAMGlue.strength)
        atamGlue.orientation should be (outputATAMGlue.orientation)
        atamGlue.isBound should be (outputATAMGlue.isBound)
    }

    "A Tile" should "correctly convert to and from JSON" in
    {
        val atamTile = new ATAMTile(Vector(null, null, null, null), Vector(Colour.White), new Vector3(1,2,3), 12)
        val smtamTile = new SMTAMTile(Vector(null, null, null, null), Vector(new Colour(0.1f, 0.2f, 0.3f)), new Vector3(-3,-2,-1), -1)

        val outputATAMTile = JSONTileFactory.createTile(atamTile.toJSON(new JSONObject()))
        val outputSMTAMTile = JSONTileFactory.createTile(smtamTile.toJSON(new JSONObject()))

        atamTile.getColour should matchColour(outputATAMTile.getColour)
        atamTile.glues.map(glue => glue != null).size should be (outputATAMTile.glues.map(glue => glue != null).size)
        atamTile.glues.map(glue => glue == null).size should be (outputATAMTile.glues.map(glue => glue == null).size)
        atamTile.getPosition should be (outputATAMTile.getPosition)
        atamTile.typeID should be (outputATAMTile.typeID)

        smtamTile.getColour should matchColour(outputSMTAMTile.getColour)
        smtamTile.glues.map(glue => glue != null).size should be (outputSMTAMTile.glues.map(glue => glue != null).size)
        smtamTile.glues.map(glue => glue == null).size should be (outputSMTAMTile.glues.map(glue => glue == null).size)
        smtamTile.getPosition should be (outputSMTAMTile.getPosition)
        smtamTile.typeID should be (outputSMTAMTile.typeID)
    }
}
