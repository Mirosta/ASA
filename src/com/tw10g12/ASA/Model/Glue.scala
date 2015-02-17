package com.tw10g12.ASA.Model

/**
 * Created by Tom on 27/10/2014.
 */
abstract class Glue(val label: String, val strength: Int, val orientation: Int, val parent: Tile, val isBound: Boolean)
{
    def clone(newParent: Tile, newOrientation: Int): Glue
    def clone(newIsBound: Boolean): Glue
    def canBind(otherGlue: Glue): Boolean
}
