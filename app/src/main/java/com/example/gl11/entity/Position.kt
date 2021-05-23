package com.example.gl11.entity

import kotlin.math.cos
import kotlin.math.sin

open class Position {
    open val latitude:Double
    open val longitude:Double
    open val lat:Long
    open val lon:Long

    constructor(latitude:Double, longitude:Double){
        this.latitude = latitude
        this.longitude = longitude
        this.lat = (latitude * COE).toLong()
        this.lon = (longitude * COE).toLong()
    }
    constructor(lat:Long, lon:Long){
        this.latitude = (lat.toDouble() / COE)
        this.longitude = (lon.toDouble() / COE)
        this.lat = lat
        this.lon = lon
    }

    companion object{
        const val COE = 10000000
        const val EARTH_RADIUS = 6378150
    }
    fun move(distance:Long, heading:Double):Position{
        val latitudeDistance = distance * cos(heading * Math.PI / 180)
        val earthCircle = 2 * Math.PI * EARTH_RADIUS
        val latitudePerMeter = 360 / earthCircle

        val latitudeDelta = latitudeDistance * latitudePerMeter
        val newLatitude = latitude + latitudeDelta

        val longitudeDistance = distance * sin(heading * Math.PI / 180)

        val earthRadiusAtLongitude = EARTH_RADIUS * cos(newLatitude * Math.PI / 180)
        val earthCircleAtLongitude = 2 * Math.PI * earthRadiusAtLongitude
        val longitudePerMeter = 360 / earthCircleAtLongitude

        val longitudeDelta = longitudeDistance * longitudePerMeter

        return Position(newLatitude, longitude + longitudeDelta)
    }
}