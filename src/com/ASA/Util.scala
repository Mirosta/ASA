package com.ASA

import java.util.concurrent.ThreadPoolExecutor

import com.sun.javaws.exceptions.InvalidArgumentException
import com.tw10g12.Maths.Vector3

import scala.actors.threadpool.Executors

/**
 * Created by Tom on 20/10/2014.
 */
object Util
{
    lazy val threadPool = Executors.newFixedThreadPool(5)

    def toRunnable(fun : () => Unit) : Runnable =
    {
        return new Runnable
        {
            override def run(): Unit =
            {
                fun()
            }
        }

    }

    def orientationToVector(orientation: Int): Vector3 =
    {
        orientation match
        {
            case 0 => return new Vector3(0, 1, 0)//North
            case 1 => return new Vector3(-1, 0, 0) //East
            case 2 => return new Vector3(0, -1, 0) //South
            case 3 => return new Vector3(1, 0, 0)//West
            case 4 => return new Vector3(0, 0, -1)//Up
            case 5 => return new Vector3(0, 0, 1) //Down
        }
        throw new IllegalArgumentException("Unknown orientation " + orientation)
    }

    def vectorToOrientation(vector: Vector3): Int =
    {
        val vectorMap = Map(new Vector3(0, 1, 0) -> 0, new Vector3(-1, 0, 0) -> 1, new Vector3(0, -1, 0) -> 2, new Vector3(1, 0, 0) -> 3, new Vector3(0, 0, -1) -> 4, new Vector3(0, 0, 1) -> 5);
        if(!vectorMap.contains(vector)) throw new IllegalArgumentException("Unknown orientation vector " + vector)
        return vectorMap(vector)
    }

    def oppositeOrientation(orientation: Int): Int =
    {
        orientation match
        {
            case 0 => return 2 //North
            case 1 => return 3 //East
            case 2 => return 0 //South
            case 3 => return 1 //West
            case 4 => return 5 //Up
            case 5 => return 4 //Down
        }
        throw new IllegalArgumentException("Unknown orientation " + orientation)
    }
}