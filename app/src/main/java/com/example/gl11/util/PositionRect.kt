package com.example.gl11.util

import com.example.gl11.MainActivity
import com.example.gl11.entity.Position

class PositionRect(range:Long, latitude:Double, longitude:Double) {
    private val pos:Position = Position(latitude, longitude)
    val north: Position = pos.move(range, 0.0)
    val south: Position = pos.move(range, 180.0)
    val east: Position = pos.move(range, 90.0)
    val west: Position = pos.move(range, 270.0)

    fun isContain(latitude:Double, longitude:Double):Boolean =
        ((south.latitude < latitude &&  north.latitude > latitude)
                && (west.longitude < longitude && east.longitude > longitude))

    companion object{
        const val RELOAD_RANGE = 5000L
    }
}