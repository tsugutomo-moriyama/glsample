package com.example.gl11.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.gl11.entity.Favorite
import com.example.gl11.entity.Station

class StationDB(context: Context): DBHelper<Station>(context) {
    fun selectAll():List<Station>{
        return select(SELECT_ALL_SQL){
            Station.load(it)
        }
    }
    fun selectId(id:Long):List<Station>{
        return select(SELECT_ID, arrayOf(id.toString())){
            Station.load(it)
        }
    }
    fun selectRange(lat0:Long, lat1:Long, lon0:Long, lon1:Long):List<Station>{
        return select(SELECT_RANGE_SQL, arrayOf(
            lat0.toString(),
            lat1.toString(),
            lon0.toString(),
            lon1.toString())){

            Station.load(it)
        }
    }
    fun insert(data:Station){
        insert(INSERT_SQL){
            it.apply {
                bindString(1, data.title)
                bindString(2, data.contents)
                bindLong(3, data.lat)
                bindLong(4, data.lon)
                bindString(5, data.colors)
                bindLong(6, data.updateAt)
            }
        }
    }
    companion object{
        private const val TABLE_NAME = "STATION"
        const val CREATE_TABLE_SQL =
            "CREATE TABLE $TABLE_NAME(" +
                "_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TITLE TEXT, " +
                "CONTENT TEXT, " +
                "LAT INTEGER, " +
                "LON INTEGER, " +
                "COLORS TEXT, " +
                "UPDATE_AT INTEGER NOT NULL)"
        private const val SELECT_ALL_SQL =
            "SELECT " +
                "_ID, " +
                "TITLE, " +
                "CONTENT, " +
                "LAT, " +
                "LON, " +
                "COLORS, " +
                "UPDATE_AT " +
                "FROM $TABLE_NAME"

        private const val SELECT_ID =
            "SELECT " +
                    "_ID, " +
                    "TITLE, " +
                    "CONTENT, " +
                    "LAT, " +
                    "LON, " +
                    "COLORS, " +
                    "UPDATE_AT " +
                    "FROM $TABLE_NAME " +
                    "WHERE " +
                    "_ID = ?;"

        private const val SELECT_RANGE_SQL =
            "SELECT " +
                "_ID, " +
                "TITLE, " +
                "CONTENT, " +
                "LAT, " +
                "LON, " +
                "COLORS, " +
                "UPDATE_AT " +
                "FROM $TABLE_NAME " +
                "WHERE " +
                "(LAT BETWEEN ? AND ?) AND" +
                "(LON BETWEEN ? AND ?);"

        private const val INSERT_SQL =
            "INSERT INTO " +
                "$TABLE_NAME (" +
                "TITLE, " +
                "CONTENT, " +
                "LAT, " +
                "LON, " +
                "COLORS, " +
                "UPDATE_AT) " +
                "VALUES (?, ?, ?, ?, ?, ?);"
    }
}