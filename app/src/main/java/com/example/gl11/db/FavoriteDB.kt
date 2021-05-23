package com.example.gl11.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import com.example.gl11.entity.Favorite
import com.example.gl11.entity.Position
import com.example.gl11.entity.Station
import com.example.gl11.gl.PositionUnit

class FavoriteDB(context: Context): DBHelper<Favorite>(context) {
    fun selectAll():List<Favorite>{
        return select(SELECT_ALL_SQL){
            Favorite.load(it)
        }
    }
    fun selectId(id:Long):List<Favorite>{
        return select(SELECT_ID, arrayOf(id.toString())){
            Favorite.load(it)
        }
    }
    fun selectRange(lat0:Long, lat1:Long, lon0:Long, lon1:Long):List<Favorite>{

        return select(
            SELECT_RANGE_SQL, arrayOf(
            lat0.toString(),
            lat1.toString(),
            lon0.toString(),
            lon1.toString())){

            Favorite.load(it)
        }
    }
    fun insert(unit:PositionUnit): Favorite{
        val p = Position(unit.latitude, unit.longitude)
        val tmp = Favorite(
            null, unit.id, unit.type.no, unit.text, unit.contents, p.lat, p.lon, Color.WHITE
        )
        val id = insert(tmp)
        return Favorite(
            id, unit.id, unit.type.no, unit.text, unit.contents, p.lat, p.lon, Color.WHITE
        )
    }
    fun insert(data:Favorite):Long{
        return insert(INSERT_SQL){
            it.apply {
                bindLong(1, data.parentId ?: 0)
                bindLong(2, data.type.toLong())
                bindString(3, data.title)
                bindString(4, data.contents)
                bindLong(5, data.lat)
                bindLong(6, data.lon)
                bindLong(7, data.markColor.toLong())
                bindLong(8, data.updateAt)
            }
        }
    }
    fun update(data: Favorite){
        updateOrDelete(UPDATE_SQL) {
            it.apply {
                bindLong(1, data.parentId ?: 0)
                bindLong(2, data.type.toLong())
                bindString(3, data.title)
                bindString(4, data.contents)
                bindLong(5, data.lat)
                bindLong(6, data.lon)
                bindLong(7, data.markColor.toLong())
                bindLong(8, data.updateAt)
                bindLong(9, data.id!!)
            }
        }
    }
    fun delete(id:Long) {
        updateOrDelete(DELETE_SQL) {
            it.apply {
                bindLong(1, id)
            }
        }
    }
    companion object{
        private const val TABLE_NAME = "FAVORITE"
        const val CREATE_TABLE_SQL =
            "CREATE TABLE $TABLE_NAME(" +
                "_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "PARENT_ID INTEGER, " +
                "TYPE INTEGER, " +
                "TITLE TEXT, " +
                "CONTENT TEXT, " +
                "LAT INTEGER, " +
                "LON INTEGER, " +
                "MARK INTEGER, " +
                "UPDATE_AT INTEGER NOT NULL)"

        private const val SELECT_ALL_SQL =
            "SELECT " +
                "_ID, " +
                "PARENT_ID, " +
                "TYPE, " +
                "TITLE, " +
                "CONTENT, " +
                "LAT, " +
                "LON, " +
                "MARK, " +
                "UPDATE_AT " +
            "FROM $TABLE_NAME"
        private const val SELECT_ID =
            "SELECT " +
                    "_ID, " +
                    "PARENT_ID, " +
                    "TYPE, " +
                    "TITLE, " +
                    "CONTENT, " +
                    "LAT, " +
                    "LON, " +
                    "MARK, " +
                    "UPDATE_AT " +
                    "FROM $TABLE_NAME " +
                    "WHERE " +
                    "_ID = ?;"

        private const val SELECT_RANGE_SQL =
            "SELECT " +
                "_ID, " +
                "PARENT_ID, " +
                "TYPE, " +
                "TITLE, " +
                "CONTENT, " +
                "LAT, " +
                "LON, " +
                "MARK, " +
                "UPDATE_AT " +
            "FROM $TABLE_NAME " +
            "WHERE " +
                "(LAT BETWEEN ? AND ?) AND" +
                "(LON BETWEEN ? AND ?);"

        private const val INSERT_SQL =
            "INSERT INTO " +
                "$TABLE_NAME (" +
                "PARENT_ID, " +
                "TYPE, " +
                "TITLE, " +
                "CONTENT, " +
                "LAT, " +
                "LON, " +
                "MARK, " +
                "UPDATE_AT) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?);"

        private const val UPDATE_SQL =
            "UPDATE $TABLE_NAME SET " +
                "PARENT_ID = ?, " +
                "TYPE = ?, " +
                "TITLE = ?, " +
                "CONTENT = ?, " +
                "LAT = ?, " +
                "LON = ?, " +
                "MARK = ?, " +
                "UPDATE_AT = ? " +
                "WHERE " +
                "_ID = ?"
        private const val DELETE_SQL =
            "DELETE FROM $TABLE_NAME " +
                    "WHERE " +
                    "_ID = ?"
    }
}