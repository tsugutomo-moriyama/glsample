package com.example.gl11.entity

import android.database.Cursor

data class Favorite(
    val id:Long? = null,
    val parentId:Long? = null,
    val type:Int,
    val title:String,
    val contents:String,
    override val lat:Long,
    override val lon:Long,
    val markColor:Int,
    val updateAt: Long = System.currentTimeMillis()
):Position(lat, lon){
    companion object{
        fun load(c: Cursor):Favorite{
            return Favorite(
                c.getLong(0),
                c.getLong(1),
                c.getInt(2),
                c.getString(3),
                c.getString(4),
                c.getLong(5),
                c.getLong(6),
                c.getInt(7),
                c.getLong(8)
            )
        }
    }
}