package com.example.gl11.csv

data class StationData(
    val stationCd:String,
    val stationName:String,
    var lineName:String,
    var colors:String,
    val lon:Double,
    val lat:Double,
){
}