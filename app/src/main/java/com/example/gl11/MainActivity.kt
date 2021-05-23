package com.example.gl11

import android.annotation.SuppressLint
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.example.gl11.db.FavoriteDB
import com.example.gl11.db.StationDB
import com.example.gl11.entity.DataType
import com.example.gl11.entity.Favorite
import com.example.gl11.gl.PositionUnit
import com.example.gl11.gl.StringBitmap
import com.example.gl11.system.RotationSensor
import com.example.gl11.util.PositionRect
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity(){

    private val navController: NavController by lazy {findNavController(R.id.nav_host)}
    private lateinit var appBarConfiguration: AppBarConfiguration

    // gps
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var latitude:Double = 0.0
    private var longitude:Double = 0.0

    // rotation
    val rotationSensor: RotationSensor by lazy{
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            RotationSensor(getSystemService(SENSOR_SERVICE) as SensorManager, display?.rotation)
        } else {
            RotationSensor(getSystemService(SENSOR_SERVICE) as SensorManager, windowManager.defaultDisplay.rotation)
        }
    }

    // db
    private val stationDB:StationDB by lazy { StationDB(this) }
    private val favoriteDB:FavoriteDB by lazy { FavoriteDB(this) }

    // unit list
    private var reloadRect:PositionRect? = null
    var isReady = false
    val unitList = mutableListOf<PositionUnit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initGps()

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow),
            findViewById<DrawerLayout>(R.id.drawer_layout)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        findViewById<NavigationView>(R.id.nav_view).setupWithNavController(navController)

        val stations = StationDB(this).selectAll()
        println(stations)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onPause() {
        super.onPause()
        rotationSensor.setSensorsOff()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        rotationSensor.setSensorsOn()
        startLocationUpdates()
    }


    private fun initGps(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.i("gps", "${location.latitude} , ${location.longitude}")
                    loadPosition()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = createLocationRequest() ?: return
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, null)
    }
    private fun createLocationRequest(): LocationRequest? {
        return LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun loadPosition(){
        if(reloadRect?.isContain(latitude, longitude) != true){
            reloadRect = PositionRect(RELOAD_RANGE, latitude, longitude)

            val pos = PositionRect(WIDE_RANGE, latitude, longitude)
            val stationList = stationDB.selectRange(pos.south.lat, pos.north.lat, pos.west.lon, pos.east.lon)
            val favList = favoriteDB.selectRange(pos.south.lat, pos.north.lat, pos.west.lon, pos.east.lon)
            val tmpList = mutableListOf<PositionUnit>()
            tmpList.addAll(stationList.map { PositionUnit.create(it).apply { setDistance(latitude, longitude) } })
            val dupFav = mutableListOf<Favorite>()
            tmpList.filter { it.type == DataType.STATION }.forEach { unit->
                val dupList = favList.filter { it.type == DataType.STATION.no && it.parentId == unit.id }
                if(dupList.isNotEmpty()){ unit.isFav = true }
                dupFav.addAll(dupList)
            }
            tmpList.addAll(favList.map { PositionUnit.create(it).apply {
                setDistance(latitude, longitude)
                isFav = true
            } })

            val sortList = tmpList.sortedBy { it.drawDistance ?: Float.MAX_VALUE }
            sortList.forEach {
                it.bitmap = StringBitmap.createBitmap(it, true)
            }
            isReady = false
            unitList.forEach {
                it.bitmap?.recycle()
                it.bitmap = null
            }
            unitList.clear()
            unitList.addAll(sortList)
            isReady = true
        } else {
            isReady = false
            unitList.forEach { it.setDistance(latitude, longitude) }
            isReady = true
        }
    }
    companion object{
        const val WIDE_RANGE = 10000L
        const val RELOAD_RANGE = 5000L
    }
}