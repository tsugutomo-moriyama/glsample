package com.example.gl11

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.gl11.csv.CsvReader
import com.example.gl11.csv.LineData
import com.example.gl11.csv.StationData
import com.example.gl11.db.StationDB
import com.example.gl11.entity.Position
import com.example.gl11.entity.Station


class SplashActivity : AppCompatActivity() {
    private val stationDB = StationDB(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        requestPermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                starMainActivity()
            } else {
                val toast = Toast.makeText(this,
                    "これ以上なにもできません", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }

    private fun requestPermission() {
        val permissionAccessCoarseLocationApproved =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

        if (permissionAccessCoarseLocationApproved) {
            starMainActivity()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun loadStation(){
        val csv1 = CsvReader<LineData>()
        val lineList = mutableListOf<LineData>()
        csv1.reader(this, "line20210312-all.csv"){
            lineList.add(LineData.load(it))
        }
        val lineNameMap = lineList.map{ it.lineCd to it.lineName}.toMap()
        val lineColorMap = lineList.map{ it.lineCd to it.color}.toMap()

        val csv2 = CsvReader<StationData>()
        val dataMap = mutableMapOf<String, StationData>()
        csv2.reader(this, "station20210312free.csv"){ s->
            val lon = s[9].toDoubleOrNull()
            val lat = s[10].toDoubleOrNull()
            if(lon != null && lat != null){
                val gCd = s[1]
                dataMap[gCd]?.let{ data->
                    val lineName = lineNameMap[s[5]]?.let{ "${data.lineName}\n${it}"} ?: data.lineName
                    val colorName = lineColorMap[s[5]]?.let{ "${data.colors},${it}"} ?: data.colors
                    dataMap[gCd] = StationData(s[0], s[2], lineName, colorName, lon, lat)
                }?: run{
                    dataMap[gCd] = StationData(s[0], s[2], lineNameMap[s[5]] ?: "", lineColorMap[s[5]] ?: "", lon, lat)
                }
            }
        }
        dataMap.values.forEach{
                stationDB.insert(Station(
                    title = it.stationName,
                    contents = it.lineName,
                    colors = it.colors,
                    lat = (it.lat * Position.COE).toLong(),
                    lon = (it.lon * Position.COE).toLong()
                ))
        }
    }

    private fun starMainActivity() {
        if(stationDB.selectAll().isEmpty()){
            loadStation()
        }

        val intent = Intent(application, MainActivity::class.java).apply{
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    companion object{
        private const val PERMISSION_REQUEST_CODE = 1000
    }
}