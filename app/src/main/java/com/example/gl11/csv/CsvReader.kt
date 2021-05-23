package com.example.gl11.csv

import android.content.Context
import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class CsvReader<T> {
    fun reader(context: Context, fileName:String, fn:(List<String>)->Unit) {
        val assetManager: AssetManager = context.resources.assets
        try {
            val inputStream: InputStream = assetManager.open(fileName)
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferReader = BufferedReader(inputStreamReader)
            var line: String? = null
            while (bufferReader.readLine().also { line = it } != null) {
                line?.let{
                    val rowData = it.split(",")
                    fn(rowData)
                }
            }
            bufferReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}