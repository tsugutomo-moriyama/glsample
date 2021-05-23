package com.example.gl11.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteStatement
import android.util.Log

abstract class DBHelper<T>(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(FavoriteDB.CREATE_TABLE_SQL)
        db?.execSQL(StationDB.CREATE_TABLE_SQL)
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
    protected fun select(sql:String, args:Array<String>? = null, fn:(c: Cursor)->T):MutableList<T>{
        val resultList = mutableListOf<T>()
        val rdb = this.readableDatabase
        try{
            val cursor = rdb.rawQuery(sql, args)
            cursor.use {
                while (it.moveToNext()){
                    resultList.add(fn(it))
                }
            }

        } catch (ex: Exception) {
            Log.e("select", ex.toString())
        } finally {
            rdb.close()
        }
        return resultList
    }

    protected fun insert(sql:String, fn: (SQLiteStatement)->SQLiteStatement):Long{
        var insertedId:Long = -1
        val wdb = this.writableDatabase
        try{
            insertedId = fn(wdb.compileStatement(sql)).executeInsert()
        } catch (ex: Exception) {
            Log.e("insertData", ex.toString())
        } finally {
            wdb.close()
        }
        return insertedId
    }

    protected fun updateOrDelete(sql:String, fn: (SQLiteStatement)->SQLiteStatement){
        val wdb = this.writableDatabase
        try{
            fn(wdb.compileStatement(sql)).executeUpdateDelete()
        } catch (ex: Exception) {
            Log.e("updateData", ex.toString())
        } finally {
            wdb.close()
        }
    }
    companion object{
        private const val DATABASE_NAME = "LOCALS"
        private const val DATABASE_VERSION = 1
    }

}