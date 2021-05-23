package com.example.gl11.entity

import android.database.Cursor

data class Station(
    val id:Long? = null,
    val title:String,
    val contents:String,
    override val lat:Long,
    override val lon:Long,
    val colors:String,
    val updateAt: Long = System.currentTimeMillis()
):Position(lat, lon){
    companion object{
        fun load(c: Cursor):Station{
            return Station(
                c.getLong(0),
                c.getString(1),
                c.getString(2),
                c.getLong(3),
                c.getLong(4),
                c.getString(5),
                c.getLong(6)
            )
        }
    }
}