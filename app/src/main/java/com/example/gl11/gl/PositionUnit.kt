package com.example.gl11.gl

import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import com.example.gl11.entity.DataType
import com.example.gl11.entity.Favorite
import com.example.gl11.entity.Station

class PositionUnit(
    val id:Long,
    val type: DataType,
    val text:String,
    val contents:String,
    val latitude: Double,
    val longitude: Double,
    val colors:List<Int> = listOf(),
    val parentId:Long? = null
):DisplayUnit() {

    var isSelected:Boolean = false
    var isVisible:Boolean = false
    var isLike:Boolean = false
    var isFav:Boolean = false
    var drawAngle:Float? = null
    var drawDistance:Float? = null
    var bitmap:Bitmap? = null
    val arrow = DisplayUnit()
    val like = DisplayUnit()
    val fav = DisplayUnit()
    val distanceText:String
        get(){
            return drawDistance?.let{
                when{
                    (it< 1000) -> { "%.0f m".format(it) }
                    (it < 10000) -> { "%.2f Km".format(it/1000) }
                    else -> { "%d Km".format((it/1000).toInt()) }
                }
            } ?: ""
        }
    val coordinateText:String
        get(){
            return "緯度: $latitude, 経度: $longitude"
        }

    fun setDistance(lat0: Double, lon0: Double) {
        val results = FloatArray(3)
        Location.distanceBetween(lat0, lon0, latitude, longitude, results)
        drawAngle = if(results[1] < 0){ results[1]*-1 }else{ 360-results[1] } + 180
        drawDistance = results[0]
    }

    companion object{
        fun create(data:Station):PositionUnit {
            val cols = data.colors.replace("\"","").split(",")
            return PositionUnit(
                data.id!!, DataType.STATION, data.title, data.contents, data.latitude, data.longitude,
                if(cols.isEmpty()){
                    listOf(Color.WHITE)
                } else {
                    try {
                        cols.filter { it.isNotBlank() }.map { Color.parseColor(it) }
                    }catch(e:StringIndexOutOfBoundsException){
                        listOf(Color.WHITE)
                    }catch(e:IllegalArgumentException){
                        listOf(Color.WHITE)
                    }
                }
            )
        }
        fun create(data:Favorite):PositionUnit = PositionUnit(
                data.id!!, DataType.FAVORITE, data.title, data.contents, data.latitude, data.longitude, listOf(data.markColor), data.parentId)
    }

}