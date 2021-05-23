package com.example.gl11.ui.home.view

import com.example.gl11.entity.DataType
import com.example.gl11.entity.Favorite
import com.example.gl11.entity.Station
import com.example.gl11.entity.User
import com.example.gl11.gl.PositionUnit

class ContentsDto(
    val id:Long,
    val type: DataType,
    val title:String,
    var distance:String,
    val coordinate:String,
    val contents:String,
    val user: User? = null,
    val isFav:Boolean = false,
    val isLike:Boolean = false,
) {
    companion object{
        fun create(data:Station, unit:PositionUnit):ContentsDto =
            ContentsDto(
                data.id!!,
                DataType.STATION,
                data.title,
                unit.distanceText,
                unit.coordinateText,
                data.contents,
                null,
                unit.isFav,
                unit.isLike
            )
        fun create(data:Favorite, unit:PositionUnit):ContentsDto =
            ContentsDto(
                data.id!!,
                DataType.FAVORITE,
                data.title,
                unit.distanceText,
                unit.coordinateText,
                data.contents,
                null,
                unit.isFav,
                unit.isLike
            )
    }
}