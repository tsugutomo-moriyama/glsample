package com.example.gl11.csv

data class LineData(
    val lineCd:String,
    val lineName:String,
    val color:String
){
    companion object{
        fun load(list:List<String>):LineData = LineData(list[0], list[2], list[5])
    }
}